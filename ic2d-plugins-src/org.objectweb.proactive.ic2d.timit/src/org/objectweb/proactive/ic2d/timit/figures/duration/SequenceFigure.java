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
package org.objectweb.proactive.ic2d.timit.figures.duration;

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
import org.objectweb.proactive.ic2d.jmxmonitoring.data.State;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;
import org.objectweb.proactive.ic2d.timit.data.duration.SequenceObject;
import org.objectweb.proactive.ic2d.timit.data.duration.Stamp;


public class SequenceFigure extends Figure {
    public static final int DEFAULT_SEQUENCE_WIDTH = 19;
    public static final int DEFAULT_SEQUENCE_HEIGHT = 19;
    private static final Font DEFAULT_TEXT_FONT = new Font(Display.getCurrent(),
            "Courrier New", 10, SWT.BOLD);
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
        this.bDirty = true;
        this.controlWidth = this.viewer.getControl().getBounds().width;
        this.setOpaque(true);
        this.setPreferredSize(DEFAULT_SEQUENCE_WIDTH, DEFAULT_SEQUENCE_HEIGHT);
        this.setBackgroundColor(AOFigure.COLOR_WHEN_WAITING_FOR_REQUEST);
        this.label = new Label(" " + model.getName() + " ");
        this.label.setFont(DEFAULT_TEXT_FONT);
        this.label.setBorder(new LineBorder());
    }

    public void setBDirty(boolean dirty) {
        bDirty = dirty;
    }

    public Label getLabel() {
        return label;
    }

    /**
     * The overrided paintFigure method that handles all painting system.
     */
    @Override
    protected final void paintFigure(final Graphics graphics) {
        final Rectangle r = getClientArea();
        if ((this.timeIntervalManager == null) ||
                !this.timeIntervalManager.inited || (r.width <= 0) ||
                (r.height <= 0)) {
            return;
        }

        if (this.viewer.getControl().getBounds().width != this.controlWidth) {
            this.bDirty = true;
            this.controlWidth = this.viewer.getControl().getBounds().width;
        }

        if (this.bDirty) {
            this.bDirty = false;
            final Display d = Display.getCurrent();

            // OFFSCREEN IMAGE CREATION STRATEGY
            if ((this.imgChart == null) ||
                    (this.imgChart.getImageData().width != r.width)) {
                if (this.gc != null) {
                    this.gc.dispose();
                }
                if (this.imgChart != null) {
                    this.imgChart.dispose();
                }
                this.imgChart = new Image(d, r.width, r.height);
                this.gc = new GC(this.imgChart);
            }

            // Draw the background		
            this.gc.setBackground(AOFigure.COLOR_WHEN_WAITING_FOR_REQUEST);
            this.gc.fillRectangle(0, 0, r.width, r.height);
            int x1;
            int x2;

            Stamp stamp = this.model.getNextLoggedStampReversed();
            Color color = null;
            while (stamp != null) {
                if (stamp.state == State.SERVING_REQUEST) {
                    color = AOFigure.COLOR_WHEN_SERVING_REQUEST;
                } else if (stamp.state == State.WAITING_BY_NECESSITY) {
                    color = AOFigure.COLOR_WHEN_WAITING_BY_NECESSITY;
                } else {
                    break;
                }

                x1 = this.timeIntervalManager.getXPosition(stamp.startTime,
                        r.width);
                x2 = this.timeIntervalManager.getXPosition(stamp.endTime,
                        r.width);

                if (x1 == x2) {
                    this.gc.setForeground(color);
                    this.gc.drawLine(x1, 0, x1, r.height);
                } else {
                    this.gc.setBackground(color);
                    this.gc.fillRectangle(x1, 0, (x2 - x1), r.height);
                }

                stamp = this.model.getNextLoggedStampReversed();
            }
        }

        if (imgChart != null) {
            graphics.drawImage(imgChart, r.x, r.y);
        }
    }
}
