package hu.nye.progtech.rushhour.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Az N x M meretu Rush Hour jatektabla megvaltoztathatatlan allapota.
 * A peldanyositas a Builder tervezesi minta segitsegevel tortenik.
 */
public final class Board {

    /** A jatektabla minimalis sor- es oszlopszama. */
    public static final int MIN_SIZE = 4;

    /** Az ures mezot jelolo karakter a tablaracson. */
    public static final char EMPTY_CELL = '.';

    private final int rows;
    private final int cols;
    private final Map<Character, Vehicle> vehicles;
    private final Set<Character> exitedTargets;
    private final Map<Position, Vehicle> cellOccupancy;

    private Board(final Builder builder) {
        if (builder.rows < MIN_SIZE || builder.cols < MIN_SIZE) {
            throw new IllegalArgumentException(
                    "A tabla merete legalabb " + MIN_SIZE + "x" + MIN_SIZE + " kell legyen.");
        }
        this.rows = builder.rows;
        this.cols = builder.cols;
        this.vehicles = Collections.unmodifiableMap(new LinkedHashMap<>(builder.vehicles));
        this.exitedTargets = Collections.unmodifiableSet(
                new LinkedHashSet<>(builder.exitedTargets));
        this.cellOccupancy = Collections.unmodifiableMap(validateAndIndex(this.vehicles.values()));
    }

    /**
     * Uj Builder peldanyt hoz letre a megadott N x M tablamerehez.
     *
     * @param rows a sorok szama (N)
     * @param cols az oszlopok szama (M)
     * @return uj Board Builder
     */
    public static Builder builder(final int rows, final int cols) {
        return new Builder(rows, cols);
    }

    private Map<Position, Vehicle> validateAndIndex(final Collection<Vehicle> vehicleList) {
        Map<Position, Vehicle> occupied = new HashMap<>();
        for (Vehicle vehicle : vehicleList) {
            for (Position pos : vehicle.getOccupiedPositions()) {
                if (!isInside(pos)) {
                    throw new IllegalArgumentException(
                            "A(z) " + vehicle.getId() + " jarmu kilog a tablarol: " + pos);
                }
                if (occupied.containsKey(pos)) {
                    Vehicle existing = occupied.get(pos);
                    throw new IllegalArgumentException(
                            "Utkozes a(z) " + pos + " mezon: "
                                    + existing.getId() + " es " + vehicle.getId());
                }
                occupied.put(pos, vehicle);
            }
        }
        return occupied;
    }

    /**
     * Visszaadja a tabla sorainak szamat (N).
     *
     * @return a sorok szama
     */
    public int getRows() {
        return rows;
    }

    /**
     * Visszaadja a tabla oszlopainak szamat (M).
     *
     * @return az oszlopok szama
     */
    public int getCols() {
        return cols;
    }

    /**
     * Visszaadja a tablan jelenleg megtalalhato jarmuveket modosithatatlan terkepkent.
     *
     * @return azonosito -> jarmu lekepezes
     */
    public Map<Character, Vehicle> getVehicles() {
        return vehicles;
    }

    /**
     * Visszaadja a mar sikeresen kijutott celjarmuvek azonositoit.
     *
     * @return a kijutott celjarmuvek azonositoi
     */
    public Set<Character> getExitedTargets() {
        return exitedTargets;
    }

    /**
     * Megadja, hogy jutott-e mar ki legalabb egy celjarmu a tablarol.
     *
     * @return igaz, ha van mar kijutott celjarmu
     */
    public boolean hasAnyTargetExited() {
        return !exitedTargets.isEmpty();
    }

    /**
     * Megkeres egy jarmuvet az azonositoja alapjan.
     *
     * @param vehicleId a keresett jarmu betujele
     * @return a jarmu, ha meg a tablan van
     */
    public Optional<Vehicle> getVehicle(final char vehicleId) {
        return Optional.ofNullable(vehicles.get(Character.toUpperCase(vehicleId)));
    }

    /**
     * Megadja, hogy a megadott pozicio a tabla hatarain belul van-e.
     *
     * @param position a vizsgalt pozicio
     * @return igaz, ha a pozicio ervenyes
     */
    public boolean isInside(final Position position) {
        Objects.requireNonNull(position, "A pozicio nem lehet null.");
        return position.getRow() >= 0
                && position.getRow() < rows
                && position.getCol() >= 0
                && position.getCol() < cols;
    }

    /**
     * Visszaadja az adott mezon allo jarmuvet, ha van ilyen.
     *
     * @param position a vizsgalt mezo
     * @return a mezon allo jarmu, vagy ures Optional
     */
    public Optional<Vehicle> getVehicleAt(final Position position) {
        return Optional.ofNullable(cellOccupancy.get(position));
    }

    /**
     * Megadja, hogy az adott mezo foglalt-e.
     *
     * @param position a vizsgalt mezo
     * @return igaz, ha all rajta jarmu
     */
    public boolean isOccupied(final Position position) {
        return cellOccupancy.containsKey(position);
    }

