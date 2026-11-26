package hu.nye.progtech.rushhour.persistence;

/**
 * Adatmentesi, betoltesi vagy adatbazis-muveleti hiba eseten dobott kivetel.
 */
public class PersistenceException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Uj kivetel letrehozasa hibauzenettel.
     *
     * @param message a hiba leirasa
     */
    public PersistenceException(final String message) {
        super(message);
    }

    /**
     * Uj kivetel letrehozasa hibauzenettel es kivalto okkal.
     *
     * @param message a hiba leirasa
     * @param cause az eredeti kivetel
     */
    public PersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
