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
package org.objectweb.proactive.ic2d.timit.figures.timeline;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;
import org.objectweb.proactive.ic2d.timit.data.timeline.SequenceObject;
import org.objectweb.proactive.ic2d.timit.data.timeline.Stamp;
import org.objectweb.proactive.ic2d.timit.data.timeline.TimeIntervalManager;


public class SequenceFigure extends Figure {
    public static final int DEFAULT_SEQUENCE_WIDTH = 19;
    public static final int DEFAULT_SEQUENCE_HEIGHT = 19;
    private static final Font DEFAULT_TEXT_FONT = new Font(Display.getCurrent(),
            "Courrier New", 10, SWT.BOLD);
    private Display display;
    private GC gc;
    private Image imgChart;
    private boolean bDirty;
    protected Label label;
    protected TimeIntervalManager timeIntervalManager;
    protected SequenceObject model;
    protected ScrollingGraphicalViewer viewer;
    protected int controlWidth;

    public SequenceFigure() {
        this.setPreferredSize(DEFAULT_SEQUENCE_WIDTH, DEFAULT_SEQUENCE_HEIGHT);
    }

    public SequenceFigure(TimeIntervalManager timeIntervalManager,
        SequenceObject model, ScrollingGraphicalViewer viewer) {
        this.timeIntervalManager = timeIntervalManager;
        this.model = model;
        this.viewer = viewer;
        this.display = this.viewer.getControl().getDisplay();
        this.bDirty = true;
        this.controlWidth = this.viewer.getControl().getBounds().width;
        this.setPreferredSize(DEFAULT_SEQUENCE_WIDTH, DEFAULT_SEQUENCE_HEIGHT);
        this.label = new Label(" " + model.getName() + " ");
        this.label.setFont(DEFAULT_TEXT_FONT);
        this.label.setBorder(new LineBorder());
    }

    /**
     * Use this to recompute things before painting
     * @param dirty
     */
    public void setBDirty(boolean bDirty) {
        this.bDirty = bDirty;
    }

    public Label getLabel() {
        return label;
    }

    /**
     * The overrided paintFigure method that handles all painting system.
     */
    @Override
    protected final void paintFigure(final Graphics graphics) {
        // Get the visible area
        final Rectangle visibleR = graphics.getClip(Rectangle.SINGLETON);
        if ((this.timeIntervalManager == null) ||
                !this.timeIntervalManager.isInited() || (visibleR.width <= 0) ||
                (visibleR.height <= 0)) {
            return;
        }

        // Get the client area
        final Rectangle r = this.getBounds();

        // Correct the height (eclipse 3.2 bug ??) 
        if (visibleR.height > 1) {
            visibleR.height--;
        }

        // If the view part was resized recompute before paint
        if (this.viewer.getControl().getBounds().width != this.controlWidth) {
            this.bDirty = true;
            this.controlWidth = this.viewer.getControl().getBounds().width;
        }

        if (this.bDirty) {
            this.bDirty = false;
            // OFFSCREEN IMAGE CREATION STRATEGY
            if ((this.imgChart == null) ||
                    (this.imgChart.getImageData().width != visibleR.width)) {
                if (this.gc != null) {
                    this.gc.dispose();
                }
                if (this.imgChart != null) {
                    this.imgChart.dispose();
                }
                this.imgChart = new Image(this.display, visibleR.width,
                        visibleR.height);
                this.gc = new GC(this.imgChart);
            }

            // Draw the background		
            this.gc.setBackground(AOFigure.COLOR_WHEN_WAITING_FOR_REQUEST);
            this.gc.fillRectangle(0, 0, visibleR.width, visibleR.height);

            Color color = null;
            int x1;
            int x2;
            Stamp stamp = this.model.getNextLoggedStampReversed();

            // The relative xCoords of the visible area of width visibleR.width
            int rxStartVisible = visibleR.x - r.x;
            int rxStopVisible = rxStartVisible + visibleR.width;

            boolean done = false;
            while ((stamp != null) && !done) {
                x1 = this.timeIntervalManager.getXPosition(stamp.startTime,
                        r.width);
                // If the current stamp start is not visible continue to the next
                if (x1 > rxStopVisible) {
                    stamp = this.model.getNextLoggedStampReversed();
                    continue;
                }
                done = x1 < rxStartVisible;

                x2 = this.timeIntervalManager.getXPosition(stamp.endTime,
                        r.width);

                if (stamp.state == State.SERVING_REQUEST) {
                    color = AOFigure.COLOR_WHEN_SERVING_REQUEST;
                } else if (stamp.state == State.WAITING_BY_NECESSITY) {
                    color = AOFigure.COLOR_WHEN_WAITING_BY_NECESSITY;
                } else {
                    break;
                }

                int rX1 = x1 - rxStartVisible;
                int rX2 = x2 - rxStartVisible;

                if (x1 == x2) {
                    // Draw line only if x1 is visible
                    if ((rX1 >= 0) && (rX1 <= visibleR.width)) {
                        this.gc.setForeground(color);
                        this.gc.drawLine(rX1, 0, rX1, visibleR.height);
                    }
                } else { // assert x1 < x2
                         // Draw only the visible part of the state
                    if (rX1 < 0) {
                        rX1 = 0;
                    }
                    if (rX2 > visibleR.width) {
                        rX2 = visibleR.width;
                    }
                    this.gc.setBackground(color);
                    this.gc.fillRectangle(rX1, 0, (rX2 - rX1), visibleR.height);
                }

                stamp = this.model.getNextLoggedStampReversed();
            }
        }

        // Rewind the sequence
        this.model.rewind();

        if (imgChart != null) {
            graphics.drawImage(imgChart, visibleR.x, visibleR.y);
            graphics.setForegroundColor(ColorConstants.black);
            graphics.drawRectangle(visibleR);
        }
    }
}
