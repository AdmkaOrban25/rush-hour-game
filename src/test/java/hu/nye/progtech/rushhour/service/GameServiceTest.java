package hu.nye.progtech.rushhour.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hu.nye.progtech.rushhour.model.Board;
import hu.nye.progtech.rushhour.model.Direction;
import hu.nye.progtech.rushhour.model.GameState;
import hu.nye.progtech.rushhour.model.Orientation;
import hu.nye.progtech.rushhour.model.PlayerTurn;
import hu.nye.progtech.rushhour.model.Position;
import hu.nye.progtech.rushhour.model.Vehicle;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A ketjatekosos GameService es a StandardMoveValidator egysegtesztjei.
 */
class GameServiceTest {

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(new StandardMoveValidator());
    }

    @Test
    void testValidMoveUpdatesVehiclePositionAndSwitchesTurn() throws InvalidMoveException {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .addVehicle(new Vehicle('B', new Position(0, 3), 2, Orientation.VERTICAL, true))
                .build();
        GameState initial = new GameState(
                "Adam", "Laci", "Teszt", board, 0, PlayerTurn.PLAYER_ONE);

        MoveResult result = gameService.applyMove(initial, new Move('A', Direction.RIGHT, 2));

        assertFalse(result.isTargetExited());
        assertFalse(result.isGameSolved());
        assertEquals(1, result.getGameState().getMoveCount());
        assertEquals(PlayerTurn.PLAYER_TWO, result.getGameState().getCurrentPlayer());
        assertEquals("Laci", result.getGameState().getCurrentPlayerName());
        Position posA = result.getGameState().getBoard().getVehicle('A')
                .orElseThrow()
                .getPosition();
        assertEquals(new Position(2, 2), posA);
    }

    @Test
    void testPlayerCannotMoveOpponentTargetVehicle() {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .addVehicle(new Vehicle('B', new Position(0, 3), 2, Orientation.VERTICAL, true))
                .build();
        GameState p1Turn = new GameState(
                "Adam", "Laci", "Teszt", board, 0, PlayerTurn.PLAYER_ONE);
        GameState p2Turn = new GameState(
                "Adam", "Laci", "Teszt", board, 1, PlayerTurn.PLAYER_TWO);

        assertThrows(
                InvalidMoveException.class,
                () -> gameService.applyMove(p1Turn, new Move('B', Direction.DOWN, 1)));
        assertThrows(
                InvalidMoveException.class,
                () -> gameService.applyMove(p2Turn, new Move('A', Direction.RIGHT, 1)));
    }

    @Test
    void testMoveBlockedByObstacleThrowsInvalidMoveException() {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .addVehicle(new Vehicle('C', new Position(1, 2), 2, Orientation.VERTICAL, false))
                .build();
        GameState state = new GameState("Adam", "Laci", "Teszt", board, 0, PlayerTurn.PLAYER_ONE);

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
        GameState state = new GameState("Adam", "Laci", "Teszt", board, 0, PlayerTurn.PLAYER_ONE);

        assertThrows(
                InvalidMoveException.class,
                () -> gameService.applyMove(state, new Move('C', Direction.RIGHT, 1)));
    }

    @Test
    void testWrongDirectionForOrientationThrowsInvalidMoveException() {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .build();
        GameState state = new GameState("Adam", "Laci", "Teszt", board, 0, PlayerTurn.PLAYER_ONE);

        assertThrows(
                InvalidMoveException.class,
                () -> gameService.applyMove(state, new Move('A', Direction.DOWN, 1)));
    }

    @Test
    void testNonExistentVehicleThrowsInvalidMoveException() {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .build();
        GameState state = new GameState("Adam", "Laci", "Teszt", board, 0, PlayerTurn.PLAYER_ONE);

        assertThrows(
                InvalidMoveException.class,
                () -> gameService.applyMove(state, new Move('Z', Direction.RIGHT, 1)));
    }

    @Test
    void testTwoPlayerGameOnLevelOneEndsWhenPlayerTwoExitsFirst() throws InvalidMoveException {
        LevelDefinition levelOne = new LevelFactory().createLevel(1);
        GameState state = new GameState(
                "Adam", "Laci", levelOne.getName(), levelOne.getInitialBoard(),
                0, PlayerTurn.PLAYER_ONE);

        MoveResult r1 = gameService.applyMove(state, new Move('C', Direction.UP, 1));
        assertEquals(PlayerTurn.PLAYER_TWO, r1.getGameState().getCurrentPlayer());

        MoveResult r2 = gameService.applyMove(r1.getGameState(), new Move('D', Direction.LEFT, 1));
        assertEquals(PlayerTurn.PLAYER_ONE, r2.getGameState().getCurrentPlayer());

        MoveResult r3 = gameService.applyMove(r2.getGameState(), new Move('E', Direction.UP, 1));
        assertEquals(PlayerTurn.PLAYER_TWO, r3.getGameState().getCurrentPlayer());

        MoveResult r4 = gameService.applyMove(r3.getGameState(), new Move('B', Direction.DOWN, 3));
        assertTrue(r4.isTargetExited());
        assertEquals('B', r4.getExitedTargetId().orElseThrow());
        assertTrue(r4.isGameSolved());
        assertEquals("Laci", r4.getWinnerName().orElseThrow());
        assertEquals(4, r4.getGameState().getMoveCount());

        assertThrows(
                InvalidMoveException.class,
                () -> gameService.applyMove(r4.getGameState(), new Move('A', Direction.RIGHT, 1)));
    }

    @Test
    void testPlayerOneWinsWhenTargetAExits() throws InvalidMoveException {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 3), 2, Orientation.HORIZONTAL, true))
                .addVehicle(new Vehicle('B', new Position(0, 2), 2, Orientation.VERTICAL, true))
                .build();
        GameState state = new GameState("Adam", "Laci", "Teszt", board, 0, PlayerTurn.PLAYER_ONE);

        MoveResult result = gameService.applyMove(state, new Move('A', Direction.RIGHT, 3));

        assertTrue(result.isTargetExited());
        assertEquals('A', result.getExitedTargetId().orElseThrow());
        assertTrue(result.isGameSolved());
        assertEquals("Adam", result.getWinnerName().orElseThrow());
    }

    @Test
    void testGetValidMovesExcludesOpponentTargetVehicle() {
        LevelDefinition levelOne = new LevelFactory().createLevel(1);
        GameState state = new GameState(
                "Adam", "Laci", levelOne.getName(), levelOne.getInitialBoard(),
                0, PlayerTurn.PLAYER_ONE);

        List<Move> moves = gameService.getValidMoves(state);

        assertFalse(moves.isEmpty());
        assertTrue(gameService.hasAnyValidMove(state));
        assertTrue(moves.contains(new Move('C', Direction.UP, 1)));
        assertTrue(moves.stream().noneMatch(move -> move.getVehicleId() == 'B'));
    }
}
