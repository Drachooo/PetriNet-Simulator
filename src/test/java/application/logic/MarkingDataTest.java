package application.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link MarkingData} class.
 * Verifies the mathematical and logical operations of token distribution within a Petri net.
 */
class MarkingDataTest {

    private MarkingData marking;

    /**
     * Initializes an empty marking before each test.
     */
    @BeforeEach
    void setUp() {
        marking = new MarkingData();
    }

    /**
     * Verifies that the default constructor creates an empty token map.
     */
    @Test
    void testDefaultConstructor() {
        assertNotNull(marking.getTokensPerPlace(), "Token map should not be null");
        assertTrue(marking.getTokensPerPlace().isEmpty(), "Token map should be empty initially");
    }

    /**
     * Verifies that the copy constructor performs a deep copy of the token map.
     */
    @Test
    void testCopyConstructor() {
        marking.setTokens("P1", 5);
        MarkingData copy = new MarkingData(marking);

        assertEquals(5, copy.getTokens("P1"), "Copied marking should retain token counts");

        // Modify the original to ensure it's a deep copy, not just a reference
        marking.setTokens("P1", 10);
        assertEquals(5, copy.getTokens("P1"), "Copied marking should remain unaffected by changes to the original");
    }

    /**
     * Tests the setTokens method with valid inputs, including complete removal when count is 0.
     */
    @Test
    void testSetTokensValid() {
        marking.setTokens("P1", 3);
        assertEquals(3, marking.getTokens("P1"), "Token count should be exactly 3");
        assertTrue(marking.hasTokens("P1"), "Place should have tokens");

        marking.setTokens("P1", 0);
        assertEquals(0, marking.getTokens("P1"), "Token count should drop to 0");
        assertFalse(marking.hasTokens("P1"), "Place should no longer have tokens");
        assertFalse(marking.getTokensPerPlace().containsKey("P1"), "Key should be removed from map when count is 0");
    }

    /**
     * Ensures that setting a negative token count throws an {@link IllegalArgumentException}.
     */
    @Test
    void testSetTokensNegativeThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> marking.setTokens("P1", -1),
                "Should throw exception when attempting to set a negative token count");
    }

    /**
     * Tests adding valid amounts of tokens to an existing and a new place.
     */
    @Test
    void testAddTokensValid() {
        marking.addTokens("P1", 2); // New place
        assertEquals(2, marking.getTokens("P1"), "Should initialize and add 2 tokens");

        marking.addTokens("P1", 3); // Existing place
        assertEquals(5, marking.getTokens("P1"), "Should sum the new tokens to the existing 2");
    }

    /**
     * Ensures that adding a non-positive amount throws an {@link IllegalArgumentException}.
     */
    @Test
    void testAddTokensInvalidAmountThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> marking.addTokens("P1", 0),
                "Should throw exception when adding 0 tokens");

        assertThrows(IllegalArgumentException.class,
                () -> marking.addTokens("P1", -5),
                "Should throw exception when adding negative tokens");
    }

    /**
     * Tests successfully removing tokens from a place.
     */
    @Test
    void testRemoveTokensValid() {
        marking.setTokens("P1", 5);

        marking.removeTokens("P1", 2);
        assertEquals(3, marking.getTokens("P1"), "Token count should decrease by 2");
    }

    /**
     * Verifies that removing exactly the total amount of tokens deletes the key from the map.
     */
    @Test
    void testRemoveTokensExactAmountRemovesKey() {
        marking.setTokens("P1", 5);
        marking.removeTokens("P1", 5);

        assertEquals(0, marking.getTokens("P1"), "Token count should be 0");
        assertFalse(marking.getTokensPerPlace().containsKey("P1"), "Key should be entirely removed");
    }

    /**
     * Ensures that attempting to remove more tokens than available throws an {@link IllegalStateException}.
     */
    @Test
    void testRemoveTokensInsufficientThrowsException() {
        marking.setTokens("P1", 2);

        assertThrows(IllegalStateException.class,
                () -> marking.removeTokens("P1", 5),
                "Should throw exception when trying to remove more tokens than are available");
    }

    /**
     * Ensures that Jackson serialization/deserialization setters work correctly.
     */
    @Test
    void testJacksonSetters() {
        Map<String, Integer> map = new HashMap<>();
        map.put("P2", 10);

        marking.setTokensPerPlace(map);
        assertEquals(10, marking.getTokens("P2"), "Map setter should directly update the tokens");
    }
}