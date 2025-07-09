package application.ui.graphics;

import application.logic.Transition;
import application.logic.Type;
import application.ui.utils.Delta;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class TransitionViewFactory {

    /**
     * Crea il nodo grafico che rappresenta una Transizione.
     *
     * @param transition Oggetto logico Transition associato
     * @param labelText  Etichetta della transizione (es. "T1")
     * @param posX       Coordinata X della posizione iniziale
     * @param posY       Coordinata Y della posizione iniziale
     * @param onDoubleClick Callback per doppio click (cambia tipo transizione)
     * @return Gruppo grafico (Group) rappresentante la transizione
     */
    public static Group createTransitionNode(Transition transition, String labelText, double posX, double posY,
                                             Consumer<Transition> onDoubleClick) {
        // Rettangolo blu con bordo nero che rappresenta la transizione
        Rectangle transitionRect = new Rectangle(-10, -20, 20, 40);
        transitionRect.setFill(Color.BLUE);
        transitionRect.setStroke(Color.BLACK);

        // Testo dell'etichetta, centrato SOTTO la transizione
        Text transitionLabel = new Text(labelText);
        transitionLabel.setMouseTransparent(true);
        transitionLabel.setY(33);
        transitionLabel.setX(-transitionLabel.getLayoutBounds().getWidth() / 2);

        // Gruppo che contiene il rettangolo e il testo
        Group transitionGroup = new Group(transitionRect, transitionLabel);
        transitionGroup.setLayoutX(posX);
        transitionGroup.setLayoutY(posY);

        // Se clicco 2 volte la transizione, cambia colore !!!! :)))
        transitionRect.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                onDoubleClick.accept(transition);
            }
        });

        // drag & drop per spostare la transizione nel pane :-o
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

        return transitionGroup;
    }
}
