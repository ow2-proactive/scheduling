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
package org.objectweb.proactive.ic2d.timit.editors.canvas;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.device.IUpdateNotifier;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;


/**
 * The canvas to show chart.
 * @author The ProActive Team
 */
public final class GenericChartCanvas extends Canvas implements IUpdateNotifier {

    /**
     * The device render for rendering chart.
     */
    protected IDeviceRenderer render = null;

    /**
     * The chart instance.
     */
    protected Chart chart = null;

    /**
     * The chart state.
     */
    protected GeneratedChartState state = null;

    /**
     * The image which caches the chart image to improve drawing performance.
     */
    private Image cachedImage = null;

    /**
     * Constructs one canvas containing chart.
     * 
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param style
     *            the style of control to construct
     */
    public GenericChartCanvas(Composite parent, int style) {
        super(parent, style);

        // initialize the SWT rendering device
        try {
            PluginSettings ps = PluginSettings.instance();
            render = ps.getDevice("dv.SWT");
        } catch (ChartException ex) {
            ex.printStackTrace();
        }

        addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent e) {

                Composite co = (Composite) e.getSource();
                final Rectangle rect = co.getClientArea();

                if (cachedImage == null) {
                    buildChart();
                    drawToCachedImage(rect);
                }
                e.gc.drawImage(cachedImage, 0, 0, cachedImage.getBounds().width,
                        cachedImage.getBounds().height, 0, 0, rect.width, rect.height);

            }
        });

        addControlListener(new ControlAdapter() {

            public void controlResized(ControlEvent e) {

                buildChart();
                cachedImage = null;
            }
        });

        render.setProperty(IDeviceRenderer.UPDATE_NOTIFIER, this);
    }

    /**
     * Builds the chart state. This method should be call when data is changed.
     */
    private void buildChart() {
        Point size = getSize();
        Bounds bo = BoundsImpl.create(0, 0, size.x, size.y);
        int resolution = render.getDisplayServer().getDpiResolution();
        bo.scale(72d / resolution);
        try {
            Generator gr = Generator.instance();
            state = gr.build(render.getDisplayServer(), chart, bo, null, null, null);
        } catch (ChartException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Draws the chart onto the cached image in the area of the given
     * <code>Rectangle</code>.
     * 
     * @param size
     *            the area to draw
     */
    public void drawToCachedImage(Rectangle size) {
        GC gc = null;
        try {
            if (cachedImage != null)
                cachedImage.dispose();
            cachedImage = new Image(Display.getCurrent(), size.width, size.height);

            gc = new GC(cachedImage);
            render.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gc);

            Generator gr = Generator.instance();

            gr.render(render, state);
        } catch (ChartException ex) {
            ex.printStackTrace();
        } finally {
            if (gc != null)
                gc.dispose();
        }
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
     * image will be dopped, but this method doesn't reset the flag
     * <code>cachedImage</code>.
     * 
     * @param chart
     *            the chart to set
     */
    public void setChart(Chart chart) {
        if (cachedImage != null)
            cachedImage.dispose();

        cachedImage = null;
        this.chart = chart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    public void dispose() {
        if (cachedImage != null)
            cachedImage.dispose();
        super.dispose();
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
