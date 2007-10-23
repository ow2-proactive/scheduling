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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;


public class TimeAxisFigure extends Figure {

    /**
     * The size of the reference text.
     */
    public static int referenceXSize = 90;
    public static int referenceXSizeHalf = referenceXSize / 2;
    public static int referenceYSize = 10;
    public static int DEFAULT_DASH_SIZE = 5;
    public static int DEFAULT_TIME_TEXT_HEIGHT_MARGIN = 15;
    private static final Font DEFAULT_TEXT_FONT = new Font(Display.getCurrent(),
            "Courrier New", 10, SWT.NONE);
    private static final Color DEFAULT_TIME_LINE_COLOR = new Color(Display.getCurrent(),
            225, 225, 225);
    protected TimeIntervalManager timeIntervalManager;

    public TimeAxisFigure(TimeIntervalManager timeIntervalManager) {
        this.setOpaque(false);
        this.timeIntervalManager = timeIntervalManager;
    }

    /**
     * The overrided paintFigure method that handles all painting system.
     * TODO : Optimize me !!
     * TODO : ADD OFF SCREEN IMAGE STRATEGY
     */
    @Override
    protected final void paintFigure(final Graphics graphics) {
        final Rectangle r = getClientArea();
        if ((r.width <= 0) || (r.height <= 0) ||
                (this.timeIntervalManager.endTime == 0)) {
            return;
        }
        graphics.setForegroundColor(DEFAULT_TIME_LINE_COLOR);
        graphics.drawLine(r.x, (r.y + r.height) - 20, r.x + r.width,
            (r.y + r.height) - 20);

        double nrReferences = r.width / referenceXSize;
        long timeStep = Math.round(this.timeIntervalManager.timeInterval / nrReferences);
        this.timeIntervalManager.timeStep = timeStep;
        int stepInPixels = this.timeIntervalManager.getXPosition(timeStep,
                r.width);
        long rawTimeCounter = timeStep; // Skip the first step
        int timeStepPosInPixels = r.x + stepInPixels; // Skip the first step							

        // Set the default font
        graphics.setFont(DEFAULT_TEXT_FONT);

        // Write first value
        String sTime = TimeIntervalManager.convertTimeInMicrosToString(rawTimeCounter);

        graphics.setForegroundColor(ColorConstants.gray);
        int defaultNbDash = (r.height - DEFAULT_TIME_TEXT_HEIGHT_MARGIN) / DEFAULT_DASH_SIZE;
        int nbDash = defaultNbDash;
        while (nbDash > 0) {
            graphics.drawLine(timeStepPosInPixels,
                r.y + (nbDash * DEFAULT_DASH_SIZE), timeStepPosInPixels,
                r.y + ((nbDash - 1) * DEFAULT_DASH_SIZE));
            nbDash -= 2;
        }
        graphics.setForegroundColor(ColorConstants.black);
        graphics.drawString("0ms", r.x,
            (r.y + r.height) - DEFAULT_TIME_TEXT_HEIGHT_MARGIN);
        int correctCrop = FigureUtilities.getTextExtents(sTime,
                DEFAULT_TEXT_FONT).width;
        graphics.drawString(sTime, (timeStepPosInPixels - (correctCrop / 2)),
            (r.y + r.height) - DEFAULT_TIME_TEXT_HEIGHT_MARGIN);

        // Write other time values
        do {
            timeStepPosInPixels += stepInPixels;
            rawTimeCounter += timeStep;

            sTime = TimeIntervalManager.convertTimeInMicrosToString(rawTimeCounter); //convertTimeInMillisToString(rawTimeCounter);			

            graphics.setForegroundColor(ColorConstants.gray);
            nbDash = defaultNbDash;
            while (nbDash > 0) {
                graphics.drawLine(timeStepPosInPixels,
                    r.y + (nbDash * DEFAULT_DASH_SIZE), timeStepPosInPixels,
                    r.y + ((nbDash - 1) * DEFAULT_DASH_SIZE));
                nbDash -= 2;
            }
            correctCrop = FigureUtilities.getTextExtents(sTime,
                    DEFAULT_TEXT_FONT).width;
            graphics.setForegroundColor(ColorConstants.black);
            graphics.drawString(sTime,
                (timeStepPosInPixels - (correctCrop / 2)),
                (r.y + r.height) - DEFAULT_TIME_TEXT_HEIGHT_MARGIN);
        } while (timeStepPosInPixels < (r.width - referenceXSizeHalf));
    }
}
