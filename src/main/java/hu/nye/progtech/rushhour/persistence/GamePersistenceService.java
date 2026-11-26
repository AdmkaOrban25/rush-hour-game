package hu.nye.progtech.rushhour.persistence;

import hu.nye.progtech.rushhour.model.GameState;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * A jatekmenteseket es a toplistat osszefogo perzisztencia-szerviz (Dependency Injection).
 */
public final class GamePersistenceService {

    private final GameStateRepository gameStateRepository;
    private final ScoreRepository scoreRepository;

    /**
     * Uj perzisztencia-szerviz letrehozasa a szukseges tarolokkal.
     *
     * @param gameStateRepository a jatekallapotok menteset vegzo tarolo
     * @param scoreRepository a toplistat kezelo tarolo
     */
    public GamePersistenceService(
            final GameStateRepository gameStateRepository,
            final ScoreRepository scoreRepository) {
        this.gameStateRepository = Objects.requireNonNull(
                gameStateRepository, "A GameStateRepository nem lehet null.");
        this.scoreRepository = Objects.requireNonNull(
                scoreRepository, "A ScoreRepository nem lehet null.");
    }

    /**
     * Elmenti a megadott jatekallapotot fajlba.
     *
     * @param gameState a mentendo allapot
     * @param filePath a celfajl utvonala
     * @throws PersistenceException ha a mentes sikertelen
     */
    public void saveGame(final GameState gameState, final Path filePath)
            throws PersistenceException {
        gameStateRepository.save(gameState, filePath);
    }

    /**
     * Betolti a mentett jatekallapotot a megadott fajlbol.
     *
     * @param filePath a forrasfajl utvonala
     * @return a betoltott GameState
     * @throws PersistenceException ha a betoltes sikertelen
     */
    public GameState loadGame(final Path filePath) throws PersistenceException {
        return gameStateRepository.load(filePath);
    }

    /**
     * Rogziti egy megoldott palya eredmenyet a toplistaban.
     *
     * @param completedState a megoldott jatekallapot
     * @return a letrehozott es elmentett ScoreEntry
     * @throws PersistenceException adatbazis-hiba eseten
     */
    public ScoreEntry recordCompletedGame(final GameState completedState)
            throws PersistenceException {
        Objects.requireNonNull(completedState, "A jatekallapot nem lehet null.");
        if (!completedState.isSolved()) {
            throw new IllegalArgumentException(
                    "Csak megoldott jatek eredmenye rogzitheto a toplistaban.");
        }
        ScoreEntry entry = new ScoreEntry(
                completedState.getPlayerName(),
                completedState.getLevelName(),
                completedState.getMoveCount(),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        scoreRepository.saveScore(entry);
        return entry;
    }

    /**
     * Lekerdezi a legjobb eredmenyeket az osszes palyarol.
     *
     * @param limit a megjelenitendo rekordok szama
     * @return a legjobb eredmenyek listaja
     * @throws PersistenceException adatbazis-hiba eseten
     */
    public List<ScoreEntry> getTopScores(final int limit) throws PersistenceException {
        return scoreRepository.findTopScores(limit);
    }

    /**
     * Lekerdezi egy adott palya legjobb eredmenyeit.
     *
     * @param levelName a palya neve
     * @param limit a megjelenitendo rekordok szama
     * @return a palya legjobb eredmenyei
     * @throws PersistenceException adatbazis-hiba eseten
     */
    public List<ScoreEntry> getTopScoresByLevel(final String levelName, final int limit)
            throws PersistenceException {
        return scoreRepository.findTopScoresByLevel(levelName, limit);
    }

    /**
     * Lekerdezi a jatekosok osszesitett rangsorat gyozelmek szama szerint.
     *
     * @param limit a megjelenitendo jatekosok szama
     * @return a rangsor listaja
     * @throws PersistenceException adatbazis-hiba eseten
     */
    public List<PlayerRanking> getPlayerRankings(final int limit) throws PersistenceException {
        return scoreRepository.findPlayerRankings(limit);
    }
}
