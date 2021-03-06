package model

/**
 * Содержит данные о текущем состоянии игрока.
 */
class Player(
        /**
         * @return Возвращает уникальный идентификатор игрока.
         */
        val id: Long,
        /**
         * @return Возвращает `true` в том и только в том случае, если этот игрок ваш.
         */
        val isMe: Boolean,
        /**
         * @return Возвращает специальный флаг --- показатель того, что стратегия игрока <<упала>>.
         * Более подробную информацию можно найти в документации к игре.
         */
        val isStrategyCrashed: Boolean,
        /**
         * @return Возвращает количество баллов, набранное игроком.
         */
        val score: Int,
        /**
         * @return Возвращает количество тиков, оставшееся до любого следующего действия.
         * Если значение равно `0`, игрок может совершить действие в данный тик.
         */
        val remainingActionCooldownTicks: Int,
        /**
         * @return Возвращает количество тиков, оставшееся до следующего тактического ядерного удара.
         * Если значение равно `0`, игрок может запросить удар в данный тик.
         */
        val remainingNuclearStrikeCooldownTicks: Int,
        /**
         * @return Возвращает идентификатор техники, осуществляющей наведение ядерного удара на цель или `-1`.
         */
        val nextNuclearStrikeVehicleId: Long,
        /**
         * @return Возвращает тик нанесения следующего ядерного удара или `-1`.
         */
        val nextNuclearStrikeTickIndex: Int,
        /**
         * @return Возвращает абсциссу цели следующего ядерного удара или `-1.0`.
         */
        val nextNuclearStrikeX: Double,
        /**
         * @return Возвращает ординату цели следующего ядерного удара или `-1.0`.
         */
        val nextNuclearStrikeY: Double
)
