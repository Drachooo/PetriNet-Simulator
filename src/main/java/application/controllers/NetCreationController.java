package application.controllers;

import application.logic.*;
import application.repositories.PetriNetCoordinates;
import application.ui.graphics.ArcViewFactory;
import application.ui.graphics.TransitionViewFactory;
import application.ui.utils.Delta;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import application.ui.graphics.PlaceViewFactory;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Controller for the Petri Net Editor view.
 * Handles the visual creation, modification, and deletion of Places, Transitions, and Arcs.
 * Manages the synchronization between the visual elements and the logical PetriNet model.
 */
public class NetCreationController implements Initializable {

    /**
     * Represents the current active tool selected by the user.
     */
    private enum DrawingMode { NONE, PLACE, TRANSITION, ARC, DELETE }

    // --- Internal State ---
    private DrawingMode currentMode = DrawingMode.NONE;
    private Node arcSourceNode;
    private PetriNet petriNet;

    // Maps associating visual JavaFX nodes with logical model objects
    private final Map<Node, Place> placeMap = new HashMap<>();
    private final Map<Place, Group> placeViewMap = new HashMap<>();
    private final Map<Node, Transition> transitionMap = new HashMap<>();
    private final Map<Node, Arc> arcMap = new HashMap<>();

    private final Deque<Node> undoStack = new ArrayDeque<>();

    // --- FXML Elements ---
    @FXML private Pane drawingPane;
    @FXML private ScrollPane scrollPane;
    @FXML private Label statusLabel;

