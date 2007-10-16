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


/**
 * Used to represent an interval of pixels to draw (or that were drawn) as part of an Image2D.
 * these are integer values, acting as counters on arrays, to says which values in a pixel
 * array are to be set/read.
 */
public class Interval implements java.io.Serializable {
    public int number; // each interval has a number (nice to see the progression - no use in the code)
    public int totalImageWidth; // width of total image
    public int totalImageHeight; // height of total image
    public int yfrom; // which line does this interval start from
    public int yto; // line of end of this interval

    public Interval(int number, int totalWidth, int totalHeight, int yfrom,
        int yto) {
        this.number = number;
        this.totalImageWidth = totalWidth;
        this.totalImageHeight = totalHeight;
        this.yfrom = yfrom;
        this.yto = yto;
    }
}
