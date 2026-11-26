package hu.nye.progtech.rushhour;

import hu.nye.progtech.rushhour.persistence.GamePersistenceService;
import hu.nye.progtech.rushhour.persistence.JsonGameStateRepository;
import hu.nye.progtech.rushhour.persistence.JsonLevelLoader;
import hu.nye.progtech.rushhour.persistence.LevelLoader;
import hu.nye.progtech.rushhour.persistence.PersistenceException;
import hu.nye.progtech.rushhour.persistence.SqliteScoreRepository;
import hu.nye.progtech.rushhour.service.GameService;
import hu.nye.progtech.rushhour.service.StandardMoveValidator;
import hu.nye.progtech.rushhour.ui.ConsoleGameController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ketjatekosos Rush Hour konzolos alkalmazas belepesi pontja.
 */
public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main() {
    }

    /**
     * Az alkalmazas inditasa es a fuggosegek osszekapcsolasa.
     *
     * @param args parancssori argumentumok
     */
    public static void main(final String[] args) {
        LOGGER.info("Rush Hour alkalmazas inditasa...");
        try {
            GameService gameService = new GameService(new StandardMoveValidator());
            SqliteScoreRepository scoreRepository = new SqliteScoreRepository();
            GamePersistenceService persistenceService = new GamePersistenceService(
                    new JsonGameStateRepository(),
                    scoreRepository);
            LevelLoader levelLoader = new JsonLevelLoader();

            ConsoleGameController controller = new ConsoleGameController(
                    gameService,
                    persistenceService,
                    levelLoader,
                    GamePersistenceService.DEFAULT_SAVES_DIR,
                    System.in,
                    System.out);
            controller.run();
        } catch (PersistenceException ex) {
            LOGGER.error("Kritikus hiba az alkalmazas inditasakor.", ex);
            System.err.println("Nem sikerult inicializalni az adatbazist: " + ex.getMessage());
        }
    }
}
