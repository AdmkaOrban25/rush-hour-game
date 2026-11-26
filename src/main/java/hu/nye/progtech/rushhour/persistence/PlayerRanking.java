package hu.nye.progtech.rushhour.persistence;

import java.util.Objects;

/**
 * Egy jatekos osszesitett toplistajat (gyozelmek szama es legjobb lepesszam) tarolo ertekobjektum.
 */
public final class PlayerRanking {

    private final String playerName;
    private final int wins;
    private final int bestMoves;

    /**
     * Uj jatekos-rangsor bejegyzes letrehozasa.
     *
     * @param playerName a jatekos neve
     * @param wins a megnyert palyak szama
     * @param bestMoves a jatekos legjobb (legkisebb) lepesszama
     */
    public PlayerRanking(final String playerName, final int wins, final int bestMoves) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("A jatekos neve nem lehet ures.");
        }
        if (wins <= 0) {
            throw new IllegalArgumentException("A gyozelmek szamanak pozitivnak kell lennie.");
        }
        if (bestMoves <= 0) {
            throw new IllegalArgumentException("A legjobb lepesszamnak pozitivnak kell lennie.");
        }
        this.playerName = playerName.trim();
        this.wins = wins;
        this.bestMoves = bestMoves;
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
     * Visszaadja a teljesitett palyak (gyozelmek) szamat.
     *
     * @return a gyozelmek szama
     */
    public int getWins() {
        return wins;
    }

    /**
     * Visszaadja a jatekos legkisebb lepesszamat.
     *
     * @return a legjobb lepesszam
     */
    public int getBestMoves() {
        return bestMoves;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PlayerRanking other)) {
            return false;
        }
        return wins == other.wins
                && bestMoves == other.bestMoves
                && playerName.equals(other.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, wins, bestMoves);
    }

    @Override
    public String toString() {
        return "PlayerRanking{playerName='" + playerName + "'"
                + ", wins=" + wins
                + ", bestMoves=" + bestMoves + '}';
    }
}
