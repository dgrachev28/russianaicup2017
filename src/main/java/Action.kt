import model.ActionType
import model.Move
import model.VehicleType
import java.util.*

private val defaultPriority = 10
private val EMPTY: () -> Unit = {}
private val needSelectionActions = setOf(ActionType.ADD_TO_SELECTION, ActionType.DESELECT, ActionType.ASSIGN,
        ActionType.DISMISS, ActionType.MOVE, ActionType.SCALE, ActionType.ROTATE)


private val moves = mutableMapOf<Int?, PriorityQueue<MoveWrapper>>()
private val currentGroupMoves = mutableMapOf<Int?, MoveWrapper>()
private var lastMove: MoveWrapper? = null

var groupLastActionTick = mutableMapOf<Int, Int>()


fun delay(group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, queue: Int = 0, action: () -> Unit) {
    addMove(MoveWrapper({}, priority, interrupt, group, {}, action, queue = queue, action = ActionType.CLEAR_AND_SELECT))
}

fun rotate(x: Double, y: Double, group: Int = 0, angle: Double, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}, useNegativeTime: Boolean = false, queue: Int = 0) {
    val moveWrapper = MoveWrapper(getMove(ActionType.ROTATE, x, y, group, angle = angle), priority, interrupt, group, moveUpdater, useNegativeTime = useNegativeTime, queue = queue, action = ActionType.ROTATE)
    addMove(moveWrapper)
}

fun move(x: Double = 0.0, y: Double = 0.0, group: Int = 0, maxSpeed: Double = 0.0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}, useNegativeTime: Boolean = false, queue: Int = 0) {
    val moveWrapper = MoveWrapper(getMove(ActionType.MOVE, x, y, group, maxSpeed = maxSpeed), priority, interrupt, group, moveUpdater, useNegativeTime = useNegativeTime, queue = queue, action = ActionType.MOVE)
    addMove(moveWrapper)
}

fun scale(x: Double = 0.0, y: Double = 0.0, group: Int = 0, factor: Double, maxSpeed: Double = 0.0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}, useNegativeTime: Boolean = false, queue: Int = 0) {
    val moveWrapper = MoveWrapper(getMove(ActionType.SCALE, x, y, group, factor = factor, maxSpeed = maxSpeed), priority, interrupt, group, moveUpdater, useNegativeTime = useNegativeTime, queue = queue, action = ActionType.SCALE)
    addMove(moveWrapper)
}

fun clearAndSelect(vehicleType: VehicleType? = null, left: Double = 0.0, top: Double = 0.0, right: Double = world!!.width, bottom: Double = world!!.height,
                   group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}, useNegativeTime: Boolean = false, queue: Int = 0) {
    val moveWrapper = MoveWrapper(getMove(ActionType.CLEAR_AND_SELECT, group = group, left = left, top = top, right = right, bottom = bottom, vehicleType = vehicleType),
            priority, interrupt, group, moveUpdater, useNegativeTime = useNegativeTime, queue = queue, action = ActionType.CLEAR_AND_SELECT)
    addMove(moveWrapper)
}

fun clearAndSelect(vehicleType: VehicleType? = null, rect: Rectangle, group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}, useNegativeTime: Boolean = false, queue: Int = 0) {
    val moveWrapper = MoveWrapper(getMove(ActionType.CLEAR_AND_SELECT, left = rect.left, top = rect.top, right = rect.right, bottom = rect.bottom, vehicleType = vehicleType),
            priority, interrupt, group, moveUpdater, useNegativeTime = useNegativeTime, queue = queue, action = ActionType.CLEAR_AND_SELECT)
    addMove(moveWrapper)
}

fun deselect(vehicleType: VehicleType? = null, left: Double = 0.0, top: Double = 0.0, right: Double = world!!.width, bottom: Double = world!!.height,
             group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}, useNegativeTime: Boolean = false, queue: Int = 0) {
    val moveWrapper = MoveWrapper(getMove(ActionType.DESELECT, group = group, left = left, top = top, right = right, bottom = bottom, vehicleType = vehicleType),
            priority, interrupt, group, moveUpdater, useNegativeTime = useNegativeTime, queue = queue, action = ActionType.DESELECT)
    addMove(moveWrapper)
}

fun deselect(vehicleType: VehicleType? = null, rect: Rectangle, group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}, useNegativeTime: Boolean = false, queue: Int = 0) {
    val moveWrapper = MoveWrapper(getMove(ActionType.DESELECT, group = group, left = rect.left, top = rect.top, right = rect.right, bottom = rect.bottom, vehicleType = vehicleType), priority, interrupt, group, moveUpdater, useNegativeTime = useNegativeTime, queue = queue, action = ActionType.DESELECT)
    addMove(moveWrapper)
}

fun assign(group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}, useNegativeTime: Boolean = false, queue: Int = 0) {
    val moveWrapper = MoveWrapper(getMove(ActionType.ASSIGN, group = group), priority, interrupt, group, moveUpdater, useNegativeTime = useNegativeTime, queue = queue, action = ActionType.ASSIGN)
    addMove(moveWrapper)
}

fun dismiss(group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}, useNegativeTime: Boolean = false, queue: Int = 0) {
    val moveWrapper = MoveWrapper(getMove(ActionType.DISMISS, group = group), priority, interrupt, group, moveUpdater, useNegativeTime = useNegativeTime, queue = queue, action = ActionType.DISMISS)
    addMove(moveWrapper)
}

fun disband(group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}, useNegativeTime: Boolean = false, queue: Int = 0) {
    val moveWrapper = MoveWrapper(getMove(ActionType.DISBAND, group = group), priority, interrupt, group, moveUpdater, useNegativeTime = useNegativeTime, queue = queue, action = ActionType.DISBAND)
    addMove(moveWrapper)
}


