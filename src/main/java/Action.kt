import model.ActionType
import model.Move
import model.VehicleType
import java.util.*

private val defaultPriority = 10

private val moves = mutableMapOf<Int?, PriorityQueue<MoveWrapper>>()
private val currentGroupMoves = mutableMapOf<Int?, MoveWrapper>()


fun rotate(x: Double, y: Double, group: Int = 0, angle: Double, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}) {
    val moveWrapper = MoveWrapper(getMove(ActionType.ROTATE, x, y, group, angle = angle), priority, interrupt, group, moveUpdater)
    addMove(moveWrapper)
}

fun move(x: Double = 0.0, y: Double = 0.0, group: Int = 0, maxSpeed: Double = 0.0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}) {
    val moveWrapper = MoveWrapper(getMove(ActionType.MOVE, x, y, group, maxSpeed = maxSpeed), priority, interrupt, group, moveUpdater)
    addMove(moveWrapper)
}

fun scale(x: Double = 0.0, y: Double = 0.0, group: Int = 0, factor: Double, maxSpeed: Double = 0.0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}) {
    val moveWrapper = MoveWrapper(getMove(ActionType.SCALE, x, y, group, factor = factor, maxSpeed = maxSpeed), priority, interrupt, group, moveUpdater)
    addMove(moveWrapper)
}

fun clearAndSelect(vehicleType: VehicleType? = null, left: Double = 0.0, top: Double = 0.0, right: Double = world!!.width, bottom: Double = world!!.height,
                   group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}) {
    val moveWrapper = MoveWrapper(getMove(ActionType.CLEAR_AND_SELECT, group = group, left = left, top = top, right = right, bottom = bottom, vehicleType = vehicleType),
            priority, interrupt, group, moveUpdater)
    addMove(moveWrapper)
}

fun clearAndSelect(vehicleType: VehicleType? = null, rect: Rectangle, group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}) {
    val moveWrapper = MoveWrapper(getMove(ActionType.CLEAR_AND_SELECT, group = group, left = rect.left, top = rect.top, right = rect.right, bottom = rect.bottom, vehicleType = vehicleType),
            priority, interrupt, group, moveUpdater)
    addMove(moveWrapper)
}

fun deselect(vehicleType: VehicleType? = null, left: Double = 0.0, top: Double = 0.0, right: Double = world!!.width, bottom: Double = world!!.height,
             group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}) {
    val moveWrapper = MoveWrapper(getMove(ActionType.DESELECT, group = group, left = left, top = top, right = right, bottom = bottom, vehicleType = vehicleType),
            priority, interrupt, group, moveUpdater)
    addMove(moveWrapper)
}

fun deselect(vehicleType: VehicleType? = null, rect: Rectangle, group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}) {
    val moveWrapper = MoveWrapper(getMove(ActionType.DESELECT, group = group, left = rect.left, top = rect.top, right = rect.right, bottom = rect.bottom, vehicleType = vehicleType), priority, interrupt, group, moveUpdater)
    addMove(moveWrapper)
}

fun assign(group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}) {
    val moveWrapper = MoveWrapper(getMove(ActionType.ASSIGN, group = group), priority, interrupt, group, moveUpdater)
    addMove(moveWrapper)
}

fun dismiss(group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}) {
    val moveWrapper = MoveWrapper(getMove(ActionType.DISMISS, group = group), priority, interrupt, group, moveUpdater)
    addMove(moveWrapper)
}

fun addToSelection(vehicleType: VehicleType? = null, group: Int = 0, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}) {
    val moveWrapper = MoveWrapper(getMove(ActionType.ADD_TO_SELECTION, group = group, right = world!!.width, bottom = world!!.height, vehicleType = vehicleType),
            priority, interrupt, group, moveUpdater)
    addMove(moveWrapper)
}

fun nuclearStrike(x: Double, y: Double, vehicleId: Long, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}) {
    val moveWrapper = MoveWrapper(getMove(ActionType.TACTICAL_NUCLEAR_STRIKE, x, y, vehicleId = vehicleId),
            priority, interrupt, moveUpdater = moveUpdater)
    addMove(moveWrapper)
}

fun vehicleProduction(vehicleType: VehicleType, facilityId: Long, priority: Int = defaultPriority, interrupt: Boolean = false, moveUpdater: (Move) -> Unit = {}) {
    val moveWrapper = MoveWrapper(getMove(ActionType.SETUP_VEHICLE_PRODUCTION, vehicleType = vehicleType, facilityId = facilityId),
            priority, interrupt, moveUpdater = moveUpdater)
    addMove(moveWrapper)
}


private fun addMove(moveWrapper: MoveWrapper) {
    val priorityQueue = PriorityQueue<MoveWrapper>()
    priorityQueue.add(moveWrapper)
    moves.merge(moveWrapper.group, priorityQueue, { oldValue, value -> oldValue.also { oldValue.add(value.peek()) } })
}

fun makeAction(move: Move) {
    if (moves.isEmpty()) return

    val moveWrapper = moves.filterValues { it.isNotEmpty() }
            .map { it.value.peek() }.sorted()
            .let { if (it.isEmpty()) return else it[0] }

    if (moveWrapper.interrupt || currentGroupMoves[moveWrapper.group] == null || isGroupStoppedMotion(moveWrapper)) {
        moveWrapper.move.invoke(move)
        moveWrapper.moveUpdater.invoke(move)
        currentGroupMoves.put(moveWrapper.group, moves[moveWrapper.group]!!.poll())
        printMove(move)
    }
}

private fun isGroupStoppedMotion(moveWrapper: MoveWrapper) =
        streamVehicleUpdates(Ownership.ALLY, moveWrapper.group).count() == 0

private class MoveWrapper constructor(
        val move: (Move) -> Unit,
        val priority: Int = 10,
        val interrupt: Boolean = false,
        val group: Int = 0,
        val moveUpdater: (Move) -> Unit) : Comparable<MoveWrapper> {

    private val time: Long

    init {
        time = globalTime++
    }

    override fun compareTo(o: MoveWrapper): Int {
        return Comparator.comparingInt<MoveWrapper> { move -> move.priority }
                .thenComparingLong { move -> move.time }.compare(this, o)
    }

    companion object {
        private var globalTime: Long = 0
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
