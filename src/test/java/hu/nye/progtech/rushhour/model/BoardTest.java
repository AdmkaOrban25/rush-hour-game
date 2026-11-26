package hu.nye.progtech.rushhour.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * A Board es GameState modellosztalyok egysegtesztjei.
 */
class BoardTest {

    @Test
    void testRectangularBoardCreationAndCharGrid() {
        Vehicle carA = new Vehicle('A', new Position(1, 0), 2, Orientation.HORIZONTAL, true);
        Vehicle carB = new Vehicle('B', new Position(0, 4), 2, Orientation.VERTICAL, true);

        Board board = Board.builder(5, 7)
                .addVehicle(carA)
                .addVehicle(carB)
                .build();

        assertEquals(5, board.getRows());
        assertEquals(7, board.getCols());
        assertTrue(board.isOccupied(new Position(1, 0)));
        assertTrue(board.isOccupied(new Position(1, 1)));
        assertFalse(board.isOccupied(new Position(0, 0)));

        char[][] grid = board.toCharGrid();
        assertEquals('A', grid[1][0]);
        assertEquals('A', grid[1][1]);
        assertEquals('B', grid[0][4]);
        assertEquals('B', grid[1][4]);
        assertEquals(Board.EMPTY_CELL, grid[0][0]);
    }

    @Test
    void testOverlappingVehiclesThrowException() {
        Vehicle carA = new Vehicle('A', new Position(2, 1), 2, Orientation.HORIZONTAL, true);
        Vehicle carC = new Vehicle('C', new Position(1, 2), 3, Orientation.VERTICAL, false);

        assertThrows(
                IllegalArgumentException.class,
                () -> Board.builder(6, 6)
                        .addVehicle(carA)
                        .addVehicle(carC)
                        .build());
    }

    @Test
    void testOutOfBoundsVehicleThrowsException() {
        Vehicle carA = new Vehicle('A', new Position(2, 5), 2, Orientation.HORIZONTAL, true);

        assertThrows(
                IllegalArgumentException.class,
                () -> Board.builder(6, 6)
                        .addVehicle(carA)
                        .build());
    }

    @Test
    void testTwoTargetsMustBothExitToSolveBoard() {
        Vehicle carA = new Vehicle('A', new Position(2, 4), 2, Orientation.HORIZONTAL, true);
        Vehicle carB = new Vehicle('B', new Position(4, 3), 2, Orientation.VERTICAL, true);
        Vehicle obstacle = new Vehicle('C', new Position(0, 0), 2, Orientation.HORIZONTAL, false);

        Board initial = Board.builder(6, 6)
                .addVehicle(carA)
                .addVehicle(carB)
                .addVehicle(obstacle)
                .build();

        assertTrue(initial.hasReachedExit(carA));
        assertTrue(initial.hasReachedExit(carB));
        assertFalse(initial.hasReachedExit(obstacle));
        assertFalse(initial.isSolved());

        Board afterAExits = initial.withExitedTarget('A');
        assertFalse(afterAExits.isSolved());
        assertTrue(afterAExits.getVehicle('A').isEmpty());
        assertTrue(afterAExits.getExitedTargets().contains('A'));

        Board afterBExits = afterAExits.withExitedTarget('B');
        assertTrue(afterBExits.isSolved());
        assertEquals(2, afterBExits.getExitedTargets().size());
    }

    @Test
    void testGameStateWithNextMoveIncrementsMoveCount() {
        Vehicle carA = new Vehicle('A', new Position(2, 1), 2, Orientation.HORIZONTAL, true);
        Board board = Board.builder(6, 6).addVehicle(carA).build();
        GameState initial = new GameState("Adam", "Level 1", board, 0);

        Board nextBoard = board.withUpdatedVehicle(carA.moved(Direction.RIGHT, 1));
        GameState nextState = initial.withNextMove(nextBoard);

        assertEquals(0, initial.getMoveCount());
        assertEquals(1, nextState.getMoveCount());
        Position actualPos = nextState.getBoard().getVehicle('A').orElseThrow().getPosition();
        assertEquals(new Position(2, 2), actualPos);
    }
}
