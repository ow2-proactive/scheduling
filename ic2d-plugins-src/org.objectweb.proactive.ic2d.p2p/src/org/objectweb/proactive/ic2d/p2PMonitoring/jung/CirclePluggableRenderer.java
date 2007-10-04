package org.objectweb.proactive.ic2d.p2PMonitoring.jung;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.objectweb.proactive.ProActive;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.contrib.CircleLayout;


public class CirclePluggableRenderer extends PluggableRenderer {
    protected CircleLayout layout;

    public CirclePluggableRenderer() {
        super();
    }

    public CirclePluggableRenderer(CircleLayout l) {
        layout = l;
    }

    @Override
    protected void labelVertex(Graphics g, Vertex v, String label, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;

        //Component component = prepareRenderer(graphLabelRenderer, label, isPicked(v), v);
        Component component = new RotatableLabel(label,
                this.layout.getCircleData(v).getAngle());
        Dimension d = component.getPreferredSize();

        int h_offset;
        int v_offset;
        if (centerVertexLabel) {
            h_offset = -d.width / 2;
            v_offset = -d.height / 2;
        } else {
            Rectangle2D bounds = vertexShapeFunction.getShape(v).getBounds2D();
            h_offset = (int) (bounds.getWidth() / 2) + 5;
            v_offset = ((int) (bounds.getHeight() / 2) + 5) - d.height;
        }
        this.layout.getCircleData(v).getAngle();

        rendererPane.paintComponent(g, component, screenDevice, x + h_offset,
            y + v_offset, d.width, d.height, true);
        // g2d.rotate(1);
        //		System.out.println("CirclePluggableRenderer.labelVertex() x="+x + " y="+y);
        //		System.out.println(layout.getCircleData(v).getAngle());
        //System.out.println(component);
    }
}
