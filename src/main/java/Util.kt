import model.*
import java.util.ArrayList

private val eps = 0.1

class VehicleGroup(vehicle: Vehicle) {
    var vehicles: MutableList<Vehicle> = mutableListOf()
    var x: Double = 0.toDouble()
    var y: Double = 0.toDouble()

    init {
        addVehicle(vehicle)
    }

    fun addVehicle(vehicle: Vehicle) {
        x = (x * vehicles.size + vehicle.x) / (vehicles.size + 1)
        y = (y * vehicles.size + vehicle.y) / (vehicles.size + 1)
        vehicles.add(vehicle)
    }
}

fun getEnemyVehicleGroups(visionRadius: Int): List<VehicleGroup> {
    val nearEnemies = ArrayList<VehicleGroup>()
    val centerX = getAlliedCenterX()
    val centerY = getAlliedCenterY()
    for (vehicle in vehicleById.values) {
        if (vehicle.playerId != me!!.id && vehicle.getDistanceTo(centerX, centerY) < visionRadius) {
            var addedToGroup = false
            for (group in nearEnemies) {
                if (group.x - vehicle.x < 30) {
                    group.addVehicle(vehicle)
                    addedToGroup = true
                    break
                }
            }
            if (!addedToGroup) {
                nearEnemies.add(VehicleGroup(vehicle))
            }
        }
    }

    nearEnemies.sortWith(Comparator { o1, o2 ->
        val percentHP1 = o1.vehicles.map { v -> v.durability.toDouble() / v.maxDurability }.sum()
        val percentHP2 = o2.vehicles.map { v -> v.durability.toDouble() / v.maxDurability }.sum()
        if (percentHP1 < 0.75 && o1.vehicles.size > 5 || percentHP2 < 0.75 && o2.vehicles.size > 5) {
            Math.signum(percentHP1 - percentHP2).toInt()
        } else {
            o2.vehicles.size - o1.vehicles.size
        }
    })
    return nearEnemies
}


fun getVisionCoef(vehicle: Vehicle): Double =
        getVisionCoefByTerrainAndWeather(
                terrainTypeByCellXY!![vehicle.x.toInt() / 32][vehicle.y.toInt() / 32],
                weatherTypeByCellXY!![vehicle.x.toInt() / 32][vehicle.y.toInt() / 32])

fun getVisionCoefByTerrainAndWeather(terrainType: TerrainType, weatherType: WeatherType): Double {
    var coef = 1.0
    coef *= when (terrainType) {
        TerrainType.PLAIN -> game!!.plainTerrainVisionFactor
        TerrainType.SWAMP -> game!!.swampTerrainVisionFactor
        TerrainType.FOREST -> game!!.forestTerrainVisionFactor
    }
    coef *= when (weatherType) {
        WeatherType.CLEAR -> game!!.clearWeatherVisionFactor
        WeatherType.CLOUD -> game!!.cloudWeatherVisionFactor
        WeatherType.RAIN -> game!!.rainWeatherVisionFactor
    }
    return coef
}


fun calcRectangle(group: Int, vehicleType: VehicleType) =
        getSelectionRectangle(streamVehicles(group, listOf(vehicleType)))

fun getSelectionRectangle(vehicles: Iterable<Vehicle>): Rectangle =
        Rectangle(vehicles.map { it.x }.min()!! - eps,
                vehicles.map { it.y }.min()!! - eps,
                vehicles.map { it.x }.max()!! + eps,
                vehicles.map { it.y }.max()!! + eps)


data class Rectangle(val left: Double,
                     val top: Double,
                     val right: Double,
                     val bottom: Double)

fun streamVehicleUpdates(ownership: Ownership, group: Int = 0, vehicleType: VehicleType? = null): Iterable<Vehicle> {
    val stream = world!!.vehicleUpdates
            .filter { it.durability != 0 }
            .filter { if (group != 0) it.groups.contains(group) else it.groups.isEmpty() }
            .map { it.id }
            .filter { id -> vehicleById[id]!!.x != previousVehicleById[id]!!.x || vehicleById[id]!!.y != previousVehicleById[id]!!.y }
            .map { vehicleById[it]!! }

    return filterOwnershipAndVehicleType(ownership, if (vehicleType == null) null else listOf(vehicleType), stream)
}

fun streamVehicles(types: List<VehicleType>, ownership: Ownership = Ownership.ALLY): Iterable<Vehicle> =
        filterOwnershipAndVehicleType(ownership, types, vehicleById.values)

fun streamVehicles(vehicleType: VehicleType?, ownership: Ownership = Ownership.ALLY): Iterable<Vehicle> =
        filterOwnershipAndVehicleType(ownership, if (vehicleType == null) null else listOf(vehicleType), vehicleById.values)

fun streamVehicles(group: Int, types: List<VehicleType> = emptyList(), ownership: Ownership = Ownership.ALLY): Iterable<Vehicle> =
        filterOwnershipAndVehicleType(ownership, types, vehicleById.values, group)

private fun filterOwnershipAndVehicleType(ownership: Ownership, types: List<VehicleType>?, vehicles: Iterable<Vehicle>, group: Int = 0): Iterable<Vehicle> {
    var stream = vehicles
    when (ownership) {
        Ownership.ALLY -> stream = stream.filter { vehicle -> vehicle.playerId == me!!.id }
        Ownership.ENEMY -> stream = stream.filter { vehicle -> vehicle.playerId != me!!.id }
    }

    if (types != null && types.isNotEmpty()) {
        stream = stream.filter { vehicle -> types.contains(vehicle.type) }
    }

    if (group != 0) {
        stream = stream.filter { it.groups.contains(group) }
    }
    return stream
}

fun streamVehicles(ownership: Ownership): Iterable<Vehicle> = streamVehicles(null, ownership)

fun getAlliedCenterX(vehicleType: VehicleType? = null): Double =
        streamVehicles(vehicleType).map { it.x }.average()

fun getAlliedCenterY(vehicleType: VehicleType? = null): Double =
        streamVehicles(vehicleType).map { it.y }.average()

fun getAvgX(vehicles: Iterable<Vehicle>): Double = vehicles.map { it.x }.average()

fun getAvgY(vehicles: Iterable<Vehicle>): Double = vehicles.map { it.y }.average()

enum class Ownership {
    ANY, ALLY, ENEMY
}

fun printMove(move: Move) {
    println("Tick=${world!!.tickIndex}")
    println("Move(action=${move.action}, group=${move.group}, left=${move.left}, top=${move.top}, right=${move.right}, bottom=${move.bottom}, x=${move.x}, y=${move.y}, angle=${move.angle}, factor=${move.factor}, maxSpeed=${move.maxSpeed}, maxAngularSpeed=${move.maxAngularSpeed}, vehicleType=${move.vehicleType}, facilityId=${move.facilityId}, vehicleId=${move.vehicleId})")
}