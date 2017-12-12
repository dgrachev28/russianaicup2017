import model.Vehicle
import model.VehicleType
import kotlin.math.hypot
import kotlin.math.sign

private val attackRangeSuffix = 30

var bomberGroup: Int = 0
private var enemyVehicleGroups = emptyList<VehicleGroup>()

private var moveBomberTimeout = 0
private var formBomberInQueue = false

private var bomberMoveFromEnemy = false

fun formBomberGroup() {
    val bomber = streamVehicles(VehicleType.FIGHTER).maxWith(Comparator { o1, o2 -> sign(o1.x + o1.y - o2.x - o2.y).toInt() })
    bomber?.let {
        bomberGroup = 100
        freeGroups.remove(bomberGroup)
        clearAndSelect(rect = getSelectionRectangle(listOf(bomber)), priority = 5, interrupt = true, queue = bomberGroup)
        if (startAirGroup != 0) dismiss(startAirGroup, priority = 5, interrupt = true, queue = bomberGroup)
        assign(bomberGroup, priority = 5, interrupt = true, queue = bomberGroup)
        formBomberInQueue = true
    }
}

fun isBomberFormed(): Boolean = bomberGroup != 0

fun shouldStart(): Boolean =
        try {
            streamVehicles(VehicleType.FIGHTER).map { it.x }.max()!! > streamVehicles(VehicleType.HELICOPTER).map { it.x }.max()!! // TODO если все вертолеты или самолеты умрут то баг
                    || streamVehicles(VehicleType.FIGHTER).map { it.y }.max()!! > streamVehicles(VehicleType.HELICOPTER).map { it.y }.max()!!
        } catch (e: Exception) {
            false
        }

fun actBomber() {
    if (streamVehicles(bomberGroup).count() > 0 && formBomberInQueue) {
        formBomberInQueue = false
    }

    if (streamVehicles(bomberGroup).count() == 0) {
        if (formBomberInQueue) return else formBomberGroup()
    }

    enemyVehicleGroups = enemyGroups


    if (streamVehicleUpdates(bomberGroup).count() == 0 && getQueueSize(bomberGroup) == 0) {
        bomberMoveFromEnemy = false
        moveBomberTimeout = 0
    }

    if (!bomberMoveFromEnemy) {
        if (checkEnemyInAttackRange()) {
            bomberMoveFromEnemy = true
            return
        }
    }

    if (checkEnemyInVisionRange()) return
    if (enemyVehicleGroups.isNotEmpty()) {
        if (moveBomberTimeout == 0) {
            moveToEnemy()
            moveBomberTimeout = 100
        } else moveBomberTimeout--
    }
}

private fun checkEnemyInAttackRange(): Boolean {
    var enemy: Vehicle? = null
    val ally = streamVehicles(bomberGroup).find { bomber ->
        enemy = streamVehicles(Ownership.ENEMY).find { bomber.getDistanceTo(it) < it.aerialAttackRange + attackRangeSuffix }
        enemy != null
    }
    if (ally != null && enemy != null) {
        clearAndSelect(group = bomberGroup, priority = 5, interrupt = true, queue = bomberGroup)
        move((ally.x - enemy!!.x), (ally.y - enemy!!.y), priority = 5, interrupt = true, queue = bomberGroup)
        return true
    }
    return false
}

private fun checkEnemyInVisionRange(): Boolean {
    if (me!!.remainingNuclearStrikeCooldownTicks != 0) return false
    streamVehicles(bomberGroup).forEach { ally ->
        val target = enemyVehicleGroups.find { hypot(ally.x - it.x, ally.y - it.y) < ally.visionRange * getVisionCoef(ally) }
        if (target != null) {
            nuclearStrike(target.x - (target.x - ally.x) * 0.9, target.y - (target.y - ally.y) * 0.9, ally.id, priority = 5, interrupt = true)
            return true
        }
    }
    return false
}

private fun moveToEnemy() {
    val x = streamVehicles(bomberGroup).map { it.x }.average()
    val y = streamVehicles(bomberGroup).map { it.y }.average()
    enemyVehicleGroups.first().let {
        clearAndSelect(group = bomberGroup, priority = 5, interrupt = true, queue = bomberGroup)
        move(it.x - x, it.y - y, priority = 5, interrupt = true, queue = bomberGroup)
    }
}