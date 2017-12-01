package model

/**
 * Класс, определяющий технику. Содержит также все свойства круглых объектов.
 */
class Vehicle(
        id: Long, x: Double, y: Double, radius: Double,
    /**
     * @return Возвращает идентификатор игрока, которому принадлежит техника.
     */
    val playerId: Long,
    /**
     * @return Возвращает текущую прочность.
     */
    val durability: Int,
    /**
     * @return Возвращает максимальную прочность.
     */
    val maxDurability: Int,
    /**
     * @return Возвращает максимальное расстояние, на которое данная техника может переместиться за один игровой тик,
     * без учёта типа местности и погоды. При перемещении по дуге учитывается длина дуги,
     * а не кратчайшее расстояние между начальной и конечной точками.
     */
    val maxSpeed: Double,
    /**
     * @return Возвращает максимальное расстояние (от центра до центра),
     * на котором данная техника обнаруживает другие объекты, без учёта типа местности и погоды.
     */
    val visionRange: Double,
    /**
     * @return Возвращает квадрат максимального расстояния (от центра до центра),
     * на котором данная техника обнаруживает другие объекты, без учёта типа местности и погоды.
     */
    val squaredVisionRange: Double,
    /**
     * @return Возвращает максимальное расстояние (от центра до центра),
     * на котором данная техника может атаковать наземные объекты.
     */
    val groundAttackRange: Double,
    /**
     * @return Возвращает квадрат максимального расстояния (от центра до центра),
     * на котором данная техника может атаковать наземные объекты.
     */
    val squaredGroundAttackRange: Double,
    /**
     * @return Возвращает максимальное расстояние (от центра до центра),
     * на котором данная техника может атаковать воздушные объекты.
     */
    val aerialAttackRange: Double,
    /**
     * @return Возвращает квадрат максимального расстояния (от центра до центра),
     * на котором данная техника может атаковать воздушные объекты.
     */
    val squaredAerialAttackRange: Double,
    /**
     * @return Возвращает урон одной атаки по наземному объекту.
     */
    val groundDamage: Int,
    /**
     * @return Возвращает урон одной атаки по воздушному объекту.
     */
    val aerialDamage: Int,
    /**
     * @return Возвращает защиту от атак наземных юнитов.
     */
    val groundDefence: Int,
    /**
     * @return Возвращает защиту от атак воздушых юнитов.
     */
    val aerialDefence: Int,
    /**
     * @return Возвращает минимально возможный интервал между двумя последовательными атаками данной техники.
     */
    val attackCooldownTicks: Int,
    /**
     * @return Возвращает количество тиков, оставшееся до следующей атаки.
     * Для совершения атаки необходимо, чтобы это значение было равно нулю.
     */
    val remainingAttackCooldownTicks: Int,
    /**
     * @return Возвращает тип техники.
     */
    val type: VehicleType,
    /**
     * @return Возвращает `true` в том и только том случае, если эта техника воздушная.
     */
    val isAerial: Boolean,
    /**
     * @return Возвращает `true` в том и только том случае, если эта техника выделена.
     */
    val isSelected: Boolean,
    groups: IntArray
) : CircularUnit(id, x, y, radius) {
    /**
     * @return Возвращает группы, в которые входит эта техника.
     */
    val groups = groups.copyOf()

    constructor(vehicle: Vehicle, vehicleUpdate: VehicleUpdate) : this(
            vehicle.id,
            vehicleUpdate.x,
            vehicleUpdate.y,
            vehicle.radius,
            vehicle.playerId,
            vehicleUpdate.durability,
            vehicle.maxDurability,
            vehicle.maxSpeed,
            vehicle.visionRange,
            vehicle.squaredVisionRange,
            vehicle.groundAttackRange,
            vehicle.squaredGroundAttackRange,
            vehicle.aerialAttackRange,
            vehicle.squaredAerialAttackRange,
            vehicle.groundDamage,
            vehicle.aerialDamage,
            vehicle.groundDefence,
            vehicle.aerialDefence,
            vehicle.attackCooldownTicks,
            vehicleUpdate.remainingAttackCooldownTicks,
            vehicle.type,
            vehicle.isAerial,
            vehicleUpdate.isSelected,
            vehicleUpdate.groups
    )
}
