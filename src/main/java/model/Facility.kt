package model

/**
 * Класс, определяющий сооружение --- прямоугольную область на карте.
 */
class Facility(
        /**
         * @return Возвращает уникальный идентификатор сооружения.
         */
        val id: Long,
        /**
         * @return Возвращает тип сооружения.
         */
        val type: FacilityType,
        /**
         * @return Возвращает идентификатор игрока, захватившего сооружение, или `-1`, если сооружение никем не
         * контролируется.
         */
        val ownerPlayerId: Long,
        /**
         * @return Возвращает абсциссу левой границы сооружения.
         */
        val left: Double,
        /**
         * @return Возвращает ординату верхней границы сооружения.
         */
        val top: Double,
        /**
         * @return Возвращает индикатор захвата сооружения в интервале от `-game.maxFacilityCapturePoints` до
         * `game.maxFacilityCapturePoints`. Если индикатор находится в положительной зоне, очки захвата принадлежат
         * вам, иначе вашему противнику.
         */
        val capturePoints: Double,
        /**
         * @return Возвращает тип техники, производящейся в данном сооружении, или `null`. Применимо только к заводу
         * (`FacilityType.VEHICLE_FACTORY`).
         */
        val vehicleType: VehicleType?,
        /**
         * @return Возвращает неотрицательное число --- прогресс производства техники. Применимо только к заводу
         * (`FacilityType.VEHICLE_FACTORY`).
         */
        val productionProgress: Int
)
