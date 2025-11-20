package application.ui.graphics;

import application.logic.Transition;
import application.logic.Type;
import application.ui.utils.Delta;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu; // Aggiunto per il menu contestuale
import javafx.scene.control.MenuItem; // Aggiunto per il menu contestuale
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.function.Consumer;

/**
 * Factory class to create the visual representation (View) for a Transition.
 * A Transition is a Group containing a Rectangle and a Text label.
 */
public class TransitionViewFactory {

    private static final double TRANSITION_WIDTH = 15.0;
    private static final double TRANSITION_HEIGHT = 40.0;

    /**
     * Creates a new graphic node for a Transition.
     *
     * @param transition The logical Transition object associated.
     * @param labelText  The transition's label (e.g., "T1").
     * @param posX       The initial X coordinate.
     * @param posY       The initial Y coordinate.
     * @param onToggleType The callback action to change the transition type (ADMIN/USER).
     * @return The JavaFX Group node representing the transition.
     */
    public static Group createTransitionNode(Transition transition, String labelText, double posX, double posY,
                                             Consumer<Transition> onToggleType) {
        // Create the rectangle (transition)
        Rectangle transitionRect = new Rectangle(
                -TRANSITION_WIDTH / 2,
                -TRANSITION_HEIGHT / 2,
                TRANSITION_WIDTH,
                TRANSITION_HEIGHT
        );
        // Sets the initial color based on the type (Red for ADMIN, Blue for USER)
        transitionRect.setStroke(Color.BLACK);
        transitionRect.setStrokeWidth(2.0);
        transitionRect.setFill(Color.BLUE);
        transitionRect.setFill(transition.getType() == Type.ADMIN ? Color.RED : Color.BLUE);

        // Create the label
        Text transitionLabel = new Text(labelText);
        transitionLabel.setMouseTransparent(true);
        transitionLabel.setY(33);
        transitionLabel.setX(-transitionLabel.getLayoutBounds().getWidth() / 2);

        // Group node
        Group transitionGroup = new Group(transitionRect, transitionLabel);
        transitionGroup.setLayoutX(posX);
        transitionGroup.setLayoutY(posY);

        // Drag handling
        Delta dragDelta = new Delta();
        transitionGroup.setOnMousePressed(event -> {
            dragDelta.x = event.getSceneX() - transitionGroup.getLayoutX();
            dragDelta.y = event.getSceneY() - transitionGroup.getLayoutY();
            transitionGroup.toFront();
        });
        transitionGroup.setOnMouseDragged(event -> {
            transitionGroup.setLayoutX(event.getSceneX() - dragDelta.x);
            transitionGroup.setLayoutY(event.getSceneY() - dragDelta.y);
        });

        ContextMenu contextMenu = new ContextMenu();
        MenuItem toggleTypeItem = new MenuItem("Toggle Admin/User Type");
        toggleTypeItem.setOnAction(event -> onToggleType.accept(transition));

        contextMenu.getItems().add(toggleTypeItem);

        transitionGroup.setOnContextMenuRequested(event ->
                contextMenu.show(transitionGroup, event.getScreenX(), event.getScreenY())
        );

        return transitionGroup;
    }
}