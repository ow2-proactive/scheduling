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
package org.objectweb.proactive.extensions.scilab.util;

public class SciMath {
    public static String formulaPi(String pi, int iBloc, int sizeBloc) {
        return pi + " = 0;" + "j = " + (iBloc * sizeBloc) + ";" + "n = j + " +
        sizeBloc + ";" + "for i = j:n, " + pi + " = " + pi +
        " + ((-1)**i/(2**(10*i))* (- (2**5/(4*i+1)) - (1/(4*i+3)) + (2**8/(10*i+1)) - (2**6/(10*i+3)) - (2**2/(10*i+5))  - (2**2/(10*i+7)) + (1/(10*i+9))));" +
        "end;" + pi + " = " + pi + "/(2**6);";
    }

    public static String formulaMandelbrot(String name, int nbRow, int nbCol,
        double xmin, double xmax, double ymin, double ymax, int precision) {
        return name + "(" + nbRow + "," + nbCol + ") = -1; " + "xres = " +
        ((xmax - xmin) / nbCol) + "; " + "yres = " + ((ymax - ymin) / nbRow) +
        "; " + "a = " + xmin + "; " + "for i = 1:" + nbCol + ", " +
        "a = a + xres; " + "b = " + ymin + "; " + "for j = 1:" + nbRow + ", " +
        "b = b + yres; " + "x = 0; " + "y = 0; " + "for k = 0:" + precision +
        ", " + "tmp = x; " + "x = (x**2) - (y**2) + a; " +
        "y = (2 * tmp * y) + b; " + "tmp = (x**2) + (y**2); " +
        "if tmp < 4 then " + name + "(j,i) = k; " + "end, " + "end; " +
        "end; " + "end; ";
    }
}
