package hu.nye.progtech.rushhour.service;

import hu.nye.progtech.rushhour.model.Board;

/**
 * A jarmulepesek szabalyossagat ellenorzo strategia interfesz (Strategy minta).
 */
public interface MoveValidator {

    /**
     * Ellenorzi, hogy a megadott lepes szabalyos-e az aktualis tablan,
     * es jelzi, ha a lepes egy celjarmu kijutasahoz vezet.
     *
     * @param board az aktualis jatektabla
     * @param move a vizsgalt lepes
     * @return igaz, ha a mozgatott celjarmu a lepes soran eleri a kijaratot
     * @throws InvalidMoveException ha a lepes szabalyellenes
     */
    boolean validateAndCheckExit(Board board, Move move) throws InvalidMoveException;
}
