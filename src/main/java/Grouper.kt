import model.Vehicle
import model.VehicleType
import java.lang.Double.max
import kotlin.math.PI
import kotlin.math.abs

private val scaleAirAlpha = 2
private val unitShift = 6.0
private val centerEps = 20.0
private val minAirGroupCenter = 65.0

private var state = AirGroupState.START

private var waiting = false

var startAirGroup = 0

fun startGroupAir() {
    if (state === AirGroupState.AT_POSITION)
        return
    if (waiting) {
        if (streamVehicleUpdates(startAirGroup).count() == 0 && getQueueSize(startAirGroup) == 0) {
            state = AirGroupState.values()[state.ordinal + 1]
            waiting = false
        } else return
    }

    if (state === AirGroupState.START) {
        startAirGroup = freeGroups.first()
        clearAndSelect(VehicleType.FIGHTER, priority = 1, queue = startAirGroup)
        addToSelection(VehicleType.HELICOPTER, priority = 1, queue = startAirGroup)
        freeGroups.remove(startAirGroup)
        assign(startAirGroup, priority = 1, queue = startAirGroup)
        waiting = true
        return
    }
    val fightersCenterX = streamVehicles(startAirGroup, listOf(VehicleType.FIGHTER)).map { it.x }.average()
    val helicopsCenterX = streamVehicles(startAirGroup, listOf(VehicleType.HELICOPTER)).map { it.x }.average()
    val helicopsCenterY = streamVehicles(startAirGroup, listOf(VehicleType.HELICOPTER)).map { it.y }.average()
    val fightersCenterY = streamVehicles(startAirGroup, listOf(VehicleType.FIGHTER)).map { it.y }.average()
    if (state === AirGroupState.ASSIGNED) {
        if (abs(fightersCenterX - helicopsCenterX) < centerEps) {
            scaleAir(VehicleType.FIGHTER, true, 0.0) { it.x }
            scaleAir(VehicleType.HELICOPTER, true, unitShift) { it.x }
        } else {
            scaleAir(VehicleType.FIGHTER, false, 0.0) { it.y }
            scaleAir(VehicleType.HELICOPTER, false, unitShift) { it.y }
        }
    }
    if (state === AirGroupState.SCALED_UP) {
        clearAndSelect(VehicleType.FIGHTER, rect = calcRectangle(startAirGroup, VehicleType.FIGHTER), queue = startAirGroup)
        if (abs(fightersCenterX - helicopsCenterX) < centerEps) {
            move(y = helicopsCenterY - fightersCenterY, queue = startAirGroup)
        } else {
            move(x = helicopsCenterX - fightersCenterX, queue = startAirGroup)
        }
    }
    if (state === AirGroupState.MERGED) {
        clearAndSelect(group = startAirGroup, queue = startAirGroup)
        val x = streamVehicles(startAirGroup).map { it.x }.average()
        val y = streamVehicles(startAirGroup).map { it.y }.average()
        move(100 - x, 100 - y, queue = startAirGroup)

        val maxX = streamVehicles(startAirGroup).map { it.x }.max()
        val maxY = streamVehicles(startAirGroup).map { it.y }.max()
        val minX = streamVehicles(startAirGroup).map { it.x }.min()
        val minY = streamVehicles(startAirGroup).map { it.y }.min()
        if (maxX!! - minX!! > maxY!! - minY!!) {
            rotate(100.0, 100.0, angle = -PI / 4, queue = startAirGroup)
        } else {
            rotate(100.0, 100.0, angle = PI / 4, queue = startAirGroup)
        }
        scale(100.0, 100.0, factor = 0.4, queue = startAirGroup)
    }
    if (state == AirGroupState.SCALED_DOWN) {
        airGroups.add(startAirGroup)
    }
    waiting = true
}

private fun scaleAir(vehicleType: VehicleType, isX: Boolean, shift: Double, coord: (Vehicle) -> Double) {

    val airCenter = streamVehicles(listOf(VehicleType.FIGHTER, VehicleType.HELICOPTER)).map(coord).average()
    val typeCenter = streamVehicles(listOf(vehicleType)).map(coord).average()

    for (i in 0..9) {
        val j = if (i < 5) i else 14 - i
        val vehicles = streamVehicles(vehicleType).sortedBy(coord).subList(10 * j, 10 * j + 10).filter { it.groups.contains(startAirGroup) }

        val curx = vehicles.map(coord).average()
        val curcx = typeCenter
        val cx = max(minAirGroupCenter, airCenter) - shift
        val x = curx - curcx + cx

        val tx = (x - cx) * scaleAirAlpha + cx

        clearAndSelect(vehicleType, getSelectionRectangle(vehicles), interrupt = true, queue = startAirGroup)
        if (isX) {
            move(tx - curx, interrupt = true, queue = startAirGroup)
        } else {
            move(y = tx - curx, interrupt = true, queue = startAirGroup)
        }
    }
}

fun isAirGrouped(): Boolean = state == AirGroupState.AT_POSITION

private enum class AirGroupState {
    START, ASSIGNED, SCALED_UP, MERGED, SCALED_DOWN, AT_POSITION
}
