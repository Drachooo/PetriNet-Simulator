import application.logic.PetriNet;
import application.logic.Place;
import application.logic.Transition;
import application.logic.Arc;
import application.logic.MarkingData;
import application.logic.Type;

public class PetriNetApp {
    public static void main(String[] args) {
        PetriNet net = new PetriNet("SimpleNet", "AdminID");

        // Place iniziale con un token
        Place pInitial = new Place("PlaceInitial", net.getId());
        pInitial.addToken();

        // Place intermedi
        Place p1 = new Place("Place1", net.getId());
        Place p2 = new Place("Place2", net.getId());

        // Place finale
        Place pFinal = new Place("PlaceFinal", net.getId());

        // Aggiungo posti alla rete
        net.addPlace(pInitial);
        net.addPlace(p1);
        net.addPlace(p2);
        net.addPlace(pFinal);

        net.setInitial(pInitial);
        net.setFinal(pFinal);

        // Transizioni
        Transition t1 = new Transition(net.getId(), "Transition1", Type.ADMIN);
        Transition t2 = new Transition(net.getId(), "Transition2", Type.ADMIN);
        Transition t3 = new Transition(net.getId(), "Transition3", Type.ADMIN);

        net.addTransition(t1);
        net.addTransition(t2);
        net.addTransition(t3);

        // Archi
        // t1: da pInitial a p1
        t1.addInputPlace(pInitial);
        t1.addOutputPlace(p1);
        net.addArc(new Arc(net.getId(), pInitial.getId(), t1.getId()));
        net.addArc(new Arc(net.getId(), t1.getId(), p1.getId()));

        // t2: da p1 a p2
        t2.addInputPlace(p1);
        t2.addOutputPlace(p2);
        net.addArc(new Arc(net.getId(), p1.getId(), t2.getId()));
        net.addArc(new Arc(net.getId(), t2.getId(), p2.getId()));

        // t3: da p2 a pFinal
        t3.addInputPlace(p2);
        t3.addOutputPlace(pFinal);
        net.addArc(new Arc(net.getId(), p2.getId(), t3.getId()));
        net.addArc(new Arc(net.getId(), t3.getId(), pFinal.getId()));

        // Stampo i token prima del firing
        System.out.println("Stato iniziale:");
        System.out.println(pInitial);
        System.out.println(p1);
        System.out.println(p2);
        System.out.println(pFinal);

        net.validate();

        // Firing sequenziale
        t1.fire();
        System.out.println("\nDopo firing di t1:");
        System.out.println(pInitial);
        System.out.println(p1);
        System.out.println(p2);
        System.out.println(pFinal);

        t2.fire();
        System.out.println("\nDopo firing di t2:");
        System.out.println(pInitial);
        System.out.println(p1);
        System.out.println(p2);
        System.out.println(pFinal);

        t3.fire();
        System.out.println("\nDopo firing di t3:");
        System.out.println(pInitial);
        System.out.println(p1);
        System.out.println(p2);
        System.out.println(pFinal);
    }
}