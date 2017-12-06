import model.ActionType;
import model.Move;
import model.VehicleType;
import model.World;

import java.util.Deque;
import java.util.function.Consumer;

public class OldAction {
    private final Deque<Consumer<Move>> delayedMoves;
    private final World world;

    public OldAction(Deque<Consumer<Move>> delayedMoves, World world) {
        this.delayedMoves = delayedMoves;
        this.world = world;
    }


    public void rotate(double x, double y, double angle) {
        delayedMoves.add(move -> {
            move.setAction(ActionType.ROTATE);
            move.setX(x);
            move.setY(y);
            move.setAngle(angle);
        });
    }

    public void move(double x, double y, double maxSpeed) {
        delayedMoves.add(move -> {
            move.setAction(ActionType.MOVE);
            move.setX(x);
            move.setY(y);
            move.setMaxSpeed(maxSpeed);
        });
    }

    public void move(double x, double y) {
        move(x, y, 0.0);
    }

    public void scale(double x, double y, double factor) {
        delayedMoves.add(move -> {
            move.setAction(ActionType.SCALE);
            move.setX(x);
            move.setY(y);
            move.setFactor(factor);
        });
    }

    public void clearAndSelect(VehicleType vehicleType) {
        delayedMoves.add(move -> {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());
            if (vehicleType != null) {
                move.setVehicleType(vehicleType);
            }
        });
    }


    public void addToSelection(VehicleType vehicleType) {
        delayedMoves.add(move -> {
            move.setAction(ActionType.ADD_TO_SELECTION);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());
            move.setVehicleType(vehicleType);
        });
    }

}
