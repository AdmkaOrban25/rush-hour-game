package hu.nye.progtech.rushhour.service;

import hu.nye.progtech.rushhour.model.Board;
import hu.nye.progtech.rushhour.model.Orientation;
import hu.nye.progtech.rushhour.model.Position;
import hu.nye.progtech.rushhour.model.Vehicle;
import java.util.List;

/**
 * Beepitett, garantaltan megoldhato N x M Rush Hour palyakat eloallito gyar (Factory minta).
 * Minden palyan ket celjarmu ('A' vizszintes, 'B' fuggoleges) szerepel.
 */
public final class LevelFactory {

    /**
     * Visszaadja az osszes elerheto beepitett palyat.
     *
     * @return a palyak modosithatatlan listaja
     */
    public List<LevelDefinition> createAllLevels() {
        return List.of(
                createLevelOne(),
                createLevelTwo(),
                createLevelThree());
    }

    /**
     * Letrehozza a megadott sorszamu beepitett palyat.
     *
     * @param levelId a palya sorszama (1, 2 vagy 3)
     * @return a kert palya definicioja
     */
    public LevelDefinition createLevel(final int levelId) {
        return switch (levelId) {
            case 1 -> createLevelOne();
            case 2 -> createLevelTwo();
            case 3 -> createLevelThree();
            default -> throw new IllegalArgumentException(
                    "Ismeretlen palya azonosito: " + levelId + " (elerheto: 1-3)");
        };
    }

    private LevelDefinition createLevelOne() {
        Board board = Board.builder(5, 6)
                .addVehicle(targetHorizontal('A', 2, 0, 2))
                .addVehicle(targetVertical('B', 0, 3, 2))
                .addVehicle(obstacleVertical('C', 1, 2, 2))
                .addVehicle(obstacleHorizontal('D', 3, 2, 2))
                .addVehicle(obstacleVertical('E', 1, 5, 2))
                .addVehicle(obstacleHorizontal('F', 3, 4, 2))
                .build();
        return new LevelDefinition(1, "Kezdo keresztezodes", "Konnyu", board);
    }

    private LevelDefinition createLevelTwo() {
        Board board = Board.builder(6, 6)
                .addVehicle(targetHorizontal('A', 2, 0, 2))
                .addVehicle(targetVertical('B', 0, 2, 2))
                .addVehicle(obstacleVertical('C', 1, 3, 2))
                .addVehicle(obstacleVertical('D', 0, 4, 3))
                .addVehicle(obstacleHorizontal('E', 3, 2, 3))
                .addVehicle(obstacleHorizontal('F', 4, 1, 2))
                .addVehicle(obstacleVertical('G', 3, 0, 2))
                .addVehicle(obstacleHorizontal('H', 5, 2, 3))
                .addVehicle(obstacleHorizontal('I', 0, 0, 2))
                .build();
        return new LevelDefinition(2, "Belvarosi csucsforgalom", "Kozepes", board);
    }

    private LevelDefinition createLevelThree() {
        Board board = Board.builder(6, 7)
                .addVehicle(targetHorizontal('A', 2, 0, 2))
                .addVehicle(targetVertical('B', 0, 3, 2))
                .addVehicle(obstacleVertical('C', 1, 2, 2))
                .addVehicle(obstacleVertical('D', 1, 4, 3))
                .addVehicle(obstacleVertical('E', 0, 5, 3))
                .addVehicle(obstacleHorizontal('F', 3, 2, 2))
                .addVehicle(obstacleHorizontal('G', 4, 3, 3))
                .addVehicle(obstacleVertical('H', 0, 0, 2))
                .addVehicle(obstacleVertical('I', 3, 0, 2))
                .addVehicle(obstacleVertical('J', 1, 6, 3))
                .addVehicle(obstacleHorizontal('K', 5, 3, 3))
                .build();
        return new LevelDefinition(3, "Nagy korut", "Halado", board);
    }

    private Vehicle targetHorizontal(
            final char id, final int row, final int col, final int length) {
        return new Vehicle(id, new Position(row, col), length, Orientation.HORIZONTAL, true);
    }

    private Vehicle targetVertical(
            final char id, final int row, final int col, final int length) {
        return new Vehicle(id, new Position(row, col), length, Orientation.VERTICAL, true);
    }

    private Vehicle obstacleHorizontal(
            final char id, final int row, final int col, final int length) {
        return new Vehicle(id, new Position(row, col), length, Orientation.HORIZONTAL, false);
    }

    private Vehicle obstacleVertical(
            final char id, final int row, final int col, final int length) {
        return new Vehicle(id, new Position(row, col), length, Orientation.VERTICAL, false);
    }
}
