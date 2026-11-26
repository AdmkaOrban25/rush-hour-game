package hu.nye.progtech.rushhour.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.nye.progtech.rushhour.model.Board;
import hu.nye.progtech.rushhour.model.Orientation;
import hu.nye.progtech.rushhour.model.Position;
import hu.nye.progtech.rushhour.model.Vehicle;
import hu.nye.progtech.rushhour.service.LevelDefinition;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jackson segitsegevel JSON fajlokbol palyadefiniciokat betolto implementacio.
 */
public final class JsonLevelLoader implements LevelLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonLevelLoader.class);

    private static final List<String> DEFAULT_LEVEL_RESOURCES = List.of(
            "/levels/level1.json",
            "/levels/level2.json",
            "/levels/level3.json");

    private final ObjectMapper objectMapper;

    /**
     * Alapertelmezett JsonLevelLoader peldany letrehozasa.
     */
    public JsonLevelLoader() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public LevelDefinition loadFromFile(final Path filePath) throws PersistenceException {
        Objects.requireNonNull(filePath, "A palyafajl utvonala nem lehet null.");
        if (!Files.exists(filePath)) {
            throw new PersistenceException("A palyafajl nem talalhato: " + filePath);
        }
        try {
            LevelDto dto = objectMapper.readValue(filePath.toFile(), LevelDto.class);
            LevelDefinition level = fromDto(dto);
            LOGGER.info("Palya betoltve kulso fajlbol: {} ({})", level.getName(), filePath);
            return level;
        } catch (IOException | IllegalArgumentException ex) {
            throw new PersistenceException(
                    "Hibas vagy nem olvashato JSON palyafajl: " + filePath, ex);
        }
    }

    @Override
    public LevelDefinition loadFromResource(final String resourcePath)
            throws PersistenceException {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("Az eroforras utvonala nem lehet ures.");
        }
        try (InputStream stream = JsonLevelLoader.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new PersistenceException(
                        "A palya eroforras nem talalhato: " + resourcePath);
            }
            LevelDto dto = objectMapper.readValue(stream, LevelDto.class);
            LevelDefinition level = fromDto(dto);
            LOGGER.info("Palya betoltve JSON eroforrasbol: {} ({})",
                    level.getName(), resourcePath);
            return level;
        } catch (IOException | IllegalArgumentException ex) {
            throw new PersistenceException(
                    "Hibas JSON palya eroforras: " + resourcePath, ex);
        }
    }

    @Override
    public List<LevelDefinition> loadDefaultLevels() throws PersistenceException {
        List<LevelDefinition> levels = new ArrayList<>();
        for (String resource : DEFAULT_LEVEL_RESOURCES) {
            levels.add(loadFromResource(resource));
        }
        return Collections.unmodifiableList(levels);
    }

    private LevelDefinition fromDto(final LevelDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Ures palyadefinicio.");
        }
        if (dto.getVehicles() == null || dto.getVehicles().isEmpty()) {
            throw new IllegalArgumentException("A palyanak tartalmaznia kell jarmuveket.");
        }
        Board.Builder boardBuilder = Board.builder(dto.getRows(), dto.getCols());
        for (JsonGameStateRepository.SavedVehicleDto vehicleDto : dto.getVehicles()) {
            boardBuilder.addVehicle(toVehicle(vehicleDto));
        }
        Board board = boardBuilder.build();
        return new LevelDefinition(dto.getId(), dto.getName(), dto.getDifficulty(), board);
    }

    private Vehicle toVehicle(final JsonGameStateRepository.SavedVehicleDto vehicleDto) {
        if (vehicleDto == null) {
            throw new IllegalArgumentException("A jarmu bejegyzes nem lehet null.");
        }
        String rawId = vehicleDto.getId();
        if (rawId == null || rawId.trim().length() != 1) {
            throw new IllegalArgumentException("Ervenytelen jarmu azonosito: " + rawId);
        }
        if (vehicleDto.getOrientation() == null) {
            throw new IllegalArgumentException("Hianyzo tajolas a jarmunel: " + rawId);
        }
        char id = rawId.trim().charAt(0);
        Orientation orientation = Orientation.valueOf(vehicleDto.getOrientation());
        Position position = new Position(vehicleDto.getRow(), vehicleDto.getCol());
        return new Vehicle(
                id,
                position,
                vehicleDto.getLength(),
                orientation,
                vehicleDto.isTarget());
    }

    /**
     * Egy JSON palyafajl deszerializaciojahoz hasznalt DTO osztaly.
     */
    public static final class LevelDto {

        private int id;
        private String name;
        private String difficulty;
        private int rows;
        private int cols;
        private List<JsonGameStateRepository.SavedVehicleDto> vehicles = new ArrayList<>();

        /**
         * Jackson altal hasznalt parameter nelkuli konstruktor.
         */
        public LevelDto() {
        }

        /**
         * Visszaadja a palya azonositojat.
         *
         * @return a palya sorszama
         */
        public int getId() {
            return id;
        }

        /**
         * Beallitja a palya azonositojat.
         *
         * @param value a palya sorszama
         */
        public void setId(final int value) {
            this.id = value;
        }

        /**
         * Visszaadja a palya nevet.
         *
         * @return a palya neve
         */
        public String getName() {
            return name;
        }

        /**
         * Beallitja a palya nevet.
         *
         * @param value a palya neve
         */
        public void setName(final String value) {
            this.name = value;
        }

        /**
         * Visszaadja a nehezsegi szintet.
         *
         * @return a nehezsegi szint
         */
        public String getDifficulty() {
            return difficulty;
        }

        /**
         * Beallitja a nehezsegi szintet.
         *
         * @param value a nehezsegi szint
         */
        public void setDifficulty(final String value) {
            this.difficulty = value;
        }

        /**
         * Visszaadja a tabla sorainak szamat.
         *
         * @return a sorok szama
         */
        public int getRows() {
            return rows;
        }

        /**
         * Beallitja a tabla sorainak szamat.
         *
         * @param value a sorok szama
         */
        public void setRows(final int value) {
            this.rows = value;
        }

        /**
         * Visszaadja a tabla oszlopainak szamat.
         *
         * @return az oszlopok szama
         */
        public int getCols() {
            return cols;
        }

        /**
         * Beallitja a tabla oszlopainak szamat.
         *
         * @param value az oszlopok szama
         */
        public void setCols(final int value) {
            this.cols = value;
        }

        /**
         * Visszaadja a palya jarmuveinek listajat.
         *
         * @return a jarmuvek DTO listaja
         */
        public List<JsonGameStateRepository.SavedVehicleDto> getVehicles() {
            return vehicles;
        }

        /**
         * Beallitja a palya jarmuveinek listajat.
         *
         * @param value a jarmuvek DTO listaja
         */
        public void setVehicles(final List<JsonGameStateRepository.SavedVehicleDto> value) {
            this.vehicles = value;
        }
    }
}
