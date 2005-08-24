/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.c3d.gui;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import org.objectweb.proactive.examples.c3d.Image2D;
import org.objectweb.proactive.examples.c3d.Interval;


/**
 * A visual Component for an Image, which displays results coming from Renderers
 */
public class ImageCanvas extends Canvas {
    private final int my_HEIGHT;
    private final int my_WIDTH;
    private BufferedImage display; // the image that is drawn 
    Image offScreenBuffer;

    public ImageCanvas(int imageWidth, int imageHeight) {
        this.my_HEIGHT = imageWidth;
        this.my_WIDTH = imageHeight;
        display = new BufferedImage(my_WIDTH, my_HEIGHT,
                BufferedImage.TYPE_INT_RGB);
        Dimension size = getSize();
        offScreenBuffer = this.createImage(size.width, size.height);
    }

    /** Drawing the Component with a double-buffer.
     *  Thanks to "Graphics and Double-buffering Made Easy" , by Ian McFarland, July 1997
     */
    public void update(Graphics g) {
        Graphics gr;

        Dimension size = getSize();

        // Will hold the graphics context from the offScreenBuffer.
        // We need to make sure we keep our offscreen buffer the same size
        // as the graphics context we're working with.
        if ((offScreenBuffer == null) ||
                (!((offScreenBuffer.getWidth(this) == size.width) &&
                (offScreenBuffer.getHeight(this) == size.height)))) {
            offScreenBuffer = this.createImage(size.width, size.height);
        }

        // We need to use our buffer Image as a Graphics object:
        gr = offScreenBuffer.getGraphics();

        paint(gr); // Passes our off-screen buffer to our paint method, which,
        // unsuspecting, paints on it just as it would on the Graphics
        // passed by the browser or applet viewer.
        g.drawImage(offScreenBuffer, 0, 0, this);
        // And now we transfer the info in the buffer onto the
        // graphics context we got from the browser in one smooth motion.
    }

    /** Declares how the Component should draw itself */
    public void paint(Graphics g) {
        Graphics2D graphics = (Graphics2D) g;
        Dimension dim = this.getSize(); // the drawable area
        AffineTransform at = new AffineTransform();
        float rescale;
        if (dim.width > dim.height) { // push horizontally, adds space to the left
            rescale = ((float) dim.height - 1) / this.my_HEIGHT;
            at.translate(((float) dim.width - dim.height) / 2, 0);
        } else { // push verticall, adds space to the top
            rescale = ((float) dim.width - 1) / this.my_HEIGHT;
            at.translate(0, ((float) dim.height - dim.width) / 2);
        }
        at.scale(rescale, rescale);
        AffineTransformOp rh = new AffineTransformOp(at,
                AffineTransformOp.TYPE_BILINEAR);
        graphics.drawImage(display, rh, 0, 0);
    }

    /**
     * Sets new pixels in the displayed image.
     * @param image the container for the new pixels and position
     */
    public void setPixels(Image2D image) {
        Interval interval = image.getInterval();
        display.setRGB(0, interval.yfrom, interval.totalImageWidth,
            interval.yto - interval.yfrom, image.getPixels(), 0,
            interval.totalImageWidth);
        repaint();
    }

    /** Used to make the windows show there proper size */
    public Dimension getPreferedSize() {
        return new Dimension(my_HEIGHT, my_WIDTH);
    }
}
