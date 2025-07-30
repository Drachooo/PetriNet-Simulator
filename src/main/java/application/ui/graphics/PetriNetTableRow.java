package application.ui.graphics;

import application.logic.PetriNet;
import javafx.beans.property.SimpleStringProperty;

public class PetriNetTableRow {
    private final PetriNet petriNet;
    private final SimpleStringProperty name;
    private final SimpleStringProperty creator;
    private final SimpleStringProperty dateCreated;
    // private final SimpleStringProperty status;

    public PetriNetTableRow(PetriNet net, String name, String creator, String dateCreated /*, String status */) {
        this.petriNet = net;
        this.name = new SimpleStringProperty(name);
        this.creator = new SimpleStringProperty(creator);
        this.dateCreated = new SimpleStringProperty(dateCreated);
        // this.status = new SimpleStringProperty(status);
    }



    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getCreator() {
        return creator.get();
    }

    public SimpleStringProperty creatorProperty() {
        return creator;
    }

    public String getDateCreated() {
        return dateCreated.get();
    }

    public SimpleStringProperty dateCreatedProperty() {
        return dateCreated;
    }

    public PetriNet getPetriNet() {
        return petriNet;
    }

}
