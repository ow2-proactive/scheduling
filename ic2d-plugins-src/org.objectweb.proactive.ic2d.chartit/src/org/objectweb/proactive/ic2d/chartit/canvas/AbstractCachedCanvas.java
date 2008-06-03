package org.objectweb.proactive.ic2d.chartit.canvas;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;


/**
 * 
 * An abstract canvas with a cached image.
 * The height is supposed always constant.
 * 
 * @author vbodnart
 *
 */
public abstract class AbstractCachedCanvas extends Canvas implements PaintListener, ControlListener {

    /**
     * Defualt canvas width
     */
    public static final int DEFAULT_WIDTH = 400;

    /**
     * Default canvas height
     */
    public static final int DEFAULT_HEIGHT = 200;

    /**
     * The image which caches the chart image to improve drawing performance.
     */
    protected Image cachedImage;

    public boolean boundsChanged;

    public boolean recomputeChart;

    /**
     * Constructs one canvas containing chart.
     * 
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param style
     *            the style of control to construct
     */
    public AbstractCachedCanvas(final Composite parent, final int style) {
        super(parent, style);

        // Set default size 
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Set the default Layout data
        final GridData gd = new GridData();
        gd.widthHint = DEFAULT_WIDTH;
        gd.heightHint = DEFAULT_HEIGHT;
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        this.setLayoutData(gd);

        // Initialize the chached image
        this.cachedImage = new Image(Display.getDefault(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Add listeners
        super.addPaintListener(this);
        super.addControlListener(this);
    }

    /**
     * Draws the chart to the image provided in arguments
     */
    protected abstract void drawToCachedImage();

    /**
     * Builds the chart
     */
    protected abstract void buildChart();

    /**
     * Called when the canvas is repaint
     */
    public final void paintControl(final PaintEvent e) {
        final Composite co = (Composite) e.getSource();
        final Rectangle rect = co.getClientArea();

        if (this.recomputeChart) {
            // Dispose the precedent image
            this.cachedImage.dispose();
            // Then create another
            this.cachedImage = new Image(Display.getDefault(), rect);
            this.buildChart();
            this.drawToCachedImage();
            this.recomputeChart = false;
        }

        e.gc.drawImage(this.cachedImage, 0, 0);
    }

    /**
     * Called when the control is resized
     */
    public final void controlResized(final ControlEvent e) {
        // Recompute chart
        this.recomputeChart = true;
        // Redraw
        this.redraw();
    }

    /**
     * Controls cannot be moved for the moment
     */
    public final void controlMoved(final ControlEvent e) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    public void dispose() {
        if (this.cachedImage != null)
            this.cachedImage.dispose();
        super.dispose();
    }
}