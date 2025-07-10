package application.controllers;

import application.logic.*;
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

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Controller per la creazione e modifica grafica di una rete di Petri.
 * Gestisce la logica di creazione, modifica, eliminazione di posti, transizioni e archi,
 * e la navigazione tra le schermate dell'app.
 */
public class NetCreationController implements Initializable {

    /**
     * Modalità di disegno attiva nell'interfaccia.
     */
    private enum DrawingMode { NONE, PLACE, TRANSITION, ARC, DELETE }

    // --- Stato interno e dati ---
    private DrawingMode currentMode = DrawingMode.NONE;
    private Node arcSourceNode;

    private PetriNet petriNet;

    // Mappe per associare nodi grafici e dati logici
    private final Map<Node, Place> placeMap = new HashMap<>();
    private final Map<Place, Group> placeViewMap = new HashMap<>();
    private final Map<Node, Transition> transitionMap = new HashMap<>();
    private final Map<Node, Arc> arcMap = new HashMap<>();

    private final Deque<Node> undoStack = new ArrayDeque<>();

    // --- Elementi UI FXML ---
    @FXML private Pane drawingPane;
    @FXML private ScrollPane scrollPane;

    private SharedResources sharedResources;

    // --- Inizializzazione ---

    /**
     * Setta le risorse condivise e inizializza i dati.
     * @param sr risorse condivise
     */
    public void setSharedResources(SharedResources sr) {
        this.sharedResources = sr;
        initData();
    }

    /**
     * Inizializza la nuova rete e setup evento mouse.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        drawingPane.setOnMouseClicked(this::onDrawingPaneClicked);
        scrollPane.setPannable(true);
    }

    /**
     * Inizializza una nuova rete di Petri.
     */
    private void initData() {
        if (sharedResources == null) {
            throw new IllegalStateException("SharedResources non è inizializzato!");
        }
        if (sharedResources.getCurrentUser() == null) {
            throw new IllegalStateException("Nessun utente corrente settato!");
        }
        int idx = sharedResources.getPetriNetRepository().getPetriNets().size() + 1;
        String netName = "NP" + idx;
        String adminId = sharedResources.getCurrentUser().getId();
        petriNet = new PetriNet(netName, adminId);
    }

    // --- Creazione nodi ---

    /**
     * Crea un posto alla posizione data (x,y).
     * @param x coordinata x
     * @param y coordinata y
     */
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

    /**
     * Crea una transizione alla posizione data (x,y).
     * @param x coordinata x
     * @param y coordinata y
     */
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

    /**
     * Crea un arco tra due nodi (posti o transizioni).
     * @param srcNode nodo sorgente
     * @param tgtNode nodo destinazione
     */
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

    // --- Gestione stato nodi (iniziale, finale) ---

    /**
     * Imposta un posto come finale.
     * @param p posto da impostare come finale
     */
    private void designateFinal(Place p) {
        resetPlaceVisual(petriNet.getFinalPlace());
        petriNet.setFinal(p);
        Group g = placeViewMap.get(p);
        getOuterCircle(g).setStroke(Color.VIOLET);
        getToken(g).ifPresent(tok -> g.getChildren().remove(tok));
    }

    /**
     * Imposta un posto come iniziale (con token nero).
     * @param p posto da impostare come iniziale
     */
    private void designateInitial(Place p) {
        resetPlaceVisual(petriNet.getInitialPlace());
        petriNet.setInitial(p);
        Group g = placeViewMap.get(p);
        getOuterCircle(g).setStroke(Color.RED);
        if (getToken(g).isEmpty()) {
            Circle tok = new Circle(0, 0, 6, Color.BLACK);
            tok.setId("token");
            g.getChildren().add(tok);
        }
    }

    /**
     * Resetta la grafica di un posto (colore bordo nero, nessun token).
     * @param p posto da resettare
     */
    private void resetPlaceVisual(Place p) {
        if (p == null) return;
        Group g = placeViewMap.get(p);
        if (g == null) return;
        getOuterCircle(g).setStroke(Color.BLACK);
        getToken(g).ifPresent(tok -> g.getChildren().remove(tok));
    }

    // --- Toggle tipo transizione ---

    /**
     * Cambia il tipo di una transizione tra USER e ADMIN.
     * @param tr transizione da modificare
     */
    private void toggleTransitionType(Transition tr) {
        Group g = getGroupForTransition(tr);
        Rectangle rect = (Rectangle) g.getChildren().get(0);
        if (tr.getType() == Type.USER) {
            tr.setType(Type.ADMIN);
            rect.setFill(Color.RED);
        } else {
            tr.setType(Type.USER);
            rect.setFill(Color.BLUE);
        }
    }

