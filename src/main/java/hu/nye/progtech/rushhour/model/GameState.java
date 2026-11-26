package hu.nye.progtech.rushhour.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Egy ketjatekosos Rush Hour jatekmenet megvaltoztathatatlan allapotat tarolja.
 */
public final class GameState {

    /** Alapertelmezett masodik jatekosnev, ha nincs kulon megadva. */
    public static final String DEFAULT_SECOND_PLAYER = "Jatekos 2";

    private final String playerOneName;
    private final String playerTwoName;
    private final String levelName;
    private final Board board;
    private final int moveCount;
    private final PlayerTurn currentPlayer;

    /**
     * Uj ketjatekosos jatekallapot letrehozasa.
     *
     * @param playerOneName az elso jatekos neve ('A' celjarmu)
     * @param playerTwoName a masodik jatekos neve ('B' celjarmu)
     * @param levelName az aktualis palya neve
     * @param board az aktualis jatektabla
     * @param moveCount az eddig megtett lepesek szama (nem negativ)
     * @param currentPlayer a jelenleg lepesen levo jatekos
     */
    public GameState(
            final String playerOneName,
            final String playerTwoName,
            final String levelName,
            final Board board,
            final int moveCount,
            final PlayerTurn currentPlayer) {
        if (playerOneName == null || playerOneName.isBlank()) {
            throw new IllegalArgumentException("Az elso jatekos neve nem lehet ures.");
        }
        if (playerTwoName == null || playerTwoName.isBlank()) {
            throw new IllegalArgumentException("A masodik jatekos neve nem lehet ures.");
        }
        if (levelName == null || levelName.isBlank()) {
            throw new IllegalArgumentException("A palya neve nem lehet ures.");
        }
        if (moveCount < 0) {
            throw new IllegalArgumentException("A lepesszam nem lehet negativ.");
        }
        this.playerOneName = playerOneName.trim();
        this.playerTwoName = playerTwoName.trim();
        this.levelName = levelName.trim();
        this.board = Objects.requireNonNull(board, "A tabla nem lehet null.");
        this.moveCount = moveCount;
        this.currentPlayer = Objects.requireNonNull(
                currentPlayer, "A lepesen levo jatekos nem lehet null.");
    }

    /**
     * Roviditett konstruktor alapertelmezett masodik jatekosnevvel es elso jatekos kezdesevel.
     *
     * @param playerName az elso jatekos neve
     * @param levelName az aktualis palya neve
     * @param board az aktualis jatektabla
     * @param moveCount az eddig megtett lepesek szama
     */
    public GameState(
            final String playerName,
            final String levelName,
            final Board board,
            final int moveCount) {
        this(playerName, DEFAULT_SECOND_PLAYER, levelName, board, moveCount, PlayerTurn.PLAYER_ONE);
    }

    /**
     * Visszaadja az elso jatekos nevet ('A' celjarmu).
     *
     * @return az elso jatekos neve
     */
    public String getPlayerOneName() {
        return playerOneName;
    }

    /**
     * Visszaadja a masodik jatekos nevet ('B' celjarmu).
     *
     * @return a masodik jatekos neve
     */
    public String getPlayerTwoName() {
        return playerTwoName;
    }

    /**
     * Visszaadja az elso jatekos nevet (kompatibilitasi lekerdezes).
     *
     * @return az elso jatekos neve
     */
    public String getPlayerName() {
        return playerOneName;
    }

    /**
     * Visszaadja az adott korhoz tartozo jatekos nevet.
     *
     * @param turn a keresett jatekos
     * @return a jatekos neve
     */
    public String getPlayerName(final PlayerTurn turn) {
        Objects.requireNonNull(turn, "A jatekos nem lehet null.");
        return turn == PlayerTurn.PLAYER_ONE ? playerOneName : playerTwoName;
    }

    /**
     * Visszaadja, hogy eppen melyik jatekos van lepesen.
     *
     * @return az aktualisan lepo jatekos
     */
    public PlayerTurn getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Visszaadja a jelenleg lepesen levo jatekos nevet.
     *
     * @return a soron levo jatekos neve
     */
    public String getCurrentPlayerName() {
        return getPlayerName(currentPlayer);
    }

    /**
     * Visszaadja a palya nevet.
     *
     * @return a palya neve
     */
    public String getLevelName() {
        return levelName;
    }

    /**
     * Visszaadja a jatektabla aktualis allapotat.
     *
     * @return a tabla
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Visszaadja az eddig megtett lepesek szamat.
     *
     * @return a lepesszam
     */
    public int getMoveCount() {
        return moveCount;
    }

    /**
     * Visszaadja a gyoztes jatekost, ha valamelyik celjarmu mar kijutott a tablarol.
     *
     * @return a gyoztes jatekos, vagy ures Optional folyamatban levo jateknal
     */
    public Optional<PlayerTurn> getWinner() {
        if (board.getExitedTargets().contains(PlayerTurn.PLAYER_ONE.getTargetVehicleId())) {
            return Optional.of(PlayerTurn.PLAYER_ONE);
        }
        if (board.getExitedTargets().contains(PlayerTurn.PLAYER_TWO.getTargetVehicleId())) {
            return Optional.of(PlayerTurn.PLAYER_TWO);
        }
        if (board.hasAnyTargetExited()) {
            return Optional.of(currentPlayer);
        }
        return Optional.empty();
    }

    /**
     * Visszaadja a gyoztes jatekos nevet, ha a jatek mar eldolt.
     *
     * @return a gyoztes neve Optional-ben
     */
    public Optional<String> getWinnerName() {
        return getWinner().map(this::getPlayerName);
    }

    /**
     * Megadja, hogy a jatek veget ert-e (valamelyik jatekos kijuttatta a celjarmuvet).
     *
     * @return igaz, ha van gyoztes
     */
    public boolean isSolved() {
        return getWinner().isPresent();
    }

    /**
     * Uj jatekallapotot hoz letre a kovetkezo tablaallapottal, eggyel novelt lepesszammal,
     * es ha a jatek meg nem ert veget, atadja a lepest a masik jatekosnak.
     *
     * @param nextBoard a lepes utani tabla
     * @return az uj GameState peldany
     */
    public GameState withNextMove(final Board nextBoard) {
        Objects.requireNonNull(nextBoard, "A kovetkezo tabla nem lehet null.");
        PlayerTurn nextTurn = nextBoard.hasAnyTargetExited()
                ? currentPlayer
                : currentPlayer.other();
        return new GameState(
                playerOneName,
                playerTwoName,
                levelName,
                nextBoard,
                moveCount + 1,
                nextTurn);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GameState other)) {
            return false;
        }
        return moveCount == other.moveCount
                && currentPlayer == other.currentPlayer
                && playerOneName.equals(other.playerOneName)
                && playerTwoName.equals(other.playerTwoName)
                && levelName.equals(other.levelName)
                && board.equals(other.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                playerOneName,
                playerTwoName,
                levelName,
                board,
                moveCount,
                currentPlayer);
    }

    @Override
    public String toString() {
        return "GameState{playerOneName='" + playerOneName + "'"
                + ", playerTwoName='" + playerTwoName + "'"
                + ", levelName='" + levelName + "'"
                + ", moveCount=" + moveCount
                + ", currentPlayer=" + currentPlayer
                + ", solved=" + isSolved() + '}';
    }
}
