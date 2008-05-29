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
package org.objectweb.proactive.extra.p2p.service.util;

import java.util.Random;


public class NOAPowerLawGenerator {
    protected double a;
    protected double min;
    protected double max;
    protected Random random;

    public NOAPowerLawGenerator(int a) {
        this(0, 0, a);
    }

    public NOAPowerLawGenerator(double min, double max, double a) {
        random = new Random();
        this.a = a;
        this.min = min;
        this.max = max;
    }

    public int nextInt() {
        int result = 0;
        double tmp = Math.pow(max, a + 1.0) - Math.pow(min, a + 1.0);
        tmp = (tmp * random.nextDouble()) + Math.pow(min, a + 1.0);
        result = (int) Math.pow(tmp, 1.0 / (a + 1.0));
        return result;
    }

    public double nextDouble() {
        double tmp = Math.pow(max, a + 1.0) - Math.pow(min, a + 1.0);
        tmp = (tmp * random.nextDouble()) + Math.pow(min, a + 1.0);
        return Math.pow(tmp, 1.0 / (a + 1.0));
        //compute the shape value

        //		double shape = a/(a-1);
        //		return 1/Math.pow(random.nextDouble(),shape);
    }

    public static void main(String[] args) {
        NOAPowerLawGenerator l = new NOAPowerLawGenerator(4, 10, -3);
        for (int i = 0; i < 1000; i++) {
            System.out.println(l.nextInt());
        }
    }
}
