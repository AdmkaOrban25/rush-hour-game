package hu.nye.progtech.rushhour.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hu.nye.progtech.rushhour.persistence.GamePersistenceService;
import hu.nye.progtech.rushhour.persistence.JsonGameStateRepository;
import hu.nye.progtech.rushhour.persistence.JsonLevelLoader;
import hu.nye.progtech.rushhour.persistence.PersistenceException;
import hu.nye.progtech.rushhour.persistence.ScoreEntry;
import hu.nye.progtech.rushhour.persistence.SqliteScoreRepository;
import hu.nye.progtech.rushhour.service.GameService;
import hu.nye.progtech.rushhour.service.StandardMoveValidator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A ConsoleGameController vegponttol vegpontig tarto konzolos munkamenet-tesztjei.
 */
class ConsoleGameControllerTest {

    @Test
    void testTwoPlayerSessionWithSaveLoadAndVictory(@TempDir final Path tempDir)
            throws PersistenceException {
        Path dbFile = tempDir.resolve("test-scores.db");
        Path savesDir = tempDir.resolve("saves");
        SqliteScoreRepository scoreRepo = new SqliteScoreRepository("jdbc:sqlite:" + dbFile);
        GamePersistenceService persistenceService = new GamePersistenceService(
                new JsonGameStateRepository(), scoreRepo);

        String script = String.join(System.lineSeparator(),
                "4",
                "1",
                "1",
                "Adam",
                "Laci",
                "moves",
                "scores",
                "C fel 1",
                "save koztes-allas",
                "A jobbra 1",
                "B le 1",
                "load koztes-allas",
                "D balra 1",
                "E fel 1",
                "B le 3",
                "4",
                "5") + System.lineSeparator();

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ConsoleGameController controller = new ConsoleGameController(
                new GameService(new StandardMoveValidator()),
                persistenceService,
                new JsonLevelLoader(),
                savesDir,
                new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(outputBuffer, true, StandardCharsets.UTF_8));

        controller.run();

        String consoleOutput = outputBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(consoleOutput.contains("Meg nincs rogzitett eredmeny."));
        assertTrue(consoleOutput.contains("Jatekallapot elmentve:"));
        assertTrue(consoleOutput.contains("Szabalytalan lepes:"));
        assertTrue(consoleOutput.contains("Jatekallapot betoltve! Lepesen: Laci"));
        assertTrue(consoleOutput.contains("GYOZTES: Laci!"));
        assertTrue(consoleOutput.contains("Gratulalunk, Laci!"));
        assertTrue(consoleOutput.contains("1. Laci - Kezdo keresztezodes (4 lepes"));
        assertTrue(consoleOutput.contains("Kilepes a jatekbol. Viszlat!"));

        List<ScoreEntry> scores = persistenceService.getTopScores(5);
        assertEquals(1, scores.size());
        assertEquals("Laci", scores.get(0).getPlayerName());
        assertEquals(4, scores.get(0).getMoveCount());
    }

    @Test
    void testStartFromExternalJsonLevelFileAndExitMatch(@TempDir final Path tempDir)
            throws IOException, PersistenceException {
        Path dbFile = tempDir.resolve("external-scores.db");
        Path savesDir = tempDir.resolve("saves");
        Path customLevel = tempDir.resolve("custom.json");
        String json = """
                {
                  "id": 9,
                  "name": "Kulso minipalya",
                  "difficulty": "Gyors",
                  "rows": 5,
                  "cols": 5,
                  "vehicles": [
                    {
                      "id": "A",
                      "row": 2,
                      "col": 1,
                      "length": 2,
                      "orientation": "HORIZONTAL",
                      "target": true
                    },
                    {
                      "id": "B",
                      "row": 0,
                      "col": 3,
                      "length": 2,
                      "orientation": "VERTICAL",
                      "target": true
                    }
                  ]
                }
                """;
        Files.writeString(customLevel, json);

        SqliteScoreRepository scoreRepo = new SqliteScoreRepository("jdbc:sqlite:" + dbFile);
        GamePersistenceService persistenceService = new GamePersistenceService(
                new JsonGameStateRepository(), scoreRepo);

        String script = String.join(System.lineSeparator(),
                "99",
                "1",
                "abc",
                "1",
                "99",
                "2",
                "",
                "2",
                "nincs-ilyen.json",
                "2",
                customLevel.toString(),
                "Adam",
                "Bela",
                "help",
                "hibas parancs",
                "load nem-letezik",
                "save ../tilos",
                "exit",
                "5") + System.lineSeparator();

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ConsoleGameController controller = new ConsoleGameController(
                new GameService(new StandardMoveValidator()),
                persistenceService,
                new JsonLevelLoader(),
                savesDir,
                new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(outputBuffer, true, StandardCharsets.UTF_8));

        controller.run();

        String consoleOutput = outputBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(consoleOutput.contains("Ervenytelen valasztas."));
        assertTrue(consoleOutput.contains("Ervenytelen sorszam."));
        assertTrue(consoleOutput.contains("Nincs ilyen sorszamu palya."));
        assertTrue(consoleOutput.contains("Nem adtal meg fajlutvonalat."));
        assertTrue(consoleOutput.contains("Hiba a kulso palyafajl betoltesekor:"));
        assertTrue(consoleOutput.contains("Palya sikeresen beolvasva: 9. Kulso minipalya"));
        assertTrue(consoleOutput.contains("Betoltes sikertelen:"));
        assertTrue(consoleOutput.contains("Mentes sikertelen:"));
        assertTrue(consoleOutput.contains("Aktualis meccs megszakitva"));
        assertTrue(consoleOutput.contains("Kilepes a jatekbol. Viszlat!"));
    }

    @Test
    void testResumeSavedGameFromMainMenuAndHandleMissingSave(@TempDir final Path tempDir)
            throws PersistenceException {
        Path dbFile = tempDir.resolve("resume-scores.db");
        Path savesDir = tempDir.resolve("saves");
        SqliteScoreRepository scoreRepo = new SqliteScoreRepository("jdbc:sqlite:" + dbFile);
        GamePersistenceService persistenceService = new GamePersistenceService(
                new JsonGameStateRepository(), scoreRepo);

        String script = String.join(System.lineSeparator(),
                "3",
                "",
                "3",
                "nem-letezo-mentes",
                "1",
                "1",
                "Adam",
                "Laci",
                "C fel 1",
                "save folytathato",
                "exit",
                "3",
                "folytathato",
                "exit",
                "5") + System.lineSeparator();

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ConsoleGameController controller = new ConsoleGameController(
                new GameService(new StandardMoveValidator()),
                persistenceService,
                new JsonLevelLoader(),
                savesDir,
                new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(outputBuffer, true, StandardCharsets.UTF_8));

        controller.run();

        String consoleOutput = outputBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(consoleOutput.contains("A mentes neve nem lehet ures."));
        assertTrue(consoleOutput.contains("Nem sikerult betolteni a mentest:"));
        assertTrue(consoleOutput.contains("Mentes betoltve! Folytatas innen (lepesen: Laci)."));
        assertTrue(consoleOutput.contains("Kilepes a jatekbol. Viszlat!"));
    }
}
