import model.*
import java.util.*


private var random: Random? = null

var terrainTypeByCellXY: Array<Array<TerrainType>>? = null
var weatherTypeByCellXY: Array<Array<WeatherType>>? = null

var me: Player? = null
var world: World? = null
var game: Game? = null
var move: Move? = null

val vehicleById = HashMap<Long, Vehicle>()
var previousVehicleById: Map<Long, Vehicle> = HashMap()
private val updateTickByVehicleId = HashMap<Long, Int>()

var enemyGroups = emptyList<VehicleGroup>()

var nuclearX: Double = 0.0
var nuclearY: Double = 0.0
var nuclearGroup = IntArray(0)
var nuclearEvasionTicks: Int = 0

val freeGroups = (1..100).toMutableSet()

val startCoord1 = 45.0
val startCoord2 = 119.0
val startCoord3 = 193.0


class MyStrategy : Strategy {

    private fun initializeStrategy(world: World, game: Game) {
        if (random == null) {
            random = Random(game.randomSeed)
            terrainTypeByCellXY = world.terrainByCellXY
            weatherTypeByCellXY = world.weatherByCellXY
        }
    }

    private fun initializeTick(pMe: Player, pWorld: World, pGame: Game, pMove: Move) {
        me = pMe
        world = pWorld
        game = pGame
        move = pMove
        previousVehicleById = HashMap(vehicleById)
        enemyGroups = getEnemyGroups()

        for (vehicle in world!!.newVehicles) {
            vehicleById.put(vehicle.id, vehicle)
            updateTickByVehicleId.put(vehicle.id, world!!.tickIndex)
        }

        for (vehicleUpdate in world!!.vehicleUpdates) {
            val vehicleId = vehicleUpdate.id

            if (vehicleUpdate.durability == 0) {
                vehicleById.remove(vehicleId)
                updateTickByVehicleId.remove(vehicleId)
            } else {
                vehicleById.put(vehicleId, Vehicle(vehicleById[vehicleId]!!, vehicleUpdate))
                updateTickByVehicleId.put(vehicleId, world!!.tickIndex)
            }
        }
    }

    private fun move() {
        startGroupAir()
        startGroupLand()
        if (avoidNuclearStrike()) return

        if (!isAirGrouped()) {
            if (!isBomberFormed() && shouldStart()) {
                formBomberGroup()
            }
            if (isBomberFormed()) {
                actBomber()
            }
        } else {
            nuclearAttack()
        }

        sendGroups()
    }

    private fun nuclearAttack(): Boolean {
        if (me!!.remainingNuclearStrikeCooldownTicks != 0) {
            return false
        }
        for (enemyGroup in enemyGroups) {
            for (vehicle in vehicleById.values) {
                if (vehicle.playerId != me!!.id) {
                    continue
                }
                val coef = getVisionCoef(vehicle)
                if (vehicle.getDistanceTo(enemyGroup.x, enemyGroup.y) < vehicle.visionRange * coef) {
                    var allyGroupSize = 0
                    for (veh in vehicleById.values) {
                        if (veh.playerId == me!!.id && veh.getDistanceTo(enemyGroup.x, enemyGroup.y) < 40) {
                            allyGroupSize++
                            if (allyGroupSize > enemyGroup.vehicles.size) {
                                break
                            }
                        }
                    }

                    if (allyGroupSize < enemyGroup.vehicles.size) {
                        nuclearStrike(enemyGroup.x, enemyGroup.y, vehicle.id, priority = 3, interrupt = true)
                        return true
                    }
                }
            }
        }
        return false
    }


    private fun avoidNuclearStrike(): Boolean {
        if (!isAirGrouped() || !isLandGrouped()) {
            return false
        }
        if (nuclearEvasionTicks != 0) {
            if (world!!.opponentPlayer.nextNuclearStrikeTickIndex == -1) {
                if (nuclearGroup.isEmpty()) {
//                    clearAndSelect(left = nuclearX - 70, top = nuclearY - 70, bottom = nuclearX + 70, right = nuclearX + 70, priority = 2, interrupt = true)
//                    scale(nuclearX, nuclearY, factor = 10.0, priority = 2, interrupt = true)
                } else {
                    nuclearGroup.forEach {
                        clearAndSelect(group = it, priority = 2, interrupt = true, queue = it)
                        scale(nuclearX, nuclearY, factor = 0.1, priority = 2, interrupt = true, queue = it)
                    }
                    nuclearGroup = IntArray(0)
                }
            }
            nuclearEvasionTicks--
            return true
        }
        if (world!!.opponentPlayer.nextNuclearStrikeTickIndex != -1) {
            nuclearX = world!!.opponentPlayer.nextNuclearStrikeX
            nuclearY = world!!.opponentPlayer.nextNuclearStrikeY
            val vehicle = streamVehicles(Ownership.ALLY).reduce { v1, v2 -> if (v1.getDistanceTo(nuclearX, nuclearY) < v2.getDistanceTo(nuclearX, nuclearY)) v1 else v2 }
            nuclearX = vehicle.x
            nuclearY = vehicle.y
            nuclearGroup = vehicle.groups
            if (nuclearGroup.isEmpty()) {
//                clearAndSelect(left = nuclearX - 70, top = nuclearY - 70, bottom = nuclearX + 70, right = nuclearX + 70, priority = 2, interrupt = true)
//                scale(nuclearX, nuclearY, factor = 10.0, priority = 2, interrupt = true)
            } else {
                nuclearGroup.forEach {
                    clearAndSelect(group = it, priority = 2, interrupt = true, queue = it)
                    scale(nuclearX, nuclearY, factor = 10.0, priority = 2, interrupt = true, queue = it)
                }
            }
            nuclearEvasionTicks = 40
            return true
        }
        return false
    }


    var oldStrategy: OldMyStrategy? = null

    override fun move(me: Player, world: World, game: Game, move: Move) {
        if (world.facilities.isEmpty()) {
            if (oldStrategy == null) {
                oldStrategy = OldMyStrategy()
            }
            oldStrategy!!.move(me, world, game, move)
            return
        }
        initializeStrategy(world, game)
        initializeTick(me, world, game, move)

        if (me.remainingActionCooldownTicks > 0) return
        move()
        makeAction(move)
    }

}
