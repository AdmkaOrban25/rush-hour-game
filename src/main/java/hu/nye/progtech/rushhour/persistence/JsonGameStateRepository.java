package hu.nye.progtech.rushhour.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import hu.nye.progtech.rushhour.model.Board;
import hu.nye.progtech.rushhour.model.GameState;
import hu.nye.progtech.rushhour.model.Orientation;
import hu.nye.progtech.rushhour.model.PlayerTurn;
import hu.nye.progtech.rushhour.model.Position;
import hu.nye.progtech.rushhour.model.Vehicle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ketjatekosos GameState objektumokat Jackson segitsegevel JSON formatumban mento tarolo.
 */
public final class JsonGameStateRepository implements GameStateRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonGameStateRepository.class);

    private final ObjectMapper objectMapper;

    /**
     * Alapertelmezett, formazott JSON kimenetet hasznalo peldany letrehozasa.
     */
    public JsonGameStateRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void save(final GameState gameState, final Path filePath) throws PersistenceException {
        Objects.requireNonNull(gameState, "A jatekallapot nem lehet null.");
        Objects.requireNonNull(filePath, "A fajlutvonal nem lehet null.");

        SavedGameDto dto = toDto(gameState);
        try {
            Path parent = filePath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writeValue(filePath.toFile(), dto);
            LOGGER.info("Jatekallapot elmentve JSON fajlba: {} (lepesen: {})",
                    filePath, gameState.getCurrentPlayerName());
        } catch (IOException ex) {
            throw new PersistenceException(
                    "Nem sikerult elmenteni a jatekot ide: " + filePath, ex);
        }
    }

    @Override
    public GameState load(final Path filePath) throws PersistenceException {
        Objects.requireNonNull(filePath, "A fajlutvonal nem lehet null.");
        if (!Files.exists(filePath)) {
            throw new PersistenceException("A mentett fajl nem talalhato: " + filePath);
        }

        try {
            SavedGameDto dto = objectMapper.readValue(filePath.toFile(), SavedGameDto.class);
            GameState loadedState = fromDto(dto);
            LOGGER.info("Jatekallapot betoltve JSON fajlbol: {} (lepesen: {})",
                    filePath, loadedState.getCurrentPlayerName());
            return loadedState;
        } catch (IOException | IllegalArgumentException ex) {
            throw new PersistenceException(
                    "Hibas vagy serult JSON mentesi fajl: " + filePath, ex);
        }
    }

    private SavedGameDto toDto(final GameState state) {
        Board board = state.getBoard();
        SavedGameDto dto = new SavedGameDto();
        dto.setPlayerOneName(state.getPlayerOneName());
        dto.setPlayerTwoName(state.getPlayerTwoName());
        dto.setCurrentPlayer(state.getCurrentPlayer().name());
        dto.setLevelName(state.getLevelName());
        dto.setMoveCount(state.getMoveCount());
        dto.setRows(board.getRows());
        dto.setCols(board.getCols());

        List<String> exited = new ArrayList<>();
        for (Character targetId : board.getExitedTargets()) {
            exited.add(String.valueOf(targetId));
        }
        dto.setExitedTargets(exited);

        List<SavedVehicleDto> vehicleDtos = new ArrayList<>();
        for (Vehicle vehicle : board.getVehicles().values()) {
            SavedVehicleDto vehicleDto = new SavedVehicleDto();
            vehicleDto.setId(String.valueOf(vehicle.getId()));
            vehicleDto.setRow(vehicle.getPosition().getRow());
            vehicleDto.setCol(vehicle.getPosition().getCol());
            vehicleDto.setLength(vehicle.getLength());
            vehicleDto.setOrientation(vehicle.getOrientation().name());
            vehicleDto.setTarget(vehicle.isTarget());
            vehicleDtos.add(vehicleDto);
        }
        dto.setVehicles(vehicleDtos);
        return dto;
    }

    private GameState fromDto(final SavedGameDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Ures JSON tartalom.");
        }
        if (dto.getCurrentPlayer() == null || dto.getCurrentPlayer().isBlank()) {
            throw new IllegalArgumentException("Hianyzik a lepesen levo jatekos a mentesbol.");
        }
        PlayerTurn currentTurn = PlayerTurn.valueOf(dto.getCurrentPlayer().trim());

        Board.Builder boardBuilder = Board.builder(dto.getRows(), dto.getCols());
        if (dto.getVehicles() != null) {
            for (SavedVehicleDto vehicleDto : dto.getVehicles()) {
                boardBuilder.addVehicle(toVehicle(vehicleDto));
            }
        }
        if (dto.getExitedTargets() != null) {
            for (String targetId : dto.getExitedTargets()) {
                boardBuilder.addExitedTarget(parseSingleChar(targetId, "kijutott celjarmu"));
            }
        }

        Board board = boardBuilder.build();
        return new GameState(
                dto.getPlayerOneName(),
                dto.getPlayerTwoName(),
                dto.getLevelName(),
                board,
                dto.getMoveCount(),
                currentTurn);
    }

    private Vehicle toVehicle(final SavedVehicleDto vehicleDto) {
        if (vehicleDto == null) {
            throw new IllegalArgumentException("A jarmu bejegyzes nem lehet null.");
        }
        char id = parseSingleChar(vehicleDto.getId(), "jarmu azonosito");
        if (vehicleDto.getOrientation() == null) {
            throw new IllegalArgumentException("Hianyzo tajolas a(z) " + id + " jarmunel.");
        }
        Orientation orientation = Orientation.valueOf(vehicleDto.getOrientation());
        Position position = new Position(vehicleDto.getRow(), vehicleDto.getCol());
        return new Vehicle(
                id,
                position,
                vehicleDto.getLength(),
                orientation,
                vehicleDto.isTarget());
    }

    private char parseSingleChar(final String text, final String fieldName) {
        if (text == null || text.trim().length() != 1) {
            throw new IllegalArgumentException(
                    "Ervenytelen " + fieldName + " ertek a JSON fajlban: " + text);
        }
        return text.trim().charAt(0);
    }

    /**
     * A JSON szerializaciohoz hasznalt teljes ketjatekosos jatekallapot DTO.
     */
    public static final class SavedGameDto {

        private String playerOneName;
        private String playerTwoName;
        private String currentPlayer;
        private String levelName;
        private int moveCount;
        private int rows;
        private int cols;
        private List<String> exitedTargets = new ArrayList<>();
        private List<SavedVehicleDto> vehicles = new ArrayList<>();

        /**
         * Jackson altal hasznalt parameter nelkuli konstruktor.
         */
        public SavedGameDto() {
        }

        /**
         * Visszaadja az elso jatekos nevet.
         *
         * @return az elso jatekos neve
         */
        public String getPlayerOneName() {
            return playerOneName;
        }

        /**
         * Beallitja az elso jatekos nevet.
         *
         * @param value az elso jatekos neve
         */
        public void setPlayerOneName(final String value) {
            this.playerOneName = value;
        }

        /**
         * Visszaadja a masodik jatekos nevet.
         *
         * @return a masodik jatekos neve
         */
        public String getPlayerTwoName() {
            return playerTwoName;
        }

        /**
         * Beallitja a masodik jatekos nevet.
         *
         * @param value a masodik jatekos neve
         */
        public void setPlayerTwoName(final String value) {
            this.playerTwoName = value;
        }

        /**
         * Visszaadja a lepesen levo jatekos azonositojat.
         *
         * @return a PlayerTurn neve szovegkent
         */
        public String getCurrentPlayer() {
            return currentPlayer;
        }

        /**
         * Beallitja a lepesen levo jatekos azonositojat.
         *
         * @param value a PlayerTurn neve szovegkent
         */
        public void setCurrentPlayer(final String value) {
            this.currentPlayer = value;
        }

        /**
         * Visszaadja a palya nevet.
         *
         * @return a palya neve
         */
        public String getLevelName() {
            return levelName;
        }

        /**
         * Beallitja a palya nevet.
         *
         * @param value a palya neve
         */
        public void setLevelName(final String value) {
            this.levelName = value;
        }

        /**
         * Visszaadja a megtett lepesek szamat.
         *
         * @return a lepesszam
         */
        public int getMoveCount() {
            return moveCount;
        }

        /**
         * Beallitja a megtett lepesek szamat.
         *
         * @param value a lepesszam
         */
        public void setMoveCount(final int value) {
            this.moveCount = value;
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
         * Visszaadja a kijutott celjarmuvek azonositoit.
         *
         * @return a kijutott celjarmuvek listaja
         */
        public List<String> getExitedTargets() {
            return exitedTargets;
        }

        /**
         * Beallitja a kijutott celjarmuvek azonositoit.
         *
         * @param value a kijutott celjarmuvek listaja
         */
        public void setExitedTargets(final List<String> value) {
            this.exitedTargets = value;
        }

        /**
         * Visszaadja a tablan levo jarmuvek DTO listajat.
         *
         * @return a jarmuvek listaja
         */
        public List<SavedVehicleDto> getVehicles() {
            return vehicles;
        }

        /**
         * Beallitja a tablan levo jarmuvek DTO listajat.
         *
         * @param value a jarmuvek listaja
         */
        public void setVehicles(final List<SavedVehicleDto> value) {
            this.vehicles = value;
        }
    }

    /**
     * Egyetlen jarmu JSON szerializaciojahoz hasznalt DTO.
     */
    public static final class SavedVehicleDto {

        private String id;
        private int row;
        private int col;
        private int length;
        private String orientation;
        private boolean target;

        /**
         * Jackson altal hasznalt parameter nelkuli konstruktor.
         */
        public SavedVehicleDto() {
        }

        /**
         * Visszaadja a jarmu azonositojat.
         *
         * @return a jarmu azonositoja
         */
        public String getId() {
            return id;
        }

        /**
         * Beallitja a jarmu azonositojat.
         *
         * @param value a jarmu azonositoja
         */
        public void setId(final String value) {
            this.id = value;
        }

        /**
         * Visszaadja a sor indexet.
         *
         * @return a sor indexe
         */
        public int getRow() {
            return row;
        }

        /**
         * Beallitja a sor indexet.
         *
         * @param value a sor indexe
         */
        public void setRow(final int value) {
            this.row = value;
        }

        /**
         * Visszaadja az oszlop indexet.
         *
         * @return az oszlop indexe
         */
        public int getCol() {
            return col;
        }

        /**
         * Beallitja az oszlop indexet.
         *
         * @param value az oszlop indexe
         */
        public void setCol(final int value) {
            this.col = value;
        }

        /**
         * Visszaadja a jarmu hosszat.
         *
         * @return a jarmu hossza
         */
        public int getLength() {
            return length;
        }

        /**
         * Beallitja a jarmu hosszat.
         *
         * @param value a jarmu hossza
         */
        public void setLength(final int value) {
            this.length = value;
        }

        /**
         * Visszaadja a jarmu tajolasat szovegkent.
         *
         * @return a tajolas neve
         */
        public String getOrientation() {
            return orientation;
        }

        /**
         * Beallitja a jarmu tajolasat szovegkent.
         *
         * @param value a tajolas neve
         */
        public void setOrientation(final String value) {
            this.orientation = value;
        }

        /**
         * Megadja, hogy a jarmu celjarmu-e.
         *
         * @return igaz, ha celjarmu
         */
        public boolean isTarget() {
            return target;
        }

        /**
         * Beallitja, hogy a jarmu celjarmu-e.
         *
         * @param value igaz, ha celjarmu
         */
        public void setTarget(final boolean value) {
            this.target = value;
        }
    }
}
