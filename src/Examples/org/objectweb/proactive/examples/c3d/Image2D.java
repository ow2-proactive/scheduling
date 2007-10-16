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
package org.objectweb.proactive.examples.c3d;

import java.io.Serializable;


/**
 * Used to represent a (possibly partial) 2D image representing a 3D scene.
 * The pixels are stored as an array.
 * As it is returned by rendering engines, it may contain only a fraction of the Image,
 * the complete image being made up of the sum of all these partial images.
 */
public class Image2D implements Serializable {
    // integer array representing the pixels, starting from interval.yfrom * interval.width 
    private int[] pixels;

    // positioning of the above pixels in the image
    private Interval interval;

    // engine on which this was rendered
    private int engineNb;

    /**  Needed by ProActive, if we wish to use it as a future */
    public Image2D() {
    }

    /**
     * Makes a new Image2D, with given parameters filling the private fields.
     * @param array the pixels of the image
     * @param interval the description of which part of the final image is given
     * @param engineNb the number on which engine this was drawn (useful for assigning new task)
     */
    public Image2D(int[] array, Interval interval, int engineNb) {
        this.pixels = array;
        this.interval = interval;
        this.engineNb = engineNb;
    }

    /**
     * @return the pixels as an int array.
     */
    public int[] getPixels() {
        return pixels;
    }

    /**
     * @return the interval, ie the information about where these pixels are in the whole image.
     */
    public Interval getInterval() {
        return interval;
    }

    /**
     * Watch out, engineNb depends on which engines are used, and may not be simulation-wide constant.
     * @return the engine on which it was computed.
     */
    public int getEngineNb() {
        return engineNb;
    }
}
