import model.Vehicle
import model.VehicleType
import java.lang.Double.max
import kotlin.math.abs

private val scaleAirAlpha = 2
private val unitShift = 6.0
private val centerEps = 5.0
private val minAirGroupCenter = 65.0

var startGrouped = false
var startAirGroup = 0

fun startGroupAir() {
    clearAndSelect(VehicleType.FIGHTER)
    addToSelection(VehicleType.HELICOPTER)

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

    val fightersCenterX = getAlliedCenterX(VehicleType.FIGHTER)
    val helicopsCenterX = getAlliedCenterX(VehicleType.HELICOPTER)
    val helicopsCenterY = getAlliedCenterY(VehicleType.HELICOPTER)
    val fightersCenterY = getAlliedCenterY(VehicleType.FIGHTER)
    if (abs(fightersCenterX - helicopsCenterX) < centerEps) {
        scaleAir(VehicleType.FIGHTER, true, 0.0) { it.x }
        scaleAir(VehicleType.HELICOPTER, true, unitShift) { it.x }
    } else {
        scaleAir(VehicleType.FIGHTER, false, 0.0) { it.y }
        scaleAir(VehicleType.HELICOPTER, false, unitShift) { it.y }
    }

    delay {
        clearAndSelect(VehicleType.FIGHTER, rect = calcRectangle(startAirGroup, VehicleType.FIGHTER), queue = startAirGroup, useNegativeTime = true)
        if (abs(fightersCenterX - helicopsCenterX) < centerEps) {
            move(y = helicopsCenterY - fightersCenterY, queue = startAirGroup, useNegativeTime = true)
        } else {
            move(x = helicopsCenterX - fightersCenterX, queue = startAirGroup, useNegativeTime = true)
        }
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

    val airCenter = streamVehicles(listOf(VehicleType.FIGHTER, VehicleType.HELICOPTER)).map(coord).average()
    val typeCenter = streamVehicles(listOf(vehicleType)).map(coord).average()

    for (i in 0..9) {
        delay(interrupt = true) {
            val j = if (i < 5) i else 14 - i
            val vehicles = streamVehicles(vehicleType).sortedBy(coord).subList(10 * j, 10 * j + 10).filter { it.groups.contains(startAirGroup) } // TODO тут баг

            val curx = vehicles.map(coord).average()
            val curcx = typeCenter
            val cx = max(minAirGroupCenter, airCenter) - shift
            val x = curx - curcx + cx

            val tx = (x - cx) * scaleAirAlpha + cx

            clearAndSelect(vehicleType, getSelectionRectangle(vehicles), interrupt = true, queue = startAirGroup, useNegativeTime = true)
            if (isX) {
                move(tx - curx, interrupt = true,queue = startAirGroup, useNegativeTime = true)
            } else {
                move(y = tx - curx, interrupt = true, queue = startAirGroup, useNegativeTime = true)
            }
        }
    }
}