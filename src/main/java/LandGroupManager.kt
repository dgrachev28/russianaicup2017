import model.FacilityType
import model.Vehicle
import model.VehicleType
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

private val facilityLength = 64
private val stopRadius = 30
private val enemyRadius = 50
private val facilitiesTicksPeriod = 3000
private val powerThreshold = 5.0


private var groupsToFacilitiesMap = mutableMapOf<Int, Long>()
val facilityGroups = mutableListOf<Int>()
val landGroups = mutableListOf<Int>()
val airGroups = mutableListOf<Int>()

val moveFromAlly = mutableSetOf<Int>()
var facilitiesTick = mutableMapOf<Long, Int>()

fun setupFacilities() {
    world!!.facilities.filter { it.type == FacilityType.VEHICLE_FACTORY }.filter { it.ownerPlayerId == me!!.id }.forEach {
        if (!facilitiesTick.contains(it.id) || it.vehicleType == null) {
            vehicleProduction(VehicleType.TANK, it.id, priority = 4, interrupt = true)
            facilitiesTick[it.id] = world!!.tickIndex
        } else if (facilitiesTick.contains(it.id) && facilitiesTick[it.id]!! + facilitiesTicksPeriod <= world!!.tickIndex) {
            val group = freeGroups.first()
            freeGroups.remove(group)
            clearAndSelect(left = it.left, top = it.top, right = it.left + facilityLength, bottom = it.top + facilityLength, queue = group)
            assign(group, queue = group)

            if (listOf(VehicleType.IFV, VehicleType.ARRV, VehicleType.TANK).contains(it.vehicleType)) {
                facilityGroups.add(group)
            } else {
                airGroups.add(group)
            }
            vehicleProduction(VehicleType.TANK, it.id, priority = 4, interrupt = true)
            facilitiesTick[it.id] = world!!.tickIndex
        }
    }
}

private fun getSmallestType(): VehicleType {
    return VehicleType.values().filterNot { it == VehicleType.ARRV }.minBy { streamVehicles(it).count() }!!
}

private fun getGroupType(group: Int): VehicleType {
    return VehicleType.values().maxBy { streamVehicles(group, listOf(it)).count() }!!
}

fun sendGroups() {
    setupFacilities()
    groupsToFacilitiesMap = groupsToFacilitiesMap.filterValues { world!!.facilities.first { f -> f.id == it }.ownerPlayerId != me!!.id }.toMutableMap()
    if (isLandGrouped()) {
        facilityGroups.forEach {
            if (streamVehicles(it).count() == 0 && getQueueSize(it) == 0) {
                groupsToFacilitiesMap.remove(it)
            } else {
                sendGroup(it)
            }
        }
        facilityGroups.removeIf { streamVehicles(it).count() == 0 && getQueueSize(it) == 0 }
    }
    if (isAirGrouped()) {
        manageAirGroups()
    }
    if (isAirGrouped() && isLandGrouped()) { // TODO: нужен ли этот if
        manageLandGroups()
    }
}


fun manageLandGroups() {
    landGroups.forEach { ally ->
        val (avgX, avgY) = preventCollision(ally)

        if (streamVehicleUpdates(ally).count() == 0 && getQueueSize(ally) == 0) {
            moveFromAlly.remove(ally)

            val powers = enemyGroups.associateBy({ it }, { comparePower(ally, it.vehicles) })
            val sortedEnemies = enemyGroups.filter { powers[it]!!.first > powerThreshold && powers[it]!!.second > powerThreshold }
                    .sortedByDescending { powers[it]!!.second * it.vehicles.size / hypot(it.x - avgX, it.y - avgY) }
            if (sortedEnemies.isNotEmpty()) {
                sortedEnemies.first()
                clearAndSelect(group = ally, interrupt = true, queue = ally)
                scale(avgX, avgY, factor = 0.5, queue = ally)
                val distX = sortedEnemies.first().x - avgX
                val distY = sortedEnemies.first().y - avgY
                move(distX, distY, maxSpeed = 0.2, queue = ally)
            }
//            val facility = world!!.facilities.filter { it.ownerPlayerId != me!!.id }
//                    .filter { !groupsToFacilitiesMap.containsValue(it.id) || it.id == groupsToFacilitiesMap[ally] }
//                    .minBy { hypot(avgX - it.left + facilityLength / 2, avgY - it.top + facilityLength / 2) }
//            if (facility != null) {
//                groupsToFacilitiesMap.put(group, facility.id)
//                clearAndSelect(group = group, interrupt = true, queue = group)
//                scale(avgX, avgY, factor = 0.5, queue = group)
//                val distX = facility.left + facilityLength / 2 - avgX
//                val distY = facility.top + facilityLength / 2 - avgY
//                move(distX, distY, maxSpeed = 0.2, queue = group)
//            }
        }

    }
}

