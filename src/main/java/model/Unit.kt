package model

import kotlin.math.hypot

/**
 * Базовый класс для определения объектов (<<юнитов>>) на игровом поле.
 */
abstract class Unit protected constructor(
        /**
         * @return Возвращает уникальный идентификатор объекта.
         */
        val id: Long,
        /**
         * @return Возвращает X-координату центра объекта. Ось абсцисс направлена слева направо.
         */
        val x: Double,
        /**
         * @return Возвращает Y-координату центра объекта. Ось ординат направлена сверху вниз.
         */
        val y: Double
) {
    /**
     * @param x X-координата точки.
     * @param y Y-координата точки.
     * @return Возвращает расстояние до точки от центра данного объекта.
     */
    fun getDistanceTo(x: Double, y: Double): Double = hypot(x - this.x, y - this.y)

    /**
     * @param unit Объект, до центра которого необходимо определить расстояние.
     * @return Возвращает расстояние от центра данного объекта до центра указанного объекта.
     */
    fun getDistanceTo(unit: Unit): Double = getDistanceTo(unit.x, unit.y)

    /**
     * @param x X-координата точки.
     * @param y Y-координата точки.
     * @return Возвращает квадрат расстояния до точки от центра данного объекта.
     */
    fun getSquaredDistanceTo(x: Double, y: Double): Double {
        val dx = x - this.x
        val dy = y - this.y
        return dx * dx + dy * dy
    }

    /**
     * @param unit Объект, до центра которого необходимо определить квадрат расстояния.
     * @return Возвращает квадрат расстояния от центра данного объекта до центра указанного объекта.
     */
    fun getSquaredDistanceTo(unit: Unit): Double = getSquaredDistanceTo(unit.x, unit.y)
}
