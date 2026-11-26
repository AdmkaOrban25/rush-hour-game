package hu.nye.progtech.rushhour.service;

/**
 * Ervenytelen jarmumozgatas eseten dobott kivetel.
 */
public class InvalidMoveException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Uj kivetel letrehozasa hibauzenettel.
     *
     * @param message a hiba oka
     */
    public InvalidMoveException(final String message) {
        super(message);
    }
}
