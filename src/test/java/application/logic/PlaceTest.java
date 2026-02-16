package application.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Place} class.
 * Verifies ID generation, property assignment, and string representation.
 */
class PlaceTest {

    /**
     * Tests that the constructor correctly initializes fields and generates a valid UUID-based ID.
     */
    @Test
    void testPlaceInitialization() {
        String netId = "NET_123";
        String placeName = "StartPlace";

        Place place = new Place(netId, placeName);

        assertEquals(placeName, place.getName(), "Name should match the constructor argument");
        assertEquals(netId, place.getPetriNetId(), "PetriNet ID should match the constructor argument");

        assertNotNull(place.getId(), "ID should be automatically generated");
        assertTrue(place.getId().startsWith("P"), "ID should start with the prefix 'P'");
    }

    /**
     * Verifies the custom toString implementation.
     * Note: Your implementation returns "P-" + name.
     */
    @Test
    void testToStringFormat() {
        Place place = new Place("Net1", "Buffer");
        assertEquals("P-Buffer", place.toString(), "toString should follow the 'P-name' format");
    }

    /**
     * Ensures that the constructor throws a NullPointerException if mandatory fields are missing.
     */
    @Test
    void testConstructorNullChecks() {
        assertThrows(NullPointerException.class, () -> new Place(null, "Name"),
                "Should throw NPE if PetriNetId is null");

        assertThrows(NullPointerException.class, () -> new Place("Net1", null),
                "Should throw NPE if Name is null");
    }

    /**
     * Verifies that the default constructor exists (required for Jackson deserialization).
     */
    @Test
    void testDefaultConstructor() {
        Place place = new Place();
        assertNotNull(place, "Default constructor should instantiate the object");
        assertNull(place.getId(), "Fields should be null when using default constructor");
    }
}