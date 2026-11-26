package hu.nye.progtech.rushhour.service;

import hu.nye.progtech.rushhour.model.GameState;
import java.util.Objects;
import java.util.Optional;

/**
 * Egy vegrehajtott lepes eredmenyet osszefoglalo megvaltoztathatatlan objektum.
 */
public final class MoveResult {

    private final GameState gameState;
    private final Character exitedTargetId;

    /**
     * Uj lepeseredmeny letrehozasa.
     *
     * @param gameState a lepes utani frissitett jatekallapot
     * @param exitedTargetId a lepes soran kijutott celjarmu azonositoja, vagy null
     */
    public MoveResult(final GameState gameState, final Character exitedTargetId) {
        this.gameState = Objects.requireNonNull(gameState, "A jatekallapot nem lehet null.");
        this.exitedTargetId = exitedTargetId;
    }

    /**
     * Visszaadja a lepes utani uj jatekallapotot.
     *
     * @return az uj GameState
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Visszaadja a lepes soran kijutott celjarmu azonositojat, ha tortent kijutas.
     *
     * @return a kijutott celjarmu azonositoja Optional-ben
     */
    public Optional<Character> getExitedTargetId() {
        return Optional.ofNullable(exitedTargetId);
    }

    /**
     * Megadja, hogy a lepes soran kijutott-e egy celjarmu a tablarol.
     *
     * @return igaz, ha celjarmu jutott ki
     */
    public boolean isTargetExited() {
        return exitedTargetId != null;
    }

    /**
     * Megadja, hogy a lepes eredmenyekent veget ert-e a jatek.
     *
     * @return igaz, ha valamelyik jatekos megnyerte a jatekot
     */
    public boolean isGameSolved() {
        return gameState.isSolved();
    }

    /**
     * Visszaadja a gyoztes jatekos nevet, ha a lepes gyozelmet hozott.
     *
     * @return a gyoztes neve Optional-ben
     */
    public Optional<String> getWinnerName() {
        return gameState.getWinnerName();
    }
}
