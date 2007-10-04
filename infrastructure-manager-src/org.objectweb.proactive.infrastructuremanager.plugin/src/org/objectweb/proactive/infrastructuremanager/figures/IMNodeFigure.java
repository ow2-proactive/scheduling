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
package org.objectweb.proactive.infrastructuremanager.figures;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.infrastructuremanager.IMConstants;


public class IMNodeFigure extends AbstractRectangleFigure {
    protected final static int DEFAULT_WIDTH = 17;
    private IFigure contentPane;

    //
    // -- CONSTRUCTOR -----------------------------------------------
    //

    /**
     * Create a new node figure
     * @param text The text to display
     * @param protocol The protocol used
     */
    public IMNodeFigure(String text, String status) {
        super(text);
        setStatus(status);
    }

    /**
     * Creates a new node figure (used to display the legend)
     * @param protocol The protocol used
     */
    public IMNodeFigure(String status) {
        super();
        setStatus(status);
    }

    @Override
    public IFigure getContentPane() {
        return contentPane;
    }

    public void setStatus(String status) {
        if (status.equals(IMConstants.STATUS_AVAILABLE)) {
            backgroundColor = IMConstants.AVAILABLE_COLOR;
        } else if (status.equals(IMConstants.STATUS_BUSY)) {
            backgroundColor = IMConstants.BUSY_COLOR;
        } else if (status.equals(IMConstants.STATUS_DOWN)) {
            backgroundColor = IMConstants.DOWN_COLOR;
        }
    }

    //
    // -- PROTECTED METHODS --------------------------------------------
    //
    @Override
    protected void initColor() {
        Device device = Display.getCurrent();
        borderColor = IMConstants.DEFAULT_BORDER_COLOR;
        shadowColor = new Color(device, 230, 230, 230);
    }

    @Override
    protected void initFigure() {
        BorderLayout layout = new NodeBorderLayout();
        layout.setVerticalSpacing(5);
        setLayoutManager(layout);

        add(label, BorderLayout.TOP);

        contentPane = new Figure();
        ToolbarLayout contentPaneLayout = new NodeToolbarLayout();
        contentPaneLayout.setSpacing(0);
        contentPaneLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
        contentPane.setLayoutManager(contentPaneLayout);
        add(contentPane, BorderLayout.CENTER);
    }

    @Override
    protected int getDefaultWidth() {
        return DEFAULT_WIDTH;
    }

    @Override
    protected Color getDefaultBorderColor() {
        return IMConstants.DEFAULT_BORDER_COLOR;
    }

    //
    // -- INNER CLASS --------------------------------------------
    //
    private class NodeBorderLayout extends BorderLayout {
        @Override
        protected Dimension calculatePreferredSize(IFigure container,
            int wHint, int hHint) {
            if (legend) {
                return super.calculatePreferredSize(container, wHint, hHint)
                            .expand(50, -5);
            }
            return super.calculatePreferredSize(container, wHint, hHint)
                        .expand(20, -10);
        }
    }

    private class NodeToolbarLayout extends ToolbarLayout {
        public NodeToolbarLayout() {
            super(false);
        }

        @Override
        protected Dimension calculatePreferredSize(IFigure container,
            int wHint, int hHint) {
            return super.calculatePreferredSize(container, wHint, hHint)
                        .expand(0, 15);
        }
    }
}
