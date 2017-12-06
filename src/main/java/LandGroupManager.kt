import model.FacilityType
import model.VehicleType
import kotlin.math.hypot

private val facilityLength = 64
private val stopRadius = 30
private val facilitiesTicksPeriod = 1800


private var groupsToFacilitiesMap = mutableMapOf<Int, Long>()
val landGroups = mutableListOf<Int>()
val moveFromAlly = mutableSetOf<Int>()
var facilitiesTick = mutableMapOf<Long, Int>()

fun setupFacilities() {
    world!!.facilities.filter { it.type == FacilityType.VEHICLE_FACTORY }.filter { it.ownerPlayerId == me!!.id }.forEach {
        if (!facilitiesTick.contains(it.id) || it.vehicleType == null) {
            vehicleProduction(getSmallestType(), it.id, priority = 4, interrupt = true)
            facilitiesTick[it.id] = world!!.tickIndex
        } else if (facilitiesTick.contains(it.id) && facilitiesTick[it.id]!! + facilitiesTicksPeriod <= world!!.tickIndex) {
            val group = freeGroups.first()
            freeGroups.remove(group)
            clearAndSelect(left = it.left, top = it.top, right = it.left + facilityLength, bottom = it.top + facilityLength, queue = group)
            assign(group, queue = group)

            if (landGroups.size < 3 && listOf(VehicleType.IFV, VehicleType.ARRV, VehicleType.TANK).contains(it.vehicleType)) {
                landGroups.add(group)
            } else {
                move(300 - it.left, 300 - it.top, queue = group)
            }
            vehicleProduction(getSmallestType(), it.id, priority = 4, interrupt = true)
            facilitiesTick[it.id] = world!!.tickIndex
        }
    }
}

private fun getSmallestType(): VehicleType {
    return VehicleType.values().minBy { streamVehicles(it).count() }!!
}

private fun getGroupType(group: Int): VehicleType {
    return VehicleType.values().maxBy { streamVehicles(group, listOf(it)).count() }!!
}

fun sendGroups() {
    setupFacilities()
    killAll()
    groupsToFacilitiesMap = groupsToFacilitiesMap.filterValues { world!!.facilities.first { f -> f.id == it }.ownerPlayerId != me!!.id }.toMutableMap()
    if (isLandGrouped()) {
        landGroups.forEach {
            if (streamVehicles(it).count() == 0) {
                landGroups.remove(it)
                groupsToFacilitiesMap.remove(it)
            } else {
                sendGroup(it)
            }
        }
    }
}

var killGroup = 0

private fun killAll() {
    if (killGroup != 0) {
        if (world!!.tickIndex % 500 == 0) {
            val x = streamVehicles(killGroup).map { it.x }.average()
            val y = streamVehicles(killGroup).map { it.y }.average()
            clearAndSelect(group = killGroup, interrupt = true, queue = killGroup)
            scale(x, y, factor = 0.2, interrupt = true, queue = killGroup)
            move(getEnemyVehicleGroups(1500).first().x - x, getEnemyVehicleGroups(1500).first().y - y, queue = killGroup)
        }
    }
    if (world!!.tickIndex > 12000 && killGroup == 0) {
        killGroup = freeGroups.first()
        freeGroups.remove(killGroup)
        clearAndSelect(left = 150.0, top = 150.0, right = 450.0, bottom = 450.0, interrupt = true, queue = killGroup)
        assign(killGroup, queue = killGroup)
    }

}

private fun sendGroup(group: Int) {
    val avgX = getAvgX(streamVehicles(group))
    val avgY = getAvgY(streamVehicles(group))

    if (!moveFromAlly.contains(group)) {
        val nearAlly = streamVehicles(Ownership.ALLY)
                .filter { listOf(VehicleType.IFV, VehicleType.ARRV, VehicleType.TANK).contains(it.type) }
                .filterNot { it.groups.contains(group) }
                .find { it.getDistanceTo(avgX, avgY) < stopRadius }
        if (nearAlly != null) {
            clearAndSelect(group = group, interrupt = true, queue = group)
            move((avgX - nearAlly.x) * 0.5, (avgY - nearAlly.y) * 0.5, interrupt = true, queue = group)
            moveFromAlly.add(group)
        }
    }


    if (streamVehicleUpdates(Ownership.ALLY, group).count() == 0 && getQueueSize(group) == 0) {
        moveFromAlly.remove(group)
        val facility = world!!.facilities.filter { it.ownerPlayerId != me!!.id }
                .filter { !groupsToFacilitiesMap.containsValue(it.id) || it.id == groupsToFacilitiesMap[group] }
                .minBy { hypot(avgX - it.left + facilityLength / 2, avgY - it.top + facilityLength / 2) }
        if (facility != null) {

            groupsToFacilitiesMap.put(group, facility.id)
            clearAndSelect(group = group, interrupt = true, queue = group)
            scale(avgX, avgY, factor = 0.5, queue = group)
            val distX = facility.left + facilityLength / 2 - avgX
            val distY = facility.top + facilityLength / 2 - avgY
            move(distX, distY, maxSpeed = 0.2, queue = group)
        }
    }
}

//private enum class LandGroupState {
//    START, THREE_IN_ROW, SCALED_UP, MERGED
//}