package model

/**
 * Базовый класс для определения круглых объектов. Содержит также все свойства юнита.
 */
abstract class CircularUnit protected constructor(
        id: Long,
        x: Double,
        y: Double,
        /**
         * @return Возвращает радиус объекта.
         */
        val radius: Double
) : Unit(id, x, y)
