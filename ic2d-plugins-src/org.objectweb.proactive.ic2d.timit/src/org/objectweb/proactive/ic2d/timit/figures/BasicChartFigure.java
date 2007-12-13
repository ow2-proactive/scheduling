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
package org.objectweb.proactive.ic2d.timit.figures;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.device.IDisplayServer;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.ui.util.ChartUIUtil;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;


public class BasicChartFigure extends Figure {
    public static final Color UNSELECTED_BORDER_COLOR = Display.getCurrent().getSystemColor(
            SWT.COLOR_DARK_GRAY);
    public static final Color SELECTED_BORDER_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
    private static int UNSELECTED_BORDER_SIZE = 2;
    private static int SELECTED_BORDER_SIZE = 4;

    ////////////////////////////////////////////////////////
    private Color currentBorderColor;
    private int currentBorderSize;
    private GeneratedChartState state;
    private Generator gr;
    private Image imgChart;
    private GC gc;
    private IDeviceRenderer idr;
    private double scaleValue;
    private Chart chart;
    private boolean bDirty;
    private Bounds bo;

    public BasicChartFigure(final Chart chart) {
        this.chart = chart;

        this.currentBorderColor = UNSELECTED_BORDER_COLOR;
        this.currentBorderSize = UNSELECTED_BORDER_SIZE;

        this.gr = Generator.instance();
        final IDisplayServer idsSWT = ChartUIUtil.getDisplayServer();
        this.scaleValue = 72d / idsSWT.getDpiResolution();
        this.bo = chart.getBlock().getBounds().scaledInstance(this.scaleValue);
        this.bDirty = true;
        try {
            idr = PluginSettings.instance().getDevice("dv.SWT");
        } catch (Exception pex) {
            pex.printStackTrace();
        }
    }

    /**
     *
     * @param chart
     */
    public final void setChart(final Chart chart) {
        this.chart = chart;
        this.bDirty = true;
        // Since the chart has changed we need to compute a new ChartState 
        this.computeState();
    }

    public final void setSelected() {
        this.currentBorderColor = SELECTED_BORDER_COLOR;
        this.currentBorderSize = SELECTED_BORDER_SIZE;
        this.bDirty = false;
        this.repaint();
    }

    public final void setUnselected() {
        this.currentBorderColor = UNSELECTED_BORDER_COLOR;
        this.currentBorderSize = UNSELECTED_BORDER_SIZE;
        this.bDirty = false;
        this.repaint();
    }

    /**
     * The overrided paintFigure method that handles all
     * painting system.
     */
    @Override
    protected final void paintFigure(final Graphics graphics) {
        final Rectangle r = getClientArea();
        if ((r.width <= 0) || (r.height <= 0)) {
            return;
        }

        if (this.bDirty) {
            this.bDirty = false;
            final Display d = Display.getCurrent();

            // OFFSCREEN IMAGE CREATION STRATEGY
            if ((this.imgChart == null) || (this.imgChart.getImageData().width != r.width) ||
                (imgChart.getImageData().height != r.height)) {
                if (this.gc != null) {
                    this.gc.dispose();
                }
                if (this.imgChart != null) {
                    this.imgChart.dispose();
                }

                this.imgChart = new Image(d, r.width, r.height);
                this.gc = new GC(this.imgChart);

                this.computeState();

                this.idr.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, this.gc);
            }

            try {
                this.gr.render(this.idr, this.state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (imgChart != null) {
            graphics.drawImage(imgChart, r.x, r.y);
        }
        graphics.setLineWidth(this.currentBorderSize);
        graphics.setForegroundColor(this.currentBorderColor);
        graphics.drawRectangle(r.x + (this.currentBorderSize / 2), r.y + (this.currentBorderSize / 2),
                r.width - this.currentBorderSize, r.height - this.currentBorderSize);
    }

    private final void computeState() {
        try {
            // RESCALE THE RENDERING AREA
            this.bo.setWidth(getClientArea().width);
            this.bo.setHeight(getClientArea().height);
            this.bo.scale(this.scaleValue);
            this.state = gr.build(this.idr.getDisplayServer(), this.chart, this.bo, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
