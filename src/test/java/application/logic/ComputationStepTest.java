package application.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ComputationStep} class.
 * Verifies object initialization, null safety, and proper property assignments.
 */
class ComputationStepTest {

    private MarkingData sampleMarking;
    private final String compId = "CO_123";
    private final String transId = "T1";

    /**
     * Initializes sample data before each test.
     */
    @BeforeEach
    void setUp() {
        sampleMarking = new MarkingData();
        sampleMarking.setTokens("P1", 2);
    }

    /**
     * Verifies the business constructor for a standard state transition.
     */
    @Test
    void testBusinessConstructorValid() {
        ComputationStep step = new ComputationStep(compId, transId, sampleMarking);

        assertNotNull(step.getId(), "Step ID should be auto-generated");
        assertTrue(step.getId().startsWith("S"), "Step ID should start with 'S'");
        assertEquals(compId, step.getComputationId(), "Computation ID should match");
        assertEquals(transId, step.getTransitionId(), "Transition ID should match");
        assertEquals(sampleMarking, step.getMarkingData(), "Marking data should match");
        assertNotNull(step.getTimeStamp(), "Timestamp should be auto-generated");
    }

    /**
     * Verifies the business constructor for the initial step (where transition ID is expected to be null).
     */
    @Test
    void testBusinessConstructorInitialStep() {
        ComputationStep step = new ComputationStep(compId, null, sampleMarking);

        assertNull(step.getTransitionId(), "Transition ID should be null for the initial step");
        assertNotNull(step.getId(), "Step ID should still be auto-generated");
    }

    /**
     * Verifies that the business constructor throws a {@link NullPointerException} if required fields are missing.
     */
    @Test
    void testBusinessConstructorNullChecks() {
        assertThrows(NullPointerException.class,
                () -> new ComputationStep(null, transId, sampleMarking),
                "Computation ID cannot be null");

        assertThrows(NullPointerException.class,
                () -> new ComputationStep(compId, transId, null),
                "MarkingData cannot be null");
    }

    /**
     * Verifies that the Jackson deserialization constructor sets all fields exactly as provided.
     */
    @Test
    void testJacksonConstructorValid() {
        LocalDateTime customTime = LocalDateTime.of(2025, 1, 1, 12, 0);

        ComputationStep step = new ComputationStep("S123", compId, transId, customTime, sampleMarking);

        assertEquals("S123", step.getId(), "ID should match the provided value");
        assertEquals(compId, step.getComputationId(), "Computation ID should match the provided value");
        assertEquals(transId, step.getTransitionId(), "Transition ID should match the provided value");
        assertEquals(customTime, step.getTimeStamp(), "Timestamp should match the provided value");
        assertEquals(sampleMarking, step.getMarkingData(), "MarkingData should match the provided value");
    }

    /**
     * Verifies that the Jackson constructor strictly checks for nulls on mandatory properties.
     */
    @Test
    void testJacksonConstructorNullChecks() {
        LocalDateTime time = LocalDateTime.now();

        assertThrows(NullPointerException.class,
                () -> new ComputationStep(null, compId, transId, time, sampleMarking));

        assertThrows(NullPointerException.class,
                () -> new ComputationStep("S123", null, transId, time, sampleMarking));

        assertThrows(NullPointerException.class,
                () -> new ComputationStep("S123", compId, transId, null, sampleMarking));

        assertThrows(NullPointerException.class,
                () -> new ComputationStep("S123", compId, transId, time, null));
    }
}