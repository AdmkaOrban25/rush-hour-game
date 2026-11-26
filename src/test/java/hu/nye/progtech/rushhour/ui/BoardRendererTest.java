package hu.nye.progtech.rushhour.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hu.nye.progtech.rushhour.model.Board;
import hu.nye.progtech.rushhour.model.GameState;
import hu.nye.progtech.rushhour.model.Orientation;
import hu.nye.progtech.rushhour.model.PlayerTurn;
import hu.nye.progtech.rushhour.model.Position;
import hu.nye.progtech.rushhour.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A BoardRenderer konzolos megjelenito egysegtesztjei.
 */
class BoardRendererTest {

    private BoardRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new BoardRenderer();
    }

    @Test
    void testRenderBoardIncludesVehiclesAndBothExitMarkers() {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .addVehicle(new Vehicle('B', new Position(0, 3), 2, Orientation.VERTICAL, true))
                .addVehicle(new Vehicle('C', new Position(1, 2), 2, Orientation.VERTICAL, false))
                .build();

        String rendered = renderer.renderBoard(board);

        assertTrue(rendered.contains("0 1 2 3 4 5"));
        assertTrue(rendered.contains("2 | A A C . . . | ==> EXIT A"));
        assertTrue(rendered.contains("EXIT B"));
        assertTrue(rendered.contains("0 | . . . B . . |"));
    }

    @Test
    void testRenderFullViewDisplaysActivePlayerAndTurnInfo() {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .addVehicle(new Vehicle('B', new Position(0, 3), 2, Orientation.VERTICAL, true))
                .build();
        GameState state = new GameState(
                "Adam", "Laci", "Kezdo keresztezodes", board, 3, PlayerTurn.PLAYER_TWO);

        String view = renderer.renderFullView(state);

        assertTrue(view.contains("Palya: Kezdo keresztezodes | Lepesek: 3"));
        assertTrue(view.contains("1. jatekos [A -> jobbra]: Adam"));
        assertTrue(view.contains("2. jatekos [B -> lefele]: Laci"));
        assertTrue(view.contains("Lepesen: Laci (celjarmu: B)"));
        assertTrue(view.contains("==> EXIT A"));
        assertTrue(view.contains("EXIT B"));
    }

    @Test
    void testRenderStatusDisplaysWinnerWhenGameIsSolved() {
        Board solvedBoard = Board.builder(5, 6)
                .addVehicle(new Vehicle('B', new Position(0, 3), 2, Orientation.VERTICAL, true))
                .addExitedTarget('A')
                .build();
        GameState solvedState = new GameState(
                "Adam", "Laci", "Kezdo keresztezodes", solvedBoard, 5, PlayerTurn.PLAYER_ONE);

        String status = renderer.renderStatus(solvedState);
        String boardOutput = renderer.renderBoard(solvedBoard);

        assertTrue(status.contains("GYOZTES: Adam!"));
        assertFalse(status.contains("Lepesen:"));
        assertFalse(boardOutput.contains("==> EXIT A"));
        assertTrue(boardOutput.contains("EXIT B"));
    }

    @Test
    void testRenderRejectsNullInputs() {
        assertThrows(NullPointerException.class, () -> renderer.renderBoard(null));
        assertThrows(NullPointerException.class, () -> renderer.renderStatus(null));
        assertThrows(NullPointerException.class, () -> renderer.renderFullView(null));
    }
}
