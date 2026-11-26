package hu.nye.progtech.rushhour.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Beagyazott SQLite JDBC adatbazist hasznalo toplista tarolo implementacio.
 */
public final class SqliteScoreRepository implements ScoreRepository {

    /** Az alapertelmezett helyi SQLite adatbazis JDBC URL-je. */
    public static final String DEFAULT_JDBC_URL = "jdbc:sqlite:rush-hour-scores.db";

    private static final Logger LOGGER = LoggerFactory.getLogger(SqliteScoreRepository.class);

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS scores ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "player_name TEXT NOT NULL, "
                    + "level_name TEXT NOT NULL, "
                    + "move_count INTEGER NOT NULL, "
                    + "played_at TEXT NOT NULL)";

    private static final String INSERT_SCORE_SQL =
            "INSERT INTO scores (player_name, level_name, move_count, played_at) "
                    + "VALUES (?, ?, ?, ?)";

    private static final String SELECT_TOP_SCORES_SQL =
            "SELECT player_name, level_name, move_count, played_at "
                    + "FROM scores "
                    + "ORDER BY move_count ASC, played_at ASC "
                    + "LIMIT ?";

    private static final String SELECT_TOP_BY_LEVEL_SQL =
            "SELECT player_name, level_name, move_count, played_at "
                    + "FROM scores "
                    + "WHERE level_name = ? "
                    + "ORDER BY move_count ASC, played_at ASC "
                    + "LIMIT ?";

    private static final String SELECT_RANKINGS_SQL =
            "SELECT player_name, COUNT(*) AS wins, MIN(move_count) AS best_moves "
                    + "FROM scores "
                    + "GROUP BY player_name "
                    + "ORDER BY wins DESC, best_moves ASC, player_name ASC "
                    + "LIMIT ?";

    private final String jdbcUrl;

    /**
     * Alapertelmezett helyi SQLite fajlt hasznalo tarolo letrehozasa.
     *
     * @throws PersistenceException ha az adatbazistabla inicializalasa sikertelen
     */
    public SqliteScoreRepository() throws PersistenceException {
        this(DEFAULT_JDBC_URL);
    }

    /**
     * Egyedi JDBC URL-t hasznalo SQLite tarolo letrehozasa es inicializalasa.
     *
     * @param jdbcUrl a SQLite kapcsolat JDBC URL-je
     * @throws PersistenceException ha az adatbazistabla inicializalasa sikertelen
     */
    public SqliteScoreRepository(final String jdbcUrl) throws PersistenceException {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalArgumentException("A JDBC URL nem lehet ures.");
        }
        this.jdbcUrl = jdbcUrl;
        initializeSchema();
    }

    private void initializeSchema() throws PersistenceException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
            LOGGER.debug("SQLite scores tabla sikeresen inicializalva: {}", jdbcUrl);
        } catch (SQLException ex) {
            throw new PersistenceException(
                    "Nem sikerult inicializalni az SQLite adatbazist: " + jdbcUrl, ex);
        }
    }

    @Override
    public void saveScore(final ScoreEntry entry) throws PersistenceException {
        Objects.requireNonNull(entry, "Az eredmenybejegyzes nem lehet null.");
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(INSERT_SCORE_SQL)) {
            statement.setString(1, entry.getPlayerName());
            statement.setString(2, entry.getLevelName());
            statement.setInt(3, entry.getMoveCount());
            statement.setString(4, entry.getPlayedAt().toString());
            statement.executeUpdate();
            LOGGER.info("Eredmeny mentve SQLite adatbazisba: {} ({}, {} lepes)",
                    entry.getPlayerName(), entry.getLevelName(), entry.getMoveCount());
        } catch (SQLException ex) {
            throw new PersistenceException("Nem sikerult elmenteni az eredmenyt.", ex);
        }
    }

    @Override
    public List<ScoreEntry> findTopScores(final int limit) throws PersistenceException {
        validateLimit(limit);
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(SELECT_TOP_SCORES_SQL)) {
            statement.setInt(1, limit);
            return readScoreEntries(statement);
        } catch (SQLException ex) {
            throw new PersistenceException("Nem sikerult lekerdezni a legjobb eredmenyeket.", ex);
        }
    }

    @Override
    public List<ScoreEntry> findTopScoresByLevel(final String levelName, final int limit)
            throws PersistenceException {
        if (levelName == null || levelName.isBlank()) {
            throw new IllegalArgumentException("A palya neve nem lehet ures.");
        }
        validateLimit(limit);
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(SELECT_TOP_BY_LEVEL_SQL)) {
            statement.setString(1, levelName.trim());
            statement.setInt(2, limit);
            return readScoreEntries(statement);
        } catch (SQLException ex) {
            throw new PersistenceException(
                    "Nem sikerult lekerdezni a palya eredmenyeit: " + levelName, ex);
        }
    }

    @Override
    public List<PlayerRanking> findPlayerRankings(final int limit) throws PersistenceException {
        validateLimit(limit);
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement statement = connection.prepareStatement(SELECT_RANKINGS_SQL)) {
            statement.setInt(1, limit);
            List<PlayerRanking> rankings = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rankings.add(new PlayerRanking(
                            rs.getString("player_name"),
                            rs.getInt("wins"),
                            rs.getInt("best_moves")));
                }
            }
            return Collections.unmodifiableList(rankings);
        } catch (SQLException ex) {
            throw new PersistenceException("Nem sikerult lekerdezni a rangsort.", ex);
        }
    }

    private List<ScoreEntry> readScoreEntries(final PreparedStatement statement)
            throws SQLException {
        List<ScoreEntry> results = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                results.add(new ScoreEntry(
                        rs.getString("player_name"),
                        rs.getString("level_name"),
                        rs.getInt("move_count"),
                        LocalDateTime.parse(rs.getString("played_at"))));
            }
        }
        return Collections.unmodifiableList(results);
    }

    private void validateLimit(final int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("A limit ertekenek pozitivnak kell lennie.");
        }
    }
}
