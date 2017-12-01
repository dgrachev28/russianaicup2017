package model

/**
 * Тип сооружения.
 */
enum class FacilityType {
    /**
     * Центр управления. Увеличивает возможное количество действий игрока на
     * `game.additionalActionCountPerControlCenter` за `game.actionDetectionInterval` игровых тиков.
     */
    CONTROL_CENTER,

    /**
     * Завод. Может производить технику любого типа по выбору игрока.
     */
    VEHICLE_FACTORY
}
