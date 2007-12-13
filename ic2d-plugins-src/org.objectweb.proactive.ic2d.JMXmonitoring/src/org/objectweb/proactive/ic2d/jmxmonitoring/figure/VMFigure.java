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
package org.objectweb.proactive.ic2d.jmxmonitoring.figure;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;


public class VMFigure extends AbstractRectangleFigure {
    protected final static int DEFAULT_WIDTH = 19;
    private IFigure contentPane;
    public static final Color STANDARD_COLOR;
    public static final Color GLOBUS_COLOR;
    public static final Color NOT_RESPONDING = ColorConstants.red;
    private static final Color DEFAULT_BORDER_COLOR;

    static {
        Display device = Display.getCurrent();
        STANDARD_COLOR = new Color(device, 240, 240, 240);
        GLOBUS_COLOR = new Color(device, 255, 208, 208);
        DEFAULT_BORDER_COLOR = new Color(device, 140, 200, 225);
    }

    //
    // -- CONSTRUCTOR -----------------------------------------------
    //
    public VMFigure(String text) {
        super(text);
    }

    /**
     * Used to display the legend
     */
    public VMFigure() {
        super();
    }

    //
    // -- PUBLIC METHOD --------------------------------------------
    //
    public IFigure getContentPane() {
        return contentPane;
    }

    /**
     * To indicate that the JVM started with Globus.
     */
    public void withGlobus() {
        backgroundColor = GLOBUS_COLOR;
        this.repaint();
    }

    /**
     * To indicate that the JVM does not answer any more.
     */
    public void notResponding() {
        backgroundColor = NOT_RESPONDING;
        this.repaint();
    }

    //
    // -- PROTECTED METHOD --------------------------------------------
    //
    protected void initColor() {
        Device device = Display.getCurrent();
        borderColor = DEFAULT_BORDER_COLOR;
        backgroundColor = STANDARD_COLOR;
        shadowColor = new Color(device, 230, 230, 230);
    }

    protected void initFigure() {
        BorderLayout layout = new VMBorderLayout();
        layout.setVerticalSpacing(5);
        setLayoutManager(layout);
        add(label, BorderLayout.TOP);

        contentPane = new Figure();
        ToolbarLayout contentPaneLayout = new VMToolbarLayout();
        contentPaneLayout.setSpacing(10);
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
        return DEFAULT_BORDER_COLOR;
    }

    //
    // -- INNER CLASS --------------------------------------------
    //
    private class VMBorderLayout extends BorderLayout {
        protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
            if (legend) {
                return super.calculatePreferredSize(container, wHint, hHint).expand( /*90*/
                50, /*5*/
                0);
            }

            return super.calculatePreferredSize(container, wHint, hHint).expand(5, 0);
        }
    }

    private class VMToolbarLayout extends ToolbarLayout {
        public VMToolbarLayout() {
            super(false);
        }

        protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
            return super.calculatePreferredSize(container, wHint, hHint).expand(0, 8);
        }
    }
}
