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
package org.objectweb.proactive.ic2d.chronolog.canvas;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.device.IUpdateNotifier;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;


/**
 * The canvas to show chart.
 * @author The ProActive Team
 */
public final class BIRTChartCanvas extends AbstractCachedCanvas implements IUpdateNotifier {

    /**
     * The device render for rendering chart.
     */
    protected IDeviceRenderer render;

    /**
     * The chart instance.
     */
    protected Chart chart;

    /**
     * The chart state.
     */
    protected GeneratedChartState state;

    /**
     * Constructs one canvas containing chart.
     * 
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param style
     *            the style of control to construct
     */
    public BIRTChartCanvas(Composite parent, int style) {
        super(parent, style);

        // initialize the SWT rendering device
        try {
            PluginSettings ps = PluginSettings.instance();
            this.render = ps.getDevice("dv.SWT");
        } catch (ChartException ex) {
            ex.printStackTrace();
        }

        render.setProperty(IDeviceRenderer.UPDATE_NOTIFIER, this);
    }

    /**
     * Builds the chart state. This method should be call when data is changed.
     * May be slow : 20-60ms Intel(R) Core(TM)2 Duo CPU E4400 @ 2.00GHz JVM 1.5 64bits
     */
    @Override
    protected void buildChart() {
        final Point size = getSize();
        Bounds bo = BoundsImpl.create(0, 0, size.x, size.y);
        int resolution = render.getDisplayServer().getDpiResolution();
        bo.scale(72d / resolution);
        try {
            this.state = Generator.instance().build(render.getDisplayServer(), chart, bo, null, null, null);
        } catch (ChartException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Draws the chart onto the cached image in the area of the given
     * <code>Rectangle</code>.
     */
    @Override
    public void drawToCachedImage() {
        GC gc = null;
        try {
            gc = new GC(super.cachedImage);
            render.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gc);
            Generator.instance().render(render, state);
        } catch (ChartException ex) {
            ex.printStackTrace();
        } finally {
            if (gc != null)
                gc.dispose();
        }
    }

    /**
     * Refreshes the chart
     */
    public void refreshChartAndRedrawCanvas() {
        //        try {
        //            // Refresh from saved state
        //            Generator.instance().refresh(this.state);
        //        } catch (ChartException ex) {
        //            ex.printStackTrace();
        //        }
        // THE MINIMAL REFRESH IS NOT ENOUGH only axes are updated (only under BIRT 2.2.1 v20070710)
        this.buildChart();
        // Draw the changes to the cached image
        this.drawToCachedImage();
        // Draw the cached image to the screen
        this.redraw();
    }

    /**
     * Returns the chart which is contained in this canvas.
     * 
     * @return the chart contained in this canvas.
     */
    public Chart getChart() {
        return chart;
    }

    /**
     * Sets the chart into this canvas. Note: When the chart is set, the cached
     * image will be dropped, but this method doesn't reset the flag
     * <code>cachedImage</code>.
     * 
     * @param chart
     *            the chart to set
     */
    public void setChart(final Chart chart) {
        if (super.cachedImage != null)
            super.cachedImage.dispose();

        super.cachedImage = null;
        this.chart = chart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.birt.chart.device.IUpdateNotifier#regenerateChart()
     */
    public void regenerateChart() {
        redraw();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.birt.chart.device.IUpdateNotifier#repaintChart()
     */
    public void repaintChart() {
        redraw();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.birt.chart.device.IUpdateNotifier#peerInstance()
     */
    public Object peerInstance() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.birt.chart.device.IUpdateNotifier#getDesignTimeModel()
     */
    public Chart getDesignTimeModel() {
        return chart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.birt.chart.device.IUpdateNotifier#getRunTimeModel()
     */
    public Chart getRunTimeModel() {
        return state.getChartModel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.birt.chart.device.IUpdateNotifier#getContext(java.lang.Object)
     */
    public Object getContext(Object arg0) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.birt.chart.device.IUpdateNotifier#putContext(java.lang.Object,
     *      java.lang.Object)
     */
    public Object putContext(Object arg0, Object arg1) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.birt.chart.device.IUpdateNotifier#removeContext(java.lang.Object)
     */
    public Object removeContext(Object arg0) {
        return null;
    }

}