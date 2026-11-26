package hu.nye.progtech.rushhour.service;

import hu.nye.progtech.rushhour.model.Direction;
import java.util.Objects;

/**
 * Egyetlen jatekoslepest leiro megvaltoztathatatlan ertekobjektum.
 */
public final class Move {

    private final char vehicleId;
    private final Direction direction;
    private final int steps;

    /**
     * Uj lepes letrehozasa.
     *
     * @param vehicleId a mozgatni kivant jarmu azonositoja
     * @param direction a mozgas iranya
     * @param steps a lepesek szama
     */
    public Move(final char vehicleId, final Direction direction, final int steps) {
        this.vehicleId = Character.toUpperCase(vehicleId);
        this.direction = Objects.requireNonNull(direction, "Az irany nem lehet null.");
        this.steps = steps;
    }

    /**
     * Visszaadja a mozgatott jarmu azonositojat.
     *
     * @return a jarmu nagybetus azonositoja
     */
    public char getVehicleId() {
        return vehicleId;
    }

    /**
     * Visszaadja a mozgas iranyat.
     *
     * @return az irany
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Visszaadja a lepesek szamat.
     *
     * @return a lepesszam
     */
    public int getSteps() {
        return steps;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Move other)) {
            return false;
        }
        return vehicleId == other.vehicleId
                && steps == other.steps
                && direction == other.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, direction, steps);
    }

    @Override
    public String toString() {
        return vehicleId + " " + direction + " " + steps;
    }
}
