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

    /** A megadott nevvel mentett allasok alapertelmezett mappaja. */
    public static final Path DEFAULT_SAVES_DIR = Path.of("saves");

    private static final String JSON_EXTENSION = ".json";

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
     * Elmenti a jatekallapotot a felhasznalo altal megadott nevvel a celmappaba.
     *
     * @param gameState a mentendo allapot
     * @param savesDirectory a mentesek mappaja
     * @param saveName a mentes neve (kiterjesztessel vagy anelkul)
     * @return a letrehozott mentesi fajl teljes utvonala
     * @throws PersistenceException ha a mentes sikertelen
     */
    public Path saveGameByName(
            final GameState gameState,
            final Path savesDirectory,
            final String saveName) throws PersistenceException {
        Path targetPath = resolveSavePath(savesDirectory, saveName);
        gameStateRepository.save(gameState, targetPath);
        return targetPath;
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
     * Betolt egy korabban nev szerint elmentett jatekallapotot.
     *
     * @param savesDirectory a mentesek mappaja
     * @param saveName a mentes neve (kiterjesztessel vagy anelkul)
     * @return a betoltott GameState
     * @throws PersistenceException ha a fajl nem talalhato vagy hibas
     */
    public GameState loadGameByName(final Path savesDirectory, final String saveName)
            throws PersistenceException {
        Path sourcePath = resolveSavePath(savesDirectory, saveName);
        return gameStateRepository.load(sourcePath);
    }

    /**
     * Ellenorzi a mentesi nevet es eloallitja a biztonsagos JSON fajlutvonalat.
     *
     * @param savesDirectory a mentesek mappaja
     * @param saveName a megadott mentesi nev
     * @return a feloldott fajlutvonal
     */
    public Path resolveSavePath(final Path savesDirectory, final String saveName) {
        Objects.requireNonNull(savesDirectory, "A mentesi mappa nem lehet null.");
        if (saveName == null || saveName.isBlank()) {
            throw new IllegalArgumentException("A mentes neve nem lehet ures.");
        }
        String trimmed = saveName.trim();
        if (trimmed.contains("/") || trimmed.contains("\\") || trimmed.contains("..")) {
            throw new IllegalArgumentException(
                    "A mentes neve nem tartalmazhat utvonal-elvalasztot: " + saveName);
        }
        String fileName = trimmed.endsWith(JSON_EXTENSION)
                ? trimmed
                : trimmed + JSON_EXTENSION;
        return savesDirectory.resolve(fileName);
    }

    /**
     * Rogziti egy befejezett ketjatekosos jatek gyoztesenek eredmenyet a toplistaban.
     *
     * @param completedState a lezart jatekallapot
     * @return a letrehozott es elmentett ScoreEntry
     * @throws PersistenceException adatbazis-hiba eseten
     */
    public ScoreEntry recordCompletedGame(final GameState completedState)
            throws PersistenceException {
        Objects.requireNonNull(completedState, "A jatekallapot nem lehet null.");
        String winnerName = completedState.getWinnerName().orElseThrow(
                () -> new IllegalArgumentException(
                        "Csak megnyert jatek eredmenye rogzitheto a toplistaban."));
        ScoreEntry entry = new ScoreEntry(
                winnerName,
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
