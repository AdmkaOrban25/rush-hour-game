package hu.nye.progtech.rushhour.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hu.nye.progtech.rushhour.service.LevelDefinition;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A JsonLevelLoader egysegtesztjei.
 */
class JsonLevelLoaderTest {

    private JsonLevelLoader loader;

    @BeforeEach
    void setUp() {
        loader = new JsonLevelLoader();
    }

    @Test
    void testLoadDefaultLevelsLoadsAllThreeJsonLevels() throws PersistenceException {
        List<LevelDefinition> levels = loader.loadDefaultLevels();

        assertEquals(3, levels.size());
        assertEquals("Kezdo keresztezodes", levels.get(0).getName());
        assertEquals(5, levels.get(0).getInitialBoard().getRows());
        assertEquals(6, levels.get(0).getInitialBoard().getCols());
        assertTrue(levels.get(0).getInitialBoard().getVehicle('A').isPresent());
        assertTrue(levels.get(0).getInitialBoard().getVehicle('B').isPresent());

        assertEquals("Belvarosi csucsforgalom", levels.get(1).getName());
        assertEquals("Nagy korut", levels.get(2).getName());
        assertEquals(7, levels.get(2).getInitialBoard().getCols());
    }

    @Test
    void testLoadFromExternalFileReadsValidCustomLevel(@TempDir final Path tempDir)
            throws IOException, PersistenceException {
        Path customFile = tempDir.resolve("custom-level.json");
        String json = """
                {
                  "id": 4,
                  "name": "Sajat tesztpalya",
                  "difficulty": "Egyedi",
                  "rows": 5,
                  "cols": 5,
                  "vehicles": [
                    {
                      "id": "A",
                      "row": 2,
                      "col": 0,
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
        Files.writeString(customFile, json);

        LevelDefinition loaded = loader.loadFromFile(customFile);

        assertEquals(4, loaded.getId());
        assertEquals("Sajat tesztpalya", loaded.getName());
        assertEquals("Egyedi", loaded.getDifficulty());
        assertEquals(5, loaded.getInitialBoard().getRows());
        assertEquals(5, loaded.getInitialBoard().getCols());
        assertEquals(2, loaded.getInitialBoard().getVehicles().size());
    }

    @Test
    void testLoadFromFileRejectsOneByOneVehicle(@TempDir final Path tempDir) throws IOException {
        Path invalidFile = tempDir.resolve("invalid-vehicle.json");
        String json = """
                {
                  "id": 9,
                  "name": "Hibas palya",
                  "difficulty": "Teszt",
                  "rows": 5,
                  "cols": 5,
                  "vehicles": [
                    {
                      "id": "A",
                      "row": 2,
                      "col": 0,
                      "length": 1,
                      "orientation": "HORIZONTAL",
                      "target": true
                    }
                  ]
                }
                """;
        Files.writeString(invalidFile, json);

        assertThrows(PersistenceException.class, () -> loader.loadFromFile(invalidFile));
    }

    @Test
    void testLoadMissingFileOrResourceThrowsPersistenceException(@TempDir final Path tempDir) {
        Path missing = tempDir.resolve("nem-letezik.json");

        assertThrows(PersistenceException.class, () -> loader.loadFromFile(missing));
        assertThrows(
                PersistenceException.class,
                () -> loader.loadFromResource("/levels/nincs-ilyen.json"));
    }
}
