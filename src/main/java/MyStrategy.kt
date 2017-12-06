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
        startGroupLand()
//        if (!startGrouped) startGroupAir()
        if (avoidNuclearStrike()) return
        if (!isBomberFormed() && shouldStart()) {
            formBomberGroup()
        }
        if (isBomberFormed()) {
            actBomber()
        }
        sendGroups()
    }


    private fun avoidNuclearStrike() : Boolean {
        if (world!!.opponentPlayer.nextNuclearStrikeTickIndex != -1) {
            nuclearX = world!!.opponentPlayer.nextNuclearStrikeX
            nuclearY = world!!.opponentPlayer.nextNuclearStrikeY
            val vehicle = streamVehicles(Ownership.ALLY).reduce { v1, v2 -> if (v1.getDistanceTo(nuclearX, nuclearY) < v2.getDistanceTo(nuclearX, nuclearY)) v1 else v2 }
            nuclearX = vehicle.x
            nuclearY = vehicle.y
            nuclearGroup = vehicle.groups
            if (nuclearGroup.isEmpty()) {
                clearAndSelect(priority = 2, interrupt = true)
                scale(nuclearX, nuclearY, factor = 10.0, priority = 2, interrupt = true)
            } else {
                nuclearGroup.forEach {
                    scale(nuclearX, nuclearY, it, factor = 10.0, priority = 2, interrupt = true)
                }
            }
            nuclearEvasionTicks = 40
            return true
        }
        if (nuclearEvasionTicks != 0) {
            if (world!!.opponentPlayer.nextNuclearStrikeTickIndex == -1) {
                if (nuclearGroup.isEmpty()) {
                    clearAndSelect(priority = 2, interrupt = true)
                    scale(nuclearX, nuclearY, factor = 0.1, priority = 2, interrupt = true)
                } else {
                    nuclearGroup.forEach {
                        scale(nuclearX, nuclearY, it, factor = 0.1, priority = 2, interrupt = true)
                    }
                }
            }
            nuclearEvasionTicks--
            return true
        }
        return false
    }

    override fun move(me: Player, world: World, game: Game, move: Move) {
        initializeStrategy(world, game)
        initializeTick(me, world, game, move)

        if (me.remainingActionCooldownTicks > 0) return
        move()
        makeAction(move)
    }

}
