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
package org.objectweb.proactive.examples.nbody.common;

import java.util.Random;


/**
 * $LastChangedDate: 2006-05-14 13:21:43 +0200 (Sun, 14 May 2006) $
 * $LastChangedRevision: 31 $
 *
 *
 *
 * @author The ProActive Team
 *
 */
public class RandomFactory {
    private static Random r = new Random();

    /**
     * @param max
     * @return a double in the interval [ 0.0 , max [
     */
    public static double nextDouble(double max) {
        return max * r.nextDouble();
    }

    /**
     * @param min
     * @param max
     * @return a double in the interval [ min , max [
     */
    public static double nextDouble(double min, double max) {
        return min + nextDouble(max - min);
    }
}
