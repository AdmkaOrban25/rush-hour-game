package hu.nye.progtech.rushhour.model;

import java.util.Objects;

/**
 * A jatektabla egy mezojenek 0-tol indexelt koordinatait tarolo
 * megvaltoztathatatlan ertekobjektum (Value Object).
 */
public final class Position {

    private final int row;
    private final int col;

    /**
     * Uj pozicio letrehozasa sor- es oszlopindex alapjan.
     *
     * @param row a sor indexe (0-tol)
     * @param col az oszlop indexe (0-tol)
     */
    public Position(final int row, final int col) {
        this.row = row;
        this.col = col;
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
     * Visszaadja az oszlop indexet.
     *
     * @return az oszlop indexe
     */
    public int getCol() {
        return col;
    }

    /**
     * Uj poziciot szamol a megadott iranyba torteno eltolassal.
     *
     * @param direction az eltolas iranya
     * @param steps a lepesek szama
     * @return az eltolt pozicio uj peldanya
     */
    public Position translate(final Direction direction, final int steps) {
        Objects.requireNonNull(direction, "Az irany nem lehet null.");
        int newRow = row + direction.getDeltaRow() * steps;
        int newCol = col + direction.getDeltaCol() * steps;
        return new Position(newRow, newCol);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Position other)) {
            return false;
        }
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
