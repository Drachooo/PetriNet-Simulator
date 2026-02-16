package application.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PetriNetTest {

    private PetriNet net;

    @BeforeEach
    void setUp() {
        net = new PetriNet("TestNet", "Admin123");
    }

    private Place createPlace() {
        return new Place(net.getId(), "Sample Place");
    }

    private Transition createTransition() {
        return new Transition(net.getId(), "Sample Transition", Type.USER);
    }

    @Test
    void testAddElementsSuccessfully() {
        Place p1 = createPlace();
        net.addPlace(p1);

        assertNotNull(net.getPlaces().get(p1.getId()), "Place should be retrievable by its generated ID");
        assertEquals(p1.getId(), net.getPlaces().get(p1.getId()).getId());
    }

    @Test
    void testAddArcValidation() {
        Place p1 = createPlace();
        Transition t1 = createTransition();
        net.addPlace(p1);
        net.addTransition(t1);

        Arc arc = new Arc(net.getId(), p1.getId(), t1.getId());
        net.addArc(arc);

        assertTrue(net.getArcs().containsKey(arc.getId()));
    }

    @Test
    void testSetInitialAndFinalPlaces() {
        Place p1 = createPlace();
        Place p2 = createPlace();
        net.addPlace(p1);
        net.addPlace(p2);

        net.setInitial(p1);
        assertEquals(p1.getId(), net.getInitialPlaceId(), "Initial ID should match p1's UUID");

        net.setFinal(p2);
        assertEquals(p2.getId(), net.getFinalPlaceId());
    }

    @Test
    void testTransitionIsEnabled() {
        Place p1 = createPlace();
        Transition t1 = createTransition();
        net.addPlace(p1);
        net.addTransition(t1);

        Arc arc = new Arc(net.getId(), p1.getId(), t1.getId());
        arc.setWeight(2);
        net.addArc(arc);

        MarkingData marking = new MarkingData();
        marking.setTokens(p1.getId(), 2);

        assertTrue(net.isEnabled(t1.getId(), marking), "Transition should be enabled with 2 tokens");
    }



    @Test
    void testFireTransitionSuccess() {
        Place p1 = createPlace();
        Transition t1 = createTransition();
        Place p2 = createPlace();

        net.addPlace(p1);
        net.addTransition(t1);
        net.addPlace(p2);

        net.addArc(new Arc(net.getId(), p1.getId(), t1.getId()));
        Arc outArc = new Arc(net.getId(), t1.getId(), p2.getId());
        outArc.setWeight(2);
        net.addArc(outArc);

        MarkingData marking = new MarkingData();
        marking.setTokens(p1.getId(), 1);

        MarkingData nextMarking = net.fire(t1.getId(), marking);

        assertEquals(0, nextMarking.getTokens(p1.getId()));
        assertEquals(2, nextMarking.getTokens(p2.getId()));
    }

    @Test
    void testValidationLogic() {
        Place p1 = createPlace();
        Transition t1 = createTransition();
        Place p2 = createPlace();

        net.addPlace(p1);
        net.addTransition(t1);
        net.addPlace(p2);

        net.setInitial(p1);
        net.setFinal(p2);

        net.addArc(new Arc(net.getId(), p1.getId(), t1.getId()));
        net.addArc(new Arc(net.getId(), t1.getId(), p2.getId()));

        assertDoesNotThrow(() -> net.validate());
    }

    @Test
    void testRemoveElements() {
        Place p1 = createPlace();
        net.addPlace(p1);
        Transition t1 = createTransition();
        net.addTransition(t1);

        Arc arc = new Arc(net.getId(), p1.getId(), t1.getId());
        net.addArc(arc);

        net.removePlace(p1.getId());

        assertFalse(net.getPlaces().containsKey(p1.getId()));
        assertTrue(net.getArcs().isEmpty());
    }
}