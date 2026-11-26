package hu.nye.progtech.rushhour.service;

import hu.nye.progtech.rushhour.model.Board;
import hu.nye.progtech.rushhour.model.Direction;
import hu.nye.progtech.rushhour.model.Orientation;
import hu.nye.progtech.rushhour.model.Position;
import hu.nye.progtech.rushhour.model.Vehicle;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Rush Hour szabalyai szerinti alapertelmezett lepesellenorzo implementacio.
 */
public final class StandardMoveValidator implements MoveValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardMoveValidator.class);

    @Override
    public boolean validateAndCheckExit(final Board board, final Move move)
            throws InvalidMoveException {
        Objects.requireNonNull(board, "A tabla nem lehet null.");
        Objects.requireNonNull(move, "A lepes nem lehet null.");

        if (move.getSteps() <= 0) {
            throw new InvalidMoveException("A lepesszamnak legalabb 1-nek kell lennie.");
        }

        Vehicle vehicle = board.getVehicle(move.getVehicleId())
                .orElseThrow(() -> new InvalidMoveException(
                        "Nem talalhato '" + move.getVehicleId() + "' azonositoju jarmu a tablan."));

        Direction direction = move.getDirection();
        if (!vehicle.getOrientation().allows(direction)) {
            throw new InvalidMoveException(
                    "A(z) " + vehicle.getId() + " jarmu csak "
                            + (vehicle.getOrientation() == Orientation.HORIZONTAL
                            ? "vizszintesen (balra/jobbra)"
                            : "fuggolegesen (fel/le)")
                            + " mozoghat.");
        }

        boolean movingTowardExit = isMovingTowardExit(vehicle, direction);

        for (int step = 1; step <= move.getSteps(); step++) {
            Position leadingCell = computeLeadingCell(vehicle, direction, step);

            if (!board.isInside(leadingCell)) {
                if (movingTowardExit) {
                    LOGGER.debug("A(z) {} celjarmu kilepett a tablarol.", vehicle.getId());
                    return true;
                }
                throw new InvalidMoveException(
                        "A(z) " + vehicle.getId() + " jarmu nem lephet ki a tablarol: "
                                + leadingCell);
            }

            if (board.isOccupied(leadingCell)) {
                char blockerId = board.getVehicleAt(leadingCell)
                        .map(Vehicle::getId)
                        .orElse('?');
                throw new InvalidMoveException(
                        "Utkozes: a(z) " + blockerId + " jarmu elallja az utat a(z) "
                                + leadingCell + " mezon.");
            }

            if (movingTowardExit && reachesExitAtStep(board, vehicle, step)) {
                LOGGER.debug("A(z) {} celjarmu elerte a kijaratot.", vehicle.getId());
                return true;
            }
        }

        return false;
    }

    private boolean isMovingTowardExit(final Vehicle vehicle, final Direction direction) {
        if (!vehicle.isTarget()) {
            return false;
        }
        if (vehicle.getOrientation() == Orientation.HORIZONTAL) {
            return direction == Direction.RIGHT;
        }
        return direction == Direction.DOWN;
    }

    private Position computeLeadingCell(
            final Vehicle vehicle,
            final Direction direction,
            final int step) {
        int row = vehicle.getPosition().getRow();
        int col = vehicle.getPosition().getCol();
        int length = vehicle.getLength();

        return switch (direction) {
            case UP -> new Position(row - step, col);
            case DOWN -> new Position(row + length - 1 + step, col);
            case LEFT -> new Position(row, col - step);
            case RIGHT -> new Position(row, col + length - 1 + step);
        };
    }

    private boolean reachesExitAtStep(
            final Board board,
            final Vehicle vehicle,
            final int step) {
        if (vehicle.getOrientation() == Orientation.HORIZONTAL) {
            return vehicle.getPosition().getCol() + vehicle.getLength() + step == board.getCols();
        }
        return vehicle.getPosition().getRow() + vehicle.getLength() + step == board.getRows();
    }
}
