package hu.nye.progtech.rushhour.ui;

import hu.nye.progtech.rushhour.service.Move;
import java.util.Objects;
import java.util.Optional;

/**
 * A felhasznalo altal a konzolon megadott, ertelmezett parancsot reprezentalo ertekobjektum.
 */
public final class GameCommand {

    private final CommandType type;
    private final Move move;
    private final String argument;

    private GameCommand(final CommandType type, final Move move, final String argument) {
        this.type = Objects.requireNonNull(type, "A parancs tipusa nem lehet null.");
        this.move = move;
        this.argument = argument;
    }

    /**
     * Lepes parancs letrehozasa.
     *
     * @param move a vegrehajtando jarmulepes
     * @return uj MOVE tipusu GameCommand
     */
    public static GameCommand move(final Move move) {
        Objects.requireNonNull(move, "A lepes nem lehet null.");
        return new GameCommand(CommandType.MOVE, move, null);
    }

    /**
     * Jatekallas mentese parancs letrehozasa megadott nevvel.
     *
     * @param saveName a mentes neve
     * @return uj SAVE tipusu GameCommand
     */
    public static GameCommand save(final String saveName) {
        if (saveName == null || saveName.isBlank()) {
            throw new IllegalArgumentException("A mentes neve nem lehet ures.");
        }
        return new GameCommand(CommandType.SAVE, null, saveName.trim());
    }

    /**
     * Mentett jatekallas betoltese parancs letrehozasa megadott nevvel.
     *
     * @param saveName a betoltendo mentes neve
     * @return uj LOAD tipusu GameCommand
     */
    public static GameCommand load(final String saveName) {
        if (saveName == null || saveName.isBlank()) {
            throw new IllegalArgumentException("A betoltendo mentes neve nem lehet ures.");
        }
        return new GameCommand(CommandType.LOAD, null, saveName.trim());
    }

    /**
     * Parameter nelkuli parancs letrehozasa (pl. HELP, SCORES, MOVES, EXIT).
     *
     * @param type a parancs tipusa
     * @return uj GameCommand
     */
    public static GameCommand simple(final CommandType type) {
        Objects.requireNonNull(type, "A parancs tipusa nem lehet null.");
        if (type == CommandType.MOVE || type == CommandType.SAVE || type == CommandType.LOAD) {
            throw new IllegalArgumentException(
                    "Ez a parancstipus parametert igenyel: " + type);
        }
        return new GameCommand(type, null, null);
    }

    /**
     * Visszaadja a parancs tipusat.
     *
     * @return a CommandType erteke
     */
    public CommandType getType() {
        return type;
    }

    /**
     * Visszaadja a parancshoz tartozo lepest, ha MOVE tipusu.
     *
     * @return a Move peldany Optional-ben
     */
    public Optional<Move> getMove() {
        return Optional.ofNullable(move);
    }

    /**
     * Visszaadja a parancs szoveges argumentumat (pl. mentesi nev).
     *
     * @return az argumentum Optional-ben
     */
    public Optional<String> getArgument() {
        return Optional.ofNullable(argument);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GameCommand other)) {
            return false;
        }
        return type == other.type
                && Objects.equals(move, other.move)
                && Objects.equals(argument, other.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, move, argument);
    }

    @Override
    public String toString() {
        return "GameCommand{type=" + type
                + ", move=" + move
                + ", argument='" + argument + "'}";
    }

    /**
     * A tamogatott konzolos parancstipusok felsorolasa.
     */
    public enum CommandType {

        /** Jarmu mozgatasa a tablan. */
        MOVE,

        /** Aktualis jatekallapot mentese megadott nevvel. */
        SAVE,

        /** Korabban mentett jatekallapot betoltese nev alapjan. */
        LOAD,

        /** Toplista es jatekosrangsor megjelenitese. */
        SCORES,

        /** A soron levo jatekos szabalyos lepeseinek listazasa. */
        MOVES,

        /** Parancsok sugojanak kiirasa. */
        HELP,

        /** Kilepes a jatekbol. */
        EXIT
    }
}
