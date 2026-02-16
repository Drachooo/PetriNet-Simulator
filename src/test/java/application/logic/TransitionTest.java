package application.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Transition} class.
 */
class TransitionTest {

    /**
     * Verifies that the constructor correctly initializes fields and generates a prefixed ID.
     */
    @Test
    void testTransitionInitialization() {
        String netId = "NET_99";
        String name = "ProcessData";
        Type type = Type.USER;

        Transition transition = new Transition(netId, name, type);

        assertEquals(name, transition.getName());
        assertEquals(netId, transition.getPetriNetId());
        assertEquals(type, transition.getType());
        assertNotNull(transition.getId());
        assertTrue(transition.getId().startsWith("T"), "Transition ID should start with 'T'");
    }

    /**
     * Verifies the toggleType method switches between USER and ADMIN.
     */
    @Test
    void testToggleType() {
        Transition transition = new Transition("Net1", "Task", Type.USER);

        // First toggle: USER -> ADMIN
        Type firstToggle = transition.toggleType();
        assertEquals(Type.ADMIN, firstToggle);
        assertEquals(Type.ADMIN, transition.getType());

        // Second toggle: ADMIN -> USER
        Type secondToggle = transition.toggleType();
        assertEquals(Type.USER, secondToggle);
        assertEquals(Type.USER, transition.getType());
    }

    /**
     * Ensures that null parameters in constructor throw NullPointerException.
     */
    @Test
    void testConstructorNullConstraints() {
        assertThrows(NullPointerException.class, () -> new Transition(null, "Name", Type.USER));
        assertThrows(NullPointerException.class, () -> new Transition("Net1", null, Type.USER));
        assertThrows(NullPointerException.class, () -> new Transition("Net1", "Name", null));
    }

    /**
     * Verifies that the setId method allows manual ID overrides.
     */
    @Test
    void testSetId() {
        Transition transition = new Transition("Net1", "Task", Type.USER);
        transition.setId("T-CUSTOM-01");
        assertEquals("T-CUSTOM-01", transition.getId());
    }

    /**
     * Verifies the toString output format.
     */
    @Test
    void testToString() {
        Transition transition = new Transition("Net1", "MyTrans", Type.ADMIN);
        String result = transition.toString();

        assertTrue(result.contains("name=MyTrans"));
        assertTrue(result.contains("type=ADMIN"));
    }
}