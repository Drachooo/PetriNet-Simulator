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

public class NetCreationController implements Initializable {

    // modalità disegno attiva
    private enum DrawingMode { NONE, PLACE, TRANSITION, ARC }

    private DrawingMode currentMode = DrawingMode.NONE;
    private Node arcSourceNode;

    // dati logici e viste
    private PetriNet petriNet;
    private final Map<Node, Place> placeMap = new HashMap<>();
    private final Map<Place, Group> placeViewMap = new HashMap<>();
    private final Map<Node, Transition> transitionMap = new HashMap<>();
    private final Map<Node, Arc> arcMap = new HashMap<>();
    private final Deque<Node> undoStack = new ArrayDeque<>();

    // elementi FXML
    @FXML private Pane drawingPane;
    @FXML private ScrollPane scrollPane;

    private SharedResources sharedResources;

    public void setSharedResources(SharedResources sr) {
        this.sharedResources = sr;
        initData();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        drawingPane.setOnMouseClicked(this::onDrawingPaneClicked);
        scrollPane.setPannable(true);
    }

    // inizializza nuova rete
    private void initData() {
        int idx = sharedResources.getPetriNetRepository().getPetriNets().size() + 1;
        String netName = "NP" + idx;
        String adminId = sharedResources.getCurrentUser().getId();
        petriNet = new PetriNet(netName, adminId);
    }

    // crea posto alla posizione cliccata
    private void createPlace(double x, double y) {
        String name = "P" + petriNet.getPlaces().size();
        Place place = new Place(petriNet.getId(), name);
        petriNet.addPlace(place);

        int number = petriNet.getPlaces().size();
        String n = "P" + number;
        Group group = PlaceViewFactory.createPlaceNode(place,n, x, y, this::designateInitial, this::designateFinal);
        drawingPane.getChildren().add(group);
        placeMap.put(group, place);
        placeViewMap.put(place, group);
        undoStack.push(group);
    }

    // imposta un posto come finale
    private void designateFinal(Place p) {
        resetPlaceVisual(petriNet.getFinalPlace());
        petriNet.setFinal(p);
        Group g = placeViewMap.get(p);
        getOuterCircle(g).setStroke(Color.VIOLET);
        getToken(g).ifPresent(tok -> g.getChildren().remove(tok));
    }

    // imposta un posto come iniziale (con token)
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

    // resetta stile posto (nessun colore, niente token)
    private void resetPlaceVisual(Place p) {
        if (p == null) return;
        Group g = placeViewMap.get(p);
        if (g == null) return;
        getOuterCircle(g).setStroke(Color.BLACK);
        getToken(g).ifPresent(tok -> g.getChildren().remove(tok));
    }

    // crea transizione alla posizione cliccata
    private void createTransition(double x, double y) {
        String name = "T" + (petriNet.getTransitions().size() + 1);
        Transition t = new Transition(petriNet.getId(), name, Type.USER);
        petriNet.addTransition(t);


        int number = petriNet.getTransitions().size();
        String n = "T" + number;
        Group group = TransitionViewFactory.createTransitionNode(t,n,x, y, this::toggleTransitionType);
        drawingPane.getChildren().add(group);
        transitionMap.put(group, t);
        undoStack.push(group);
    }

    // cambia tipo della transizione (USER <-> ADMIN)
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

    // trova il Group grafico per una transizione
    private Group getGroupForTransition(Transition t) {
        return transitionMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(t))
                .map(Map.Entry::getKey)
                .filter(n -> n instanceof Group)
                .map(n -> (Group) n)
                .findFirst().orElse(null);
    }

    // crea arco tra due nodi (posto o transizione)
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

    // cerca nodo a coordinate (x,y)
    private Node findNodeAt(double x, double y) {
        for (Node n : placeMap.keySet())
            if (n.contains(x - n.getLayoutX(), y - n.getLayoutY())) return n;
        for (Node n : transitionMap.keySet())
            if (n.contains(x - n.getLayoutX(), y - n.getLayoutY())) return n;
        return null;
    }

    // mostra errore in popup
    private void showError(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(header);
        a.setContentText(msg);
        a.showAndWait();
    }

    // gestisce click sul pane disegno
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
            default -> {}
        }
    }

    @FXML private void drawPlace() { currentMode = DrawingMode.PLACE; }
    @FXML private void drawTransition() { currentMode = DrawingMode.TRANSITION; }
    @FXML private void drawArc() { currentMode = DrawingMode.ARC; arcSourceNode = null; }

    // helpers grafici
    private Circle getOuterCircle(Group g) {
        return (Circle) g.getChildren().get(0);
    }

    private Optional<Circle> getToken(Group g) {
        return g.getChildren().stream()
                .filter(n -> "token".equals(n.getId()))
                .map(n -> (Circle) n)
                .findFirst();
    }

    // navigazione interfaccia
    @FXML private void goToAdminArea(ActionEvent e) throws IOException {
        FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/AdminArea.fxml"));
        Parent r = l.load();
        l.<AdminAreaController>getController().setSharedResources(sharedResources);
        Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
        s.setScene(new Scene(r));
        s.show();
    }

    @FXML private void goToExploreNets() { } // TODO
    @FXML private void goToYourNets() { }    // TODO
    @FXML private void goToHelp() { }        // TODO

    @FXML private void logOut(ActionEvent e) throws IOException {
        FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent r = l.load();
        l.<LoginViewController>getController().setSharedResources(sharedResources);
        Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
        s.setScene(new Scene(r));
        s.show();
    }

    @FXML
    private void savePetriNet(ActionEvent e) {
        try {
            petriNet.validate();
            sharedResources.getPetriNetRepository().savePetriNet(petriNet);

            // popup conferma
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Net Saved");
            alert.setContentText("Your Net was saved successfully.");
            alert.showAndWait();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            // popup errore
            showError("Validation error! ", ex.getMessage());
        }
    }

}
