package hu.nye.progtech.rushhour.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hu.nye.progtech.rushhour.model.Board;
import hu.nye.progtech.rushhour.model.Vehicle;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * A LevelFactory es a beepitett N x M palyak egysegtesztjei.
 */
class LevelFactoryTest {

    @Test
    void testAllBuiltInLevelsAreValidAndHaveTwoTargets() {
        LevelFactory factory = new LevelFactory();
        List<LevelDefinition> levels = factory.createAllLevels();

        assertEquals(3, levels.size());
        for (LevelDefinition level : levels) {
            Board board = level.getInitialBoard();
            long targetCount = board.getVehicles().values().stream()
                    .filter(Vehicle::isTarget)
                    .count();
            assertEquals(2, targetCount, "Minden palyan 2 celjarmunek kell lennie.");
            assertTrue(board.getVehicle('A').isPresent());
            assertTrue(board.getVehicle('B').isPresent());
        }
    }

    @Test
    void testLevelsIncludeRectangularBoards() {
        LevelFactory factory = new LevelFactory();

        Board level1 = factory.createLevel(1).getInitialBoard();
        Board level3 = factory.createLevel(3).getInitialBoard();

        assertEquals(5, level1.getRows());
        assertEquals(6, level1.getCols());
        assertEquals(6, level3.getRows());
        assertEquals(7, level3.getCols());
    }

    @Test
    void testInvalidLevelIdThrowsIllegalArgumentException() {
        LevelFactory factory = new LevelFactory();

        assertThrows(IllegalArgumentException.class, () -> factory.createLevel(99));
    }
}
