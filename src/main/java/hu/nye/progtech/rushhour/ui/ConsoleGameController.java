package hu.nye.progtech.rushhour.ui;

import hu.nye.progtech.rushhour.model.GameState;
import hu.nye.progtech.rushhour.model.PlayerTurn;
import hu.nye.progtech.rushhour.persistence.GamePersistenceService;
import hu.nye.progtech.rushhour.persistence.LevelLoader;
import hu.nye.progtech.rushhour.persistence.PersistenceException;
import hu.nye.progtech.rushhour.persistence.PlayerRanking;
import hu.nye.progtech.rushhour.persistence.ScoreEntry;
import hu.nye.progtech.rushhour.service.GameService;
import hu.nye.progtech.rushhour.service.InvalidMoveException;
import hu.nye.progtech.rushhour.service.LevelDefinition;
import hu.nye.progtech.rushhour.service.Move;
import hu.nye.progtech.rushhour.service.MoveResult;
import hu.nye.progtech.rushhour.ui.GameCommand.CommandType;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A konzolos ketjatekosos Rush Hour alkalmazas teljes interaktiv folyamatat vezerlo osztaly.
 */
public final class ConsoleGameController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleGameController.class);
    private static final int TOP_LIMIT = 10;
    private static final int PREVIEW_MOVES_LIMIT = 12;

    private final GameService gameService;
    private final GamePersistenceService persistenceService;
    private final LevelLoader levelLoader;
    private final BoardRenderer renderer;
    private final CommandParser commandParser;
    private final Path savesDirectory;
    private final Scanner scanner;
    private final PrintStream out;

    /**
     * Uj konzolos vezerlo letrehozasa fuggoseginjektalassal.
     *
     * @param gameService a jateklogikat kezelo szerviz
     * @param persistenceService a menteseket es toplistat kezelo szerviz
     * @param levelLoader a palyakat JSON-bol betolto komponens
     * @param savesDirectory a mentesi fajlok celmappaja
     * @param inputStream a bemeneti adatfolyam
     * @param outputStream a kimeneti adatfolyam
     */
    public ConsoleGameController(
            final GameService gameService,
            final GamePersistenceService persistenceService,
            final LevelLoader levelLoader,
            final Path savesDirectory,
            final InputStream inputStream,
            final PrintStream outputStream) {
        this.gameService = Objects.requireNonNull(gameService, "A GameService nem lehet null.");
        this.persistenceService = Objects.requireNonNull(
                persistenceService, "A GamePersistenceService nem lehet null.");
        this.levelLoader = Objects.requireNonNull(levelLoader, "A LevelLoader nem lehet null.");
        this.renderer = new BoardRenderer();
        this.commandParser = new CommandParser();
        this.savesDirectory = Objects.requireNonNull(
                savesDirectory, "A mentesi mappa nem lehet null.");
        Objects.requireNonNull(inputStream, "A bemeneti folyam nem lehet null.");
        this.out = Objects.requireNonNull(outputStream, "A kimeneti folyam nem lehet null.");
        this.scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * Elinditja a fomenu ciklust, amelybol uj jatek, fajlbetoltes vagy toplista indithato.
     */
    public void run() {
        printWelcomeBanner();
        boolean running = true;

        while (running) {
            printMainMenu();
            Optional<String> choiceOpt = readLine("Valasztas: ");
            if (choiceOpt.isEmpty()) {
                break;
            }
            String choice = choiceOpt.get().trim();
            switch (choice) {
                case "1" -> startFromDefaultLevels();
                case "2" -> startFromExternalLevelFile();
                case "3" -> loadSavedGameAndPlay();
                case "4" -> printLeaderboards();
                case "5", "exit", "quit", "q" -> running = false;
                default -> out.println("Ervenytelen valasztas. Adj meg egy szamot 1 es 5 kozott!");
            }
        }

        out.println("Kilepes a jatekbol. Viszlat!");
    }

    private void printWelcomeBanner() {
        out.println("==================================================");
        out.println("       RUSH HOUR - KETJATEKOSOS KIADAS            ");
        out.println("  1. jatekos: 'A' (jobbra) | 2. jatekos: 'B' (le) ");
        out.println("==================================================");
    }

    private void printMainMenu() {
        out.println();
        out.println("--- FOMENU ---");
        out.println("1. Uj 2-szemelyes jatek (beepitett JSON palyak)");
        out.println("2. Uj 2-szemelyes jatek kulso palyafajlbol (.json)");
        out.println("3. Mentett jatek folytatasa nev alapjan");
        out.println("4. Toplista es jatekosrangsor megtekintese");
        out.println("5. Kilepes");
    }

    private void startFromDefaultLevels() {
        List<LevelDefinition> levels;
        try {
            levels = levelLoader.loadDefaultLevels();
        } catch (PersistenceException ex) {
            LOGGER.error("Nem sikerult betolteni az alapertelmezett palyakat.", ex);
            out.println("Hiba a palyak betoltesekor: " + ex.getMessage());
            return;
        }

        out.println();
        out.println("Elerheto palyak:");
        for (int i = 0; i < levels.size(); i++) {
            out.println("  " + (i + 1) + ". " + levels.get(i));
        }

        Optional<String> selectedOpt = readLine("Valassz palyat (1-" + levels.size() + "): ");
        if (selectedOpt.isEmpty()) {
            return;
        }

        int index;
        try {
            index = Integer.parseInt(selectedOpt.get().trim()) - 1;
        } catch (NumberFormatException ex) {
            out.println("Ervenytelen sorszam.");
            return;
        }
        if (index < 0 || index >= levels.size()) {
            out.println("Nincs ilyen sorszamu palya.");
            return;
        }

        LevelDefinition chosen = levels.get(index);
        promptPlayersAndStart(chosen);
    }

    private void startFromExternalLevelFile() {
        Optional<String> pathOpt = readLine("Add meg a kulso JSON palyafajl eleresi utjat: ");
        if (pathOpt.isEmpty() || pathOpt.get().isBlank()) {
            out.println("Nem adtal meg fajlutvonalat.");
            return;
        }
        try {
            LevelDefinition loaded = levelLoader.loadFromFile(Path.of(pathOpt.get().trim()));
            out.println("Palya sikeresen beolvasva: " + loaded);
            promptPlayersAndStart(loaded);
        } catch (PersistenceException ex) {
            LOGGER.warn("Kulso palyafajl betoltese sikertelen: {}", ex.getMessage());
            out.println("Hiba a kulso palyafajl betoltesekor: " + ex.getMessage());
        }
    }

    private void promptPlayersAndStart(final LevelDefinition level) {
        Optional<String> p1Opt = readLine("1. jatekos neve ('A' celjarmu): ");
        if (p1Opt.isEmpty()) {
            return;
        }
        String p1Name = p1Opt.get().isBlank() ? "Jatekos 1" : p1Opt.get().trim();

        Optional<String> p2Opt = readLine("2. jatekos neve ('B' celjarmu): ");
        if (p2Opt.isEmpty()) {
            return;
        }
        String p2Name = p2Opt.get().isBlank() ? "Jatekos 2" : p2Opt.get().trim();

        GameState initialState = new GameState(
                p1Name,
                p2Name,
                level.getName(),
                level.getInitialBoard(),
                0,
                PlayerTurn.PLAYER_ONE);
        playMatch(initialState);
    }

    private void loadSavedGameAndPlay() {
        Optional<String> nameOpt = readLine("Add meg a betoltendo mentes nevet: ");
        if (nameOpt.isEmpty() || nameOpt.get().isBlank()) {
            out.println("A mentes neve nem lehet ures.");
            return;
        }
        try {
            GameState loadedState = persistenceService.loadGameByName(
                    savesDirectory, nameOpt.get().trim());
            out.println("Mentes betoltve! Folytatas innen (lepesen: "
                    + loadedState.getCurrentPlayerName() + ").");
            playMatch(loadedState);
        } catch (PersistenceException | IllegalArgumentException ex) {
            LOGGER.warn("Mentes betoltese sikertelen: {}", ex.getMessage());
            out.println("Nem sikerult betolteni a mentest: " + ex.getMessage());
        }
    }

    private void playMatch(final GameState initialState) {
        GameState currentState = initialState;
        printCommandHelp();

        while (!currentState.isSolved()) {
            out.println();
            out.println(renderer.renderFullView(currentState));

            if (!gameService.hasAnyValidMove(currentState)) {
                out.println("Figyelem: a soron levo jatekosnak jelenleg nincs szabalyos lepese!");
            }

            Optional<String> lineOpt = readLine(
                    "[" + currentState.getCurrentPlayerName() + "] Parancs > ");
            if (lineOpt.isEmpty()) {
                return;
            }

            GameCommand command;
            try {
                command = commandParser.parse(lineOpt.get());
            } catch (IllegalArgumentException ex) {
                out.println("Hiba: " + ex.getMessage());
                continue;
            }

            if (command.getType() == CommandType.EXIT) {
                out.println("Aktualis meccs megszakitva, visszateres a fomenube.");
                return;
            }

            currentState = executeInGameCommand(currentState, command);
        }

        out.println();
        out.println(renderer.renderFullView(currentState));
        handleVictory(currentState);
    }

    private GameState executeInGameCommand(
            final GameState currentState,
            final GameCommand command) {
        return switch (command.getType()) {
            case MOVE -> handleMove(currentState, command.getMove().orElseThrow());
            case SAVE -> {
                handleSave(currentState, command.getArgument().orElseThrow());
                yield currentState;
            }
            case LOAD -> handleLoad(currentState, command.getArgument().orElseThrow());
            case MOVES -> {
                printAvailableMoves(currentState);
                yield currentState;
            }
            case SCORES -> {
                printLeaderboards();
                yield currentState;
            }
            case HELP -> {
                printCommandHelp();
                yield currentState;
            }
            case EXIT -> currentState;
        };
    }

    private GameState handleMove(final GameState currentState, final Move move) {
        try {
            MoveResult result = gameService.applyMove(currentState, move);
            if (result.isTargetExited()) {
                out.println("A(z) '" + result.getExitedTargetId().orElseThrow()
                        + "' celjarmu kijutott a tablarol!");
            }
            return result.getGameState();
        } catch (InvalidMoveException ex) {
            out.println("Szabalytalan lepes: " + ex.getMessage());
            return currentState;
        }
    }

    private void handleSave(final GameState currentState, final String saveName) {
        try {
            Path savedPath = persistenceService.saveGameByName(
                    currentState, savesDirectory, saveName);
            out.println("Jatekallapot elmentve: " + savedPath
                    + " (lepesen: " + currentState.getCurrentPlayerName() + ")");
        } catch (PersistenceException | IllegalArgumentException ex) {
            out.println("Mentes sikertelen: " + ex.getMessage());
        }
    }

    private GameState handleLoad(final GameState fallbackState, final String saveName) {
        try {
            GameState loaded = persistenceService.loadGameByName(savesDirectory, saveName);
            out.println("Jatekallapot betoltve! Lepesen: " + loaded.getCurrentPlayerName());
            return loaded;
        } catch (PersistenceException | IllegalArgumentException ex) {
            out.println("Betoltes sikertelen: " + ex.getMessage());
            return fallbackState;
        }
    }

    private void handleVictory(final GameState solvedState) {
        String winner = solvedState.getWinnerName().orElse("Ismeretlen");
        out.println("Gratulalunk, " + winner + "! Megnyerted a jatekot "
                + solvedState.getMoveCount() + " lepesbol!");
        try {
            persistenceService.recordCompletedGame(solvedState);
            out.println("Az eredmeny rogzitesre kerult az SQLite toplistaban.");
        } catch (PersistenceException ex) {
            LOGGER.error("Nem sikerult menteni a gyozelmet az adatbazisba.", ex);
            out.println("Figyelem: az eredmeny adatbazisba mentese sikertelen: "
                    + ex.getMessage());
        }
    }

    private void printAvailableMoves(final GameState state) {
        List<Move> moves = gameService.getValidMoves(state);
        if (moves.isEmpty()) {
            out.println("Nincs elerheto szabalyos lepes.");
            return;
        }
        String preview = moves.stream()
                .limit(PREVIEW_MOVES_LIMIT)
                .map(Move::toString)
                .collect(Collectors.joining(", "));
        out.println("Szabalyos lepesek (" + moves.size() + " db): " + preview);
    }

    private void printLeaderboards() {
        out.println();
        out.println("=== TOPLISTA (LEGKEVESEBB LEPES) ===");
        try {
            List<ScoreEntry> topScores = persistenceService.getTopScores(TOP_LIMIT);
            if (topScores.isEmpty()) {
                out.println("Meg nincs rogzitett eredmeny.");
            } else {
                for (int i = 0; i < topScores.size(); i++) {
                    ScoreEntry entry = topScores.get(i);
                    out.println((i + 1) + ". " + entry.getPlayerName()
                            + " - " + entry.getLevelName()
                            + " (" + entry.getMoveCount() + " lepes, "
                            + entry.getPlayedAt() + ")");
                }
            }

            out.println();
            out.println("=== JATEKOSRANGSOR (GYOZELMEK SZAMA) ===");
            List<PlayerRanking> rankings = persistenceService.getPlayerRankings(TOP_LIMIT);
            if (rankings.isEmpty()) {
                out.println("Meg nincs rangsorolt jatekos.");
            } else {
                for (int i = 0; i < rankings.size(); i++) {
                    PlayerRanking rank = rankings.get(i);
                    out.println((i + 1) + ". " + rank.getPlayerName()
                            + " - " + rank.getWins() + " gyozelem (legjobb: "
                            + rank.getBestMoves() + " lepes)");
                }
            }
        } catch (PersistenceException ex) {
            LOGGER.error("Toplista lekerdezesi hiba.", ex);
            out.println("Nem sikerult lekerdezni a toplistat: " + ex.getMessage());
        }
    }

    private void printCommandHelp() {
        out.println("Parancsok: <jarmu> <irany> [lepes] (pl. 'A jobbra 2', 'B le 1'), "
                + "'save <nev>', 'load <nev>', 'moves', 'scores', 'help', 'exit'");
    }

    private Optional<String> readLine(final String prompt) {
        out.print(prompt);
        out.flush();
        if (!scanner.hasNextLine()) {
            return Optional.empty();
        }
        return Optional.of(scanner.nextLine());
    }
}
