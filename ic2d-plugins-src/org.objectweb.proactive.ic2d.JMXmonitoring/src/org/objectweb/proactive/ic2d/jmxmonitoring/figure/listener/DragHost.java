/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Rectangle;


/**
 * This class is used to drag a host.
 * @author The ProActive Team
 */
public class DragHost extends MouseMotionListener.Stub implements MouseListener {
    private IFigure figure;
    private int deltaX;
    private int deltaY;

    //
    // -- PUBLIC METHODS ---------------------------------------------
    //

    /**
     * Set the figure to drag.
     */
    public void setFigure(IFigure figure) {
        this.figure = figure;
    }

    public void mouseReleased(MouseEvent e) {
        moveFigure(e);
        this.figure = null;
    }

    public void mouseClicked(MouseEvent e) { /*Do nothing*/
    }

    public void mouseDoubleClicked(MouseEvent e) { /*Do nothing*/
    }

    public void mousePressed(MouseEvent e) {
        this.figure = ((Figure) e.getSource());
        Rectangle rectangle = figure.getBounds();
        this.deltaX = e.x - rectangle.x;
        this.deltaY = e.y - rectangle.y;
    }

    public void mouseDragged(MouseEvent e) {
        moveFigure(e);
    }

    //
    // -- PRIVATE METHODS ---------------------------------------------
    //

    /**
     * Drag a figure.
     */
    private void moveFigure(MouseEvent e) {
        if (this.figure != null) {
            Rectangle rectangle = this.figure.getBounds();
            this.figure
                    .setBounds(new Rectangle(e.x - deltaX, e.y - deltaY, rectangle.width, rectangle.height));
        }
    }
}
