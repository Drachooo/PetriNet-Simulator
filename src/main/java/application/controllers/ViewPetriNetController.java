package application.controllers;

import application.logic.*;
import application.repositories.PetriNetCoordinates;
import application.repositories.PetriNetRepository;
import application.ui.graphics.ArcViewFactory;
import application.ui.graphics.PlaceViewFactory;
import application.ui.graphics.TransitionViewFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewPetriNetController implements Initializable {

    @FXML
    private Pane drawingPane;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label netNameLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label messageLabel;

    private SharedResources sharedResources;
    private ProcessService processService;
    private PetriNetRepository petriNetRepository;

    private Stage stage;
    private User currentUser;
    private Computation currentComputation;
    private PetriNet currentNet;
    private PetriNetCoordinates coordinates;

    private final Map<String, Group> placeNodes = new HashMap<>();
    private final Map<String, Group> transitionNodes = new HashMap<>();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void loadComputation(User user, Computation computation){
        this.currentUser = user;
        this.currentComputation = computation;
        this.currentNet=petriNetRepository.getPetriNets().get(computation.getPetriNetId());

        if(this.currentNet==null){
            showError("FATAL: load net failed");
            return;
        }
        try{
            this.coordinates=PetriNetCoordinates.loadFromFile("src/main/resources/data/coords"+currentNet.getId()+"_coords.json");

        }catch (IOException e){
            showError("Coords file not found. using default layout");
            this.coordinates=new PetriNetCoordinates();
        }

        netNameLabel.setText(currentNet.getName());
        updateStatusLabel();

        drawNetStructure();

    }

    /**
     * Mappa che associa gli id logici dei nodi con i nodi grafici JavaFX,
     * utile per creare e legare gli archi.
     */
    private final Map<String, Node> nodeMap = new HashMap<>();


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
     * Draws all places
     */
    private void drawPlaces() {
        for (Place place : currentNet.getPlaces().values()) {
            PetriNetCoordinates.Position pos = coordinates.getPlacePosition(place.getId());
            if (pos == null) {
                pos = new PetriNetCoordinates.Position(100, 100 + (placeNodes.size() * 50));
            }

            Group placeNode = PlaceViewFactory.createPlaceNode(
                    place, place.getName(), pos.x, pos.y,
                    null, null //No menu (view only)
            );

           //no drag and drop
            placeNode.setOnMousePressed(null);
            placeNode.setOnMouseDragged(null);

            drawingPane.getChildren().add(placeNode);
            placeNodes.put(place.getId(), placeNode);
        }
    }

    /**
     * Draws all transitions
     */
    private void drawTransitions() {
        for (Transition transition : currentNet.getTransitions().values()) {
            PetriNetCoordinates.Position pos = coordinates.getTransitionPosition(transition.getId());
            if (pos == null) {
                pos = new PetriNetCoordinates.Position(300, 100 + (transitionNodes.size() * 50));
            }

            Group transitionNode = TransitionViewFactory.createTransitionNode(
                    transition, transition.getName(), pos.x, pos.y,
                    (t) -> handleTransitionClick(t)
            );

            // Riassegna il click all'intero gruppo per facilitare
            transitionNode.setOnMouseClicked(e -> handleTransitionClick(transition));
            transitionNode.setOnMousePressed(null);
            transitionNode.setOnMouseDragged(null);

            drawingPane.getChildren().add(transitionNode);
            transitionNodes.put(transition.getId(), transitionNode);
        }
    }
    /**
     * Draws all arcs
     */
    private void drawArcs() {
        for (Arc arc : currentNet.getArcs().values()) {
            Node source = arc.isSourcePlace() ? placeNodes.get(arc.getSourceId()) : transitionNodes.get(arc.getSourceId());
            Node target = arc.isSourcePlace() ? transitionNodes.get(arc.getTargetId()) : placeNodes.get(arc.getTargetId());

            if (source != null && target != null) {
                Line arcLine = ArcViewFactory.createArcLine(source, target);
                drawingPane.getChildren().addFirst(arcLine);
            }
        }
    }

    /**
     * Goes to Dashboard
     */
    @FXML
    void handleGoBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();

        MainViewController controller = loader.getController();
        controller.setSharedResources(SharedResources.getInstance());
        controller.setCurrentUser(currentUser);
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void goToHelp(ActionEvent event) throws IOException {
        //TODO
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.GREEN);
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.RED);
    }

    private void updateStatusLabel() {
        statusLabel.setText(currentComputation.getStatus().toString());
        if (!currentComputation.isActive()) {
            statusLabel.setTextFill(Color.RED);
            messageLabel.setText("Computazione Completata.");
            messageLabel.setTextFill(Color.BLACK);
        } else {
            statusLabel.setTextFill(Color.GREEN);
        }
    }




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.sharedResources = SharedResources.getInstance();
        this.processService = sharedResources.getProcessService();
        this.petriNetRepository = sharedResources.getPetriNetRepository();
    }
}
