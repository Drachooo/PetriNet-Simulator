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
     * @param transition Oggetto logico Transition associato
     * @param labelText  Etichetta della transizione (es. "T1")
     * @param posX       Coordinata X della posizione iniziale
     * @param posY       Coordinata Y della posizione iniziale
     * @param onToggleType Callback per cambiare il tipo di transizione (ADMIN/USER).
     * @return Gruppo grafico (Group) rappresentante la transizione
     */
    public static Group createTransitionNode(Transition transition, String labelText, double posX, double posY,
                                             Consumer<Transition> onToggleType) {
        Rectangle transitionRect = new Rectangle(
                -TRANSITION_WIDTH / 2,
                -TRANSITION_HEIGHT / 2,
                TRANSITION_WIDTH,
                TRANSITION_HEIGHT
        );
        transitionRect.setStroke(Color.BLACK);
        transitionRect.setStrokeWidth(2.0);

        transitionRect.setFill(Color.BLUE);
        transitionRect.setFill(transition.getType() == Type.ADMIN ? Color.RED : Color.BLUE);
        Text transitionLabel = new Text(labelText);
        transitionLabel.setMouseTransparent(true);
        transitionLabel.setY(33);
        transitionLabel.setX(-transitionLabel.getLayoutBounds().getWidth() / 2);

        Group transitionGroup = new Group(transitionRect, transitionLabel);
        transitionGroup.setLayoutX(posX);
        transitionGroup.setLayoutY(posY);


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