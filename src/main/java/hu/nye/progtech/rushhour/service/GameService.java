package hu.nye.progtech.rushhour.service;

import hu.nye.progtech.rushhour.model.Board;
import hu.nye.progtech.rushhour.model.Direction;
import hu.nye.progtech.rushhour.model.GameState;
import hu.nye.progtech.rushhour.model.Orientation;
import hu.nye.progtech.rushhour.model.Vehicle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Rush Hour jatek fo uzleti logikajat kezelo szervizosztaly.
 */
public final class GameService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameService.class);

    private final MoveValidator moveValidator;

    /**
     * Uj GameService peldany letrehozasa fuggoseginjektalassal.
     *
     * @param moveValidator a lepeseket ellenorzo strategia
     */
    public GameService(final MoveValidator moveValidator) {
        this.moveValidator = Objects.requireNonNull(
                moveValidator, "A MoveValidator nem lehet null.");
    }

    /**
     * Vegrehajt egy lepest a jelenlegi jatekallapoton es visszaadja a frissitett eredmenyt.
     *
     * @param currentState az aktualis jatekallapot
     * @param move a vegrehajtando lepes
     * @return a lepes eredmenye (uj allapot, kijutas es gyozelmi allapot)
     * @throws InvalidMoveException ha a lepes nem engedelyezett vagy a jatek mar veget ert
     */
    public MoveResult applyMove(final GameState currentState, final Move move)
            throws InvalidMoveException {
        Objects.requireNonNull(currentState, "Az aktualis allapot nem lehet null.");
        Objects.requireNonNull(move, "A lepes nem lehet null.");

        if (currentState.isSolved()) {
            throw new InvalidMoveException("A palya mar megoldodott, nem lehet tobbet lepni.");
        }

        Board currentBoard = currentState.getBoard();
        boolean targetExits = moveValidator.validateAndCheckExit(currentBoard, move);

        Board nextBoard;
        Character exitedTargetId = null;

        if (targetExits) {
            exitedTargetId = move.getVehicleId();
            nextBoard = currentBoard.withExitedTarget(exitedTargetId);
            LOGGER.info("A(z) {} celjarmu kijutott a tablarol! (Lepes: {})",
                    exitedTargetId, move);
        } else {
            Vehicle vehicle = currentBoard.getVehicle(move.getVehicleId()).orElseThrow();
            Vehicle movedVehicle = vehicle.moved(move.getDirection(), move.getSteps());
            nextBoard = currentBoard.withUpdatedVehicle(movedVehicle);
            LOGGER.info("Lepes vegrehajtva: {} -> uj pozicio: {}",
                    move, movedVehicle.getPosition());
        }

        GameState nextState = currentState.withNextMove(nextBoard);
        if (nextState.isSolved()) {
            LOGGER.info("Jatek megnyerve! Jatekos: {}, palya: {}, lepesek: {}",
                    nextState.getPlayerName(),
                    nextState.getLevelName(),
                    nextState.getMoveCount());
        }

        return new MoveResult(nextState, exitedTargetId);
    }

    /**
     * Osszegyujti az osszes jelenleg szabalyos lepest a tablan.
     *
     * @param state az aktualis jatekallapot
     * @return a szabalyos lepesek modosithatatlan listaja
     */
    public List<Move> getValidMoves(final GameState state) {
        Objects.requireNonNull(state, "Az allapot nem lehet null.");
        if (state.isSolved()) {
            return Collections.emptyList();
        }

        Board board = state.getBoard();
        List<Move> validMoves = new ArrayList<>();
        int maxSteps = Math.max(board.getRows(), board.getCols());

        for (Vehicle vehicle : board.getVehicles().values()) {
            List<Direction> allowedDirections = vehicle.getOrientation() == Orientation.HORIZONTAL
                    ? List.of(Direction.LEFT, Direction.RIGHT)
                    : List.of(Direction.UP, Direction.DOWN);

            for (Direction direction : allowedDirections) {
                for (int step = 1; step <= maxSteps; step++) {
                    Move candidate = new Move(vehicle.getId(), direction, step);
                    try {
                        boolean exits = moveValidator.validateAndCheckExit(board, candidate);
                        validMoves.add(candidate);
                        if (exits) {
                            break;
                        }
                    } catch (InvalidMoveException ex) {
                        break;
                    }
                }
            }
        }

        return Collections.unmodifiableList(validMoves);
    }

    /**
     * Ellenorzi, hogy van-e legalabb egy szabalyos lepes az aktualis allapotbol.
     *
     * @param state az aktualis jatekallapot
     * @return igaz, ha a jatekos meg tud lepni
     */
    public boolean hasAnyValidMove(final GameState state) {
        return !getValidMoves(state).isEmpty();
    }
}
