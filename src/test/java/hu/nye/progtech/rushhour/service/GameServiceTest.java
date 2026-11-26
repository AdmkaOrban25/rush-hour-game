package hu.nye.progtech.rushhour.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hu.nye.progtech.rushhour.model.Board;
import hu.nye.progtech.rushhour.model.Direction;
import hu.nye.progtech.rushhour.model.GameState;
import hu.nye.progtech.rushhour.model.Orientation;
import hu.nye.progtech.rushhour.model.Position;
import hu.nye.progtech.rushhour.model.Vehicle;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A GameService es a StandardMoveValidator egysegtesztjei.
 */
class GameServiceTest {

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(new StandardMoveValidator());
    }

    @Test
    void testValidMoveUpdatesVehiclePositionAndIncrementsMoves() throws InvalidMoveException {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .addVehicle(new Vehicle('B', new Position(0, 3), 2, Orientation.VERTICAL, true))
                .build();
        GameState initial = new GameState("Adam", "Teszt", board, 0);

        MoveResult result = gameService.applyMove(initial, new Move('A', Direction.RIGHT, 2));

        assertFalse(result.isTargetExited());
        assertFalse(result.isGameSolved());
        assertEquals(1, result.getGameState().getMoveCount());
        Position posA = result.getGameState().getBoard().getVehicle('A')
                .orElseThrow()
                .getPosition();
        assertEquals(new Position(2, 2), posA);
    }

    @Test
    void testMoveBlockedByObstacleThrowsInvalidMoveException() {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .addVehicle(new Vehicle('C', new Position(1, 2), 2, Orientation.VERTICAL, false))
                .build();
        GameState state = new GameState("Adam", "Teszt", board, 0);

        assertThrows(
                InvalidMoveException.class,
                () -> gameService.applyMove(state, new Move('A', Direction.RIGHT, 2)));
    }

    @Test
    void testNonTargetVehicleOutOfBoundsThrowsInvalidMoveException() {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .addVehicle(new Vehicle('C', new Position(0, 4), 2, Orientation.HORIZONTAL, false))
                .build();
        GameState state = new GameState("Adam", "Teszt", board, 0);

        assertThrows(
                InvalidMoveException.class,
                () -> gameService.applyMove(state, new Move('C', Direction.RIGHT, 1)));
    }

    @Test
    void testWrongDirectionForOrientationThrowsInvalidMoveException() {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .build();
        GameState state = new GameState("Adam", "Teszt", board, 0);

        assertThrows(
                InvalidMoveException.class,
                () -> gameService.applyMove(state, new Move('A', Direction.DOWN, 1)));
    }

    @Test
    void testNonExistentVehicleThrowsInvalidMoveException() {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .build();
        GameState state = new GameState("Adam", "Teszt", board, 0);

        assertThrows(
                InvalidMoveException.class,
                () -> gameService.applyMove(state, new Move('Z', Direction.RIGHT, 1)));
    }

    @Test
    void testCompleteLevelOneSolutionSolvesGameInFiveMoves() throws InvalidMoveException {
        LevelFactory factory = new LevelFactory();
        LevelDefinition levelOne = factory.createLevel(1);
        GameState state = new GameState("Adam", levelOne.getName(), levelOne.getInitialBoard(), 0);

        MoveResult r1 = gameService.applyMove(state, new Move('C', Direction.UP, 1));
        assertFalse(r1.isTargetExited());

        MoveResult r2 = gameService.applyMove(r1.getGameState(), new Move('D', Direction.LEFT, 1));
        assertFalse(r2.isTargetExited());

        MoveResult r3 = gameService.applyMove(r2.getGameState(), new Move('B', Direction.DOWN, 3));
        assertTrue(r3.isTargetExited());
        assertEquals('B', r3.getExitedTargetId().orElseThrow());
        assertFalse(r3.isGameSolved());

        MoveResult r4 = gameService.applyMove(r3.getGameState(), new Move('E', Direction.UP, 1));
        assertFalse(r4.isTargetExited());

        MoveResult r5 = gameService.applyMove(r4.getGameState(), new Move('A', Direction.RIGHT, 4));
        assertTrue(r5.isTargetExited());
        assertEquals('A', r5.getExitedTargetId().orElseThrow());
        assertTrue(r5.isGameSolved());
        assertEquals(5, r5.getGameState().getMoveCount());
    }

    @Test
    void testTargetCanExitWhenDrivingPastExitEdgeWithClearPath() throws InvalidMoveException {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 3), 2, Orientation.HORIZONTAL, true))
                .build();
        GameState state = new GameState("Adam", "Teszt", board, 0);

        MoveResult result = gameService.applyMove(state, new Move('A', Direction.RIGHT, 3));

        assertTrue(result.isTargetExited());
        assertTrue(result.isGameSolved());
    }

    @Test
    void testGetValidMovesReturnsAvailableMoves() {
        LevelDefinition levelOne = new LevelFactory().createLevel(1);
        GameState state = new GameState("Adam", levelOne.getName(), levelOne.getInitialBoard(), 0);

        List<Move> moves = gameService.getValidMoves(state);

        assertFalse(moves.isEmpty());
        assertTrue(gameService.hasAnyValidMove(state));
        assertTrue(moves.contains(new Move('C', Direction.UP, 1)));
    }
}
