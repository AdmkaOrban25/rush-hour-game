package hu.nye.progtech.rushhour.model;

import java.util.Locale;
import java.util.Optional;

/**
 * A jarmuvek lehetseges mozgasi iranyait reprezentalo enum.
 */
public enum Direction {

    /** Felfele iranyulo mozgas (sor csokken). */
    UP(-1, 0),

    /** Lefele iranyulo mozgas (sor no). */
    DOWN(1, 0),

    /** Balra iranyulo mozgas (oszlop csokken). */
    LEFT(0, -1),

    /** Jobbra iranyulo mozgas (oszlop no). */
    RIGHT(0, 1);

    private final int deltaRow;
    private final int deltaCol;

    Direction(final int deltaRow, final int deltaCol) {
        this.deltaRow = deltaRow;
        this.deltaCol = deltaCol;
    }

    /**
     * Visszaadja a soriranyu elmozdulast egy lepes eseten.
     *
     * @return -1, 0 vagy 1
     */
    public int getDeltaRow() {
        return deltaRow;
    }

    /**
     * Visszaadja az oszlopiranyu elmozdulast egy lepes eseten.
     *
     * @return -1, 0 vagy 1
     */
    public int getDeltaCol() {
        return deltaCol;
    }

    /**
     * Megadja, hogy az irany vizszintes-e.
     *
     * @return igaz, ha balra vagy jobbra iranyul
     */
    public boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
    }

    /**
     * Megadja, hogy az irany fuggoleges-e.
     *
     * @return igaz, ha felfele vagy lefele iranyul
     */
    public boolean isVertical() {
        return this == UP || this == DOWN;
    }

    /**
     * Szoveges bemenetbol (magyar vagy angol iranynevbol) iranyt allit elo.
     *
     * @param input a felhasznalo altal megadott irany szovegkent
     * @return az azonosithato irany, vagy ures Optional ervenytelen bemenet eseten
     */
    public static Optional<Direction> fromString(final String input) {
        if (input == null) {
            return Optional.empty();
        }
        String normalized = input.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "up", "u", "fel", "f" -> Optional.of(UP);
            case "down", "d", "le", "l" -> Optional.of(DOWN);
            case "left", "balra", "bal", "b" -> Optional.of(LEFT);
            case "right", "r", "jobbra", "jobb", "j" -> Optional.of(RIGHT);
            default -> Optional.empty();
        };
    }
}
