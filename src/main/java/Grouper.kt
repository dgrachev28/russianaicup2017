import model.Vehicle
import model.VehicleType
import java.lang.Double.max
import kotlin.math.abs

private val scaleAirAlpha = 2
private val unitShift = 6.0
private val centerEps = 5.0

var startGrouped = false
var startAirGroup = 0

fun startGroupAir() {
    clearAndSelect(VehicleType.FIGHTER)
    addToSelection(VehicleType.HELICOPTER)
//    val vehicles = streamVehicles(VehicleType.FIGHTER).filter { it.groups.isNotEmpty() }
//    val bomber = streamVehicles(VehicleType.FIGHTER).maxWith(Comparator { o1, o2 -> sign(o1.x + o1.y - o2.x - o2.y).toInt() })

    deselect(moveUpdater = {
        val vehicles = streamVehicles(VehicleType.FIGHTER).filter { it.groups.isNotEmpty() }
        if (vehicles.isNotEmpty()) {
            val rect = getSelectionRectangle(vehicles)
            it.left = rect.left
            it.top = rect.top
            it.right = rect.right
            it.bottom = rect.bottom
        }
    })
    startAirGroup = freeGroups.first()
    freeGroups.remove(startAirGroup)
    assign(startAirGroup)

    if (abs(getAlliedCenterX(VehicleType.FIGHTER) - getAlliedCenterX(VehicleType.HELICOPTER)) < centerEps) {
        scaleAir(VehicleType.FIGHTER, true, 0.0) { it.x }
        scaleAir(VehicleType.HELICOPTER, true, unitShift) { it.x }
    } else {
        scaleAir(VehicleType.FIGHTER, false, 0.0) { it.y }
        scaleAir(VehicleType.HELICOPTER, false, unitShift) { it.y }
    }

    if (abs(getAlliedCenterX(VehicleType.FIGHTER) - getAlliedCenterX(VehicleType.HELICOPTER)) < centerEps) {
        clearAndSelect(group = startAirGroup)
        move(y = getAlliedCenterY(VehicleType.HELICOPTER) - getAlliedCenterY(VehicleType.FIGHTER))
    } else {
        clearAndSelect(group = startAirGroup)
        move(x = getAlliedCenterX(VehicleType.HELICOPTER) - getAlliedCenterX(VehicleType.FIGHTER))
    }
//    clearAndSelect(VehicleType.FIGHTER)
//    addToSelection(VehicleType.HELICOPTER)
//    scale(factor = 0.6, maxSpeed = 5.0, moveUpdater = {
//        it.x = streamVehicles(Ownership.ALLY, listOf(VehicleType.FIGHTER, VehicleType.HELICOPTER)).map { it.x }.average()
//        it.y = streamVehicles(Ownership.ALLY, listOf(VehicleType.FIGHTER, VehicleType.HELICOPTER)).map { it.y }.average()
//    })
    startGrouped = true
}

private fun scaleAir(vehicleType: VehicleType, isX: Boolean, shift: Double, coord: (Vehicle) -> Double) {
    val vehicles = streamVehicles(vehicleType).sortedBy(coord)

    for (i in 0..9) {
        val veh = vehicles.subList(10 * i, 10 * i + 10).filter { it.groups.contains(startAirGroup) } // TODO тут баг

        val curx = veh.map(coord).average()
        val curcx = streamVehicles(listOf(vehicleType)).map(coord).average()
        val cx = max(60.0, streamVehicles(listOf(VehicleType.FIGHTER, VehicleType.HELICOPTER)).map(coord).average()) + shift
        val x = curx - curcx + cx

        val tx = (x - cx) * scaleAirAlpha + cx

        clearAndSelect(vehicleType, getSelectionRectangle(veh), interrupt = true)
        if (isX) {
            move(tx - curx, interrupt = true)
        } else {
            move(y = tx - curx, interrupt = true)
        }
    }
}