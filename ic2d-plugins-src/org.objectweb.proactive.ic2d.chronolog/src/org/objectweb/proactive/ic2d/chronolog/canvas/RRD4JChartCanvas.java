package org.objectweb.proactive.ic2d.chronolog.canvas;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;


public final class RRD4JChartCanvas extends AbstractCachedCanvas {

    private static final PaletteData PALETTE_DATA = new PaletteData(0xFF0000, 0xFF00, 0xFF);

    public static final int DEFAULT_WIDTH_CROP = 80;
    public static final int DEFAULT_HEIGHT_CROP = 30;

    /**
     * 
     */
    private final RrdGraphDef graphDef;

    /**
     * 
     */
    private RrdGraph graph;

    /**
     * Cached awt image
     */
    private BufferedImage cachedAwtImage;

    private ImageData swtImageData;

    /**
     * Constructs one canvas containing chart.
     * 
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param style
     *            the style of control to construct
     */
    public RRD4JChartCanvas(final Composite parent, int style, final RrdGraphDef graphDef) {
        super(parent, style);
        this.graphDef = graphDef;
    }

    /**
     * 
     * @param left
     * @param right
     */
    public void updateTimeSpan(final long left, final long right) {
        // Update the time span
        this.graphDef.setTimeSpan(left, right);
        this.drawToCachedImage();
    }

    /**
     * Draws the chart onto the cached image in the area of the given
     * <code>Image</code>.
     */
    @Override
    public void drawToCachedImage() {
        GC gc = null;
        try {
            final int boundsWidth = super.cachedImage.getBounds().width;
            final int boundsHeigth = super.cachedImage.getBounds().height;

            this.graphDef.setWidth(boundsWidth - DEFAULT_WIDTH_CROP);
            this.graphDef.setHeight(boundsHeigth - DEFAULT_HEIGHT_CROP);

            // Create the graph
            this.graph = new RrdGraph(graphDef);

            // First Draw to an AWT Image then convert to SWT if bounds has changed
            if (this.cachedAwtImage == null || this.cachedAwtImage.getWidth() != boundsWidth) {
                this.cachedAwtImage = new BufferedImage(boundsWidth, boundsHeigth, BufferedImage.TYPE_INT_RGB);
                // We can force bitdepth to be 24 bit because BufferedImage getRGB allows us to always
                // retrieve 24 bit data regardless of source color depth.
                this.swtImageData = new ImageData(boundsWidth, boundsHeigth, 24, PALETTE_DATA);
            }
            // Render the graph
            this.graph.render(cachedAwtImage.getGraphics());

            // Convert from BufferedImage to Image                      
            super.cachedImage = convert(cachedAwtImage, swtImageData);

            // Draw the image in the gc
            gc = new GC(cachedImage);

            // Manually erase gray contours
            gc.setForeground(ColorConstants.white);
            gc.setLineWidth(3);
            gc.drawRectangle(0, 0, boundsWidth - 1, boundsHeigth - 1);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (gc != null)
                gc.dispose();
        }
    }

    @Override
    protected void buildChart() {
    }

    /**
     * Converts an AWT based buffered image into an SWT <code>Image</code>.  This will always return an
     * <code>Image</code> that has 24 bit depth regardless of the type of AWT buffered image that is 
     * passed into the method.
     * 
     * @param srcImage the {@link java.awt.image.BufferedImage} to be converted to an <code>Image</code>
     * @param swtImageData the {@link org.eclipse.swt.graphics.ImageData} used as target
     * @return an <code>Image</code> that represents the same image data as the AWT 
     * <code>BufferedImage</code> type.
     */
    public static Image convert(final BufferedImage srcImage, final ImageData swtImageData) {
        final int w = srcImage.getWidth();
        final int h = srcImage.getHeight();

        // ensure scansize is aligned on 32 bit.
        final int scansize = (((w * 3) + 3) * 4) / 4;

        final WritableRaster alphaRaster = srcImage.getAlphaRaster();
        final byte[] alphaBytes = new byte[w];
        int[] buff, alpha;
        for (int y = h - 1; --y >= 0;) {
            buff = srcImage.getRGB(0, y, w, 1, null, 0, scansize);
            swtImageData.setPixels(0, y, w, buff, 0);

            // check for alpha channel
            if (alphaRaster != null) {
                alpha = alphaRaster.getPixels(0, y, w, 1, (int[]) null);
                for (int i = w - 1; --i >= 0;)
                    alphaBytes[i] = (byte) alpha[i];
                swtImageData.setAlphas(0, y, w, alphaBytes, 0);
            }
        }

        return new Image(PlatformUI.getWorkbench().getDisplay(), swtImageData);
    }
}
