package hu.nye.progtech.rushhour.ui;

import hu.nye.progtech.rushhour.model.Board;
import hu.nye.progtech.rushhour.model.GameState;
import hu.nye.progtech.rushhour.model.Orientation;
import hu.nye.progtech.rushhour.model.PlayerTurn;
import hu.nye.progtech.rushhour.model.Vehicle;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * A Rush Hour jatektabla es a ketjatekosos allapot konzolos megjeleniteseert felelos osztaly.
 */
public final class BoardRenderer {

    private static final String SEPARATOR = "==================================================";
    private static final int GRID_LEFT_OFFSET = 6;

    /**
     * Osszeallitja a teljes konzolos nezetet a jatekallapot fejlecevel es a tablaraccsal.
     *
     * @param state az aktualis jatekallapot
     * @return a formazott konzolos szoveg
     */
    public String renderFullView(final GameState state) {
        Objects.requireNonNull(state, "A jatekallapot nem lehet null.");
        return renderStatus(state) + System.lineSeparator() + renderBoard(state.getBoard());
    }

    /**
     * Osszeallitja a jatekallapot informacios fejlecet (palya, jatekosok, soron levo vagy gyoztes).
     *
     * @param state az aktualis jatekallapot
     * @return a formazott statuszszoveg
     */
    public String renderStatus(final GameState state) {
        Objects.requireNonNull(state, "A jatekallapot nem lehet null.");
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR).append(nl);
        sb.append("Palya: ").append(state.getLevelName())
                .append(" | Lepesek: ").append(state.getMoveCount()).append(nl);
        sb.append("1. jatekos [A -> jobbra]: ").append(state.getPlayerOneName()).append(nl);
        sb.append("2. jatekos [B -> lefele]: ").append(state.getPlayerTwoName()).append(nl);

        if (state.isSolved()) {
            sb.append("GYOZTES: ")
                    .append(state.getWinnerName().orElse("Ismeretlen"))
                    .append("!")
                    .append(nl);
        } else {
            PlayerTurn turn = state.getCurrentPlayer();
            sb.append("Lepesen: ")
                    .append(state.getCurrentPlayerName())
                    .append(" (celjarmu: ")
                    .append(turn.getTargetVehicleId())
                    .append(")")
                    .append(nl);
        }
        sb.append(SEPARATOR);
        return sb.toString();
    }

    /**
     * Kirajzolja az N x M meretu jatektablat koordinatakkal es a celjarmuvek kijarataival.
     *
     * @param board a kirajzolando tabla
     * @return a formazott ASCII tablarajz
     */
    public String renderBoard(final Board board) {
        Objects.requireNonNull(board, "A tabla nem lehet null.");
        String nl = System.lineSeparator();
        char[][] grid = board.toCharGrid();
        int rows = board.getRows();
        int cols = board.getCols();

        OptionalInt exitRowA = findHorizontalTargetRow(board, PlayerTurn.PLAYER_ONE);
        OptionalInt exitColB = findVerticalTargetCol(board, PlayerTurn.PLAYER_TWO);

        StringBuilder sb = new StringBuilder();
        appendColumnHeader(sb, cols, nl);
        appendHorizontalBorder(sb, cols, nl);

        for (int r = 0; r < rows; r++) {
            sb.append(String.format("%3d | ", r));
            for (int c = 0; c < cols; c++) {
                sb.append(grid[r][c]).append(' ');
            }
            sb.append('|');
            if (exitRowA.isPresent() && exitRowA.getAsInt() == r) {
                sb.append(" ==> EXIT A");
            }
            sb.append(nl);
        }

        appendHorizontalBorder(sb, cols, nl);

        if (exitColB.isPresent()) {
            int arrowPadding = GRID_LEFT_OFFSET + exitColB.getAsInt() * 2;
            sb.append(" ".repeat(arrowPadding)).append('v').append(nl);
            int labelPadding = Math.max(0, arrowPadding - 2);
            sb.append(" ".repeat(labelPadding)).append("EXIT B").append(nl);
        }

        return sb.toString();
    }

    private void appendColumnHeader(final StringBuilder sb, final int cols, final String nl) {
        sb.append(" ".repeat(GRID_LEFT_OFFSET));
        for (int c = 0; c < cols; c++) {
            sb.append(c).append(' ');
        }
        sb.append(nl);
    }

    private void appendHorizontalBorder(final StringBuilder sb, final int cols, final String nl) {
        sb.append("    +").append("-".repeat(cols * 2 + 1)).append('+').append(nl);
    }

    private OptionalInt findHorizontalTargetRow(final Board board, final PlayerTurn player) {
        return board.getVehicle(player.getTargetVehicleId())
                .filter(Vehicle::isTarget)
                .filter(vehicle -> vehicle.getOrientation() == Orientation.HORIZONTAL)
                .map(vehicle -> OptionalInt.of(vehicle.getPosition().getRow()))
                .orElseGet(OptionalInt::empty);
    }

    private OptionalInt findVerticalTargetCol(final Board board, final PlayerTurn player) {
        return board.getVehicle(player.getTargetVehicleId())
                .filter(Vehicle::isTarget)
                .filter(vehicle -> vehicle.getOrientation() == Orientation.VERTICAL)
                .map(vehicle -> OptionalInt.of(vehicle.getPosition().getCol()))
                .orElseGet(OptionalInt::empty);
    }
}
