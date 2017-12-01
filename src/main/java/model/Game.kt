package model

/**
 * Предоставляет доступ к различным игровым константам.
 */
class Game(
        /**
         * @return Возвращает некоторое число, которое ваша стратегия может использовать для инициализации генератора
         * случайных чисел. Данное значение имеет рекомендательный характер, однако позволит более точно воспроизводить
         * прошедшие игры.
         */
        val randomSeed: Long,
        /**
         * @return Возвращает базовую длительность игры в тиках. Реальная длительность может отличаться от этого значения в
         * меньшую сторону. Эквивалентно `world.tickCount`.
         */
        val tickCount: Int,
        /**
         * @return Возвращает ширину карты.
         */
        val worldWidth: Double,
        /**
         * @return Возвращает высоту карты.
         */
        val worldHeight: Double,
        /**
         * @return Возвращает `true`, если и только если в данной игре включен режим частичной видимости.
         */
        val isFogOfWarEnabled: Boolean,
        /**
         * @return Возвращает количество баллов, получаемое игроком в случае уничтожения всех юнитов противника.
         */
        val victoryScore: Int,
        /**
         * @return Возвращает количество баллов за захват сооружения.
         */
        val facilityCaptureScore: Int,
        /**
         * @return Возвращает количество баллов за уничтожение юнита противника.
         */
        val vehicleEliminationScore: Int,
        /**
         * @return Возвращает интервал, учитываемый в ограничении количества действий стратегии.
         */
        val actionDetectionInterval: Int,
        /**
         * @return Возвращает базовое количество действий, которое может совершить стратегия за
         * `actionDetectionInterval` последовательных тиков.
         */
        val baseActionCount: Int,
        /**
         * @return Возвращает дополнительное количество действий за каждый захваченный центр управления
         * (`FacilityType.CONTROL_CENTER`).
         */
        val additionalActionCountPerControlCenter: Int,
        /**
         * @return Возвращает максимально возможный индекс группы юнитов.
         */
        val maxUnitGroup: Int,
        /**
         * @return Возвращает количество столбцов в картах местности и погоды.
         */
        val terrainWeatherMapColumnCount: Int,
        /**
         * @return Возвращает количество строк в картах местности и погоды.
         */
        val terrainWeatherMapRowCount: Int,
        /**
         * @return Возвращает мультипликатор радиуса обзора наземной техники, находящейся на равнинной местности
         * (`TerrainType.PLAIN`).
         */
        val plainTerrainVisionFactor: Double,
        /**
         * @return Возвращает мультипликатор радиуса обзора любой техники при обнаружении наземной техники противника,
         * находящейся на равнинной местности (`TerrainType.PLAIN`).
         */
        val plainTerrainStealthFactor: Double,
        /**
         * @return Возвращает мультипликатор максимальной скорости наземной техники, находящейся на равнинной местности
         * (`TerrainType.PLAIN`).
         */
        val plainTerrainSpeedFactor: Double,
        /**
         * @return Возвращает мультипликатор радиуса обзора наземной техники, находящейся в болотистой местности
         * (`TerrainType.SWAMP`).
         */
        val swampTerrainVisionFactor: Double,
        /**
         * @return Возвращает мультипликатор радиуса обзора любой техники при обнаружении наземной техники противника,
         * находящейся в болотистой местности (`TerrainType.SWAMP`).
         */
        val swampTerrainStealthFactor: Double,
        /**
         * @return Возвращает мультипликатор максимальной скорости наземной техники, находящейся в болотистой местности
         * (`TerrainType.SWAMP`).
         */
        val swampTerrainSpeedFactor: Double,
        /**
         * @return Возвращает мультипликатор радиуса обзора наземной техники, находящейся в лесистой местности
         * (`TerrainType.FOREST`).
         */
        val forestTerrainVisionFactor: Double,
        /**
         * @return Возвращает мультипликатор радиуса обзора любой техники при обнаружении наземной техники противника,
         * находящейся в лесистой местности (`TerrainType.FOREST`).
         */
        val forestTerrainStealthFactor: Double,
        /**
         * @return Возвращает мультипликатор максимальной скорости наземной техники, находящейся в лесистой местности
         * (`TerrainType.FOREST`).
         */
        val forestTerrainSpeedFactor: Double,
        /**
         * @return Возвращает мультипликатор радиуса обзора воздушной техники, находящейся в области ясной погоды
         * (`WeatherType.CLEAR`).
         */
        val clearWeatherVisionFactor: Double,
        /**
         * @return Возвращает мультипликатор радиуса обзора любой техники при обнаружении воздушной техники противника,
         * находящейся в области ясной погоды (`WeatherType.CLEAR`).
         */
        val clearWeatherStealthFactor: Double,
        /**
         * @return Возвращает мультипликатор максимальной скорости воздушной техники, находящейся в области ясной погоды
         * (`WeatherType.CLEAR`).
         */
        val clearWeatherSpeedFactor: Double,
        /**
         * @return Возвращает мультипликатор радиуса обзора воздушной техники, находящейся в плотных облаках
         * (`WeatherType.CLOUD`).
         */
        val cloudWeatherVisionFactor: Double,
        /**
         * @return Возвращает мультипликатор радиуса обзора любой техники при обнаружении воздушной техники противника,
         * находящейся в плотных облаках (`WeatherType.CLOUD`).
         */
        val cloudWeatherStealthFactor: Double,
        /**
         * @return Возвращает мультипликатор максимальной скорости воздушной техники, находящейся в плотных облаках
         * (`WeatherType.CLOUD`).
         */
        val cloudWeatherSpeedFactor: Double,
        /**
         * @return Возвращает мультипликатор радиуса обзора воздушной техники, находящейся в условиях сильного дождя
         * (`WeatherType.RAIN`).
         */
        val rainWeatherVisionFactor: Double,
        /**
         * @return Возвращает мультипликатор радиуса обзора любой техники при обнаружении воздушной техники противника,
         * находящейся в условиях сильного дождя (`WeatherType.RAIN`).
         */
        val rainWeatherStealthFactor: Double,
        /**
         * @return Возвращает мультипликатор максимальной скорости воздушной техники, находящейся в условиях сильного дождя
         * (`WeatherType.RAIN`).
         */
        val rainWeatherSpeedFactor: Double,
        /**
         * @return Возвращает радиус техники.
         */
        val vehicleRadius: Double,
        /**
         * @return Возвращает максимальную прочность танка.
         */
        val tankDurability: Int,
        /**
         * @return Возвращает максимальную скорость танка.
         */
        val tankSpeed: Double,
        /**
         * @return Возвращает базовый радиус обзора танка.
         */
        val tankVisionRange: Double,
        /**
         * @return Возвращает дальность атаки танка по наземным целям.
         */
        val tankGroundAttackRange: Double,
        /**
         * @return Возвращает дальность атаки танка по воздушным целям.
         */
        val tankAerialAttackRange: Double,
        /**
         * @return Возвращает урон одной атаки танка по наземной технике.
         */
        val tankGroundDamage: Int,
        /**
         * @return Возвращает урон одной атаки танка по воздушной технике.
         */
        val tankAerialDamage: Int,
        /**
         * @return Возвращает защиту танка от атак наземной техники.
         */
        val tankGroundDefence: Int,
        /**
         * @return Возвращает защиту танка от атак воздушной техники.
         */
        val tankAerialDefence: Int,
        /**
         * @return Возвращает интервал в тиках между двумя последовательными атаками танка.
         */
        val tankAttackCooldownTicks: Int,
        /**
         * @return Возвращает количество тиков, необхожимое для производства одного танка на заводе
         * (`FacilityType.VEHICLE_FACTORY`).
         */
        val tankProductionCost: Int,
        /**
         * @return Возвращает максимальную прочность БМП.
         */
        val ifvDurability: Int,
        /**
         * @return Возвращает максимальную скорость БМП.
         */
        val ifvSpeed: Double,
        /**
         * @return Возвращает базовый радиус обзора БМП.
         */
        val ifvVisionRange: Double,
        /**
         * @return Возвращает дальность атаки БМП по наземным целям.
         */
        val ifvGroundAttackRange: Double,
        /**
         * @return Возвращает дальность атаки БМП по воздушным целям.
         */
        val ifvAerialAttackRange: Double,
        /**
         * @return Возвращает урон одной атаки БМП по наземной технике.
         */
        val ifvGroundDamage: Int,
        /**
         * @return Возвращает урон одной атаки БМП по воздушной технике.
         */
        val ifvAerialDamage: Int,
        /**
         * @return Возвращает защиту БМП от атак наземной техники.
         */
        val ifvGroundDefence: Int,
        /**
         * @return Возвращает защиту БМП от атак воздушной техники.
         */
        val ifvAerialDefence: Int,
        /**
         * @return Возвращает интервал в тиках между двумя последовательными атаками БМП.
         */
        val ifvAttackCooldownTicks: Int,
        /**
         * @return Возвращает количество тиков, необхожимое для производства одной БМП на заводе
         * (`FacilityType.VEHICLE_FACTORY`).
         */
        val ifvProductionCost: Int,
        /**
         * @return Возвращает максимальную прочность БРЭМ.
         */
        val arrvDurability: Int,
        /**
         * @return Возвращает максимальную скорость БРЭМ.
         */
        val arrvSpeed: Double,
        /**
         * @return Возвращает базовый радиус обзора БРЭМ.
         */
        val arrvVisionRange: Double,
        /**
         * @return Возвращает защиту БРЭМ от атак наземной техники.
         */
        val arrvGroundDefence: Int,
        /**
         * @return Возвращает защиту БРЭМ от атак воздушной техники.
         */
        val arrvAerialDefence: Int,
        /**
         * @return Возвращает количество тиков, необхожимое для производства одной БРЭМ на заводе
         * (`FacilityType.VEHICLE_FACTORY`).
         */
        val arrvProductionCost: Int,
        /**
         * @return Возвращает максимальное расстояние (от центра до центра), на котором БРЭМ может ремонтировать
         * дружественную технику.
         */
        val arrvRepairRange: Double,
        /**
         * @return Возвращает максимальное количество прочности, которое БРЭМ может восстановить дружественной технике за
         * один тик.
         */
        val arrvRepairSpeed: Double,
        /**
         * @return Возвращает максимальную прочность ударного вертолёта.
         */
        val helicopterDurability: Int,
        /**
         * @return Возвращает максимальную скорость ударного вертолёта.
         */
        val helicopterSpeed: Double,
        /**
         * @return Возвращает базовый радиус обзора ударного вертолёта.
         */
        val helicopterVisionRange: Double,
        /**
         * @return Возвращает дальность атаки ударного вертолёта по наземным целям.
         */
        val helicopterGroundAttackRange: Double,
        /**
         * @return Возвращает дальность атаки ударного вертолёта по воздушным целям.
         */
        val helicopterAerialAttackRange: Double,
        /**
         * @return Возвращает урон одной атаки ударного вертолёта по наземной технике.
         */
        val helicopterGroundDamage: Int,
        /**
         * @return Возвращает урон одной атаки ударного вертолёта по воздушной технике.
         */
        val helicopterAerialDamage: Int,
        /**
         * @return Возвращает защиту ударного вертолёта от атак наземной техники.
         */
        val helicopterGroundDefence: Int,
        /**
         * @return Возвращает защиту ударного вертолёта от атак воздушной техники.
         */
        val helicopterAerialDefence: Int,
        /**
         * @return Возвращает интервал в тиках между двумя последовательными атаками ударного вертолёта.
         */
        val helicopterAttackCooldownTicks: Int,
        /**
         * @return Возвращает количество тиков, необхожимое для производства одного ударного вертолёта на заводе
         * (`FacilityType.VEHICLE_FACTORY`).
         */
        val helicopterProductionCost: Int,
        /**
         * @return Возвращает максимальную прочность истребителя.
         */
        val fighterDurability: Int,
        /**
         * @return Возвращает максимальную скорость истребителя.
         */
        val fighterSpeed: Double,
        /**
         * @return Возвращает базовый радиус обзора истребителя.
         */
        val fighterVisionRange: Double,
        /**
         * @return Возвращает дальность атаки истребителя по наземным целям.
         */
        val fighterGroundAttackRange: Double,
        /**
         * @return Возвращает дальность атаки истребителя по воздушным целям.
         */
        val fighterAerialAttackRange: Double,
        /**
         * @return Возвращает урон одной атаки истребителя по наземной технике.
         */
        val fighterGroundDamage: Int,
        /**
         * @return Возвращает урон одной атаки истребителя по воздушной технике.
         */
        val fighterAerialDamage: Int,
        /**
         * @return Возвращает защиту истребителя от атак наземной техники.
         */
        val fighterGroundDefence: Int,
        /**
         * @return Возвращает защиту истребителя от атак воздушной техники.
         */
        val fighterAerialDefence: Int,
        /**
         * @return Возвращает интервал в тиках между двумя последовательными атаками истребителя.
         */
        val fighterAttackCooldownTicks: Int,
        /**
         * @return Возвращает количество тиков, необхожимое для производства одного истребителя на заводе
         * (`FacilityType.VEHICLE_FACTORY`).
         */
        val fighterProductionCost: Int,
        /**
         * @return Возвращает максимально возможную абсолютную величину индикатора захвата сооружения
         * (`facility.capturePoints`).
         */
        val maxFacilityCapturePoints: Double,
        /**
         * @return Возвращает скорость изменения индикатора захвата сооружения (`facility.capturePoints`) за каждую
         * единицу техники, центр которой находится внутри сооружения.
         */
        val facilityCapturePointsPerVehiclePerTick: Double,
        /**
         * @return Возвращает ширину сооружения.
         */
        val facilityWidth: Double,
        /**
         * @return Возвращает высоту сооружения.
         */
        val facilityHeight: Double,
        /**
         * @return Возвращает минимально возможный интервал между двумя последовательными тактическими ядерными ударами
         * (`ActionType.TACTICAL_NUCLEAR_STRIKE`).
         */
        val baseTacticalNuclearStrikeCooldown: Int,
        /**
         * @return Возвращает уменьшение интервала между тактическими ядерными ударами за каждый захваченный центр
         * управления (`FacilityType.CONTROL_CENTER`).
         */
        val tacticalNuclearStrikeCooldownDecreasePerControlCenter: Int,
        /**
         * @return Возвращает урон тактического ядерного удара в центре взрыва.
         */
        val maxTacticalNuclearStrikeDamage: Double,
        /**
         * @return Возвращает радиус взрыва тактического ядерного удара.
         */
        val tacticalNuclearStrikeRadius: Double,
        /**
         * @return Возвращает задержку между запросом нанесения тактического ядерного удара и собственно самим нанесением.
         */
        val tacticalNuclearStrikeDelay: Int
)
