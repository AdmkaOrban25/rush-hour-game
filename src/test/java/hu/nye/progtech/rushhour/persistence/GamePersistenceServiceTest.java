package hu.nye.progtech.rushhour.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import hu.nye.progtech.rushhour.model.Board;
import hu.nye.progtech.rushhour.model.GameState;
import hu.nye.progtech.rushhour.model.Orientation;
import hu.nye.progtech.rushhour.model.PlayerTurn;
import hu.nye.progtech.rushhour.model.Position;
import hu.nye.progtech.rushhour.model.Vehicle;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * A GamePersistenceService Mockito alapu egysegtesztjei.
 */
@ExtendWith(MockitoExtension.class)
class GamePersistenceServiceTest {

    @Mock
    private GameStateRepository gameStateRepository;

    @Mock
    private ScoreRepository scoreRepository;

    private GamePersistenceService persistenceService;

    @BeforeEach
    void setUp() {
        persistenceService = new GamePersistenceService(gameStateRepository, scoreRepository);
    }

    @Test
    void testSaveAndLoadByNameAppendsJsonExtension() throws PersistenceException {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .addVehicle(new Vehicle('B', new Position(0, 3), 2, Orientation.VERTICAL, true))
                .build();
        GameState state = new GameState(
                "Adam", "Laci", "Kezdo keresztezodes", board, 3, PlayerTurn.PLAYER_TWO);
        Path savesDir = Path.of("saves");
        Path expectedPath = savesDir.resolve("esti-meccs.json");

        when(gameStateRepository.load(expectedPath)).thenReturn(state);

        Path actualPath = persistenceService.saveGameByName(state, savesDir, "esti-meccs");
        GameState loaded = persistenceService.loadGameByName(savesDir, "esti-meccs.json");

        assertEquals(expectedPath, actualPath);
        assertEquals(state, loaded);
        verify(gameStateRepository).save(state, expectedPath);
        verify(gameStateRepository).load(expectedPath);
    }

    @Test
    void testResolveSavePathRejectsBlankOrPathTraversalNames() {
        Path savesDir = Path.of("saves");

        assertThrows(
                IllegalArgumentException.class,
                () -> persistenceService.resolveSavePath(savesDir, "   "));
        assertThrows(
                IllegalArgumentException.class,
                () -> persistenceService.resolveSavePath(savesDir, "../titkos"));
        assertThrows(
                IllegalArgumentException.class,
                () -> persistenceService.resolveSavePath(savesDir, "sub/file"));
    }

    @Test
    void testRecordCompletedGameSavesActualWinnerPlayerTwo() throws PersistenceException {
        Board wonByB = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .addExitedTarget('B')
                .build();
        GameState completedState = new GameState(
                "Adam", "Laci", "Kezdo keresztezodes", wonByB, 4, PlayerTurn.PLAYER_TWO);

        ScoreEntry savedEntry = persistenceService.recordCompletedGame(completedState);

        assertEquals("Laci", savedEntry.getPlayerName());
        assertEquals("Kezdo keresztezodes", savedEntry.getLevelName());
        assertEquals(4, savedEntry.getMoveCount());
        verify(scoreRepository).saveScore(any(ScoreEntry.class));
    }

    @Test
    void testRecordCompletedGameRejectsUnsolvedGame() {
        Board activeBoard = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .addVehicle(new Vehicle('B', new Position(0, 3), 2, Orientation.VERTICAL, true))
                .build();
        GameState unsolvedState = new GameState(
                "Adam", "Laci", "Kezdo keresztezodes", activeBoard, 3, PlayerTurn.PLAYER_ONE);

        assertThrows(
                IllegalArgumentException.class,
                () -> persistenceService.recordCompletedGame(unsolvedState));
        verifyNoInteractions(scoreRepository);
    }

    @Test
    void testGetPlayerRankingsDelegatesToScoreRepository() throws PersistenceException {
        List<PlayerRanking> expected = List.of(new PlayerRanking("Laci", 3, 4));
        when(scoreRepository.findPlayerRankings(5)).thenReturn(expected);

        List<PlayerRanking> actual = persistenceService.getPlayerRankings(5);

        assertEquals(expected, actual);
        verify(scoreRepository).findPlayerRankings(5);
    }
}
