package application.controllers;

import application.logic.*;
import application.repositories.PetriNetCoordinates;
import application.repositories.PetriNetRepository;
import application.ui.graphics.ArcViewFactory;
import application.ui.graphics.PlaceViewFactory;
import application.ui.graphics.TransitionViewFactory;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

/**
 * Controller for the Petri Net execution view.
 * This view allows the user to interact with an active Computation instance
 * by firing enabled transitions. Implements Use Case 6.2.3.
 */
public class ViewPetriNetController implements Initializable {

    @FXML private Pane drawingPane;
    @FXML private Label netNameLabel;
    @FXML private Label statusLabel;
    @FXML private Label messageLabel; // Used for displaying success/error messages

    @FXML private StackPane rootStackPane;
    @FXML private ImageView backgroundImage;

    private final Timeline errorClearer = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                if (messageLabel != null) {
                    messageLabel.setText("");
                }
            })
    );

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

    private ComputationViewObserver viewObserver;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /*Used by observer to update state*/
    public void setCurrentComputation(Computation comp) {
        this.currentComputation = comp;
    }


    /**
     * Loads the specific computation instance and its associated Petri Net structure
     * and visual layout data. This is the main entry point for the view.
     */
    public void loadComputation(User user, Computation computation){
        this.currentUser = user;
        this.currentComputation = computation;
        this.currentNet=petriNetRepository.getPetriNets().get(computation.getPetriNetId());

        if(this.currentNet==null){
            showError("FATAL: load net failed");
            return;
        }
        try{
            // Load coordinates
            this.coordinates=PetriNetCoordinates.loadFromFile("data/coords/"+currentNet.getId()+"_coords.json");

        }catch (IOException e){
            showError("Coords file not found. using default layout");
            this.coordinates=new PetriNetCoordinates();
        }

        netNameLabel.setText(currentNet.getName());
        updateStatusLabel();

        //Creates Observer
        this.viewObserver = new ComputationViewObserver(computation, this);

        drawPetriNet();

        refreshState();
    }


    /**
     * Draws the static Petri Net structure (Places, Transitions, Arcs) using factory methods.
     */
    public void drawPetriNet() {
        drawingPane.getChildren().clear();
        placeNodes.clear();
        transitionNodes.clear();

        drawPlaces();
        drawTransitions();
        drawArcs();
    }

    /**
     * Draws all Place nodes onto the drawing pane.
     */
    private void drawPlaces() {
        for (Place place : currentNet.getPlaces().values()) {
            PetriNetCoordinates.Position pos = coordinates.getPlacePosition(place.getId());
            if (pos == null) {
                pos = new PetriNetCoordinates.Position(100, 100 + (placeNodes.size() * 50));
            }

            Group placeNode = PlaceViewFactory.createPlaceNode(
                    place, place.getName(), pos.x, pos.y,
                    null, null // No context menu in view-only mode
            );

            // No drag and drop in execution view
            placeNode.setOnMousePressed(null);
            placeNode.setOnMouseDragged(null);

            drawingPane.getChildren().add(placeNode);
            placeNodes.put(place.getId(), placeNode);
        }
    }

    /**
     * Draws all Transition nodes onto the drawing pane and sets the click handler.
     */
    private void drawTransitions() {
        for (Transition transition : currentNet.getTransitions().values()) {
            PetriNetCoordinates.Position pos = coordinates.getTransitionPosition(transition.getId());
            if (pos == null) {
                pos = new PetriNetCoordinates.Position(300, 100 + (transitionNodes.size() * 50));
            }

            Group transitionNode = TransitionViewFactory.createTransitionNode(
                    transition, transition.getName(), pos.x, pos.y,
                    (t) -> handleTransitionClick(t) // Pass the click handler
            );


            // Set the handler explicitly on click (for ease of use)
            transitionNode.setOnMouseClicked(e -> handleTransitionClick(transition));
            transitionNode.setOnMousePressed(null);
            transitionNode.setOnMouseDragged(null);

            drawingPane.getChildren().add(transitionNode);
            transitionNodes.put(transition.getId(), transitionNode);
        }
    }

    /**
     * Draws all Arc connections between the Place and Transition nodes.
     */
    private void drawArcs() {
        for (Arc arc : currentNet.getArcs().values()) {
            Node source = arc.isSourcePlace() ? placeNodes.get(arc.getSourceId()) : transitionNodes.get(arc.getSourceId());
            Node target = arc.isSourcePlace() ? transitionNodes.get(arc.getTargetId()) : placeNodes.get(arc.getTargetId());

            if (source != null && target != null) {
                Line arcLine = ArcViewFactory.createArcLine(source, target);
                drawingPane.getChildren().addFirst(arcLine); // Add behind nodes
            }
        }
    }

    /**
     * Refreshes the dynamic state of the view (Tokens and enabled Transitions).
     */
    public void refreshState(){
        MarkingData curr= currentComputation.getLastStep().getMarkingData();

        // Get transitions available based on marking AND user role permissions
        List<Transition> availableTransitions=processService.getAvailableTransitions(currentComputation.getId(),currentUser.getId());

        // Update Place tokens
        for(Map.Entry<String,Group> entry: placeNodes.entrySet()){
            String placeId = entry.getKey();
            Group group = entry.getValue();
            int tokens = curr.getTokens(placeId);
            updatePlaceTokensVisual(group, tokens);
        }

        // Update Transition colors and stroke (NFR2.2)
        for (Map.Entry<String, Group> entry : transitionNodes.entrySet()) {
            Transition t = currentNet.getTransitions().get(entry.getKey());
            Group group = entry.getValue();
            Rectangle rect = (Rectangle) group.getChildren().getFirst(); // Assumes rectangle is the first child

            boolean isAvailable = availableTransitions.contains(t);

            if (isAvailable && currentComputation.isActive()) {
                rect.setStroke(Color.LIMEGREEN);
                rect.setStrokeWidth(4.0); // Highlight
            } else {
                rect.setStroke(Color.BLACK);
                rect.setStrokeWidth(2.0); // Default
            }
        }
        updateStatusLabel();
    }

    /**
     * Updates the visual representation of tokens within a Place node.
     */
    private void updatePlaceTokensVisual(Group placeGroup, int tokenCount) {
        // Clean all old tokens (nodes marked with id="token")
        placeGroup.getChildren().removeIf(node ->
                node.getId() != null && node.getId().equals("token")
        );

        // Add new tokens
        if (tokenCount > 0) {
            Circle token = new Circle(0, 0, 8, Color.BLACK);
            token.setId("token"); // Mark for cleanup

            // TODO: add label if token count > 1

            placeGroup.getChildren().add(token);
        }
    }

    /**
     * Handles the user clicking on an interactive transition.
     * Calls the ProcessService to attempt the transition execution (Use Case 6.2.3).
     */
    private void handleTransitionClick(Transition t) {
        if(t==null) return;

        if(!currentComputation.isActive()){
            showError("Computation is " +currentComputation.getStatus());
            return;
        }

        try{
            //Observer now handles:
            //1 ProcessService calls notifyObservers()
            //2 Observer (this class) calls updata()
            //3 update() calls refreshState()=
            processService.fireTransition(currentComputation.getId(),t.getId(),currentUser.getId());
            showSuccess("Transition "+t.getName()+" fired");
        }catch(IllegalStateException e){
            // Error handling for permission denial or insufficient tokens
            showError("Action Denied: " + e.getMessage());
        }
    }

    /**
     * Navigates back to the main dashboard.
     */
    @FXML
    void handleGoBack(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event,"/fxml/MainView.fxml",currentUser);
    }

    @FXML
    public void goToHelp(ActionEvent event) throws IOException {
        // TODO
    }

    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setTextFill(Color.GREEN);
        }
        errorClearer.stop();
        errorClearer.playFromStart();
    }

    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setTextFill(Color.RED);
        }
        errorClearer.stop();
        errorClearer.playFromStart();
    }

    private void updateStatusLabel() {
        if(statusLabel!=null)
            statusLabel.setText(currentComputation.getStatus().toString());
        if (!currentComputation.isActive()) {
            statusLabel.setTextFill(Color.RED);
            if(messageLabel != null) {
                messageLabel.setText("Computation Completed.");
                messageLabel.setTextFill(Color.BLACK);
            }
        } else {
            statusLabel.setTextFill(Color.GREEN);
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.sharedResources = SharedResources.getInstance();
        this.processService = sharedResources.getProcessService();
        this.petriNetRepository = sharedResources.getPetriNetRepository();

        if(backgroundImage != null && rootStackPane != null) {
            backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());

            backgroundImage.setPreserveRatio(false);
        }
    }
}