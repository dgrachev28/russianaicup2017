import model.*

private val eps = 0.1
private val mapSize = 128
private val squareSize = 8 // squareSize * mapSize = 1024  - Размер всей карты



class VehicleGroup(vehicle: Vehicle? = null) {
    var vehicles: MutableList<Vehicle> = mutableListOf()
    var x: Double = 0.0
    var y: Double = 0.0

    init {
        vehicle?.let { addVehicle(it) }
    }

    fun addVehicle(vehicle: Vehicle) {
        x = (x * vehicles.size + vehicle.x) / (vehicles.size + 1)
        y = (y * vehicles.size + vehicle.y) / (vehicles.size + 1)
        vehicles.add(vehicle)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VehicleGroup

        if (vehicles != other.vehicles) return false
        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vehicles.hashCode()
        result = 31 * result + x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }


}

fun getEnemyGroups(): List<VehicleGroup> {
    val map: Array<Array<MutableList<Vehicle>>> = Array(mapSize, { Array(mapSize, { mutableListOf<Vehicle>() }) })
    val visited: Array<BooleanArray> = Array(mapSize, { BooleanArray(mapSize) })
    streamVehicles(Ownership.ENEMY).forEach { map[(it.x / squareSize).toInt()][(it.y / squareSize).toInt()].add(it) }

    val groups = mutableListOf<VehicleGroup>()

    for (i in 0 until mapSize) {
        for (j in 0 until mapSize) {
            if (visited[i][j]) {
                continue
            }
            if (map[i][j].isNotEmpty()) {
                val vehicleGroup = VehicleGroup()
                dfs(i, j, map, visited, vehicleGroup)
                groups.add(vehicleGroup)
            }
        }
    }
    sortEnemies(groups)
    return groups
}

fun sortEnemies(groups: MutableList<VehicleGroup>) {
    groups.sortWith(Comparator { o1, o2 ->
        val percentHP1 = o1.vehicles.map { v -> v.durability.toDouble() / v.maxDurability }.sum()
        val percentHP2 = o2.vehicles.map { v -> v.durability.toDouble() / v.maxDurability }.sum()
        if (percentHP1 < 0.75 && o1.vehicles.size > 5 || percentHP2 < 0.75 && o2.vehicles.size > 5) {
            Math.signum(percentHP1 - percentHP2).toInt()
        } else {
            o2.vehicles.size - o1.vehicles.size
        }
    })
}

fun dfs(i: Int, j: Int, map: Array<Array<MutableList<Vehicle>>>, visited: Array<BooleanArray>, res: VehicleGroup) {
    if (visited[i][j] || map[i][j].isEmpty()) {
        return
    }

    map[i][j].forEach { res.addVehicle(it) }
    visited[i][j] = true
    if (i > 0) {
        dfs(i - 1, j, map, visited, res)
    }
    if (j > 0) {
        dfs(i, j - 1, map, visited, res)
    }
    if (i < map.size - 1) {
        dfs(i + 1, j, map, visited, res)
    }
    if (j < map[0].size - 1) {
        dfs(i, j + 1, map, visited, res)
    }
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

fun streamVehicleUpdates(group: Int = 0, ownership: Ownership = Ownership.ALLY, vehicleType: VehicleType? = null): Iterable<Vehicle> {
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
    println("Move(action=${move.action}, group=${move.group}, left=${move.left}, top=${move.top}, right=${move.right}," +
            " bottom=${move.bottom}, x=${move.x}, y=${move.y}, angle=${move.angle}, factor=${move.factor}, maxSpeed=${move.maxSpeed}," +
            " maxAngularSpeed=${move.maxAngularSpeed}, vehicleType=${move.vehicleType}, facilityId=${move.facilityId}, vehicleId=${move.vehicleId})")
}