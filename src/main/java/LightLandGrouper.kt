import model.Vehicle
import model.VehicleType
import java.lang.Double.max
import kotlin.math.abs

private val scaleLandAlpha = 3
private val unitShift = 6.0
private val centerEps = 12.0
private val minLandGroupCenter = 100.0

//private var state: LandGroupState = LandGroupState.START
private var waiting = false

//var startLandGroup = 0


fun startLightGroupLand() {
    makeGroup(VehicleType.TANK)
    makeGroup(VehicleType.ARRV)
    makeGroup(VehicleType.IFV)
}

private fun makeGroup(vehicleType: VehicleType) {
    val group = freeGroups.first()
    freeGroups.remove(group)
    clearAndSelect(vehicleType, interrupt = true, queue = group)
    assign(group, interrupt = true, queue = group)
    facilityGroups.add(group)
}

private fun scaleRowsUp() {
    val avgX: List<Pair<VehicleType, Double>> = getCentersX()
    val avgY: List<Pair<VehicleType, Double>> = getCentersY()
    if (avgX[0].second != avgX[1].second && avgX[1].second != avgX[2].second) {
        scaleLand(VehicleType.TANK, false, 0.0, { it.y })
        scaleLand(VehicleType.ARRV, false, unitShift, { it.y })
        scaleLand(VehicleType.IFV, false, unitShift * 2, { it.y })
    } else if (avgY[0].second != avgY[1].second && avgY[1].second != avgY[2].second) {
        scaleLand(VehicleType.TANK, true, 0.0, { it.x })
        scaleLand(VehicleType.ARRV, true, unitShift, { it.x })
        scaleLand(VehicleType.IFV, true, unitShift * 2, { it.x })
    }
}

private fun mergeLandGroups() {
    val avgX: List<Pair<VehicleType, Double>> = getCentersX()
    val avgY: List<Pair<VehicleType, Double>> = getCentersY()
    val landVehicles = streamVehicles(listOf(VehicleType.TANK, VehicleType.ARRV, VehicleType.IFV))
    if (abs(avgX[0].second - avgX[1].second) > centerEps && abs(avgX[2].second - avgX[1].second) > centerEps) {
        clearAndSelect(avgX[0].first, interrupt = true, queue = startLandGroup)
        move(x = avgX[1].second - avgX[0].second, interrupt = true, queue = startLandGroup)
        clearAndSelect(avgX[2].first, interrupt = true, queue = startLandGroup)
        move(x = avgX[1].second - avgX[2].second, interrupt = true, queue = startLandGroup)

        for (i in 0..2) {
            val group = freeGroups.first()
            freeGroups.remove(group)
            facilityGroups.add(group)

            val top = landVehicles.map { it.y }.min()!! - 3 + 60 * i
            val bottom = top + 60
            clearAndSelect(VehicleType.ARRV, top = top, bottom = bottom, interrupt = true, queue = group)
            addToSelection(VehicleType.TANK, top = top, bottom = bottom, interrupt = true, queue = group)
            addToSelection(VehicleType.IFV, top = top, bottom = bottom, interrupt = true, queue = group)
            assign(group, interrupt = true, queue = group)
        }
    } else if (abs(avgY[0].second - avgY[1].second) > centerEps && abs(avgY[2].second - avgY[1].second) > centerEps) {
        clearAndSelect(avgY[0].first, interrupt = true, queue = startLandGroup)
        move(y = avgY[1].second - avgY[0].second, interrupt = true, queue = startLandGroup)
        clearAndSelect(avgY[2].first, interrupt = true, queue = startLandGroup)
        move(y = avgY[1].second - avgY[2].second, interrupt = true, queue = startLandGroup)

        for (i in 0..2) {
            val group = freeGroups.first()
            freeGroups.remove(group)
            facilityGroups.add(group)

            val top = landVehicles.map { it.x }.min()!! - 3 + 60 * i
            val bottom = top + 60
            clearAndSelect(VehicleType.ARRV, left = top, right = bottom, interrupt = true, queue = group)
            addToSelection(VehicleType.TANK, left = top, right = bottom, interrupt = true, queue = group)
            addToSelection(VehicleType.IFV, left = top, right = bottom, interrupt = true, queue = group)
            assign(group, interrupt = true, queue = group)
        }
    }
}


private fun getCentersX(): List<Pair<VehicleType, Double>> {
    return mutableListOf(
            getStartCenter(VehicleType.TANK) { it.x },
            getStartCenter(VehicleType.ARRV) { it.x },
            getStartCenter(VehicleType.IFV) { it.x })
            .sortedBy { it.second }
}

private fun getCentersY(): List<Pair<VehicleType, Double>> {
    return mutableListOf(
            getStartCenter(VehicleType.TANK) { it.y },
            getStartCenter(VehicleType.ARRV) { it.y },
            getStartCenter(VehicleType.IFV) { it.y })
            .sortedBy { it.second }
}

private fun getStartCenter(vehicleType: VehicleType, coord: (Vehicle) -> Double): Pair<VehicleType, Double> {
    var center = streamVehicles(vehicleType).map(coord).average()
    center = if (abs(center - startCoord1) < abs(center - startCoord2)) {
        if (abs(center - startCoord1) < abs(center - startCoord3)) {
            startCoord1
        } else {
            startCoord3
        }
    } else {
        if (abs(center - startCoord2) < abs(center - startCoord3)) {
            startCoord2
        } else {
            startCoord3
        }
    }
    return Pair(vehicleType, center)
}

private fun scaleLand(vehicleType: VehicleType, isX: Boolean, shift: Double, coord: (Vehicle) -> Double) {

    val typeCenter = streamVehicles(listOf(vehicleType)).map(coord).average()

    for (i in 0..9) {
        val j = if (i < 5) i else 14 - i
        val vehicles = streamVehicles(vehicleType).sortedBy(coord).subList(10 * j, 10 * j + 10)

        val curx = vehicles.map(coord).average()
        val curcx = typeCenter
        val cx = max(minLandGroupCenter, startCoord2) - shift
        val x = curx - curcx + cx

        val tx = (x - cx) * scaleLandAlpha + cx

        clearAndSelect(vehicleType, getSelectionRectangle(vehicles), interrupt = true, queue = startLandGroup)
        if (isX) {
            move(tx - curx, interrupt = true, queue = startLandGroup)
        } else {
            move(y = tx - curx, interrupt = true, queue = startLandGroup)
        }
    }
}

//fun isLandGrouped(): Boolean = state == LandGroupState.MERGED

//private enum class LandGroupState {
//    START, THREE_IN_ROW, SCALED_UP, DISBANDED, MERGED
//}