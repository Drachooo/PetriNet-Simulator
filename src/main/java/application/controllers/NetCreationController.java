package application.controllers;

import application.logic.*;
import application.repositories.PetriNetCoordinates;
import application.ui.graphics.ArcViewFactory;
import application.ui.graphics.TransitionViewFactory;
import application.ui.utils.UnsavedChangesGuard;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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

    private boolean isDeleteMode = false;

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

    //cursore gomma
    private ImageCursor eraserCursor;

    private final Timeline errorClearer = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                if (statusLabel != null) {
                    statusLabel.setText("Status: Ready.");

                    statusLabel.setTextFill(Color.web("#4da6ff"));
                }
            })
    );

    private SharedResources sharedResources;
    private User currentUser;
    private Stage stage;
    private boolean isDirty=false;

    //ToggleButton
    @FXML private ToggleButton placeButton;
    @FXML private ToggleButton transitionButton;
    @FXML private ToggleButton arcButton;
    @FXML private ToggleButton deleteButton;



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


        //Cursore che diventa gomma quando attivo la modalità Cancella
        try{
            Image image = new Image(getClass().getResourceAsStream("/images/logo_gomma.png"));

            // v , v1 indicano il punto di click (corrispondono al punto centrale della gomma) -> l'immagine ha dimnesione 48x48
            eraserCursor = new ImageCursor(image, 18, 30);
        } catch (Exception e) {
            System.err.println("Impossibile caricare l'icona della gomma: " + e.getMessage());
        }

        //Listener per i toggleButton
        setupToggleButtons();

        // Enable mouse interactions
        drawingPane.setOnMouseClicked(this::onDrawingPaneClicked);
    }


    //Gestione deselezione tool
    private void setupToggleButtons() {
        // Lista dei bottoni
        List<ToggleButton> tools = Arrays.asList(placeButton, transitionButton, arcButton, deleteButton);

        for (ToggleButton button : tools) {
            //Stile (Illuminato/Spento)
            button.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    if(button == deleteButton)
                        button.setStyle("-fx-background-color: rgba(196, 30, 58, 0.8); -fx-text-fill: white; -fx-background-radius: 10; -fx-border-color: #c41e3a; -fx-border-radius: 10;");
                    else
                        button.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3); -fx-text-fill: white; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.5); -fx-border-radius: 10;");
                } else {
                    if(button == deleteButton)
                        button.setStyle("-fx-background-color: rgba(196, 30, 58, 0.2); -fx-text-fill: #ff6b6b; -fx-background-radius: 10; -fx-border-color: #c41e3a; -fx-border-radius: 10;");
                    else
                        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 10;");
                }
            });

            //Deselezione
            //EventFilter per intercettare il click PRIMA che il ToggleGroup agisca
            button.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                if (button.isSelected()) {
                    //Se è GIÀ selezionato e lo clicco, voglio spegnerlo
                    //Spengo lo stato logico e grafico
                    button.setSelected(false);

                    //Resetto il controller
                    resetToolState();
                    currentMode = DrawingMode.NONE;
                    statusLabel.setText("Status: Ready");

                    //Consumo l'evento per impedire a JavaFX di riaccenderlo
                    e.consume();
                }
                //Se NON è selezionato, lascio passare l'evento così JavaFX lo accende normalmente
            });
        }
    }



    private void resetToolState(){
        isDeleteMode = false;
        arcSourceNode = null;

        drawingPane.setCursor(Cursor.DEFAULT);
        drawingPane.setStyle("-fx-background-color: #fcfcfc; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5);");
    }


    private void addToggleListener(ToggleButton button){
        button.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal) {
                button.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3); -fx-text-fill: white; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.5); -fx-border-radius: 10;");
            }else { //se deselezionato è trasparente
                button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 10;");
            }
        });
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

        this.isDirty=true;
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

        this.isDirty=true;
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
        this.isDirty=true;
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

        this.isDirty=true;
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

        this.isDirty=true;
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

        this.isDirty=true;
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
                //Continuo a disegnare fino a quando non cambio strumento
                //currentMode = DrawingMode.NONE;
            }
            case TRANSITION -> {
                createTransition(x, y);
                //Continuo a disegnare fino a quando non cambio strumento
                //currentMode = DrawingMode.NONE;
            }
            case ARC -> {
                Node clicked = findNodeAt(x, y);
                if (clicked == null) return;
                if (arcSourceNode == null) {
                    arcSourceNode = clicked;
                } else {
                    createArcBetween(arcSourceNode, clicked);
                    arcSourceNode = null;
                    //Continuo a disegnare fino a quando non cambio strumento
                    //currentMode = DrawingMode.NONE;
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


    // --- TOOLBAR Button Actions ---
    @FXML private void drawPlace() {
        resetToolState(); // Pulisce tutto prima di iniziare

        if (placeButton.isSelected()) {
            currentMode = DrawingMode.PLACE;
            statusLabel.setText("Tool: Place Active (Click to create)");
        } else {
            //Continuo a disegnare fino a quando non cambio strumento
            //currentMode = DrawingMode.NONE;
            statusLabel.setText("Status: Ready");
        }
    }

    @FXML private void drawTransition() {
        resetToolState();

        if (transitionButton.isSelected()) {
            currentMode = DrawingMode.TRANSITION;
            statusLabel.setText("Tool: Transition Active (Click to create)");
        } else {
            //Continuo a disegnare fino a quando non cambio strumento
            //currentMode = DrawingMode.NONE;
            statusLabel.setText("Status: Ready");
        }
    }

    @FXML private void drawArc() {
        resetToolState();

        if (arcButton.isSelected()) {
            currentMode = DrawingMode.ARC;
            statusLabel.setText("Tool: Arc Active (Click Source -> Click Target)");
        } else {
            //Continuo a disegnare fino a quando non cambio strumento
            //currentMode = DrawingMode.NONE;
            statusLabel.setText("Status: Ready");
        }
    }

    @FXML private void deleteMode() {
        if (currentMode == DrawingMode.DELETE) {
            exitDeleteMode();
        } else {
            currentMode = DrawingMode.DELETE;
            //cambio lo sfondo della lavagna in rosso
            //drawingPane.setStyle("-fx-background-color: rgba(255,0,0,0.2);");

            if(eraserCursor != null){
                drawingPane.setCursor(eraserCursor);
            }else{
                //Se non trova l'immagine usa la croce
                drawingPane.setCursor(Cursor.CROSSHAIR);
            }

            statusLabel.setText("Mode: DELETE (Click to erase)");
        }
    }

    private void exitDeleteMode() {
        resetToolState();
        currentMode = DrawingMode.NONE;
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

        this.isDirty=true;
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
            this.isDirty=false;
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

        this.isDirty=true;
    }

    private void removeTransition(Node node) {
        Transition t = transitionMap.remove(node);
        if (t == null) return;
        petriNet.removeTransition(t.getId());
        removeConnectedArcs(t.getId());
        drawingPane.getChildren().remove(node);

        this.isDirty=true;
    }

    private void removeArc(Node node) {
        Arc a = arcMap.remove(node);
        if (a == null) return;
        petriNet.removeArc(a.getId());
        drawingPane.getChildren().remove(node);

        this.isDirty=true;
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

        this.isDirty=true;
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
            statusLabel.setTextFill(isError ? Color.RED : Color.web("#4da6ff"));
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

        if (!isDirty) {
            NavigationHelper.navigate(event,"/fxml/AdminArea.fxml",currentUser);
            return;
        }

        UnsavedChangesGuard.SaveChoice choice = UnsavedChangesGuard.promptUserForSaveConfirmation();

        switch (choice) {
            case SAVE_AND_CONTINUE:
                if (savePetriNet(event)) {
                    NavigationHelper.navigate(event,"/fxml/AdminArea.fxml",currentUser);
                }
                break;
            case DISCARD_AND_CONTINUE:
                NavigationHelper.navigate(event,"/fxml/AdminArea.fxml",currentUser);
                break;
            case CANCEL_EXIT:
                break;
        }
    }


    @FXML
    private void goToExploreNets(ActionEvent event) throws IOException {

        if (!isDirty) {
            NavigationHelper.navigate(event,"/fxml/ExploreNetsView.fxml",currentUser);
            return;
        }

        UnsavedChangesGuard.SaveChoice choice = UnsavedChangesGuard.promptUserForSaveConfirmation();

        switch (choice) {
            case SAVE_AND_CONTINUE:
                if (savePetriNet(event)) {
                    NavigationHelper.navigate(event,"/fxml/ExploreNetsView.fxml",currentUser);
                }
                break;
            case DISCARD_AND_CONTINUE:
                NavigationHelper.navigate(event,"/fxml/ExploreNetsView.fxml",currentUser);
                break;
            case CANCEL_EXIT:
                break;
        }
    }

    @FXML
    private void goToMainView(ActionEvent event) throws IOException {

        if (!isDirty) {
            NavigationHelper.navigate(event,"/fxml/MainView.fxml",currentUser);
            return;
        }

        UnsavedChangesGuard.SaveChoice choice = UnsavedChangesGuard.promptUserForSaveConfirmation();

        switch (choice) {
            case SAVE_AND_CONTINUE:
                if (savePetriNet(event)) {
                    NavigationHelper.navigate(event,"/fxml/MainView.fxml",currentUser);
                }
                break;
            case DISCARD_AND_CONTINUE:
                NavigationHelper.navigate(event,"/fxml/MainView.fxml",currentUser);
                break;
            case CANCEL_EXIT:
                break;
        }
    }

    @FXML private void goToHelp() {
        showError("Help", "Not implemented yet.");
    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {

        if (!isDirty) {
            NavigationHelper.navigate(event,"/fxml/LoginView.fxml",currentUser);
            return;
        }

        UnsavedChangesGuard.SaveChoice choice = UnsavedChangesGuard.promptUserForSaveConfirmation();

        switch (choice) {
            case SAVE_AND_CONTINUE:
                if (savePetriNet(event)) {
                    NavigationHelper.navigate(event,"/fxml/LoginView.fxml",currentUser);
                }
                break;
            case DISCARD_AND_CONTINUE:
                NavigationHelper.navigate(event,"/fxml/LoginView.fxml",currentUser);
                break;
            case CANCEL_EXIT:
                break;
        }
    }
}