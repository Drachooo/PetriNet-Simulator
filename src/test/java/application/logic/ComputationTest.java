package application.logic;

import application.repositories.PetriNetCoordinates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Computation} class.
 * Verifies initialization, state lifecycle, step management, and observer pattern logic.
 */
class ComputationTest {

    private PetriNet dummyNet;
    private PetriNetCoordinates dummyCoordinates;
    private String userId;
    private Computation computation;

    /**
     * Initializes default objects required for testing before each test method runs.
     */
    @BeforeEach
    void setUp() {
        dummyNet = new PetriNet();

        dummyCoordinates = new PetriNetCoordinates();
        userId = "USER_123";

        computation = new Computation(dummyNet, dummyCoordinates, userId);
    }

    /**
     * Verifies that the business constructor initializes properties correctly.
     */
    @Test
    void testBusinessConstructorInitialization() {
        assertNotNull(computation.getId(), "Computation ID should not be null");
        assertTrue(computation.getId().startsWith("CO"), "Computation ID should start with 'CO'");
        assertEquals(dummyNet.getId(), computation.getPetriNetId(), "PetriNet ID should match");
        assertEquals(userId, computation.getUserId(), "User ID should match");

        assertEquals(Computation.ComputationStatus.ACTIVE, computation.getStatus(), "Initial status should be ACTIVE");
        assertTrue(computation.isActive(), "isActive() should return true initially");

        assertNotNull(computation.getStartTime(), "Start time should be captured");
        assertNull(computation.getEndTime(), "End time should be null initially");

        assertEquals(dummyNet, computation.getPetriNetSnapshot(), "PetriNet snapshot should be stored");
        assertEquals(dummyCoordinates, computation.getCoordinatesSnapshot(), "Coordinates snapshot should be stored");
        assertTrue(computation.getSteps().isEmpty(), "Steps list should be empty initially");
    }

    /**
     * Verifies that the constructor throws a {@link NullPointerException} if the PetriNet is null.
     */
    @Test
    void testConstructorNullPetriNet() {
        assertThrows(NullPointerException.class,
                () -> new Computation(null, dummyCoordinates, userId),
                "Should throw NPE when PetriNet is null");
    }

    /**
     * Verifies that the constructor throws a {@link NullPointerException} if the user ID is null.
     */
    @Test
    void testConstructorNullUserId() {
        assertThrows(NullPointerException.class,
                () -> new Computation(dummyNet, dummyCoordinates, null),
                "Should throw NPE when User ID is null");
    }

    /**
     * Tests the lifecycle transition to the COMPLETED state.
     */
    @Test
    void testCompleteComputationSuccess() {
        computation.completeComputation();

        assertEquals(Computation.ComputationStatus.COMPLETED, computation.getStatus(), "Status should be COMPLETED");
        assertFalse(computation.isActive(), "isActive() should return false after completion");
        assertNotNull(computation.getEndTime(), "End time should be set upon completion");
    }

    /**
     * Verifies that attempting to complete an already completed computation throws an {@link IllegalStateException}.
     */
    @Test
    void testCompleteComputationAlreadyCompletedThrowsException() {
        computation.completeComputation();

        assertThrows(IllegalStateException.class,
                () -> computation.completeComputation(),
                "Should throw IllegalStateException when completing a computation twice");
    }

    /**
     * Tests adding a valid step to the computation history.
     */
    @Test
    void testAddStepValid() {
        MarkingData emptyMarking = new MarkingData();
        ComputationStep step = new ComputationStep(computation.getId(), "T1", emptyMarking);

        computation.addStep(step);

        assertEquals(1, computation.getSteps().size(), "Steps list should contain 1 item");
        assertEquals(step, computation.getLastStep(), "Last step should match the inserted step");
    }

    /**
     * Verifies that adding a step with a mismatching computation ID throws an {@link IllegalArgumentException}.
     */
    @Test
    void testAddStepWrongComputationId() {
        MarkingData emptyMarking = new MarkingData();
        ComputationStep invalidStep = new ComputationStep("WRONG_ID", "T1", emptyMarking);

        assertThrows(IllegalArgumentException.class,
                () -> computation.addStep(invalidStep),
                "Should throw IllegalArgumentException if step's computation ID does not match");
    }

    /**
     * Verifies that adding a null step throws a {@link NullPointerException}.
     */
    @Test
    void testAddStepNull() {
        assertThrows(NullPointerException.class,
                () -> computation.addStep(null),
                "Should throw NPE when adding a null step");
    }

    /**
     * Tests that {@link Computation#getInitialStep()} correctly identifies the step with a null transition ID.
     */
    @Test
    void testGetInitialStep() {
        MarkingData emptyMarking = new MarkingData();
        ComputationStep initialStep = new ComputationStep(computation.getId(), null, emptyMarking);
        ComputationStep nextStep = new ComputationStep(computation.getId(), "T1", emptyMarking);

        computation.addStep(initialStep);
        computation.addStep(nextStep);

        assertEquals(initialStep, computation.getInitialStep(), "Should return the step with a null transition ID");
    }


    /**
     * Verifies that the Observer pattern correctly notifies attached observers upon state changes.
     */
    @Test
    void testObserverNotificationOnStateChange() {
        final boolean[] wasNotified = {false};

        ComputationObserver testObserver = new ComputationObserver() {
            @Override
            public void update(Computation comp) {
                wasNotified[0] = true;
            }
        };

        computation.attach(testObserver);

        // Trigger a state change
        computation.completeComputation();

        assertTrue(wasNotified[0], "Observer should be notified when computation is completed");
    }

    /**
     * Verifies that the Observer pattern correctly notifies attached observers upon step additions.
     */
    @Test
    void testObserverNotificationOnStepAdded() {
        final boolean[] wasNotified = {false};

        ComputationObserver testObserver = new ComputationObserver() {
            @Override
            public void update(Computation comp) {
                wasNotified[0] = true;
            }
        };

        computation.attach(testObserver);

        MarkingData emptyMarking = new MarkingData();
        computation.addStep(new ComputationStep(computation.getId(), "T1", emptyMarking));

        assertTrue(wasNotified[0], "Observer should be notified when a step is added");
    }
}