import model.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static model.VehicleType.*;

@SuppressWarnings({"UnsecureRandomNumberGeneration", "FieldCanBeLocal", "unused", "OverlyLongMethod"})
public final class OldMyStrategy {
    /**
     * Список целей для каждого типа техники, упорядоченных по убыванию урона по ним.
     */
    private static final Map<VehicleType, VehicleType[]> preferredTargetTypesByVehicleType;

    static {
        preferredTargetTypesByVehicleType = new EnumMap<>(VehicleType.class);

        preferredTargetTypesByVehicleType.put(FIGHTER, new VehicleType[]{
                HELICOPTER, FIGHTER
        });

        preferredTargetTypesByVehicleType.put(HELICOPTER, new VehicleType[]{
                VehicleType.TANK, VehicleType.ARRV, HELICOPTER, VehicleType.IFV, FIGHTER
        });

        preferredTargetTypesByVehicleType.put(VehicleType.IFV, new VehicleType[]{
                HELICOPTER, VehicleType.ARRV, VehicleType.IFV, FIGHTER, VehicleType.TANK
        });

        preferredTargetTypesByVehicleType.put(VehicleType.TANK, new VehicleType[]{
                VehicleType.IFV, VehicleType.ARRV, VehicleType.TANK, FIGHTER, HELICOPTER
        });
    }

    private double getVisionCoefByTerrainAndWeather(TerrainType terrainType, WeatherType weatherType) {
        double coef = 1;
        switch (terrainType) {
            case PLAIN:
                coef *= game.getPlainTerrainVisionFactor();
                break;
            case SWAMP:
                coef *= game.getSwampTerrainVisionFactor();
                break;
            case FOREST:
                coef *= game.getForestTerrainVisionFactor();
                break;
        }
        switch (weatherType) {
            case CLEAR:
                coef *= game.getClearWeatherVisionFactor();
                break;
            case CLOUD:
                coef *= game.getCloudWeatherVisionFactor();
                break;
            case RAIN:
                coef *= game.getRainWeatherVisionFactor();
                break;
        }
        return coef;
    }

    private Random random;

    private TerrainType[][] terrainTypeByCellXY;
    private WeatherType[][] weatherTypeByCellXY;

    private Player me;
    private World world;
    private Game game;
    private Move move;

    private final Map<Long, Vehicle> vehicleById = new HashMap<>();
    private Map<Long, Vehicle> previousVehicleById = new HashMap<>();
    private final Map<Long, Integer> updateTickByVehicleId = new HashMap<>();
    private final Deque<Consumer<Move>> delayedMoves = new ArrayDeque<>();
    private OldAction action;

    private final Map<VehicleType, State> vehicleStates = Arrays.stream(VehicleType.values())
            .collect(Collectors.toMap(Function.identity(), v -> State.START));

    private State landState = State.START;

    private int nuclearEvasionTicks = 0;
    private double nuclearX = 0;
    private double nuclearY = 0;

    private static final List<VehicleType> AIR_TYPES = Arrays.asList(HELICOPTER, FIGHTER);
    private static final List<VehicleType> LAND_TYPES = Arrays.asList(IFV, TANK, ARRV);

    /**
     * Основной метод стратегии, осуществляющий управление армией. Вызывается каждый тик.
     *
     * @param me    Информация о вашем игроке.
     * @param world Текущее состояние мира.
     * @param game  Различные игровые константы.
     * @param move  Результатом работы метода является изменение полей данного объекта.
     */
    public void move(Player me, World world, Game game, Move move) {
        initializeStrategy(world, game);
        initializeTick(me, world, game, move);
        if (landState != State.SCALED) {
            prepareLand();
            positionateArmy();
        }

        if (me.getRemainingActionCooldownTicks() > 0) {
            return;
        }
        if (avoidNuclearAttack()) {
            return;
        }

        if (nuclearAttack()) {
            return;
        }

        if (executeDelayedMove()) {
            return;
        }

        move();

        executeDelayedMove();
    }

