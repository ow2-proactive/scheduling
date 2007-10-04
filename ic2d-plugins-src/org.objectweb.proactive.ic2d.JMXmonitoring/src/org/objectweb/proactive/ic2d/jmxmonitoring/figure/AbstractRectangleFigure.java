/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.figure;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;


public abstract class AbstractRectangleFigure extends AbstractFigure {
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    protected AbstractRectangleFigure(String text) {
        super(text);
        label.setText(getTextResized(text));
    }

    /**
     * Used to display the legend.
     *
     */
    protected AbstractRectangleFigure() {
        super();
    }

    //
    // -- PUBLIC METHOD ---------------------------------------------
    //
    public ConnectionAnchor getAnchor() {
        return new ChopboxAnchor(this) {
                protected Rectangle getBox() {
                    Rectangle base = super.getBox();
                    return base.getResized(-4, -4).getTranslated(4, 4);
                }
            };
    }

    //
    // -- PROTECTED METHOD --------------------------------------------
    //
    protected void paintIC2DFigure(Graphics graphics) {
        // Inits
        Rectangle bounds = getBounds().getCopy().resize(-5, -9) /*.translate(4, 0)*/;
        final int round = 15;

        //final int sround = 30;

        // Shadow
        if (showShadow) {
            graphics.setBackgroundColor(this.shadowColor);
            graphics.fillRoundRectangle(bounds.getTranslated(4, 4), round, /*s*/
                round);
        }
        // Drawings
        graphics.setForegroundColor(this.borderColor);
        graphics.setBackgroundColor(this.backgroundColor);
        graphics.fillRoundRectangle(bounds, round, round);
        graphics.drawRoundRectangle(bounds, round, round);
        if (highlight != null) {
            graphics.drawRoundRectangle(bounds.getCopy().resize(-2, -2)
                                              .translate(1, 1), round - 3,
                round - 3);
        }

        // Cleanups
        graphics.restoreState();
    }

    protected abstract int getDefaultWidth();

    /**
     *
     * @param text
     * @return
     */
    protected String getTextResized(String text) {
        if ((text != null) && (text.length() > (getDefaultWidth() + 3))) {
            return (text.substring(0, getDefaultWidth()) + "...");
        }
        return text;
    }
}
