package org.objectweb.proactive.ic2d.jmxmonitoring.editpart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;


public class GraphicalCommunication {
    private AOFigure source;
    private AOFigure destination;
    private IFigure panel;
    private Color color;

    /**
     *
     *
     * @param target the target of the connection
     * @param panel the connection is added to this panel
     */
    public GraphicalCommunication(AOFigure source, AOFigure destination,
        IFigure panel, Color color) {
        this.source = source;
        this.destination = destination;
        this.panel = panel;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public AOFigure getDestination() {
        return destination;
    }

    public IFigure getPanel() {
        return panel;
    }

    public AOFigure getSource() {
        return source;
    }

    /**
     * Draw the communication
     */
    public void draw() {
        source.addConnection(destination, panel, color);
    }

    @Override
    public boolean equals(Object o) {
        GraphicalCommunication communication = (GraphicalCommunication) o;
        return ((this.source == communication.getSource()) &&
        (this.destination == communication.getDestination()) &&
        (this.panel == communication.getPanel()) &&
        (this.color == communication.getColor()));
    }
}
