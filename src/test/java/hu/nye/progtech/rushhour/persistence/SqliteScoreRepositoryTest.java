package hu.nye.progtech.rushhour.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A SqliteScoreRepository integracios egysegtesztjei valos ideiglenes SQLite fajllal.
 */
class SqliteScoreRepositoryTest {

    private SqliteScoreRepository createRepository(final Path tempDir)
            throws PersistenceException {
        Path dbFile = tempDir.resolve("test-rush-hour.db");
        return new SqliteScoreRepository("jdbc:sqlite:" + dbFile.toAbsolutePath());
    }

    @Test
    void testSaveAndFindTopScoresOrderedByMoveCount(@TempDir final Path tempDir)
            throws PersistenceException {
        SqliteScoreRepository repository = createRepository(tempDir);
        LocalDateTime base = LocalDateTime.of(2026, 11, 26, 10, 0);
        repository.saveScore(new ScoreEntry("Bela", "Kezdo keresztezodes", 9, base));
        repository.saveScore(new ScoreEntry("Adam", "Kezdo keresztezodes", 5, base.plusMinutes(1)));
        repository.saveScore(new ScoreEntry("Cecil", "Nagy korut", 12, base.plusMinutes(2)));

        List<ScoreEntry> topTwo = repository.findTopScores(2);

        assertEquals(2, topTwo.size());
        assertEquals("Adam", topTwo.get(0).getPlayerName());
        assertEquals(5, topTwo.get(0).getMoveCount());
        assertEquals("Bela", topTwo.get(1).getPlayerName());
        assertEquals(9, topTwo.get(1).getMoveCount());
    }

    @Test
    void testFindTopScoresByLevelFiltersCorrectly(@TempDir final Path tempDir)
            throws PersistenceException {
        SqliteScoreRepository repository = createRepository(tempDir);
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 11, 0);
        repository.saveScore(new ScoreEntry("Adam", "Kezdo keresztezodes", 5, now));
        repository.saveScore(new ScoreEntry("Laci", "Nagy korut", 11, now.plusMinutes(1)));
        repository.saveScore(new ScoreEntry("Adam", "Nagy korut", 10, now.plusMinutes(2)));

        List<ScoreEntry> levelScores = repository.findTopScoresByLevel("Nagy korut", 5);

        assertEquals(2, levelScores.size());
        assertEquals("Adam", levelScores.get(0).getPlayerName());
        assertEquals(10, levelScores.get(0).getMoveCount());
        assertEquals("Laci", levelScores.get(1).getPlayerName());
    }

    @Test
    void testFindPlayerRankingsAggregatesWinsAndBestMoves(@TempDir final Path tempDir)
            throws PersistenceException {
        SqliteScoreRepository repository = createRepository(tempDir);
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 12, 0);
        repository.saveScore(new ScoreEntry("Adam", "Kezdo keresztezodes", 6, now));
        repository.saveScore(new ScoreEntry("Adam", "Nagy korut", 10, now.plusMinutes(1)));
        repository.saveScore(new ScoreEntry("Laci", "Kezdo keresztezodes", 5, now.plusMinutes(2)));

        List<PlayerRanking> rankings = repository.findPlayerRankings(10);

        assertEquals(2, rankings.size());
        assertEquals(new PlayerRanking("Adam", 2, 6), rankings.get(0));
        assertEquals(new PlayerRanking("Laci", 1, 5), rankings.get(1));
    }

    @Test
    void testNonPositiveLimitThrowsIllegalArgumentException(@TempDir final Path tempDir)
            throws PersistenceException {
        SqliteScoreRepository repository = createRepository(tempDir);

        assertThrows(IllegalArgumentException.class, () -> repository.findTopScores(0));
        assertThrows(IllegalArgumentException.class, () -> repository.findPlayerRankings(-1));
    }
}
