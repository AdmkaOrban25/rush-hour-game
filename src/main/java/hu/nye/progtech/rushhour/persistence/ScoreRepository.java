package hu.nye.progtech.rushhour.persistence;

import java.util.List;

/**
 * A jatekoseredmenyek es a toplista adatbazis-kezelesehez hasznalt interfesz (Repository minta).
 */
public interface ScoreRepository {

    /**
     * Elment egy uj jatekoseredmenyt az adatbazisba.
     *
     * @param entry a mentendo eredmeny
     * @throws PersistenceException adatbazis-hiba eseten
     */
    void saveScore(ScoreEntry entry) throws PersistenceException;

    /**
     * Lekerdezi a legjobb eredmenyeket lepesszam szerint novekvo sorrendben.
     *
     * @param limit a visszaadando rekordok maximalis szama
     * @return a legjobb eredmenyek listaja
     * @throws PersistenceException adatbazis-hiba eseten
     */
    List<ScoreEntry> findTopScores(int limit) throws PersistenceException;

    /**
     * Lekerdezi egy adott palya legjobb eredmenyeit lepesszam szerint novekvo sorrendben.
     *
     * @param levelName a palya neve
     * @param limit a visszaadando rekordok maximalis szama
     * @return az adott palya legjobb eredmenyei
     * @throws PersistenceException adatbazis-hiba eseten
     */
    List<ScoreEntry> findTopScoresByLevel(String levelName, int limit) throws PersistenceException;

    /**
     * Lekerdezi a jatekosok osszesitett rangsorat gyozelmek szama szerint csokkeno sorrendben.
     *
     * @param limit a visszaadando jatekosok maximalis szama
     * @return a jatekosok rangsora
     * @throws PersistenceException adatbazis-hiba eseten
     */
    List<PlayerRanking> findPlayerRankings(int limit) throws PersistenceException;
}
