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
    private int my_HEIGHT;
    private int my_WIDTH;
    private Dimension preferedSize; // the Canvas prefered size
    private BufferedImage display; // the image that is drawn 
    Image offScreenBuffer;

    public ImageCanvas() {
        this.my_HEIGHT = 200; // this will change on first image received (see setPixels)
        this.my_WIDTH = 200;
        this.display = new BufferedImage(my_WIDTH, my_HEIGHT,
                BufferedImage.TYPE_INT_RGB);
    }

    /** Drawing the Component with a double-buffer.
     *  Thanks to "Graphics and Double-buffering Made Easy" , by Ian McFarland, July 1997
     */
    @Override
    public void update(Graphics g) {
        Graphics gr;

        Dimension size = getSize();

        // Will hold the graphics context from the offScreenBuffer.
        // We need to make sure we keep our offscreen buffer the same size
        // as the graphics context we're working with.
        if ((this.offScreenBuffer == null) ||
                (!((this.offScreenBuffer.getWidth(this) == size.width) &&
                (this.offScreenBuffer.getHeight(this) == size.height)))) {
            this.offScreenBuffer = this.createImage(size.width, size.height);
        }

        // We need to use our buffer Image as a Graphics object:
        gr = this.offScreenBuffer.getGraphics();

        paint(gr); // Passes our off-screen buffer to our paint method, which,
                   // unsuspecting, paints on it just as it would on the Graphics
                   // passed by the browser or applet viewer.

        g.drawImage(this.offScreenBuffer, 0, 0, this);
        // And now we transfer the info in the buffer onto the
        // graphics context we got from the browser in one smooth motion.
    }

    /** Declares how the Component should draw itself */
    @Override
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
        graphics.drawImage(this.display, rh, 0, 0);
    }

    /**
     * Sets new pixels in the displayed image.
     * @param image the container for the new pixels and position
     */
    public void setPixels(Image2D image) {
        Interval interval = image.getInterval();
        if ((interval.totalImageHeight != my_HEIGHT) ||
                (interval.totalImageWidth != my_WIDTH)) {
            // just in case image format has unsuspectedly been changed
            this.my_WIDTH = interval.totalImageWidth;
            this.my_HEIGHT = interval.totalImageHeight;
            this.display = new BufferedImage(my_WIDTH, my_HEIGHT,
                    BufferedImage.TYPE_INT_RGB);
            setPreferredSize(new Dimension(this.my_WIDTH, this.my_HEIGHT));
        }
        this.display.setRGB(0, interval.yfrom, interval.totalImageWidth,
            interval.yto - interval.yfrom, image.getPixels(), 0,
            interval.totalImageWidth);
        repaint();
    }

    /** Used to make the windows show there proper size */
    public Dimension getPreferedSize() {
        return new Dimension(this.my_HEIGHT, this.my_WIDTH);
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        this.preferedSize = preferredSize;
    }

    @Override
    public Dimension getPreferredSize() {
        return this.preferedSize;
    }
}
