package application.controllers;

import application.exceptions.EntityNotFoundException;
import application.exceptions.InvalidComputationStateException;
import application.exceptions.SystemContextException;
import application.exceptions.TransitionNotEnabledException;
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
import javafx.scene.control.Button;
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
 * Allows users to interact with an active Computation instance by firing enabled transitions.
 * Implements Use Case 6.2.3.
 */
public class ViewPetriNetController implements Initializable {

    @FXML private Pane drawingPane;
    @FXML private Label netNameLabel;
    @FXML private Label statusLabel;
    @FXML private Label messageLabel; // Displays success/error messages

    @FXML private StackPane rootStackPane;
    @FXML private ImageView backgroundImage;

    @FXML private Button adminAreaButton;

    // Timeline to auto-clear messages after 3 seconds
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

    // Track the currently open Help stage
    private Stage currentHelpStage;

    /**
     * Sets the primary stage for this controller.
     *
     * @param stage The primary stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Updates the current computation instance.
     * Called by the observer when computation state changes.
     *
     * @param comp The updated computation
     */
    public void setCurrentComputation(Computation comp) {
        this.currentComputation = comp;
    }

    /**
     * Loads a computation instance and its associated Petri Net structure.
     * This is the main entry point for the view.
     *
     * @param user        The current user viewing the computation
     * @param computation The computation instance to load
     * @throws EntityNotFoundException if the associated Petri Net cannot be found
     */
    public void loadComputation(User user, Computation computation) {
        if (user == null) {
            throw new SystemContextException("Cannot load computation: user is null");
        }
        if (computation == null) {
            throw new EntityNotFoundException("Cannot load a null computation");
        }

        this.currentUser = user;
        this.currentComputation = computation;
        this.currentNet = computation.getPetriNetSnapshot();

        if (this.currentNet == null) {
            throw new EntityNotFoundException("Petri Net with ID " + computation.getPetriNetId() +
                    " not found for computation " + computation.getId());
        }

        if(adminAreaButton != null){
            adminAreaButton.setVisible(user.isAdmin());
            adminAreaButton.setManaged(user.isAdmin());
        }

        // Load coordinates from the computation snapshot
        this.coordinates = computation.getCoordinatesSnapshot();

        if (this.coordinates == null) {
            this.coordinates = new PetriNetCoordinates();
        }

        netNameLabel.setText(currentNet.getName());
        updateStatusLabel();

        // Create observer to watch for computation state changes
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

            // Disable drag and drop in execution view
            placeNode.setOnMousePressed(null);
            placeNode.setOnMouseDragged(null);

            drawingPane.getChildren().add(placeNode);
            placeNodes.put(place.getId(), placeNode);
        }
    }

    /**
     * Draws all Transition nodes onto the drawing pane and sets click handlers.
     */
    private void drawTransitions() {
        for (Transition transition : currentNet.getTransitions().values()) {
            PetriNetCoordinates.Position pos = coordinates.getTransitionPosition(transition.getId());
            if (pos == null) {
                pos = new PetriNetCoordinates.Position(300, 100 + (transitionNodes.size() * 50));
            }

            Group transitionNode = TransitionViewFactory.createTransitionNode(
                    transition, transition.getName(), pos.x, pos.y,
                    (t) -> handleTransitionClick(t) // Pass click handler
            );

            // Set click handler for firing transitions
            transitionNode.setOnMouseClicked(e -> handleTransitionClick(transition));
            transitionNode.setOnMousePressed(null);
            transitionNode.setOnMouseDragged(null);

            drawingPane.getChildren().add(transitionNode);
            transitionNodes.put(transition.getId(), transitionNode);
        }
    }

    /**
     * Draws all Arc connections between Place and Transition nodes.
     */
    private void drawArcs() {
        for (Arc arc : currentNet.getArcs().values()) {
            Node source = arc.isSourcePlace()
                    ? placeNodes.get(arc.getSourceId())
                    : transitionNodes.get(arc.getSourceId());

            Node target = arc.isSourcePlace()
                    ? transitionNodes.get(arc.getTargetId())
                    : placeNodes.get(arc.getTargetId());

            if (source != null && target != null) {
                Line arcLine = ArcViewFactory.createArcLine(source, target);
                drawingPane.getChildren().addFirst(arcLine); // Add behind nodes
            }
        }
    }

    /**
     * Refreshes the dynamic state of the view (tokens and enabled transitions).
     * Updates visual elements based on current marking and user permissions.
     */
    public void refreshState() {
        MarkingData curr = currentComputation.getLastStep().getMarkingData();

        // Get transitions available based on marking and user role permissions
        List<Transition> availableTransitions = processService.getAvailableTransitions(
                currentComputation.getId(),
                currentUser.getId()
        );

        // Update token counts in places
        for (Map.Entry<String, Group> entry : placeNodes.entrySet()) {
            String placeId = entry.getKey();
            Group group = entry.getValue();
            int tokens = curr.getTokens(placeId);
            updatePlaceTokensVisual(group, tokens);
        }

        // Update transition appearance based on availability (NFR2.2)
        for (Map.Entry<String, Group> entry : transitionNodes.entrySet()) {
            Transition t = currentNet.getTransitions().get(entry.getKey());
            Group group = entry.getValue();
            Rectangle rect = (Rectangle) group.getChildren().getFirst(); // Rectangle is first child

            boolean isAvailable = availableTransitions.contains(t);

            if (isAvailable && currentComputation.isActive()) {
                rect.setStroke(Color.LIMEGREEN);
                rect.setStrokeWidth(4.0); // Highlight enabled transitions
            } else {
                rect.setStroke(Color.BLACK);
                rect.setStrokeWidth(2.0); // Default appearance
            }
        }

        updateStatusLabel();
    }

    /**
     * Updates the visual representation of tokens within a Place node.
     *
     * @param placeGroup The place group node
     * @param tokenCount The number of tokens to display
     */
    private void updatePlaceTokensVisual(Group placeGroup, int tokenCount) {
        // Remove all existing tokens (nodes marked with id="token")
        placeGroup.getChildren().removeIf(node ->
                node.getId() != null && node.getId().equals("token")
        );

        // Add new tokens if present
        if (tokenCount > 0) {
            Circle token = new Circle(0, 0, 8, Color.BLACK);
            token.setId("token"); // Mark for future cleanup

            // TODO: add label if token count > 1

            placeGroup.getChildren().add(token);
        }
    }

    /**
     * Handles user clicks on transition nodes.
     * Attempts to fire the transition through the ProcessService (Use Case 6.2.3).
     *
     * @param t The transition to fire
     */
    private void handleTransitionClick(Transition t) {
        if (t == null) return;

        if (!currentComputation.isActive()) {
            showError("Computation is completed.");
            return;
        }

        try {
            // Observer pattern handles the flow:
            // 1. ProcessService fires transition and calls notifyObservers()
            // 2. Observer (this class) receives update notification
            // 3. Observer calls refreshState() to update the view
            processService.fireTransition(
                    currentComputation.getId(),
                    t.getId(),
                    currentUser.getId()
            );
            showSuccess("Transition " + t.getName() + " fired");

        } catch (TransitionNotEnabledException e) {
            showError("Transition not enabled: " + e.getMessage());
        } catch (IllegalStateException e) {
            // Handle permission denial or other state errors
            showError("Action denied: " + e.getMessage());
        }
    }

    /**
     * Navigates back to the main dashboard.
     *
     * @param event The action event
     * @throws IOException if navigation fails
     */
    @FXML
    void handleGoBack(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event, "/fxml/MainView.fxml", currentUser);
    }

    /**
     * Opens the help dialog in a separate window so the user doesn't lose their state.
     *
     * @param event The action event
     * @throws IOException if navigation fails
     */
    @FXML
    void goToHelp(ActionEvent event) throws IOException {
        if (currentHelpStage != null && currentHelpStage.isShowing()) {
            currentHelpStage.toFront();
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HelpView.fxml"));
        Parent root = loader.load();

        HelpViewController controller = loader.getController();
        controller.setCurrentUser(currentUser);

        controller.setExternalWindow(true);

        currentHelpStage = new Stage();
        currentHelpStage.setTitle("Petri Net Help");
        currentHelpStage.setScene(new Scene(root));
        currentHelpStage.setAlwaysOnTop(true);
        currentHelpStage.show();
    }

    @FXML
    void goToExploreNets(ActionEvent event) throws IOException{
        NavigationHelper.navigate(event, "/fxml/ExploreNetsView.fxml", currentUser);
    }

    @FXML
    void goToAdminArea(ActionEvent event) throws IOException{
        NavigationHelper.navigate(event, "/fxml/AdminArea.fxml", currentUser);
    }

    @FXML
    void handleLogout(ActionEvent event) throws IOException{
        NavigationHelper.navigate(event, "/fxml/LoginView.fxml");
    }


    /**
     * Displays a success message to the user.
     * Message auto-clears after 3 seconds.
     *
     * @param message The success message to display
     */
    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setTextFill(Color.GREEN);
        }
        errorClearer.stop();
        errorClearer.playFromStart();
    }

    /**
     * Displays an error message to the user.
     * Message auto-clears after 3 seconds.
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setTextFill(Color.RED);
        }
        errorClearer.stop();
        errorClearer.playFromStart();
    }

    /**
     * Updates the status label to reflect the current computation state.
     */
    private void updateStatusLabel() {
        if (statusLabel != null)
            statusLabel.setText(currentComputation.getStatus().toString());

        if (!currentComputation.isActive()) {
            statusLabel.setTextFill(Color.RED);
            if (messageLabel != null) {
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

        if (sharedResources == null) {
            throw new SystemContextException("SharedResources service is not initialized");
        }

        this.processService = sharedResources.getProcessService();
        this.petriNetRepository = sharedResources.getPetriNetRepository();

        if (processService == null || petriNetRepository == null) {
            throw new SystemContextException("Required services not available in SharedResources");
        }

        if (backgroundImage != null && rootStackPane != null) {
            backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());
            backgroundImage.setPreserveRatio(false);
        }
    }
}