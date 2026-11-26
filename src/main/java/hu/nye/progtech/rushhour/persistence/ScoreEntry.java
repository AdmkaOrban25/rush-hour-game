package hu.nye.progtech.rushhour.persistence;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Egy sikeresen teljesitett palya eredmenyet tarolo megvaltoztathatatlan ertekobjektum.
 */
public final class ScoreEntry {

    private final String playerName;
    private final String levelName;
    private final int moveCount;
    private final LocalDateTime playedAt;

    /**
     * Uj eredmenybejegyzes letrehozasa.
     *
     * @param playerName a jatekos neve
     * @param levelName a teljesitett palya neve
     * @param moveCount a megoldashoz szukseges lepesek szama (pozitiv)
     * @param playedAt a teljesites idopontja
     */
    public ScoreEntry(
            final String playerName,
            final String levelName,
            final int moveCount,
            final LocalDateTime playedAt) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("A jatekos neve nem lehet ures.");
        }
        if (levelName == null || levelName.isBlank()) {
            throw new IllegalArgumentException("A palya neve nem lehet ures.");
        }
        if (moveCount <= 0) {
            throw new IllegalArgumentException("A lepesszamnak pozitivnak kell lennie.");
        }
        this.playerName = playerName.trim();
        this.levelName = levelName.trim();
        this.moveCount = moveCount;
        this.playedAt = Objects.requireNonNull(playedAt, "Az idopont nem lehet null.");
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
     * Visszaadja a teljesitett palya nevet.
     *
     * @return a palya neve
     */
    public String getLevelName() {
        return levelName;
    }

    /**
     * Visszaadja a megoldas lepesszamat.
     *
     * @return a lepesszam
     */
    public int getMoveCount() {
        return moveCount;
    }

    /**
     * Visszaadja a teljesites idopontjat.
     *
     * @return az idopont
     */
    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ScoreEntry other)) {
            return false;
        }
        return moveCount == other.moveCount
                && playerName.equals(other.playerName)
                && levelName.equals(other.levelName)
                && playedAt.equals(other.playedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, levelName, moveCount, playedAt);
    }

    @Override
    public String toString() {
        return "ScoreEntry{playerName='" + playerName + "'"
                + ", levelName='" + levelName + "'"
                + ", moveCount=" + moveCount
                + ", playedAt=" + playedAt + '}';
    }
}
