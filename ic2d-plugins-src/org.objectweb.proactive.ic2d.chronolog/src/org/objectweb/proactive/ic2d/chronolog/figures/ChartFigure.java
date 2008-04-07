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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.chronolog.figures;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;


/**
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class ChartFigure extends Figure {

    /**
     * 
     */
    private RrdGraph graph;
    /**
     * 
     */
    private Image image;

    /**
     * @param graphDef
     */
    public ChartFigure(final RrdGraphDef graphDef) {
        // Init width to avoid check in computeImageFromGraphDef
        this.computeImageFromGraphDef(graphDef);
        this.bounds.width = graph.getRrdGraphInfo().getWidth();
        this.bounds.height = graph.getRrdGraphInfo().getHeight();
        // this.setOpaque(true);
    }

    /**
     * @param graphDef
     */
    public final void computeImageFromGraphDef(final RrdGraphDef graphDef) {
        try {
            // final long s = System.currentTimeMillis();
            this.graph = new RrdGraph(graphDef);
            final BufferedImage awtImage = new BufferedImage(graph.getRrdGraphInfo().getWidth(), graph
                    .getRrdGraphInfo().getHeight(), BufferedImage.TYPE_INT_RGB);
            graph.render(awtImage.getGraphics());
            this.image = new Image(Display.getCurrent(), convertToSWT(awtImage));
            // System.out.println("Image computed in : "
            // + (System.currentTimeMillis() - s));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void paintFigure(final Graphics graphics) {
        final Rectangle visibleR = graphics.getClip(Rectangle.SINGLETON);
        if (this.image == null || (visibleR.width <= 0) || (visibleR.height <= 0)) {
            return;
        }
        super.paintFigure(graphics);
        // to center the graph in client area
        // graphics.drawImage(this.image, visibleR.getCenter().x
        // - (this.image.getBounds().width / 2), 0);
        graphics.drawImage(this.image, 0, 0);
        graphics.setForegroundColor(ColorConstants.white);
        graphics.setLineWidth(3);
        graphics.drawRectangle(0, 0, this.image.getBounds().width - 1, this.image.getBounds().height - 1);
    }

    /**
     * Converts an awt bufferd image into an swt ImageData
     * 
     * @param bufferedImage
     *            The awt image to convert
     * @return The instance of the converted ImageData
     */
    public static final ImageData convertToSWT(final BufferedImage bufferedImage) {
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            final DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
            final PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(),
                colorModel.getBlueMask());
            final ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
                colorModel.getPixelSize(), palette);
            final WritableRaster raster = bufferedImage.getRaster();
            final int[] pixelArray = new int[3];
            int y, x;
            for (y = data.height - 1; --y >= 0;) {
                for (x = data.width - 1; --x >= 0;) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, palette
                            .getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2])));
                }
            }
            return data;
        } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            final IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
            final int size = colorModel.getMapSize();
            final byte[] reds = new byte[size];
            final byte[] greens = new byte[size];
            final byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            final RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
            }
            final PaletteData palette = new PaletteData(rgbs);
            final ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
                colorModel.getPixelSize(), palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            final WritableRaster raster = bufferedImage.getRaster();
            final int[] pixelArray = new int[1];
            int y, x;
            for (y = data.height - 1; --y >= 0;) {
                for (x = data.width - 1; --x >= 0;) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        }
        return null;
    }
}