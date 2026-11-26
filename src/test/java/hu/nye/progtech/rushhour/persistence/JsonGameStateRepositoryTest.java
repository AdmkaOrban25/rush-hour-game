package hu.nye.progtech.rushhour.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hu.nye.progtech.rushhour.model.Board;
import hu.nye.progtech.rushhour.model.GameState;
import hu.nye.progtech.rushhour.model.Orientation;
import hu.nye.progtech.rushhour.model.Position;
import hu.nye.progtech.rushhour.model.Vehicle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A JsonGameStateRepository egysegtesztjei.
 */
class JsonGameStateRepositoryTest {

    private JsonGameStateRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JsonGameStateRepository();
    }

    @Test
    void testSaveAndLoadRoundTripPreservesFullGameState(@TempDir final Path tempDir)
            throws PersistenceException {
        Board board = Board.builder(5, 7)
                .addVehicle(new Vehicle('A', new Position(2, 1), 2, Orientation.HORIZONTAL, true))
                .addVehicle(new Vehicle('C', new Position(0, 4), 3, Orientation.VERTICAL, false))
                .addExitedTarget('B')
                .build();
        GameState original = new GameState("Adam", "Kezdo keresztezodes", board, 4);
        Path saveFile = tempDir.resolve("saves").resolve("game.json");

        repository.save(original, saveFile);
        assertTrue(Files.exists(saveFile));

        GameState loaded = repository.load(saveFile);

        assertEquals(original, loaded);
        assertEquals(5, loaded.getBoard().getRows());
        assertEquals(7, loaded.getBoard().getCols());
        assertTrue(loaded.getBoard().getExitedTargets().contains('B'));
    }

    @Test
    void testLoadNonExistentFileThrowsPersistenceException(@TempDir final Path tempDir) {
        Path missingFile = tempDir.resolve("nincs-ilyen.json");

        assertThrows(PersistenceException.class, () -> repository.load(missingFile));
    }

    @Test
    void testLoadCorruptedJsonThrowsPersistenceException(@TempDir final Path tempDir)
            throws IOException {
        Path brokenFile = tempDir.resolve("broken.json");
        Files.writeString(brokenFile, "{ ez nem ervenyes json");

        assertThrows(PersistenceException.class, () -> repository.load(brokenFile));
    }
}
