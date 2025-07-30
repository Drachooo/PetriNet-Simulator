package application.controllers;

import application.logic.*;
import application.repositories.CoordinatesRepository;
import application.ui.graphics.ArcViewFactory;
import application.ui.graphics.PlaceViewFactory;
import application.ui.graphics.TransitionViewFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewPetriNetController {

    @FXML
    private Pane drawingPane;

    private PetriNet petriNet;
    private CoordinatesRepository coordinatesRepo;
    private SharedResources sharedResources;
    private Stage stage;

    public void setSharedResources(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Mappa che associa gli id logici dei nodi con i nodi grafici JavaFX,
     * utile per creare e legare gli archi.
     */
    private final Map<String, Node> nodeMap = new HashMap<>();

    /**
     * Imposta la rete di Petri da visualizzare.
     * @param petriNet istanza di PetriNet
     */
    public void setPetriNet(PetriNet petriNet) {
        this.petriNet = petriNet;
    }

    /**
     * Imposta il repository delle coordinate dei nodi.
     * @param coordinatesRepo repository delle coordinate
     */
    public void setCoordinatesRepo(CoordinatesRepository coordinatesRepo) {
        this.coordinatesRepo = coordinatesRepo;
    }

    /**
     * Disegna la rete di Petri nella Pane associata.
     * La rete è visualizzata in modalità di sola lettura, senza possibilità
     * di interazioni utente sui nodi.
     */
    public void drawPetriNet() {
        drawingPane.getChildren().clear();
        nodeMap.clear();

        drawPlaces();
        drawTransitions();
        drawArcs();
    }

    /**
     * Disegna tutti i Place della rete.
     */
    private void drawPlaces() {
        for (Place place : petriNet.getPlaces().values()) {
            CoordinatesRepository.Position pos = coordinatesRepo.getPlacePosition(place.getId());
            if (pos == null) {
                pos = new CoordinatesRepository.Position(100, 100);
                coordinatesRepo.setPlacePosition(place.getId(), pos.x, pos.y);
            }

            Group placeNode = PlaceViewFactory.createPlaceNode(
                    place,
                    place.getName(),
                    pos.x,
                    pos.y,
                    null,
                    null
            );

            drawingPane.getChildren().add(placeNode);
            nodeMap.put(place.getId(), placeNode);
        }
    }

    /**
     * Disegna tutte le Transition della rete.
     */
    private void drawTransitions() {
        for (Transition transition : petriNet.getTransitions().values()) {
            CoordinatesRepository.Position pos = coordinatesRepo.getTransitionPosition(transition.getId());
            if (pos == null) {
                pos = new CoordinatesRepository.Position(300, 100);
                coordinatesRepo.setTransitionPosition(transition.getId(), pos.x, pos.y);
            }

            Group transitionNode = TransitionViewFactory.createTransitionNode(
                    transition,
                    transition.getName(),
                    pos.x,
                    pos.y,
                    null    // nessun callback per eventi di click/doppio click
            );

            drawingPane.getChildren().add(transitionNode);
            nodeMap.put(transition.getId(), transitionNode);
        }
    }
    /**
     * Disegna tutti gli archi della rete, collegandoli ai nodi grafici.
     * Porta gli archi in fondo per non sovrapporsi ai nodi.
     */
    private void drawArcs() {
        for (Arc arc : petriNet.getArcs().values()) {
            Node source = nodeMap.get(arc.getSourceId());
            Node target = nodeMap.get(arc.getTargetId());

            if (source != null && target != null) {
                Line arcLine = ArcViewFactory.createArcLine(source, target);
                drawingPane.getChildren().add(arcLine);
                arcLine.toBack();
            }
        }
    }

    /**
     * Carica una rete di Petri e la visualizza graficamente.
     * Recupera automaticamente il repository delle coordinate da SharedResources.
     * @param net la rete di Petri da visualizzare
     */
    public void loadPetriNet(PetriNet net) {
        setPetriNet(net);
        setCoordinatesRepo(sharedResources.getCoordinatesRepository());
        drawPetriNet();
    }

    @FXML
    private void goToExploreNets(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExploreNets.fxml"));
        Parent root = loader.load();

        ExploreNetsController controller = loader.getController();
        controller.setSharedResources(sharedResources);
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void goToYourNets(ActionEvent event) throws IOException {/*TODO*/ }

    public void goToHelp(ActionEvent event) throws IOException { }


}
