package hu.nye.progtech.rushhour.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hu.nye.progtech.rushhour.model.Direction;
import hu.nye.progtech.rushhour.service.Move;
import hu.nye.progtech.rushhour.ui.GameCommand.CommandType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A CommandParser es GameCommand egysegtesztjei.
 */
class CommandParserTest {

    private CommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }

    @Test
    void testParseShortAndExplicitMoveCommands() {
        GameCommand shortCmd = parser.parse("a jobbra 2");
        assertEquals(CommandType.MOVE, shortCmd.getType());
        assertEquals(new Move('A', Direction.RIGHT, 2), shortCmd.getMove().orElseThrow());

        GameCommand explicitCmd = parser.parse("move B down 3");
        assertEquals(CommandType.MOVE, explicitCmd.getType());
        assertEquals(new Move('B', Direction.DOWN, 3), explicitCmd.getMove().orElseThrow());

        GameCommand defaultStepCmd = parser.parse("C fel");
        assertEquals(CommandType.MOVE, defaultStepCmd.getType());
        assertEquals(new Move('C', Direction.UP, 1), defaultStepCmd.getMove().orElseThrow());
    }

    @Test
    void testParseSaveAndLoadCommandsWithName() {
        GameCommand saveCmd = parser.parse("save esti-meccs");
        assertEquals(CommandType.SAVE, saveCmd.getType());
        assertEquals("esti-meccs", saveCmd.getArgument().orElseThrow());

        GameCommand loadCmd = parser.parse("betoltes esti-meccs");
        assertEquals(CommandType.LOAD, loadCmd.getType());
        assertEquals("esti-meccs", loadCmd.getArgument().orElseThrow());
    }

    @Test
    void testParseSimpleCommands() {
        assertEquals(CommandType.HELP, parser.parse("help").getType());
        assertEquals(CommandType.HELP, parser.parse("sugo").getType());
        assertEquals(CommandType.EXIT, parser.parse("exit").getType());
        assertEquals(CommandType.EXIT, parser.parse("kilepes").getType());
        assertEquals(CommandType.SCORES, parser.parse("toplista").getType());
        assertEquals(CommandType.MOVES, parser.parse("lepesek").getType());
    }

    @Test
    void testInvalidInputsThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("   "));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("save"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("load"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("AB jobbra 1"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("A atlosan 1"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("A jobbra 0"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("A jobbra ketto"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("valami ismeretlen"));
    }

    @Test
    void testGameCommandEqualityAndValidation() {
        GameCommand first = GameCommand.save("meccs1");
        GameCommand second = GameCommand.save("meccs1");
        GameCommand different = GameCommand.load("meccs1");

        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
        assertNotEquals(first, "not-a-command");
        assertTrue(first.toString().contains("SAVE"));

        assertThrows(IllegalArgumentException.class, () -> GameCommand.save(" "));
        assertThrows(IllegalArgumentException.class, () -> GameCommand.load(""));
        assertThrows(IllegalArgumentException.class, () -> GameCommand.simple(CommandType.MOVE));
        assertThrows(IllegalArgumentException.class, () -> GameCommand.simple(CommandType.SAVE));
        assertThrows(IllegalArgumentException.class, () -> GameCommand.simple(CommandType.LOAD));
    }
}
