package hu.nye.progtech.rushhour.persistence;

import hu.nye.progtech.rushhour.service.LevelDefinition;
import java.nio.file.Path;
import java.util.List;

/**
 * Palya kezdoallapotok kulso fajlokbol es eroforrasokbol torteno betolteset definialo interfesz.
 */
public interface LevelLoader {

    /**
     * Betolt egy palyat a megadott kulso fajlbol.
     *
     * @param filePath a palyafajl eleresi utja
     * @return a betoltott es ellenorzott LevelDefinition
     * @throws PersistenceException ha a fajl nem letezik vagy hibas
     */
    LevelDefinition loadFromFile(Path filePath) throws PersistenceException;

    /**
     * Betolt egy palyat a classpath eroforrasai kozul.
     *
     * @param resourcePath az eroforras utvonala (pl. "/levels/level1.json")
     * @return a betoltott es ellenorzott LevelDefinition
     * @throws PersistenceException ha az eroforras nem talalhato vagy hibas
     */
    LevelDefinition loadFromResource(String resourcePath) throws PersistenceException;

    /**
     * Betolti az osszes alapertelmezett JSON palyat.
     *
     * @return a betoltott palyak listaja
     * @throws PersistenceException ha barmelyik palya betoltese sikertelen
     */
    List<LevelDefinition> loadDefaultLevels() throws PersistenceException;
}
