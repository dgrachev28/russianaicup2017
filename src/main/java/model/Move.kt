package model

/**
 * Стратегия игрока может управлять юнитами посредством установки свойств объекта данного класса.
 */
@Suppress("RedundantSetter")
class Move {
    /**
     * @return Возвращает текущее действие игрока.
     */
    var action: ActionType? = null
        /**
         * Устанавливает действие игрока.
         */
        set

    /**
     * @return Возвращает текущую группу юнитов.
     */
    var group: Int = 0
        /**
         * Устанавливает группу юнитов для различных действий.
         *
         * Является опциональным параметром для действий `ActionType.CLEAR_AND_SELECT`,
         * `ActionType.ADD_TO_SELECTION` и `ActionType.DESELECT`. Если для этих действий группа юнитов
         * установлена, то параметр `vehicleType`, а также параметры прямоугольной рамки `left`, `top`,
         * `right` и `bottom` будут проигнорированы.
         *
         * Является обязательным параметром для действий `ActionType.ASSIGN`, `ActionType.DISMISS` и
         * `ActionType.DISBAND`. Для действия `ActionType.DISBAND` является единственным учитываемым параметром.
         *
         * Корректными значениями являются целые числа от `1` до `game.maxUnitGroup` включительно.
         */
        set

    /**
     * @return Возвращает текущую левую границу прямоугольной рамки, предназначенной для выделения юнитов.
     */
    var left: Double = 0.0
        /**
         * @return Устанавливает левую границу прямоугольной рамки для выделения юнитов.
         *
         * Является обязательным параметром для действий `ActionType.CLEAR_AND_SELECT`,
         * `ActionType.ADD_TO_SELECTION` и `ActionType.DESELECT`, если не установлена группа юнитов.
         * В противном случае граница будет проигнорирована.
         *
         * Корректными значениями являются вещественные числа от `0.0` до `right` включительно.
         */
        set

    /**
     * @return Возвращает текущую верхнюю границу прямоугольной рамки, предназначенной для выделения юнитов.
     */
    var top: Double = 0.0
        /**
         * @return Устанавливает верхнюю границу прямоугольной рамки для выделения юнитов.
         *
         * Является обязательным параметром для действий `ActionType.CLEAR_AND_SELECT`,
         * `ActionType.ADD_TO_SELECTION` и `ActionType.DESELECT`, если не установлена группа юнитов.
         * В противном случае граница будет проигнорирована.
         *
         * Корректными значениями являются вещественные числа от `0.0` до `bottom` включительно.
         */
        set

    /**
     * @return Возвращает текущую правую границу прямоугольной рамки, предназначенной для выделения юнитов.
     */
    var right: Double = 0.0
        /**
         * @return Устанавливает правую границу прямоугольной рамки для выделения юнитов.
         *
         * Является обязательным параметром для действий `ActionType.CLEAR_AND_SELECT`,
         * `ActionType.ADD_TO_SELECTION` и `ActionType.DESELECT`, если не установлена группа юнитов.
         * В противном случае граница будет проигнорирована.
         *
         * Корректными значениями являются вещественные числа от `left` до `game.worldWidth` включительно.
         */
        set

    /**
     * @return Возвращает текущую нижнюю границу прямоугольной рамки, предназначенной для выделения юнитов.
     */
    var bottom: Double = 0.0
        /**
         * @return Устанавливает нижнюю границу прямоугольной рамки для выделения юнитов.
         *
         * Является обязательным параметром для действий `ActionType.CLEAR_AND_SELECT`,
         * `ActionType.ADD_TO_SELECTION` и `ActionType.DESELECT`, если не установлена группа юнитов.
         * В противном случае граница будет проигнорирована.
         *
         * Корректными значениями являются вещественные числа от `top` до `game.worldHeight` включительно.
         */
        set

    /**
     * @return Возвращает текущую абсциссу точки или вектора.
     */
    var x: Double = 0.0
        /**
         * Устанавливает абсциссу точки или вектора.
         *
         * Является обязательным параметром для действия `ActionType.MOVE` и задаёт целевую величину смещения юнитов
         * вдоль оси абсцисс.
         *
         * Является обязательным параметром для действия `ActionType.ROTATE` и задаёт абсциссу точки, относительно
         * которой необходимо совершить поворот.
         *
         * Является обязательным параметром для действия `ActionType.SCALE` и задаёт абсциссу точки, относительно
         * которой необходимо совершить масштабирование.
         *
         * Является обязательным параметром для действия `ActionType.TACTICAL_NUCLEAR_STRIKE` и задаёт абсциссу цели
         * тактического ядерного удара.
         *
         * Корректными значениями для действия `ActionType.MOVE` являются вещественные числа от
         * `-game.worldWidth` до `game.worldWidth` включительно. Корректными значениями для действий
         * `ActionType.ROTATE` и `ActionType.SCALE` являются вещественные числа от `-game.worldWidth` до
         * `2.0 * game.worldWidth` включительно. Корректными значениями для действия
         * `ActionType.TACTICAL_NUCLEAR_STRIKE` являются вещественные числа от `0.0` до `game.worldWidth`
         * включительно.
         */
        set

