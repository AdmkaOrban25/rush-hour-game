package hu.nye.progtech.rushhour.persistence;

import hu.nye.progtech.rushhour.model.GameState;
import java.nio.file.Path;

/**
 * A jatekallapotok tartos tarolasat es visszatolteset definialo interfesz (Repository minta).
 */
public interface GameStateRepository {

    /**
     * Elmenti a megadott jatekallapotot a cel utvonalra.
     *
     * @param gameState a mentendo jatekallapot
     * @param filePath a mentett fajl utvonala
     * @throws PersistenceException ha a mentes soran I/O vagy szerializacios hiba tortenik
     */
    void save(GameState gameState, Path filePath) throws PersistenceException;

    /**
     * Betolt egy korabban elmentett jatekallapotot a megadott fajlbol.
     *
     * @param filePath a betoltendo fajl utvonala
     * @return a visszatoltott, ervenyesitett GameState peldany
     * @throws PersistenceException ha a fajl nem letezik, serult vagy nem olvashato
     */
    GameState load(Path filePath) throws PersistenceException;
}
