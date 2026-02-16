package application.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Arc} class.
 * Verifies the correct creation of arcs, property initialization,
 * and enforcement of Petri Net bipartite graph rules.
 */
class ArcTest {

    /**
     * Tests the successful creation of an arc from a Place to a Transition.
     * Verifies correct ID generation and default property initialization.
     */
    @Test
    void testValidArcCreationPlaceToTransition() {
        Arc arc = new Arc("Net1", "P1", "T1");

        assertNotNull(arc.getId(), "Arc ID should not be null");
        assertTrue(arc.getId().startsWith("A"), "Arc ID should start with 'A'");
        assertEquals("Net1", arc.getPetriNetId());
        assertEquals("P1", arc.getSourceId());
        assertEquals("T1", arc.getTargetId());
        assertEquals(1, arc.getWeight(), "Default weight should be 1");
    }

    /**
     * Tests the creation of an arc from a Transition to a Place.
     * Verifies the source type recognition methods.
     */
    @Test
    void testValidArcCreationTransitionToPlace() {
        Arc arc = new Arc("Net1", "T1", "P1");

        assertTrue(arc.isSourceTransition(), "Source should be recognized as a Transition");
        assertFalse(arc.isSourcePlace(), "Source should NOT be recognized as a Place");
    }

    /**
     * Ensures that attempting to connect two Places throws an {@link IllegalArgumentException},
     * enforcing the bipartite graph rule.
     */
    @Test
    void testInvalidArcCreationPlaceToPlace() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Arc("Net1", "P1", "P2")
        );

        assertEquals("You cannot connect elements of the same type!", exception.getMessage());
    }

    /**
     * Ensures that attempting to connect two Transitions throws an {@link IllegalArgumentException},
     * enforcing the bipartite graph rule.
     */
    @Test
    void testInvalidArcCreationTransitionToTransition() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Arc("Net1", "T1", "T2"),
                "Should throw an exception when connecting two Transitions"
        );
    }

    /**
     * Verifies that the constructor rejects null parameters by throwing a {@link NullPointerException}.
     */
    @Test
    void testNullParametersInConstructor() {
        assertThrows(NullPointerException.class, () -> new Arc(null, "P1", "T1"));
        assertThrows(NullPointerException.class, () -> new Arc("Net1", null, "T1"));
        assertThrows(NullPointerException.class, () -> new Arc("Net1", "P1", null));
    }

    /**
     * Tests getting and setting the weight of an arc.
     */
    @Test
    void testWeightModification() {
        Arc arc = new Arc("Net1", "P1", "T1");

        arc.setWeight(5);
        assertEquals(5, arc.getWeight(), "Weight should be updated to 5");
    }
}