    /**
     * @return Возвращает текущую ординату точки или вектора.
     */
    var y: Double = 0.0
        /**
         * Устанавливает ординату точки или вектора.
         *
         * Является обязательным параметром для действия `ActionType.MOVE` и задаёт целевую величину смещения юнитов
         * вдоль оси ординат.
         *
         * Является обязательным параметром для действия `ActionType.ROTATE` и задаёт ординату точки, относительно
         * которой необходимо совершить поворот.
         *
         * Является обязательным параметром для действия `ActionType.SCALE` и задаёт ординату точки, относительно
         * которой необходимо совершить масштабирование.
         *
         * Является обязательным параметром для действия `ActionType.TACTICAL_NUCLEAR_STRIKE` и задаёт ординату цели
         * тактического ядерного удара.
         *
         * Корректными значениями для действия `ActionType.MOVE` являются вещественные числа от
         * `-game.worldHeight` до `game.worldHeight` включительно. Корректными значениями для действий
         * `ActionType.ROTATE` и `ActionType.SCALE` являются вещественные числа от `-game.worldHeight` до
         * `2.0 * game.worldHeight` включительно. Корректными значениями для действия
         * `ActionType.TACTICAL_NUCLEAR_STRIKE` являются вещественные числа от `0.0` до `game.worldHeight`
         * включительно.
         */
        set

    /**
     * @return Возвращает текущий угол поворота.
     */
    var angle: Double = 0.0
        /**
         * Задаёт угол поворота.
         *
         * Является обязательным параметром для действия `ActionType.ROTATE` и задаёт угол поворота относительно точки
         * (`x`, `y`). Положительные значения соответствуют повороту по часовой стрелке.
         *
         * Корректными значениями являются вещественные числа от `-PI` до `PI` включительно.
         */
        set

    /**
     * @return Возвращает текущий коэффициент масштабирования.
     */
    var factor: Double = 0.0
        /**
         * Задаёт коэффициент масштабирования.
         *
         * Является обязательным параметром для действия `ActionType.SCALE` и задаёт коэффициент масштабирования
         * формации юнитов относительно точки (`x`, `y`). При значениях коэффициента больше 1.0 происходит
         * расширение формации, при значениях меньше 1.0 --- сжатие.
         *
         * Корректными значениями являются вещественные числа от `0.1` до `10.0` включительно.
         */
        set

    /**
     * @return Возвращает текущее ограничение линейной скорости.
     */
    var maxSpeed: Double = 0.0
        /**
         * Устанавливает абсолютное ограничение линейной скорости.
         *
         * Является опциональным параметром для действий `ActionType.MOVE`, `ActionType.ROTATE` и
         * `ActionType.SCALE`. Если для действия `ActionType.ROTATE` установлено ограничение скорости поворота,
         * то этот параметр будет проигнорирован.
         *
         * Корректными значениями являются вещественные неотрицательные числа. При этом, `0.0` означает, что
         * ограничение отсутствует.
         */
        set

    /**
     * @return Возвращает текущее абсолютное ограничение скорости поворота.
     */
    var maxAngularSpeed: Double = 0.0
        /**
         * Устанавливает абсолютное ограничение скорости поворота в радианах за тик.
         *
         * Является опциональным параметром для действия `ActionType.ROTATE`. Если для этого действия установлено
         * ограничение скорости поворота, то параметр `maxSpeed` будет проигнорирован.
         *
         * Корректными значениями являются вещественные числа в интервале от `0.0` до `PI` включительно. При
         * этом, `0.0` означает, что ограничение отсутствует.
         */
        set

    /**
     * @return Возвращает текущий тип техники.
     */
    var vehicleType: VehicleType? = null
        /**
         * Устанавливает тип техники.
         *
         * Является опциональным параметром для действий `ActionType.CLEAR_AND_SELECT`,
         * `ActionType.ADD_TO_SELECTION` и `ActionType.DESELECT`.
         * Указанные действия будут применены только к технике выбранного типа.
         * Параметр будет проигнорирован, если установлена группа юнитов.
         *
         * Является опциональным параметром для действия `ActionType.SETUP_VEHICLE_PRODUCTION`.
         * Завод будет настроен на производство техники данного типа. При этом, прогресс производства будет обнулён.
         * Если данный параметр не установлен, то производство техники на заводе будет остановлено.
         */
        set

    /**
     * @return Возвращает текущий идентификатор сооружения.
     */
    var facilityId = -1L
        /**
         * Устанавливает идентификатор сооружения.
         *
         * Является обязательным параметром для действия `ActionType.SETUP_VEHICLE_PRODUCTION`.
         * Если сооружение с данным идентификатором отсутствует в игре, не является заводом по производству техники
         * (`FacilityType.VEHICLE_FACTORY`) или принадлежит другому игроку, то действие будет проигнорировано.
         */
        set

    /**
     * @return Возвращает текущий идентификатор техники.
     */
    var vehicleId = -1L
        /**
         * Устанавливает идентификатор техники.
         *
         * Является обязательным параметром для действия `ActionType.TACTICAL_NUCLEAR_STRIKE`. Если юнит с данным
         * идентификатором отсутствует в игре, принадлежит другому игроку или цель удара находится вне зоны видимости этого
         * юнита, то действие будет проигнорировано.
         */
        set
}