    /**
     * Ottiene il Group grafico associato a una transizione.
     * @param t transizione
     * @return Group grafico o null se non trovato
     */
    private Group getGroupForTransition(Transition t) {
        return transitionMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(t))
                .map(Map.Entry::getKey)
                .filter(n -> n instanceof Group)
                .map(n -> (Group) n)
                .findFirst().orElse(null);
    }

    // --- Gestione eventi UI ---

    /**
     * Gestisce i click sul pane di disegno, in base alla modalità attiva.
     * @param e evento mouse click
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

    /**
     * Cerca un nodo grafico alla posizione (x,y).
     * @param x coordinata x
     * @param y coordinata y
     * @return nodo trovato o null se nessuno
     */
    private Node findNodeAt(double x, double y) {
        for (Node n : placeMap.keySet())
            if (n.contains(x - n.getLayoutX(), y - n.getLayoutY())) return n;
        for (Node n : transitionMap.keySet())
            if (n.contains(x - n.getLayoutX(), y - n.getLayoutY())) return n;
        return null;
    }

    // --- Azioni bottoni ---

    /**
     * Abilita modalità disegno posto.
     */
    @FXML private void drawPlace() {if(currentMode==DrawingMode.DELETE) exitDeleteMode(); currentMode = DrawingMode.PLACE; }

    /**
     * Abilita modalità disegno transizione.
     */
    @FXML private void drawTransition() {if(currentMode==DrawingMode.DELETE) exitDeleteMode(); currentMode = DrawingMode.TRANSITION; }

    /**
     * Abilita modalità disegno arco.
     */
    @FXML private void drawArc() {if(currentMode==DrawingMode.DELETE) exitDeleteMode(); currentMode = DrawingMode.ARC; arcSourceNode = null; }

    /**
     * Abilita o disabilita modalità cancellazione.
     */
    @FXML private void enterDeleteMode() {
        if (currentMode == DrawingMode.DELETE) {
            exitDeleteMode();
        } else {
            currentMode = DrawingMode.DELETE;
            drawingPane.setStyle("-fx-background-color: rgba(255,0,0,0.2);");
        }
    }

    /**
     * Disabilita modalità cancellazione.
     */
    private void exitDeleteMode() {
        currentMode = DrawingMode.NONE;
        drawingPane.setStyle(""); // reset colore
    }

    /**
     * Pulisce completamente la rete corrente, resetta UI e dati.
     * @param e evento azione
     */
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
                sharedResources.getCurrentUser().getId()
        );
    }

    /**
     * Salva la rete di Petri attuale DOPO ave validato.
     * @param e evento azione
     * @return se il salvataggio ha avuto successo o no
     */
    @FXML
    private boolean savePetriNet(ActionEvent e) {
        try {
            petriNet.validate();
            sharedResources.getPetriNetRepository().savePetriNet(petriNet);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Net Saved");
            alert.setContentText("Your Net was saved successfully.");
            alert.showAndWait();

            return true;

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError("Validation error! ", ex.getMessage());
            return false;
        }
    }

    // --- Rimozione nodi e archi ---

    /**
     * Rimuove un posto dalla rete e dall'interfaccia.
     * @param node nodo grafico del posto
     */
    private void removePlace(Node node) {
        Place p = placeMap.remove(node);
        if (p == null) return;
        placeViewMap.remove(p);
        petriNet.removePlace(p.getId());
        removeConnectedArcs(p.getId());
        drawingPane.getChildren().remove(node);
    }

    /**
     * Rimuove una transizione dalla rete e dall'interfaccia.
     * @param node nodo grafico della transizione
     */
    private void removeTransition(Node node) {
        Transition t = transitionMap.remove(node);
        if (t == null) return;
        petriNet.removeTransition(t.getId());
        removeConnectedArcs(t.getId());
        drawingPane.getChildren().remove(node);
    }

    /**
     * Rimuove un arco dalla rete e dall'interfaccia.
     * @param node nodo grafico dell'arco
     */
    private void removeArc(Node node) {
        Arc a = arcMap.remove(node);
        if (a == null) return;
        petriNet.removeArc(a.getId());
        drawingPane.getChildren().remove(node);
    }

    /**
     * Rimuove tutti gli archi connessi a un nodo specifico (posto o transizione).
     * @param nodeId id del nodo
     */
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

    // --- Helper grafici ---

    /**
     * Ottiene il cerchio esterno di un Place.
     * @param g gruppo grafico del posto
     * @return cerchio esterno
     */
    private Circle getOuterCircle(Group g) {
        return (Circle) g.getChildren().get(0);
    }

    /**
     * Ottiene il token all'interno di un Place (se c'è)
     * @param g gruppo grafico del posto
     * @return Optional con il cerchio token
     */
    private Optional<Circle> getToken(Group g) {
        return g.getChildren().stream()
                .filter(n -> "token".equals(n.getId()))
                .map(n -> (Circle) n)
                .findFirst();
    }

    /**
     * Mostra un alert di errore con header e messaggio.
     * @param header titolo dell'errore
     * @param msg contenuto dell'errore
     */
    private void showError(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(header);
        a.setContentText(msg);
        a.showAndWait();
    }

    // --- Navigazione tra schermate ---

    /**
     * Passa alla schermata AdminArea.
     * @param event evento azione
     * @throws IOException in caso di errore nel caricamento FXML
     */
    @FXML
    private void goToAdminArea(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Save Net");
        alert.setHeaderText("Do you want to save the current Petri net before returning to the admin area?");
        alert.setContentText("Choose an option:");

        ButtonType saveButton = new ButtonType("Save");
        ButtonType dontSaveButton = new ButtonType("Don't Save");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == saveButton && savePetriNet(event)) {
                switchToAdminArea(event);
            } else if (result.get() == dontSaveButton) {
                switchToAdminArea(event);
            }
        }
    }

    private void switchToAdminArea(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminArea.fxml"));
        Parent adminView = loader.load();
        AdminAreaController controller = loader.getController();
        controller.setSharedResources(sharedResources);
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
        controller.setSharedResources(sharedResources);
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML private void goToYourNets() { }    // TODO
    @FXML private void goToHelp() { }        // TODO
}
