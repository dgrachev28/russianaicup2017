//private var isEnemySingleGroup: Boolean = true
private var groupAssigned = false
private var mainGroup = 0

fun makeMoveWithoutFacilities() {
    if (!groupAssigned) {
        mainGroup = freeGroups.first()
        freeGroups.remove(mainGroup)
        clearAndSelect(queue = mainGroup)
        deselect(group = bomberGroup, queue = mainGroup)
        assign(mainGroup, queue = mainGroup)
    }
    val powers = enemyGroups.associateBy({ it }, { comparePower(mainGroup, it.vehicles) })
}


//private fun defineEnemyStrategy() {
//    if (isEnemySingleGroup == null) {
//
//    }
//}