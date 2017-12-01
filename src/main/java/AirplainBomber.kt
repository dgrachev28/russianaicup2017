import model.Vehicle
import model.VehicleType
import kotlin.math.hypot
import kotlin.math.sign

private val attackRangeSuffix = 5

private var bomberGroup: Int = 0
private var enemyVehicleGroups = emptyList<VehicleGroup>()

private var moveBomberTimeout = 0


fun formBomberGroup() {
    val bomber = streamVehicles(VehicleType.FIGHTER).maxWith(Comparator { o1, o2 -> sign(o1.x + o1.y - o2.x - o2.y).toInt() })
    bomber?.let {
        bomberGroup = 100
        clearAndSelect(rect = getSelectionRectangle(listOf(bomber)), priority = 5, interrupt = true)
        if (startAirGroup != 0) dismiss(startAirGroup)
        assign(bomberGroup, priority = 5, interrupt = true)
    }
}

fun isBomberFormed(): Boolean = bomberGroup != 0

fun actBomber() {
    enemyVehicleGroups = getEnemyVehicleGroups(1450)

    if (checkEnemyInAttackRange()) return
    if (checkEnemyInVisionRange()) return
    if (moveBomberTimeout == 0) {
        moveToEnemy()
        moveBomberTimeout = 100
    } else moveBomberTimeout--

}

private fun checkEnemyInAttackRange(): Boolean {
    var enemy: Vehicle? = null
    val ally = streamVehicles(bomberGroup).find { bomber ->
        enemy = streamVehicles(Ownership.ENEMY).find { bomber.getDistanceTo(it) < it.aerialAttackRange + attackRangeSuffix }
        enemy != null
    }
    if (ally != null && enemy != null) {
        clearAndSelect(group = bomberGroup, priority = 5, interrupt = true)
        move(ally.x - enemy!!.x, ally.y - enemy!!.y, group = bomberGroup, priority = 5, interrupt = true)
        return true
    }
    return false
}

private fun checkEnemyInVisionRange(): Boolean {
    if (me!!.remainingNuclearStrikeCooldownTicks != 0) return false
    streamVehicles(bomberGroup).forEach { ally ->
        val target = enemyVehicleGroups.find { hypot(ally.x - it.x, ally.y - it.y) < ally.visionRange * getVisionCoef(ally) }
        if (target != null) {
            nuclearStrike(target.x, target.y, ally.id, priority = 5, interrupt = true)
            return true
        }
    }
    return false
}

private fun moveToEnemy() {
    val x = streamVehicles(bomberGroup).map { it.x }.average()
    val y = streamVehicles(bomberGroup).map { it.y }.average()
    enemyVehicleGroups.first().let {
        clearAndSelect(group = bomberGroup, priority = 5, interrupt = true)
        move(it.x - x, it.y - y, group = bomberGroup, priority = 5, interrupt = true)
    }
}