    private final Timeline errorClearer = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                if (statusLabel != null) {
                    statusLabel.setText("");
                }
            })
    );

    private SharedResources sharedResources;
    private User currentUser;
    private Stage stage;

    // --- Initialization ---

    /**
     * Sets the currently logged-in user.
     * @param currentUser The active user.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.sharedResources = SharedResources.getInstance();
        scrollPane.setPannable(true);
        if(statusLabel != null)
            statusLabel.setText("Status: Ready");
    }

    /**
     * Initializes a new, empty Petri net for creation.
     * Called when the user clicks "Create New Net".
     */
    public void initData() {
        if (sharedResources == null) {
            throw new IllegalStateException("SharedResources is not initialized!");
        }
        if (currentUser == null) {
            throw new IllegalStateException("No current user set!");
        }
        int idx = sharedResources.getPetriNetRepository().getPetriNets().size() + 1;
        String netName = "NP" + idx;
        String adminId = currentUser.getId();
        petriNet = new PetriNet(netName, adminId);

        // Enable mouse interactions
        drawingPane.setOnMouseClicked(this::onDrawingPaneClicked);
    }

    /**
     * Loads an existing Petri net and its layout for editing.
     * Called when the user clicks "Edit" on a net.
     * @param netToEdit The logical Petri net model to load.
     */
    public void loadNetForEditing(PetriNet netToEdit) {
        this.petriNet = netToEdit;

        PetriNetCoordinates coords;
        try {
            coords = PetriNetCoordinates.loadFromFile(
                    "data/coords/" + netToEdit.getId() + "_coords.json"
            );
        } catch (IOException e) {
            showError("Warning", "Coordinate file not found. Using default layout.");
            coords = new PetriNetCoordinates();
        }

        drawExistingNet(coords);
    }

    /**
     * Reconstructs the visual representation of the net from the model and coordinates.
     * @param coords The repository containing x/y positions for nodes.
     */
    private void drawExistingNet(PetriNetCoordinates coords) {
        drawingPane.getChildren().clear();
        placeMap.clear();
        placeViewMap.clear();
        transitionMap.clear();
        arcMap.clear();

        // 1. Draw Places
        for (Place p : petriNet.getPlaces().values()) {
            PetriNetCoordinates.Position pos = coords.getPlacePosition(p.getId());
            if (pos == null) pos = new PetriNetCoordinates.Position(100, 100 + (placeMap.size() * 50));

            Group pNode = PlaceViewFactory.createPlaceNode(
                    p, p.getName(), pos.x, pos.y,
                    this::designateInitial, this::designateFinal
            );

            // Restore visual state (Initial/Final)
            if (p.getId().equals(petriNet.getInitialPlaceId())) {
                getOuterCircle(pNode).setStroke(Color.RED);
                Circle tok = new Circle(0, 0, 6, Color.BLACK);
                tok.setId("token");
                pNode.getChildren().add(tok);
            }

            if (p.getId().equals(petriNet.getFinalPlaceId())) {
                getOuterCircle(pNode).setStroke(Color.VIOLET);
            }

            drawingPane.getChildren().add(pNode);
            placeMap.put(pNode, p);
            placeViewMap.put(p, pNode);
        }

        // 2. Draw Transitions
        for (Transition t : petriNet.getTransitions().values()) {
            PetriNetCoordinates.Position pos = coords.getTransitionPosition(t.getId());
            if (pos == null) pos = new PetriNetCoordinates.Position(300, 100 + (transitionMap.size() * 50));

            Group tNode = TransitionViewFactory.createTransitionNode(
                    t, t.getName(), pos.x, pos.y,
                    this::toggleTransitionType
            );
            drawingPane.getChildren().add(tNode);
            transitionMap.put(tNode, t);
        }

        // 3. Draw Arcs
        for (Arc a : petriNet.getArcs().values()) {
            Node srcNode = null;
            Node tgtNode = null;

            if (a.isSourcePlace()) {
                srcNode = placeViewMap.get(petriNet.getPlaces().get(a.getSourceId()));
                // Find target transition
                tgtNode = transitionMap.entrySet().stream()
                        .filter(e -> e.getValue().getId().equals(a.getTargetId()))
                        .map(Map.Entry::getKey).findFirst().orElse(null);
            } else {
                // Find source transition
                srcNode = transitionMap.entrySet().stream()
                        .filter(e -> e.getValue().getId().equals(a.getSourceId()))
                        .map(Map.Entry::getKey).findFirst().orElse(null);
                tgtNode = placeViewMap.get(petriNet.getPlaces().get(a.getTargetId()));
            }

            if (srcNode != null && tgtNode != null) {
                Line line = ArcViewFactory.createArcLine(srcNode, tgtNode);
                drawingPane.getChildren().add(0, line); // Add behind nodes
                arcMap.put(line, a);
            }
        }

        drawingPane.setOnMouseClicked(this::onDrawingPaneClicked);
    }

    // --- Node Creation Logic ---

    private void createPlace(double x, double y) {
        String name = "P" + petriNet.getPlaces().size();
        Place place = new Place(petriNet.getId(), name);
        petriNet.addPlace(place);

        int number = petriNet.getPlaces().size();
        String n = "P" + number;
        Group group = PlaceViewFactory.createPlaceNode(place, n, x, y, this::designateInitial, this::designateFinal);
        drawingPane.getChildren().add(group);
        placeMap.put(group, place);
        placeViewMap.put(place, group);
        undoStack.push(group);
    }

    private void createTransition(double x, double y) {
        String name = "T" + (petriNet.getTransitions().size() + 1);
        Transition t = new Transition(petriNet.getId(), name, Type.USER);
        petriNet.addTransition(t);

        int number = petriNet.getTransitions().size();
        String n = "T" + number;
        Group group = TransitionViewFactory.createTransitionNode(t, n, x, y, this::toggleTransitionType);
        drawingPane.getChildren().add(group);
        transitionMap.put(group, t);
        undoStack.push(group);
    }

    private void createArcBetween(Node srcNode, Node tgtNode) {
        String sourceId = placeMap.containsKey(srcNode) ? placeMap.get(srcNode).getId()
                : transitionMap.containsKey(srcNode) ? transitionMap.get(srcNode).getId()
                : null;

        String targetId = placeMap.containsKey(tgtNode) ? placeMap.get(tgtNode).getId()
                : transitionMap.containsKey(tgtNode) ? transitionMap.get(tgtNode).getId()
                : null;

        if (sourceId == null || targetId == null) return;

        try {
            Arc arc = new Arc(petriNet.getId(), sourceId, targetId);
            petriNet.addArc(arc);

            Line line = ArcViewFactory.createArcLine(srcNode, tgtNode);
            drawingPane.getChildren().add(line);

            arcMap.put(line, arc);
        } catch (IllegalArgumentException ex) {
            showError("Invalid arc", ex.getMessage());
        }
    }

    // --- State Management (Initial/Final Places) ---

    private void designateFinal(Place p) {
        resetPlaceVisual(petriNet.getFinalPlace());
        petriNet.setFinal(p);
        Group g = placeViewMap.get(p);
        if(g != null) {
            getOuterCircle(g).setStroke(Color.VIOLET);
            // Ensure token is removed if it was previously the Initial place
            getToken(g).ifPresent(tok -> g.getChildren().remove(tok));
        }
    }

    private void designateInitial(Place p) {
        resetPlaceVisual(petriNet.getInitialPlace());
        petriNet.setInitial(p);
        Group g = placeViewMap.get(p);
        if(g != null) {
            getOuterCircle(g).setStroke(Color.RED);
            if (getToken(g).isEmpty()) {
                Circle tok = new Circle(0, 0, 6, Color.BLACK);
                tok.setId("token");
                g.getChildren().add(tok);
            }
        }
    }

    private void resetPlaceVisual(Place p) {
        if (p == null) return;
        Group g = placeViewMap.get(p);
        if (g == null) return;
        getOuterCircle(g).setStroke(Color.BLACK);
        getToken(g).ifPresent(tok -> g.getChildren().remove(tok));
    }

    // --- Transition Type Toggling ---

    private void toggleTransitionType(Transition transition) {
        Group g = getGroupForTransition(transition);
        if(g == null) return;
        Rectangle rect = (Rectangle) g.getChildren().get(0);

        Type newType = transition.toggleType();
        if (newType == Type.ADMIN) {
            rect.setFill(Color.RED);
        } else {
            rect.setFill(Color.BLUE);
        }
    }

    private Group getGroupForTransition(Transition t) {
        return transitionMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(t))
                .map(Map.Entry::getKey)
                .filter(n -> n instanceof Group)
                .map(n -> (Group) n)
                .findFirst().orElse(null);
    }

    // --- Event Handling ---

    /**
     * Handles mouse clicks on the canvas.
     * Delegates logic based on the active tool (DrawingMode).
     */
    private void onDrawingPaneClicked(MouseEvent e) {
        double x = e.getX(), y = e.getY();
        switch (currentMode) {
            case PLACE -> {
                createPlace(x, y);
                currentMode = DrawingMode.NONE;
            }
            case TRANSITION -> {
                createTransition(x, y);
                currentMode = DrawingMode.NONE;
            }
            case ARC -> {
                Node clicked = findNodeAt(x, y);
                if (clicked == null) return;
                if (arcSourceNode == null) {
                    arcSourceNode = clicked;
                } else {
                    createArcBetween(arcSourceNode, clicked);
                    arcSourceNode = null;
                    currentMode = DrawingMode.NONE;
                }
            }
            case DELETE -> {
                Node clicked = findNodeAt(x, y);
                if (clicked == null) return;

                if (placeMap.containsKey(clicked)) {
                    removePlace(clicked);
                } else if (transitionMap.containsKey(clicked)) {
                    removeTransition(clicked);
                } else if (arcMap.containsKey(clicked)) {
                    removeArc(clicked);
                }
            }
            default -> {}
        }
    }

    private Node findNodeAt(double x, double y) {
        for (Node n : placeMap.keySet())
            if (n.contains(x - n.getLayoutX(), y - n.getLayoutY())) return n;
        for (Node n : transitionMap.keySet())
            if (n.contains(x - n.getLayoutX(), y - n.getLayoutY())) return n;
        return null;
    }

    // --- Toolbar Button Actions ---

    @FXML private void drawPlace() { if(currentMode==DrawingMode.DELETE) exitDeleteMode(); currentMode = DrawingMode.PLACE; }
    @FXML private void drawTransition() { if(currentMode==DrawingMode.DELETE) exitDeleteMode(); currentMode = DrawingMode.TRANSITION; }
    @FXML private void drawArc() { if(currentMode==DrawingMode.DELETE) exitDeleteMode(); currentMode = DrawingMode.ARC; arcSourceNode = null; }

    @FXML private void deleteMode() {
        if (currentMode == DrawingMode.DELETE) {
            exitDeleteMode();
        } else {
            currentMode = DrawingMode.DELETE;
            drawingPane.setStyle("-fx-background-color: rgba(255,0,0,0.2);");
        }
    }

    private void exitDeleteMode() {
        currentMode = DrawingMode.NONE;
        drawingPane.setStyle("");
    }

    @FXML
    private void clearNet(ActionEvent e) {
        drawingPane.getChildren().clear();
        placeMap.clear();
        placeViewMap.clear();
        transitionMap.clear();
        arcMap.clear();
        undoStack.clear();
        petriNet = new PetriNet(
                petriNet.getName(),
                currentUser.getId()
        );
    }

    /**
     * Saves both the logical Petri net definition and the visual layout coordinates.
     */
    @FXML
    private boolean savePetriNet(ActionEvent e) {
        try {
            // 1. Validate the net logic
            petriNet.validate();

            // 2. Create and populate the coordinates object
            PetriNetCoordinates coords = new PetriNetCoordinates();
            for (Map.Entry<Node, Place> entry : placeMap.entrySet()) {
                coords.setPlacePosition(entry.getValue().getId(), entry.getKey().getLayoutX(), entry.getKey().getLayoutY());
            }
            for (Map.Entry<Node, Transition> entry : transitionMap.entrySet()) {
                coords.setTransitionPosition(entry.getValue().getId(), entry.getKey().getLayoutX(), entry.getKey().getLayoutY());
            }

            // 3. Save both files
            sharedResources.getPetriNetRepository().savePetriNet(petriNet);

            File coordsDir = new File("data/coords/");
            if(!coordsDir.exists())
                coordsDir.mkdirs();

            coords.saveToFile("data/coords/" + petriNet.getId() + "_coords.json");

            showStatus("Net saved successfully!", false);
            return true;

        } catch (IllegalArgumentException | IllegalStateException | IOException ex) {
            showError("Save Error", ex.getMessage());
            return false;
        }
    }

    // --- Removal Logic ---

    private void removePlace(Node node) {
        Place p = placeMap.remove(node);
        if (p == null) return;
        placeViewMap.remove(p);
        petriNet.removePlace(p.getId());
        removeConnectedArcs(p.getId());
        drawingPane.getChildren().remove(node);
    }

    private void removeTransition(Node node) {
        Transition t = transitionMap.remove(node);
        if (t == null) return;
        petriNet.removeTransition(t.getId());
        removeConnectedArcs(t.getId());
        drawingPane.getChildren().remove(node);
    }

    private void removeArc(Node node) {
        Arc a = arcMap.remove(node);
        if (a == null) return;
        petriNet.removeArc(a.getId());
        drawingPane.getChildren().remove(node);
    }

    private void removeConnectedArcs(String nodeId) {
        List<Node> arcsToRemove = new ArrayList<>();
        for (Map.Entry<Node, Arc> entry : arcMap.entrySet()) {
            Arc arc = entry.getValue();
            if (arc.getSourceId().equals(nodeId) || arc.getTargetId().equals(nodeId)) {
                arcsToRemove.add(entry.getKey());
            }
        }
        for (Node arcNode : arcsToRemove) {
            removeArc(arcNode);
        }
    }

    // --- Graphic Helpers ---

    private Circle getOuterCircle(Group g) {
        return (Circle) g.getChildren().get(0);
    }

    private Optional<Circle> getToken(Group g) {
        return g.getChildren().stream()
                .filter(n -> "token".equals(n.getId()))
                .map(n -> (Circle) n)
                .findFirst();
    }

    private void showStatus(String message, boolean isError) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setTextFill(isError ? Color.RED : Color.BLUE);
            errorClearer.stop();
            errorClearer.playFromStart();
        } else {
            System.err.println(message);
        }
    }

    private void showError(String header, String msg) {
        showStatus(header + ": " + msg, true);
    }

    // --- Navigation ---

    @FXML
    private void goToAdminArea(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Save Net");
        alert.setHeaderText("Save changes before exiting?");
        alert.setContentText("Choose an option:");

        ButtonType saveButton = new ButtonType("Save");
        ButtonType dontSaveButton = new ButtonType("Don't Save");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == saveButton) {
                if (savePetriNet(event)) {
                    switchToAdminArea(event);
                }
            } else if (result.get() == dontSaveButton) {
                switchToAdminArea(event);
            }
        }
    }

    private void switchToAdminArea(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminArea.fxml"));
        Parent adminView = loader.load();

        AdminAreaController controller = loader.getController();
        controller.setCurrentUser(currentUser);
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());

        Scene adminScene = new Scene(adminView);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(adminScene);
        window.show();
    }

    @FXML
    private void goToExploreNets(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExploreNets.fxml"));
        Parent root = loader.load();

        ExploreNetsController controller = loader.getController();
        controller.setCurrentUser(this.currentUser);
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void goToMainView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent mainView = loader.load();

        MainViewController controller = loader.getController();
        controller.setSharedResources(sharedResources);
        controller.setCurrentUser(this.currentUser);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(mainView));
        stage.show();
    }

    @FXML private void goToHelp() {
        showError("Help", "Not implemented yet.");
    }

    @FXML private void handleLogout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}