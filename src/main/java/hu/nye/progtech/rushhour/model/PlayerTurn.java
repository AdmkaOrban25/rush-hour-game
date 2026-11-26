package hu.nye.progtech.rushhour.model;

/**
 * A ketjatekosos Rush Hour jatekban az aktualisan lepo jatekost es celjarmuvet reprezentalo enum.
 */
public enum PlayerTurn {

    /** Elso jatekos, celjarmuve az 'A' (vizszintes kijarat jobbra). */
    PLAYER_ONE(1, 'A'),

    /** Masodik jatekos, celjarmuve a 'B' (fuggoleges kijarat lefele). */
    PLAYER_TWO(2, 'B');

    private final int playerNumber;
    private final char targetVehicleId;

    PlayerTurn(final int playerNumber, final char targetVehicleId) {
        this.playerNumber = playerNumber;
        this.targetVehicleId = targetVehicleId;
    }

    /**
     * Visszaadja a jatekos sorszamat (1 vagy 2).
     *
     * @return a jatekos sorszama
     */
    public int getPlayerNumber() {
        return playerNumber;
    }

    /**
     * Visszaadja a jatekoshoz tartozo celjarmu betujelet.
     *
     * @return 'A' vagy 'B'
     */
    public char getTargetVehicleId() {
        return targetVehicleId;
    }

    /**
     * Visszaadja a masik (kovetkezo) jatekost.
     *
     * @return az ellenfel PlayerTurn erteke
     */
    public PlayerTurn other() {
        return this == PLAYER_ONE ? PLAYER_TWO : PLAYER_ONE;
    }
}