    private boolean avoidNuclearAttack() {
        if (landState.ordinal() >= State.POSITION.ordinal()) {
            if (nuclearEvasionTicks != 0) {
                if (executeDelayedMove()) {
                    return true;
                }
                if (world.getOpponentPlayer().getNextNuclearStrikeTickIndex() == -1) {
                    action.clearAndSelect(null);
                    action.scale(nuclearX, nuclearY, 0.1);
                    moveState = MoveState.SCALE;
                }
                nuclearEvasionTicks--;
                return true;
            }
            if (world.getOpponentPlayer().getNextNuclearStrikeTickIndex() != -1) {
                nuclearEvasionTicks = 40;
                action.clearAndSelect(null);
                nuclearX = world.getOpponentPlayer().getNextNuclearStrikeX();
                nuclearY = world.getOpponentPlayer().getNextNuclearStrikeY();
                Vehicle vehicle = streamVehicles(Ownership.ALLY).reduce((v1, v2) ->
                        v1.getDistanceTo(nuclearX, nuclearY) < v2.getDistanceTo(nuclearX, nuclearY) ? v1 : v2
                ).get();
                nuclearX = vehicle.getX();
                nuclearY = vehicle.getY();
                action.scale(nuclearX, nuclearY, 10);
                return true;
            }
        }
        return false;
    }

    /**
     * Инциализируем стратегию.
     * <p>
     * Для этих целей обычно можно использовать конструктор, однако в данном случае мы хотим инициализировать генератор
     * случайных чисел значением, полученным от симулятора игры.
     */
    private void initializeStrategy(World world, Game game) {
        if (random == null) {
            random = new Random(game.getRandomSeed());

            terrainTypeByCellXY = world.getTerrainByCellXY();
            weatherTypeByCellXY = world.getWeatherByCellXY();
        }
    }

    /**
     * Сохраняем все входные данные в полях класса для упрощения доступа к ним, а также актуализируем сведения о каждой
     * технике и времени последнего изменения её состояния.
     */
    private void initializeTick(Player me, World world, Game game, Move move) {
        this.me = me;
        this.world = world;
        this.game = game;
        this.move = move;
        action = new OldAction(delayedMoves, world);
        previousVehicleById = new HashMap<>(vehicleById);

        for (Vehicle vehicle : world.getNewVehicles()) {
            vehicleById.put(vehicle.getId(), vehicle);
            updateTickByVehicleId.put(vehicle.getId(), world.getTickIndex());
        }

        for (VehicleUpdate vehicleUpdate : world.getVehicleUpdates()) {
            long vehicleId = vehicleUpdate.getId();

            if (vehicleUpdate.getDurability() == 0) {
                vehicleById.remove(vehicleId);
                updateTickByVehicleId.remove(vehicleId);
            } else {
                vehicleById.put(vehicleId, new Vehicle(vehicleById.get(vehicleId), vehicleUpdate));
                updateTickByVehicleId.put(vehicleId, world.getTickIndex());
            }
        }
    }

    double fx;
    double fy;
    double hx;
    double hy;


