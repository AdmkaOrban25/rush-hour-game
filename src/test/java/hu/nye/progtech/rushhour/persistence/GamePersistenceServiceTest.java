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
    void testSaveAndLoadDelegateToGameStateRepository() throws PersistenceException {
        Board board = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .build();
        GameState state = new GameState("Adam", "Kezdo keresztezodes", board, 2);
        Path savePath = Path.of("save.json");

        when(gameStateRepository.load(savePath)).thenReturn(state);

        persistenceService.saveGame(state, savePath);
        GameState loaded = persistenceService.loadGame(savePath);

        verify(gameStateRepository).save(state, savePath);
        verify(gameStateRepository).load(savePath);
        assertEquals(state, loaded);
    }

    @Test
    void testRecordCompletedGamePersistsScoreEntryWhenSolved() throws PersistenceException {
        Board solvedBoard = Board.builder(5, 6)
                .addVehicle(new Vehicle('C', new Position(0, 0), 2, Orientation.HORIZONTAL, false))
                .addExitedTarget('A')
                .addExitedTarget('B')
                .build();
        GameState solvedState = new GameState("Adam", "Kezdo keresztezodes", solvedBoard, 5);

        ScoreEntry savedEntry = persistenceService.recordCompletedGame(solvedState);

        assertEquals("Adam", savedEntry.getPlayerName());
        assertEquals("Kezdo keresztezodes", savedEntry.getLevelName());
        assertEquals(5, savedEntry.getMoveCount());
        verify(scoreRepository).saveScore(any(ScoreEntry.class));
    }

    @Test
    void testRecordCompletedGameRejectsUnsolvedGame() {
        Board activeBoard = Board.builder(5, 6)
                .addVehicle(new Vehicle('A', new Position(2, 0), 2, Orientation.HORIZONTAL, true))
                .build();
        GameState unsolvedState = new GameState("Adam", "Kezdo keresztezodes", activeBoard, 3);

        assertThrows(
                IllegalArgumentException.class,
                () -> persistenceService.recordCompletedGame(unsolvedState));
        verifyNoInteractions(scoreRepository);
    }

    @Test
    void testGetPlayerRankingsDelegatesToScoreRepository() throws PersistenceException {
        List<PlayerRanking> expected = List.of(new PlayerRanking("Adam", 3, 5));
        when(scoreRepository.findPlayerRankings(5)).thenReturn(expected);

        List<PlayerRanking> actual = persistenceService.getPlayerRankings(5);

        assertEquals(expected, actual);
        verify(scoreRepository).findPlayerRankings(5);
    }
}
