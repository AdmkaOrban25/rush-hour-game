package hu.nye.progtech.rushhour.service;

import hu.nye.progtech.rushhour.model.Board;
import java.util.Objects;

/**
 * Egy beepitett Rush Hour palya metaadatait es kezdo tablaallapotat tarolo osztaly.
 */
public final class LevelDefinition {

    private final int id;
    private final String name;
    private final String difficulty;
    private final Board initialBoard;

    /**
     * Uj palyadefinicio letrehozasa.
     *
     * @param id a palya sorszama (1-tol)
     * @param name a palya megnevezese
     * @param difficulty a nehezsegi szint leirasa
     * @param initialBoard a palya kiindulo tablaallapota
     */
    public LevelDefinition(
            final int id,
            final String name,
            final String difficulty,
            final Board initialBoard) {
        if (id <= 0) {
            throw new IllegalArgumentException("A palya azonositojanak pozitivnak kell lennie.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A palya neve nem lehet ures.");
        }
        if (difficulty == null || difficulty.isBlank()) {
            throw new IllegalArgumentException("A nehezseg nem lehet ures.");
        }
        this.id = id;
        this.name = name.trim();
        this.difficulty = difficulty.trim();
        this.initialBoard = Objects.requireNonNull(initialBoard, "A tabla nem lehet null.");
    }

    /**
     * Visszaadja a palya sorszamat.
     *
     * @return a palya azonositoja
     */
    public int getId() {
        return id;
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
     * Visszaadja a palya nehezsegi szintjet.
     *
     * @return a nehezseg szovegesen
     */
    public String getDifficulty() {
        return difficulty;
    }

    /**
     * Visszaadja a palya kezdo tablaallapotat.
     *
     * @return a kezdo Board
     */
    public Board getInitialBoard() {
        return initialBoard;
    }

    @Override
    public String toString() {
        return id + ". " + name + " (" + difficulty + ", "
                + initialBoard.getRows() + "x" + initialBoard.getCols() + ")";
    }
}
