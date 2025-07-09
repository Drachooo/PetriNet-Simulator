package application.ui.graphics;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Classe per creare graficamente un arco tra due nodi (Place o Transition).
 */
public class ArcViewFactory {

    /**
     * Crea una linea visiva che rappresenta un arco tra due nodi.
     *
     * @param sourceNode Nodo sorgente (Place o Transition)
     * @param targetNode Nodo destinazione (Place o Transition)
     * @return Oggetto Line che rappresenta graficamente l’arco
     */
    public static Line createArcLine(Node sourceNode, Node targetNode) {
        Line arcLine = new Line();
        bindLineToNodes(arcLine, sourceNode, targetNode);
        applyArcStyle(arcLine);
        return arcLine;
    }

    /**
     * Collega le estremità della linea alle posizioni dei due nodi.
     *
     * @param arcLine     Linea da collegare
     * @param sourceNode  Nodo sorgente
     * @param targetNode  Nodo destinazione
     */
    private static void bindLineToNodes(Line arcLine, Node sourceNode, Node targetNode) {
        arcLine.startXProperty().bind(sourceNode.layoutXProperty());
        arcLine.startYProperty().bind(sourceNode.layoutYProperty());
        arcLine.endXProperty().bind(targetNode.layoutXProperty());
        arcLine.endYProperty().bind(targetNode.layoutYProperty());
    }

    /**
     *  Applico uno stile all'arco.
     *
     * @param arcLine Linea da personalizzare
     */
    private static void applyArcStyle(Line arcLine) {
        arcLine.setStroke(Color.BLACK);
        arcLine.setStrokeWidth(2);
    }
}