fun manageAirGroups() {
    airGroups.forEach { ally ->
        val (avgX, avgY) = preventCollision(ally, false)

        if (streamVehicleUpdates(ally).count() == 0 && getQueueSize(ally) == 0) {
            moveFromAlly.remove(ally)

            val powers = enemyGroups.associateBy({ it }, { comparePower(ally, it.vehicles) })
            val sortedEnemies = enemyGroups.filter { powers[it]!!.first > powerThreshold && powers[it]!!.second > powerThreshold }
                    .sortedByDescending { powers[it]!!.second * it.vehicles.size / hypot(it.x - avgX, it.y - avgY) }
            if (sortedEnemies.isNotEmpty()) {
                sortedEnemies.first()
                clearAndSelect(group = ally, interrupt = true, queue = ally)
                scale(avgX, avgY, factor = 0.5, queue = ally)
                val distX = sortedEnemies.first().x - avgX
                val distY = sortedEnemies.first().y - avgY
                move(distX, distY, maxSpeed = 0.6, queue = ally)
            }
        }

    }
}


private fun sendGroup(group: Int) {
    val (avgX, avgY) = preventCollision(group)

    if (streamVehicleUpdates(group).count() == 0 && getQueueSize(group) == 0) {
        moveFromAlly.remove(group)
        var facility = world!!.facilities.filter { it.ownerPlayerId != me!!.id }
                .filter { !groupsToFacilitiesMap.containsValue(it.id) || it.id == groupsToFacilitiesMap[group] }
                .minBy {
                    val k = if (it.type == FacilityType.CONTROL_CENTER) 1.5 else 1.0
                    hypot(avgX - it.left + facilityLength / 2, avgY - it.top + facilityLength / 2) * k
                }


        val powers = enemyGroups.associateBy({ it }, { comparePower(group, it.vehicles) })
        var enemy = enemyGroups.filter { powers[it]!!.first > powerThreshold && powers[it]!!.second > powerThreshold }
                .maxBy { powers[it]!!.second * it.vehicles.size / hypot(it.x - avgX, it.y - avgY) }


        if (enemy != null && facility != null) {
            if (hypot(enemy.x - avgX, enemy.y - avgY) < hypot(facility.left + facilityLength / 2 - avgX, facility.top + facilityLength / 2 - avgY) &&
                    enemy.vehicles.size > 12) {
                facility = null
            } else {
                enemy = null
            }
        }

        if (enemy != null) {
            clearAndSelect(group = group, interrupt = true, queue = group)
            scale(avgX, avgY, factor = 0.5, queue = group)
            val distX = enemy.x - avgX
            val distY = enemy.y - avgY
            move(distX, distY, maxSpeed = 0.24, queue = group)
        }

        if (facility != null) {
            groupsToFacilitiesMap.put(group, facility.id)
            clearAndSelect(group = group, interrupt = true, queue = group)
            scale(avgX, avgY, factor = 0.5, queue = group)
            val distX = facility.left + facilityLength / 2 - avgX
            val distY = facility.top + facilityLength / 2 - avgY
            move(distX, distY, maxSpeed = 0.24, queue = group)
        }
    }
}

