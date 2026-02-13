package application.ui.graphics;

import application.logic.Place;
import application.ui.utils.Delta;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class PlaceViewFactory {

     /**
     * Creates a graphical node (circle with label) for a Petri Net Place.
     *
     * @param place        The logical Place object.
     * @param labelText    The text to display (e.g., "P1", "P2", ...).
     * @param x            The initial X coordinate.
     * @param y            The initial Y coordinate.
     * @param onInitial    The action to execute when designated as initial (controller callback).
     * @param onFinal      The action to execute when designated as final (controller callback).
     * @return             The JavaFX Group node representing the place.
     */
    public static Group createPlaceNode(
            Place place,
            String labelText,
            double x,
            double y,
            Consumer<Place> onInitial,
            Consumer<Place> onFinal
    ) {
        //Gray circle, black border
        Circle placeCircle = new Circle(0, 0, 20, Color.LIGHTBLUE);
        placeCircle.setStroke(Color.BLACK);

        // Place Label (e.g "P1")
        Text placeLabel = new Text(labelText);
        placeLabel.setMouseTransparent(true);
        placeLabel.setY(35);
        placeLabel.setX(-placeLabel.getLayoutBounds().getWidth() / 2);

        // Node that contains place and label
        Group placeNode = new Group(placeCircle, placeLabel);
        placeNode.setLayoutX(x);
        placeNode.setLayoutY(y);

        //Dragging handling
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

        // Menu to set place as initial or final
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