fun addToSelection(vehicleType: VehicleType? = null, left: Double = 0.0, top: Double = 0.0, right: Double = world!!.width, bottom: Double = world!!.height,
                   group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}, useNegativeTime: Boolean = false, queue: Int = 0) {
    val moveWrapper = MoveWrapper(getMove(ActionType.ADD_TO_SELECTION, group = group, left = left, top = top, right = right, bottom = bottom, vehicleType = vehicleType),
            priority, interrupt, group, moveUpdater, useNegativeTime = useNegativeTime, queue = queue, action = ActionType.ADD_TO_SELECTION)
    addMove(moveWrapper)
}

fun nuclearStrike(x: Double, y: Double, vehicleId: Long, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}, useNegativeTime: Boolean = false, queue: Int = 0) {
    val moveWrapper = MoveWrapper(getMove(ActionType.TACTICAL_NUCLEAR_STRIKE, x, y, vehicleId = vehicleId),
            priority, interrupt, moveUpdater = moveUpdater, useNegativeTime = useNegativeTime, queue = queue, action = ActionType.TACTICAL_NUCLEAR_STRIKE)
    addMove(moveWrapper)
}

fun vehicleProduction(vehicleType: VehicleType, facilityId: Long, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}, useNegativeTime: Boolean = false, queue: Int = 0) {
    val moveWrapper = MoveWrapper(getMove(ActionType.SETUP_VEHICLE_PRODUCTION, vehicleType = vehicleType, facilityId = facilityId),
            priority, interrupt, moveUpdater = moveUpdater, useNegativeTime = useNegativeTime, queue = queue, action = ActionType.SETUP_VEHICLE_PRODUCTION)
    addMove(moveWrapper)
}


private fun addMove(moveWrapper: MoveWrapper) {
    val priorityQueue = PriorityQueue<MoveWrapper>()
    priorityQueue.add(moveWrapper)
    moves.merge(moveWrapper.queue, priorityQueue, { oldValue, value -> oldValue.also { oldValue.add(value.peek()) } })
}

fun makeAction(move: Move) {
    if (moves.isEmpty()) return

    println("Queues total size: ${moves.flatMap { it.value }.count()}, Tick: ${world!!.tickIndex}")

    var moveWrapper = moves.filterValues { it.isNotEmpty() }
            .map { it.value.peek() }.sorted()
            .let { if (it.isEmpty()) return else it[0] }

    if (moveWrapper.action == ActionType.CLEAR_AND_SELECT && lastMove != null && moves[lastMove!!.queue]!!.isNotEmpty()) {
        if (needSelectionActions.contains(moves[lastMove!!.queue]!!.peek().action)) {
            moveWrapper = moves[lastMove!!.queue]!!.peek()
        }
    }

    if (moveWrapper.interrupt || currentGroupMoves[moveWrapper.queue] == null || isGroupStoppedMotion(moveWrapper)) {
        currentGroupMoves.put(moveWrapper.queue, moves[moveWrapper.queue]!!.poll())
        if (moveWrapper.delayedAction !== EMPTY) {
            moveWrapper.delayedAction.invoke()
        } else {
            moveWrapper.move.invoke(move)
            moveWrapper.moveUpdater.invoke(move)
            if (!setOf(ActionType.DISBAND, ActionType.SETUP_VEHICLE_PRODUCTION, ActionType.TACTICAL_NUCLEAR_STRIKE).contains(moveWrapper.action)) {
                lastMove = moveWrapper
            }
        }
        if (moveWrapper.queue != 0) {
            groupLastActionTick.put(moveWrapper.queue, world!!.tickIndex)
        }
        printMove(move)
    }
}

fun getQueueSize(queue: Int) = if (moves[queue] == null) 0 else moves[queue]!!.count()

fun getAllQueuesSize() = moves.values.sumBy { it.size }

private fun isGroupStoppedMotion(moveWrapper: MoveWrapper) =
        streamVehicleUpdates(moveWrapper.queue).count() == 0

private class MoveWrapper constructor(
        val move: (Move) -> Unit,
        val priority: Int = 10,
        val interrupt: Boolean = false,
        val group: Int = 0,
        val moveUpdater: (Move) -> Unit,
        val delayedAction: () -> Unit = EMPTY,
        useNegativeTime: Boolean = false,
        val queue: Int = 0,
        val action: ActionType) : Comparable<MoveWrapper> {

    private val time: Long

    init {
        time = if (useNegativeTime) negativeTime++ else globalTime++
    }

    override fun compareTo(o: MoveWrapper): Int {
        return Comparator.comparingInt<MoveWrapper> { move -> move.priority }
                .thenComparingLong { move -> move.time }.compare(this, o)
    }

    companion object {
        private var globalTime: Long = 0
        private var negativeTime: Long = Long.MIN_VALUE
    }
}

private fun getMove(action: ActionType = ActionType.NONE,
                    x: Double = 0.0,
                    y: Double = 0.0,
                    group: Int = 0,
                    vehicleType: VehicleType? = null,
                    left: Double = 0.0,
                    top: Double = 0.0,
                    right: Double = 0.0,
                    bottom: Double = 0.0,
                    angle: Double = 0.0,
                    factor: Double = 0.0,
                    maxSpeed: Double = 0.0,
                    facilityId: Long = 0,
                    vehicleId: Long = 0
): (Move) -> Unit = { move ->
    move.action = action
    move.x = x
    move.y = y
    move.vehicleType = vehicleType
    move.group = group
    move.left = left
    move.top = top
    move.right = right
    move.bottom = bottom
    move.angle = angle
    move.factor = factor
    move.maxSpeed = maxSpeed
    move.facilityId = facilityId
    move.vehicleId = vehicleId
}
