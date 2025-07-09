package application.ui.graphics;

import application.logic.Place;
import application.ui.utils.Delta;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class PlaceViewFactory {

    /**
     * Crea un nodo grafico (cerchio con etichetta) per un place della rete di Petri.
     *
     * @param place        oggetto LOFICO Place
     * @param labelText    testo da mostrare (es. "P1", "P2", ...)
     * @param x            X iniziale
     * @param y            Y iniziale
     * @param onInitial    azione da eseguire se viene scelto come iniziale
     * @param onFinal      azione da eseguire se viene scelto come finale
     * @return             NODO JavaFX Group rappresentante il posto
     */
    public static Group createPlaceNode(
            Place place,
            String labelText,
            double x,
            double y,
            Consumer<Place> onInitial,
            Consumer<Place> onFinal
    ) {
        // Cerchio grigio con bordo nero
        Circle placeCircle = new Circle(0, 0, 20, Color.GRAY);
        placeCircle.setStroke(Color.BLACK);

        // Etichetta del place (es. "P1")
        Text placeLabel = new Text(labelText);
        placeLabel.setMouseTransparent(true);
        placeLabel.setY(35);
        placeLabel.setX(-placeLabel.getLayoutBounds().getWidth() / 2);

        // Nodo di gruppo contenente il cerchio e l'etichetta
        Group placeNode = new Group(placeCircle, placeLabel);
        placeNode.setLayoutX(x);
        placeNode.setLayoutY(y);

        // Gestione del trascinamento
        Delta dragDelta = new Delta();
        placeNode.setOnMousePressed(event -> {
            dragDelta.x = event.getSceneX() - placeNode.getLayoutX();
            dragDelta.y = event.getSceneY() - placeNode.getLayoutY();
            placeNode.toFront();
            event.consume();
        });
        placeNode.setOnMouseDragged(event -> {
            placeNode.setLayoutX(event.getSceneX() - dragDelta.x);
            placeNode.setLayoutY(event.getSceneY() - dragDelta.y);
        });

        // Menu per impostare iniziale/finale
        ContextMenu contextMenu = new ContextMenu();
        MenuItem setInitialItem = new MenuItem("Set as Initial");
        MenuItem setFinalItem = new MenuItem("Set as Final");

        setInitialItem.setOnAction(event -> onInitial.accept(place));
        setFinalItem.setOnAction(event -> onFinal.accept(place));
        contextMenu.getItems().addAll(setInitialItem, setFinalItem);

        placeNode.setOnContextMenuRequested(event ->
                contextMenu.show(placeNode, event.getScreenX(), event.getScreenY())
        );

        return placeNode;
    }
}
