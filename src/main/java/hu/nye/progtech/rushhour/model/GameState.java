package hu.nye.progtech.rushhour.model;

import java.util.Objects;

/**
 * Egy folyamatban levo Rush Hour jatekmenet megvaltoztathatatlan allapotat tarolja.
 */
public final class GameState {

    private final String playerName;
    private final String levelName;
    private final Board board;
    private final int moveCount;

    /**
     * Uj jatekallapot letrehozasa.
     *
     * @param playerName a jatekos neve
     * @param levelName az aktualis palya neve
     * @param board az aktualis jatektabla
     * @param moveCount az eddig megtett lepesek szama (nem negativ)
     */
    public GameState(
            final String playerName,
            final String levelName,
            final Board board,
            final int moveCount) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("A jatekos neve nem lehet ures.");
        }
        if (levelName == null || levelName.isBlank()) {
            throw new IllegalArgumentException("A palya neve nem lehet ures.");
        }
        if (moveCount < 0) {
            throw new IllegalArgumentException("A lepesszam nem lehet negativ.");
        }
        this.playerName = playerName.trim();
        this.levelName = levelName.trim();
        this.board = Objects.requireNonNull(board, "A tabla nem lehet null.");
        this.moveCount = moveCount;
    }

    /**
     * Visszaadja a jatekos nevet.
     *
     * @return a jatekos neve
     */
    public String getPlayerName() {
        return playerName;
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
     * Megadja, hogy a jatek megoldott-e.
     *
     * @return igaz, ha minden celjarmu kijutott
     */
    public boolean isSolved() {
        return board.isSolved();
    }

    /**
     * Uj jatekallapotot hoz letre a kovetkezo tablaallapottal es eggyel novelt lepesszammal.
     *
     * @param nextBoard a lepes utani tabla
     * @return az uj GameState peldany
     */
    public GameState withNextMove(final Board nextBoard) {
        return new GameState(playerName, levelName, nextBoard, moveCount + 1);
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
                && playerName.equals(other.playerName)
                && levelName.equals(other.levelName)
                && board.equals(other.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, levelName, board, moveCount);
    }

    @Override
    public String toString() {
        return "GameState{playerName='" + playerName + "'"
                + ", levelName='" + levelName + "'"
                + ", moveCount=" + moveCount
                + ", solved=" + isSolved() + '}';
    }
}