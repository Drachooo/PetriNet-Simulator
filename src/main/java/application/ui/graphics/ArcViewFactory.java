package application.ui.graphics;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Factory class to create the visual representation (View) for an Arc (Line)
 * connecting two nodes (Place or Transition).
 */
public class ArcViewFactory {

    /**
     * Creates a visual line representing an arc between two nodes.
     *
     * @param sourceNode The source node (Place or Transition).
     * @param targetNode The target node (Place or Transition).
     * @return A Line object representing the arc graphically.
     */
    public static Line createArcLine(Node sourceNode, Node targetNode) {
        Line arcLine = new Line();
        bindLineToNodes(arcLine, sourceNode, targetNode);
        applyArcStyle(arcLine);
        return arcLine;
    }

    /**
     * Binds the endpoints of the line to the layout positions of the two nodes.
     * This ensures the line automatically moves when the nodes are dragged.
     *
     * @param arcLine     The Line to be connected.
     * @param sourceNode  The source node.
     * @param targetNode  The target node.
     */
    private static void bindLineToNodes(Line arcLine, Node sourceNode, Node targetNode) {
        // Binds the line start/end points to the center coordinates of the nodes.
        arcLine.startXProperty().bind(sourceNode.layoutXProperty());
        arcLine.startYProperty().bind(sourceNode.layoutYProperty());
        arcLine.endXProperty().bind(targetNode.layoutXProperty());
        arcLine.endYProperty().bind(targetNode.layoutYProperty());
    }

    /**
     * Applies the default visual style (color and width) to the arc line.
     *
     * @param arcLine The line to style.
     */
    private static void applyArcStyle(Line arcLine) {
        arcLine.setStroke(Color.BLACK);
        arcLine.setStrokeWidth(2);
    }
}