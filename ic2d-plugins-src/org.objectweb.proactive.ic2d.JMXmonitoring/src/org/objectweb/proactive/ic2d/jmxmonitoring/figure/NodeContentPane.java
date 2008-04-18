package org.objectweb.proactive.ic2d.jmxmonitoring.figure;

import org.eclipse.draw2d.IFigure;


public class NodeContentPane extends org.eclipse.draw2d.Figure {

    @Override
    public void remove(IFigure figure) {
        //System.out.println("NodeContentPane.remove() ---> "+ figure);
        if ((figure.getParent() != this)) {
            System.out.println("NodeContentPane: figure " + figure + " not a child of NodeConentPane");
        }

        super.remove(figure);

    }
}