    /**
     * Ellenorzi, hogy egy celjarmu elerte-e a kijaratat.
     * Vizszintes celjarmunel a jobb szel, fuggoleges celjarmunel az also szel a kijarat.
     *
     * @param vehicle a vizsgalt jarmu
     * @return igaz, ha a jarmu celjarmu es elerte a kijaratot
     */
    public boolean hasReachedExit(final Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "A jarmu nem lehet null.");
        if (!vehicle.isTarget()) {
            return false;
        }
        if (vehicle.getOrientation() == Orientation.HORIZONTAL) {
            return vehicle.getPosition().getCol() + vehicle.getLength() == cols;
        }
        return vehicle.getPosition().getRow() + vehicle.getLength() == rows;
    }

    /**
     * Megadja, hogy a palya megoldott-e (minden celjarmu kijutott a tablarol).
     *
     * @return igaz, ha nincs mar celjarmu a tablan es legalabb egy kijutott
     */
    public boolean isSolved() {
        if (exitedTargets.isEmpty()) {
            return false;
        }
        for (Vehicle vehicle : vehicles.values()) {
            if (vehicle.isTarget()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Uj tabla allapotot ad vissza egy jarmu frissitett poziciojaval.
     *
     * @param updatedVehicle a frissitett jarmu
     * @return az uj, ervenyesitett Board peldany
     */
    public Board withUpdatedVehicle(final Vehicle updatedVehicle) {
        Objects.requireNonNull(updatedVehicle, "A frissitett jarmu nem lehet null.");
        if (!vehicles.containsKey(updatedVehicle.getId())) {
            throw new IllegalArgumentException(
                    "Nem talalhato ilyen jarmu a tablan: " + updatedVehicle.getId());
        }
        Builder builder = new Builder(rows, cols);
        for (Vehicle existing : vehicles.values()) {
            if (existing.getId() == updatedVehicle.getId()) {
                builder.addVehicle(updatedVehicle);
            } else {
                builder.addVehicle(existing);
            }
        }
        builder.addExitedTargets(exitedTargets);
        return builder.build();
    }

    /**
     * Uj tabla allapotot ad vissza, amelyben a megadott celjarmu elhagyta a tablat.
     *
     * @param targetId a kijutott celjarmu azonositoja
     * @return az uj Board peldany
     */
    public Board withExitedTarget(final char targetId) {
        char normalizedId = Character.toUpperCase(targetId);
        Vehicle target = vehicles.get(normalizedId);
        if (target == null || !target.isTarget()) {
            throw new IllegalArgumentException(
                    "Csak tablan levo celjarmu juthat ki: " + normalizedId);
        }
        Builder builder = new Builder(rows, cols);
        for (Vehicle existing : vehicles.values()) {
            if (existing.getId() != normalizedId) {
                builder.addVehicle(existing);
            }
        }
        builder.addExitedTargets(exitedTargets);
        builder.addExitedTarget(normalizedId);
        return builder.build();
    }

    /**
     * Karaktermatrix formajaban visszaadja a tabla pillanatnyi nezetet.
     *
     * @return N x M meretu karaktertomb masolat
     */
    public char[][] toCharGrid() {
        char[][] grid = new char[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = EMPTY_CELL;
            }
        }
        for (Vehicle vehicle : vehicles.values()) {
            for (Position pos : vehicle.getOccupiedPositions()) {
                grid[pos.getRow()][pos.getCol()] = vehicle.getId();
            }
        }
        return grid;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Board other)) {
            return false;
        }
        return rows == other.rows
                && cols == other.cols
                && vehicles.equals(other.vehicles)
                && exitedTargets.equals(other.exitedTargets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows, cols, vehicles, exitedTargets);
    }

    @Override
    public String toString() {
        return "Board{rows=" + rows
                + ", cols=" + cols
                + ", vehicles=" + vehicles.size()
                + ", exitedTargets=" + exitedTargets + '}';
    }

    /**
     * A Board peldanyok biztonsagos felepitesehez hasznalt Builder osztaly.
     */
    public static final class Builder {

        private final int rows;
        private final int cols;
        private final Map<Character, Vehicle> vehicles = new LinkedHashMap<>();
        private final Set<Character> exitedTargets = new LinkedHashSet<>();

        private Builder(final int rows, final int cols) {
            this.rows = rows;
            this.cols = cols;
        }

        /**
         * Hozzaad egy jarmuvet az epulo tablahoz.
         *
         * @param vehicle a hozzaadando jarmu
         * @return maga a Builder peldany
         */
        public Builder addVehicle(final Vehicle vehicle) {
            Objects.requireNonNull(vehicle, "A jarmu nem lehet null.");
            if (vehicles.containsKey(vehicle.getId())) {
                throw new IllegalArgumentException(
                        "Duplikalt jarmu azonosito: " + vehicle.getId());
            }
            vehicles.put(vehicle.getId(), vehicle);
            return this;
        }

        /**
         * Hozzaad egy kijutott celjarmu azonositot.
         *
         * @param targetId a kijutott celjarmu betujele
         * @return maga a Builder peldany
         */
        public Builder addExitedTarget(final char targetId) {
            exitedTargets.add(Character.toUpperCase(targetId));
            return this;
        }

        /**
         * Hozzaad tobb kijutott celjarmu azonositot.
         *
         * @param targetIds a kijutott celjarmuvek azonositoi
         * @return maga a Builder peldany
         */
        public Builder addExitedTargets(final Collection<Character> targetIds) {
            Objects.requireNonNull(targetIds, "A lista nem lehet null.");
            for (Character id : targetIds) {
                addExitedTarget(id);
            }
            return this;
        }

        /**
         * Letrehozza es ellenorzi a megvaltoztathatatlan Board peldanyt.
         *
         * @return az elkeszult Board
         */
        public Board build() {
            return new Board(this);
        }
    }
}
