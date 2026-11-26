package hu.nye.progtech.rushhour.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A Rush Hour jatektablan szereplo jarmu megvaltoztathatatlan modellje.
 */
public final class Vehicle {

    /** Egy jarmu minimalis hossza mezokben. */
    public static final int MIN_LENGTH = 2;

    private final char id;
    private final Position position;
    private final int length;
    private final Orientation orientation;
    private final boolean target;

    /**
     * Uj jarmu peldany letrehozasa.
     *
     * @param id a jarmu egyedi betu- vagy szamazonositoja (pl. 'A', 'B', 'C')
     * @param position a jarmu bal felso mezojenek pozicioja
     * @param length a jarmu hossza (legalabb 2)
     * @param orientation a jarmu tajolasa (vizszintes vagy fuggoleges)
     * @param target igaz, ha a jarmu celjarmu (pl. 'A' vagy 'B')
     */
    public Vehicle(
            final char id,
            final Position position,
            final int length,
            final Orientation orientation,
            final boolean target) {
        if (!Character.isLetterOrDigit(id)) {
            throw new IllegalArgumentException(
                    "A jarmu azonositojanak betunek vagy szamnak kell lennie: " + id);
        }
        if (length < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "A jarmu hossza legalabb " + MIN_LENGTH + " kell legyen.");
        }
        this.id = Character.toUpperCase(id);
        this.position = Objects.requireNonNull(position, "A pozicio nem lehet null.");
        this.length = length;
        this.orientation = Objects.requireNonNull(orientation, "A tajolas nem lehet null.");
        this.target = target;
    }

    /**
     * Visszaadja a jarmu azonosito karakteret.
     *
     * @return a jarmu nagybetus azonositoja
     */
    public char getId() {
        return id;
    }

    /**
     * Visszaadja a jarmu bal felso mezojenek poziciojat.
     *
     * @return a kezdo pozicio
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Visszaadja a jarmu hosszat mezokben.
     *
     * @return a jarmu hossza
     */
    public int getLength() {
        return length;
    }

    /**
     * Visszaadja a jarmu tajolasat.
     *
     * @return vizszintes vagy fuggoleges tajolas
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Megadja, hogy a jarmu celjarmu-e.
     *
     * @return igaz, ha a jarmuvel ki kell jutni a tablarol
     */
    public boolean isTarget() {
        return target;
    }

    /**
     * Visszaadja a jarmu altal elfoglalt osszes poziciot modosithatatlan listakent.
     *
     * @return az elfoglalt poziciok listaja
     */
    public List<Position> getOccupiedPositions() {
        List<Position> occupied = new ArrayList<>(length);
        Direction stepDirection =
                orientation == Orientation.HORIZONTAL ? Direction.RIGHT : Direction.DOWN;
        for (int i = 0; i < length; i++) {
            occupied.add(position.translate(stepDirection, i));
        }
        return Collections.unmodifiableList(occupied);
    }

    /**
     * Uj jarmu peldanyt ad vissza a megadott iranyba es lepesszammal elmozgatva.
     *
     * @param direction a mozgas iranya
     * @param steps a lepesek szama (pozitiv egesz)
     * @return az elmozgatott jarmu uj peldanya
     */
    public Vehicle moved(final Direction direction, final int steps) {
        Objects.requireNonNull(direction, "Az irany nem lehet null.");
        if (!orientation.allows(direction)) {
            throw new IllegalArgumentException(
                    "A(z) " + id + " jarmu nem mozoghat ebbe az iranyba: " + direction);
        }
        if (steps <= 0) {
            throw new IllegalArgumentException(
                    "A lepesszamnak pozitivnak kell lennie: " + steps);
        }
        Position newPosition = position.translate(direction, steps);
        return new Vehicle(id, newPosition, length, orientation, target);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vehicle other)) {
            return false;
        }
        return id == other.id
                && length == other.length
                && target == other.target
                && position.equals(other.position)
                && orientation == other.orientation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, position, length, orientation, target);
    }

    @Override
    public String toString() {
        return "Vehicle{id=" + id
                + ", position=" + position
                + ", length=" + length
                + ", orientation=" + orientation
                + ", target=" + target + '}';
    }
}
