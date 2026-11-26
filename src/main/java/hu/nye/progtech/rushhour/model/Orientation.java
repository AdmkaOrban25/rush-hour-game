package hu.nye.progtech.rushhour.model;

/**
 * Egy jarmu elhelyezkedesi iranyat (vizszintes vagy fuggoleges) adja meg.
 */
public enum Orientation {

    /** Vizszintes elhelyezkedes (csak balra es jobbra mozoghat). */
    HORIZONTAL,

    /** Fuggoleges elhelyezkedes (csak fel es le mozoghat). */
    VERTICAL;

    /**
     * Ellenorzi, hogy a megadott mozgasirany engedelyezett-e ehhez a tajolashoz.
     *
     * @param direction a vizsgalt irany
     * @return igaz, ha a jarmu mozoghat a megadott iranyba
     */
    public boolean allows(final Direction direction) {
        if (direction == null) {
            return false;
        }
        if (this == HORIZONTAL) {
            return direction.isHorizontal();
        }
        return direction.isVertical();
    }
}