    private void prepareLand() {
        if (landState == State.SCALED) {
            return;
        }
        boolean makeAction = false;
        try {
            if (delayedMoves.isEmpty() && streamVehicleUpdates(Ownership.ALLY, IFV).count() == 0
                    && streamVehicleUpdates(Ownership.ALLY, TANK).count() == 0
                    && streamVehicleUpdates(Ownership.ALLY, ARRV).count() == 0) {
                landState = State.values()[landState.ordinal() + 1];
                makeAction = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!makeAction) {
            return;
        }

        List<VehicleType> vehicles = new ArrayList<>();
        vehicles.add(IFV);
        vehicles.add(TANK);
        vehicles.add(ARRV);

        Map<VehicleType, Point> centers = new HashMap<>();
        centers.put(IFV, new Point(getAlliedCenterX(IFV), getAlliedCenterY(IFV)));
        centers.put(TANK, new Point(getAlliedCenterX(TANK), getAlliedCenterY(TANK)));
        centers.put(ARRV, new Point(getAlliedCenterX(ARRV), getAlliedCenterY(ARRV)));
        if (landState == State.SCALED_UP) {
            double distance = Integer.MAX_VALUE;
            VehicleType angleVehicle = null;
            for (VehicleType vehicleType : centers.keySet()) {
                double d = getDistance(centers, vehicleType, new Point(45, 45));
                if (d < distance) {
                    distance = d;
                    angleVehicle = vehicleType;
                }
            }

            action.clearAndSelect(angleVehicle);
//            move(45 - centers.get(angleVehicle).x, 45 - centers.get(angleVehicle).y);
            action.scale(2 * centers.get(angleVehicle).x - 70, 2 * centers.get(angleVehicle).y - 70, 2);

            centers.remove(angleVehicle);
            vehicles.remove(angleVehicle);
            double d1 = getDistance(centers, vehicles.get(0), new Point(45, 119));
            double d2 = getDistance(centers, vehicles.get(0), new Point(119, 45));
            double d3 = getDistance(centers, vehicles.get(1), new Point(45, 119));
            double d4 = getDistance(centers, vehicles.get(1), new Point(119, 45));
            if (d1 + d4 < d2 + d3) {
                move.setAction(ActionType.ASSIGN);
                action.clearAndSelect(vehicles.get(0));
                action.scale(2 * centers.get(vehicles.get(0)).x - 74.1, 2 * centers.get(vehicles.get(0)).y - 210, 2);
                action.clearAndSelect(vehicles.get(1));
                action.scale(2 * centers.get(vehicles.get(1)).x - 210, 2 * centers.get(vehicles.get(1)).y - 74.1, 2);
            } else {
                action.clearAndSelect(vehicles.get(0));
                action.scale(2 * centers.get(vehicles.get(0)).x - 210, 2 * centers.get(vehicles.get(0)).y - 74.1, 2);
                action.clearAndSelect(vehicles.get(1));
                action.scale(2 * centers.get(vehicles.get(1)).x - 74.1, 2 * centers.get(vehicles.get(1)).y - 210, 2);
            }
        } else if (landState == State.SHIFT_X) {
            double maxX = 0;
            VehicleType rightVehicle = null;
            for (VehicleType vehicleType : centers.keySet()) {
                double x = centers.get(vehicleType).x;
                if (x > maxX) {
                    maxX = x;
                    rightVehicle = vehicleType;
                }
            }
            action.clearAndSelect(rightVehicle);
            action.move(-140, 0);
        } else if (landState == State.SHIFT_Y) {
            double maxY = 0;
            VehicleType downVehicle = null;
            for (VehicleType vehicleType : centers.keySet()) {
                double y = centers.get(vehicleType).y;
                if (y > maxY) {
                    maxY = y;
                    downVehicle = vehicleType;
                }
            }
            action.clearAndSelect(downVehicle);
            action.move(0, -132);
        } else if (landState == State.SMALL_SCALE) {
            action.clearAndSelect(IFV);
            action.addToSelection(TANK);
            action.addToSelection(ARRV);
            action.scale(centers.get(TANK).x, centers.get(TANK).y, 1.1);
        } else if (landState == State.SMALL_SHIFT) {
            double maxX = 0;
            VehicleType rightVehicle = null;
            for (VehicleType vehicleType : centers.keySet()) {
                double x = centers.get(vehicleType).x;
                if (x > maxX) {
                    maxX = x;
                    rightVehicle = vehicleType;
                }
            }
            action.clearAndSelect(rightVehicle);
            action.move(-4.1, 0);
        } else if (landState == State.SHIFT_X_WALL) {
            action.clearAndSelect(IFV);
            action.addToSelection(TANK);
            action.addToSelection(ARRV);
            action.move(-200, 0);
        } else if (landState == State.SHIFT_Y_WALL) {
            action.clearAndSelect(IFV);
            action.addToSelection(TANK);
            action.addToSelection(ARRV);
            action.move(0, -200);
        } else if (landState == State.POSITION) {
            action.clearAndSelect(null);
            action.move(90, 70, 0.3);
        } else if (landState == State.ROTATE_SCALE_UP) {
            action.clearAndSelect(null);
            double x = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getX).average().orElse(Double.NaN);
            double y = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getY).average().orElse(Double.NaN);
            action.scale(x, y, 1.2);
        } else if (landState == State.ROTATE) {
            action.clearAndSelect(null);
            double x = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getX).average().orElse(Double.NaN);
            double y = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getY).average().orElse(Double.NaN);
            action.rotate(x, y, Math.PI / 4);
        } else if (landState == State.ROTATE_SCALE_DOWN) {
            action.clearAndSelect(null);
            double x = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getX).average().orElse(Double.NaN);
            double y = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getY).average().orElse(Double.NaN);
            action.scale(x, y, 0.1);
        }
    }

    private double getDistance(Map<VehicleType, Point> centers, VehicleType vehicleType, Point point) {
        double x = centers.get(vehicleType).x;
        double y = centers.get(vehicleType).y;
        return Math.sqrt(Math.pow(x - point.x, 2) + Math.pow(y - point.y, 2));
    }

    private void positionateArmy() {
        if (vehicleStates.get(FIGHTER) == State.SCALED && vehicleStates.get(HELICOPTER) == State.SCALED) {
            return;
        }
        boolean makeAction = false;
        if (delayedMoves.isEmpty() && streamVehicleUpdates(Ownership.ALLY, FIGHTER).count() == 0
                && streamVehicleUpdates(Ownership.ALLY, HELICOPTER).count() == 0
                && world.getTickIndex() > 100) {
            vehicleStates.put(FIGHTER, State.values()[vehicleStates.get(FIGHTER).ordinal() + 1]);
            vehicleStates.put(HELICOPTER, State.values()[vehicleStates.get(HELICOPTER).ordinal() + 1]);
            makeAction = true;
        }
        if (world.getTickIndex() == 0 || world.getTickIndex() == 100) makeAction = true;

        if (!makeAction) {
            return;
        }

        if (world.getTickIndex() == 0) {
            fx = getAlliedCenterX(FIGHTER);
            fy = getAlliedCenterY(FIGHTER);
            hx = getAlliedCenterX(HELICOPTER);
            hy = getAlliedCenterY(HELICOPTER);
            if (hx > fx) {
                action.clearAndSelect(HELICOPTER);
                action.scale(2 * hx - 240, 2 * hy - 240, 2);
            } else if (hx < fx) {
                action.clearAndSelect(FIGHTER);
                action.scale(2 * fx - 240, 2 * fy - 240, 2);
            } else {
                if (hy > fy) {
                    action.clearAndSelect(HELICOPTER);
                    action.scale(2 * hx - 240, 2 * hy - 240, 2);
                } else {
                    action.clearAndSelect(FIGHTER);
                    action.scale(2 * fx - 240, 2 * fy - 240, 2);
                }
            }
        }
        if (world.getTickIndex() == 100) {
            if (hx - fx > 20) {
                action.clearAndSelect(FIGHTER);
                action.scale(2 * fx - 100, 2 * fy - 240 + 6, 2);
            } else if (fx - hx > 20) {
                action.clearAndSelect(HELICOPTER);
                action.scale(2 * hx - 100, 2 * hy - 240 + 6, 2);
            } else {
                if (hy > fy) {
                    action.clearAndSelect(FIGHTER);
                    action.scale(2 * fx - 240 + 6, 2 * fy - 100, 2);
                } else {
                    action.clearAndSelect(HELICOPTER);
                    action.scale(2 * hx - 240 + 6, 2 * hy - 100, 2);
                }
            }
        }

        if (vehicleStates.get(FIGHTER) == State.SCALED_UP && vehicleStates.get(HELICOPTER) == State.SCALED_UP) {
            fx = getAlliedCenterX(FIGHTER);
            fy = getAlliedCenterY(FIGHTER);
            hx = getAlliedCenterX(HELICOPTER);
            hy = getAlliedCenterY(HELICOPTER);
            if (hx - fx > 20 || fx - hx > 20) {
                action.clearAndSelect(FIGHTER);
                action.move(-500, 0);
                action.clearAndSelect(HELICOPTER);
                action.move(-500, 0);
            } else {
                if (hy > fy) {
                    action.clearAndSelect(FIGHTER);
                    action.move(0, 240 - 6 - fy);
                } else {
                    action.clearAndSelect(HELICOPTER);
                    action.move(0, 240 - 6 - hy);
                }
            }
        } else if (vehicleStates.get(FIGHTER) == State.SHIFT_X && vehicleStates.get(HELICOPTER) == State.SHIFT_X) {
            action.clearAndSelect(FIGHTER);
            action.move(-500, 0);
            action.clearAndSelect(HELICOPTER);
            action.move(-500, 0);
        } else if (vehicleStates.get(FIGHTER) == State.SHIFT_Y && vehicleStates.get(HELICOPTER) == State.SHIFT_Y) {
            action.clearAndSelect(FIGHTER);
            action.move(0, -500);
            action.clearAndSelect(HELICOPTER);
            action.move(0, -500);
        } else if (vehicleStates.get(FIGHTER) == State.SMALL_SCALE && vehicleStates.get(HELICOPTER) == State.SMALL_SCALE) {
            action.clearAndSelect(FIGHTER);
            action.move(0, 15);
            action.clearAndSelect(HELICOPTER);
            action.move(0, 15);
        }

    }

    /**
     * Достаём отложенное действие из очереди и выполняем его.
     *
     * @return Возвращает {@code true}, если и только если отложенное действие было найдено и выполнено.
     */
    private boolean executeDelayedMove() {
        Consumer<Move> delayedMove = delayedMoves.poll();
        if (delayedMove == null) {
            return false;
        }

        delayedMove.accept(move);
        return true;
    }


    private static class VehicleGroup {
        List<Vehicle> vehicles = new ArrayList<>();
        double x;
        double y;

        public VehicleGroup(Vehicle vehicle) {
            addVehicle(vehicle);
        }

        void addVehicle(Vehicle vehicle) {
            x = (x * vehicles.size() + vehicle.getX()) / (vehicles.size() + 1);
            y = (y * vehicles.size() + vehicle.getY()) / (vehicles.size() + 1);
            vehicles.add(vehicle);
        }
    }

    private boolean nuclearAttack() {
        if (me.getRemainingNuclearStrikeCooldownTicks() != 0) {
            return false;
        }
        List<VehicleGroup> nearEnemies = getEnemyVehicleGroups(300);
        for (VehicleGroup enemyGroup : nearEnemies) {
            for (Vehicle vehicle : vehicleById.values()) {
                if (vehicle.getPlayerId() != me.getId()) {
                    continue;
                }
                double coef = getVisionCoefByTerrainAndWeather(
                        terrainTypeByCellXY[(int) vehicle.getX() / 32][(int) vehicle.getY() / 32],
                        weatherTypeByCellXY[(int) vehicle.getX() / 32][(int) vehicle.getY() / 32]);

                if (vehicle.getDistanceTo(enemyGroup.x, enemyGroup.y) < vehicle.getVisionRange() * coef) {
                    int allyGroupSize = 0;
                    for (Vehicle veh : vehicleById.values()) {
                        if (veh.getPlayerId() == me.getId() && veh.getDistanceTo(enemyGroup.x, enemyGroup.y) < 40) {
                            allyGroupSize++;
                            if (allyGroupSize > enemyGroup.vehicles.size()) {
                                break;
                            }
                        }
                    }
                    if (allyGroupSize < enemyGroup.vehicles.size()) {
                        move.setAction(ActionType.TACTICAL_NUCLEAR_STRIKE);
                        move.setX(enemyGroup.x);
                        move.setY(enemyGroup.y);
                        move.setVehicleId(vehicle.getId());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<VehicleGroup> getEnemyVehicleGroups(int visionRadius) {
        List<VehicleGroup> nearEnemies = new ArrayList<>();
        double centerX = getAlliedCenterX(null);
        double centerY = getAlliedCenterY(null);
        for (Vehicle vehicle : vehicleById.values()) {
            if (vehicle.getPlayerId() != me.getId() && vehicle.getDistanceTo(centerX, centerY) < visionRadius) {
                boolean addedToGroup = false;
                for (VehicleGroup group : nearEnemies) {
                    if (group.x - vehicle.getX() < 40) {
                        group.addVehicle(vehicle);
                        addedToGroup = true;
                        break;
                    }
                }
                if (!addedToGroup) {
                    nearEnemies.add(new VehicleGroup(vehicle));
                }
            }
        }

        nearEnemies.sort(((o1, o2) -> {
            double percentHP1 = o1.vehicles.stream().mapToDouble(v -> v.getDurability() / v.getMaxDurability()).sum();
            double percentHP2 = o2.vehicles.stream().mapToDouble(v -> v.getDurability() / v.getMaxDurability()).sum();
            if (percentHP1 < 0.75 && o1.vehicles.size() > 5 || percentHP2 < 0.75 && o2.vehicles.size() > 5) {
                return (int) Math.signum(percentHP1 - percentHP2);
            }
            return o2.vehicles.size() - o1.vehicles.size();
        }));
        return nearEnemies;
    }


    private boolean compressed = true;
    private int allyDamagedTime = 0;
    private MoveState moveState = MoveState.MOVE;

    /**
     * Основная логика нашей стратегии.
     */
    private void move() {
        if (landState != State.SCALED) {
            return;
        }
        boolean allyDamaged = streamVehicles(Ownership.ALLY)
                .map(Vehicle::getId)
                .anyMatch(id -> vehicleById.get(id).getDurability() < previousVehicleById.get(id).getDurability());
        if (allyDamaged) {
            allyDamagedTime = world.getTickIndex();
        }

        if (compressed) {
            if (world.getTickIndex() > allyDamagedTime + 100) {
                action.clearAndSelect(null);
                double x = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getX).average().orElse(Double.NaN);
                double y = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getY).average().orElse(Double.NaN);
                action.scale(x, y, 1.5);
                moveState = MoveState.UNCOMPRESS;
                compressed = false;
            }
        } else {
            if (world.getTickIndex() == allyDamagedTime) {
                action.clearAndSelect(null);
                double x = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getX).average().orElse(Double.NaN);
                double y = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getY).average().orElse(Double.NaN);
                action.scale(x, y, 0.65);
                moveState = MoveState.COMPRESS;
                compressed = true;
            }
        }

        if (delayedMoves.isEmpty() && streamVehicleUpdates(Ownership.ALLY, null).count() == 0) {
            moveState = MoveState.MOVE;
        }


        if (moveState == MoveState.MOVE) {
            List<VehicleGroup> enemyVehicleGroups = getEnemyVehicleGroups(600);

            action.clearAndSelect(null);
            double x = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getX).average().orElse(Double.NaN);
            double y = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getY).average().orElse(Double.NaN);
            double targetX = streamVehicles(Ownership.ENEMY).mapToDouble(Vehicle::getX).average().orElse(Double.NaN);
            double targetY = streamVehicles(Ownership.ENEMY).mapToDouble(Vehicle::getY).average().orElse(Double.NaN);

            if (!enemyVehicleGroups.isEmpty()) {
                targetX = enemyVehicleGroups.get(0).x;
                targetY = enemyVehicleGroups.get(0).y;
            }
            action.move(targetX - x, targetY - y, 0.2);

        }
        if (compressed && world.getTickIndex() % 800 == 0) {
            action.clearAndSelect(null);
            double x = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getX).average().orElse(Double.NaN);
            double y = streamVehicles(Ownership.ALLY).mapToDouble(Vehicle::getY).average().orElse(Double.NaN);
            action.scale(x, y, 0.1);
        }
    }

    private double getAlliedCenterX(VehicleType vehicleType) {
        return streamVehicles(Ownership.ALLY, vehicleType)
                .mapToDouble(Vehicle::getX)
                .average().orElse(Double.NaN);
    }

    private double getAlliedCenterY(VehicleType vehicleType) {
        return streamVehicles(Ownership.ALLY, vehicleType)
                .mapToDouble(Vehicle::getY)
                .average().orElse(Double.NaN);
    }

    private Stream<Vehicle> streamVehicleUpdates(Ownership ownership, VehicleType vehicleType) {
        Stream<Vehicle> stream = Arrays.stream(world.getVehicleUpdates())
                .filter(vehicleUpdate -> vehicleUpdate.getDurability() != 0)
                .map(VehicleUpdate::getId)
                .filter(id -> vehicleById.get(id).getX() != previousVehicleById.get(id).getX()
                        || vehicleById.get(id).getY() != previousVehicleById.get(id).getY())
                .map(vehicleById::get);
        return filterOwnershipAndVehicleType(ownership, vehicleType == null ? null : Collections.singletonList(vehicleType), stream);
    }


    private Stream<Vehicle> streamVehiclesTypes(Ownership ownership, List<VehicleType> types) {
        Stream<Vehicle> stream = vehicleById.values().stream();
        return filterOwnershipAndVehicleType(ownership, types, stream);
    }

    private Stream<Vehicle> streamVehicles(Ownership ownership, VehicleType vehicleType) {
        Stream<Vehicle> stream = vehicleById.values().stream();
        return filterOwnershipAndVehicleType(ownership, vehicleType == null ? null : Collections.singletonList(vehicleType), stream);
    }

    private Stream<Vehicle> filterOwnershipAndVehicleType(Ownership ownership, List<VehicleType> types, Stream<Vehicle> stream) {
        switch (ownership) {
            case ALLY:
                stream = stream.filter(vehicle -> vehicle.getPlayerId() == me.getId());
                break;
            case ENEMY:
                stream = stream.filter(vehicle -> vehicle.getPlayerId() != me.getId());
                break;
            default:
        }

        if (types != null) {
            stream = stream.filter(vehicle -> types.contains(vehicle.getType()));
        }
        return stream;
    }

    private Stream<Vehicle> streamVehicles(Ownership ownership) {
        return streamVehicles(ownership, null);
    }

    private Stream<Vehicle> streamVehicles() {
        return streamVehicles(Ownership.ANY);
    }

    private enum Ownership {
        ANY, ALLY, ENEMY
    }

    private enum State {
        START, SCALED_UP, SHIFT_X, SHIFT_Y, SMALL_SCALE, SMALL_SHIFT, SHIFT_X_WALL, SHIFT_Y_WALL, POSITION, ROTATE_SCALE_UP, ROTATE, ROTATE_SCALE_DOWN, SCALED
    }

    private enum AirState {
        START, SCALED_UP, SHIFT_X, SHIFT_Y, SMALL_SCALE, SMALL_SHIFT, SCALE_DOWN, ROTATE, SCALED
    }

    private enum MoveState {
        MOVE, SCALE, COMPRESS, UNCOMPRESS
    }

    private static class Point {
        private double x;
        private double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}