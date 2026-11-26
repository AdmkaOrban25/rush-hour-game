package hu.nye.progtech.rushhour.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * A Vehicle es Direction modellosztalyok egysegtesztjei.
 */
class VehicleTest {

    @Test
    void testHorizontalVehicleOccupiedPositions() {
        Vehicle car = new Vehicle('a', new Position(2, 1), 2, Orientation.HORIZONTAL, true);

        assertEquals('A', car.getId());
        assertTrue(car.isTarget());
        assertEquals(
                List.of(new Position(2, 1), new Position(2, 2)),
                car.getOccupiedPositions());
    }

    @Test
    void testVerticalVehicleOccupiedPositions() {
        Vehicle truck = new Vehicle('C', new Position(0, 3), 3, Orientation.VERTICAL, false);

        assertEquals(
                List.of(new Position(0, 3), new Position(1, 3), new Position(2, 3)),
                truck.getOccupiedPositions());
    }

    @Test
    void testValidMoveCreatesNewVehicleAtTranslatedPosition() {
        Vehicle car = new Vehicle('A', new Position(2, 1), 2, Orientation.HORIZONTAL, true);
        Vehicle movedCar = car.moved(Direction.RIGHT, 2);

        assertEquals(new Position(2, 1), car.getPosition());
        assertEquals(new Position(2, 3), movedCar.getPosition());
    }

    @Test
    void testMoveInDisallowedDirectionThrowsException() {
        Vehicle car = new Vehicle('A', new Position(2, 1), 2, Orientation.HORIZONTAL, true);

        assertThrows(IllegalArgumentException.class, () -> car.moved(Direction.UP, 1));
    }

    @Test
    void testTooShortVehicleThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Vehicle('A', new Position(0, 0), 1, Orientation.HORIZONTAL, true));
    }

    @Test
    void testDirectionFromStringRecognizesHungarianAndEnglishInputs() {
        assertEquals(Direction.UP, Direction.fromString("fel").orElseThrow());
        assertEquals(Direction.DOWN, Direction.fromString("DOWN").orElseThrow());
        assertEquals(Direction.LEFT, Direction.fromString("balra").orElseThrow());
        assertEquals(Direction.RIGHT, Direction.fromString("j").orElseThrow());
        assertTrue(Direction.fromString("ismeretlen").isEmpty());
    }
}
