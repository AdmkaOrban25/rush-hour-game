package hu.nye.progtech.rushhour.ui;

import hu.nye.progtech.rushhour.model.Direction;
import hu.nye.progtech.rushhour.service.Move;
import java.util.Locale;

/**
 * A felhasznalo konzolos parancssorait strukturalt GameCommand objektumokka alakito osztaly.
 */
public final class CommandParser {

    private static final int MOVE_PARTS_WITHOUT_STEPS = 2;
    private static final int MOVE_PARTS_WITH_STEPS = 3;
    private static final int EXPECTED_ARG_PARTS = 2;
    private static final int DEFAULT_STEP_COUNT = 1;

    /**
     * Ertelmez egy nyers konzolos bemeneti sort.
     * Tamogatott formatumok:
     * {@code A jobbra 2}, {@code move A right 2}, {@code A j},
     * {@code save nev}, {@code load nev}, {@code moves}, {@code scores},
     * {@code help}, {@code exit}.
     *
     * @param rawInput a felhasznalo altal begepelt sor
     * @return az ertelmezett GameCommand
     * @throws IllegalArgumentException ha a parancs ures vagy ervenytelen
     */
    public GameCommand parse(final String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            throw new IllegalArgumentException(
                    "Ures parancs. Ird be a 'help' parancsot a hasznalhato parancsokhoz.");
        }

        String[] parts = rawInput.trim().split("\\s+");
        String keyword = parts[0].toLowerCase(Locale.ROOT);

        return switch (keyword) {
            case "help", "sugo", "?" -> GameCommand.simple(GameCommand.CommandType.HELP);
            case "exit", "quit", "kilepes", "q" -> GameCommand.simple(GameCommand.CommandType.EXIT);
            case "scores", "toplista", "rangsor" ->
                    GameCommand.simple(GameCommand.CommandType.SCORES);
            case "moves", "lepesek", "tipp" -> GameCommand.simple(GameCommand.CommandType.MOVES);
            case "save", "mentes" -> parseSaveCommand(parts);
            case "load", "betoltes" -> parseLoadCommand(parts);
            case "move", "lep" -> parseMoveTokens(parts, 1);
            default -> parseMoveTokens(parts, 0);
        };
    }

    private GameCommand parseSaveCommand(final String[] parts) {
        if (parts.length != EXPECTED_ARG_PARTS) {
            throw new IllegalArgumentException(
                    "Hibas mentes parancs. Hasznalat: save <mentes_neve>");
        }
        return GameCommand.save(parts[1]);
    }

    private GameCommand parseLoadCommand(final String[] parts) {
        if (parts.length != EXPECTED_ARG_PARTS) {
            throw new IllegalArgumentException(
                    "Hibas betoltes parancs. Hasznalat: load <mentes_neve>");
        }
        return GameCommand.load(parts[1]);
    }

    private GameCommand parseMoveTokens(final String[] parts, final int startIndex) {
        int tokenCount = parts.length - startIndex;
        if (tokenCount != MOVE_PARTS_WITHOUT_STEPS && tokenCount != MOVE_PARTS_WITH_STEPS) {
            throw new IllegalArgumentException(
                    "Ismeretlen vagy hibas parancs. Lepes pelda: 'A jobbra 2' vagy 'help'.");
        }

        String vehicleToken = parts[startIndex];
        if (vehicleToken.length() != 1 || !Character.isLetterOrDigit(vehicleToken.charAt(0))) {
            throw new IllegalArgumentException(
                    "Ervenytelen jarmu azonosito: '" + vehicleToken
                            + "'. Egyetlen betut vagy szamot adj meg (pl. A).");
        }
        char vehicleId = Character.toUpperCase(vehicleToken.charAt(0));

        String directionToken = parts[startIndex + 1];
        Direction direction = Direction.fromString(directionToken)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ismeretlen irany: '" + directionToken
                                + "'. Hasznalhato: fel/le/balra/jobbra (vagy u/d/b/j)."));

        int steps = DEFAULT_STEP_COUNT;
        if (tokenCount == MOVE_PARTS_WITH_STEPS) {
            steps = parsePositiveSteps(parts[startIndex + 2]);
        }

        return GameCommand.move(new Move(vehicleId, direction, steps));
    }

    private int parsePositiveSteps(final String stepToken) {
        try {
            int steps = Integer.parseInt(stepToken);
            if (steps <= 0) {
                throw new IllegalArgumentException(
                        "A lepesszamnak pozitiv egesz szamnak kell lennie: " + stepToken);
            }
            return steps;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Ervenytelen lepesszam: '" + stepToken + "'. Pozitiv szamot adj meg.", ex);
        }
    }
}