private fun preventCollision(group: Int, isLand: Boolean = true): Pair<Double, Double> {
    val avgX = getAvgX(streamVehicles(group))
    val avgY = getAvgY(streamVehicles(group))
    val collisionTypes = if (isLand) listOf(VehicleType.IFV, VehicleType.ARRV, VehicleType.TANK) else listOf(VehicleType.HELICOPTER, VehicleType.FIGHTER)

    if (!moveFromAlly.contains(group)) {
        val nearAlly = streamVehicles(Ownership.ALLY)
                .filter { collisionTypes.contains(it.type) }
                .filterNot { it.groups.contains(group) }
                .find { it.getDistanceTo(avgX, avgY) < stopRadius }
        if (nearAlly != null) {
            clearAndSelect(group = group, interrupt = true, queue = group)
            move((avgX - nearAlly.x) * 0.5, (avgY - nearAlly.y) * 0.5, interrupt = true, queue = group)
            moveFromAlly.add(group)
        }

        val enemies = streamVehicles(Ownership.ENEMY)
                .filter { it.getDistanceTo(avgX, avgY) < enemyRadius }
                .map { enemy -> enemyGroups.find { it.vehicles.contains(enemy) } }
                .filter { it != null }.distinct().flatMap { it!!.vehicles }
        if (enemies.isNotEmpty()) {
            if (comparePower(group, enemies).first < powerThreshold) {
                clearAndSelect(group = group, interrupt = true, queue = group)
                move((avgX - getAvgX(enemies)) * 0.5, (avgY - getAvgY(enemies)) * 0.5, interrupt = true, queue = group)
                moveFromAlly.add(group)
            }
        }
    }
    return Pair(avgX, avgY)
}

fun comparePower(allyGroup: Int, enemies: Iterable<Vehicle>): Pair<Double, Double> {
    var enemiesHealth = enemies.associateBy({ it }, { it.durability.toDouble() }).toMutableMap()
    val startEnemiesHealth = enemiesHealth.values.sum()
    val allies = streamVehicles(allyGroup)
    var allyHealth = allies.associateBy({ it }, { it.durability.toDouble() }).toMutableMap()

    var hpSum = 0.0
    while (abs(hpSum - (enemiesHealth.values.sum() + allyHealth.values.sum())) > 1.0) {
        hpSum = enemiesHealth.values.sum() + allyHealth.values.sum()
        enemies.filter { enemiesHealth.containsKey(it) && enemiesHealth[it]!! > 0 }.forEach { enemy ->
            var power = 0
            allies.filter { allyHealth.containsKey(it) && allyHealth[it]!! > 0 }.forEach {
                power += if (enemy.isAerial) {
                    val p = it.aerialDamage - if (it.isAerial) enemy.aerialDefence else enemy.groundDefence
                    if (p > 0) p else 0
                } else {
                    val p = it.groundDamage - if (it.isAerial) enemy.aerialDefence else enemy.groundDefence
                    if (p > 0) p else 0
                }
            }
            var damage = power / enemies.count()
            val hp = enemiesHealth[enemy]!!.minus(damage)
            enemiesHealth.put(enemy, hp)
        }
        allies.filter { allyHealth.containsKey(it) && allyHealth[it]!! > 0 }.forEach { ally ->
            var power = 0
            enemies.filter { enemiesHealth.containsKey(it) && enemiesHealth[it]!! > 0 }.forEach {
                power += if (ally.isAerial) {
                    val p = it.aerialDamage - if (it.isAerial) ally.aerialDefence else ally.groundDefence
                    if (p > 0) p else 0
                } else {
                    val p = it.groundDamage - if (it.isAerial) ally.aerialDefence else ally.groundDefence
                    if (p > 0) p else 0
                }
            }
            var damage = power / allies.count()
            allyHealth.put(ally, allyHealth[ally]!!.minus(damage))
        }

        enemies.forEach { if (enemiesHealth.containsKey(it) && enemiesHealth[it]!! <= 0) enemiesHealth.remove(it) }
        allies.forEach { if (allyHealth.containsKey(it) && allyHealth[it]!! <= 0) allyHealth.remove(it) }
    }


    val res = Pair(allyHealth.values.sum() - enemiesHealth.values.sum(), startEnemiesHealth - enemiesHealth.values.sum())
    val count = allyHealth.count() - enemiesHealth.count()
//    println("Tick index: ${world!!.tickIndex}, ally group: $allyGroup, enemiesSize: ${enemies.count()}, result: $res, count: $count")
    return res
}