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
package org.objectweb.proactive.examples.masterworker.nqueens.query;

public class Base {
    private int N = 0;

    private final void count(int y, int l, int d, int r) {
        int todo = ~(l | d | r);
        if (todo != 0) {
            if (y == 0) {
                N++;
            } else {
                do {
                    int q = -todo & todo;
                    todo ^= q;
                    count(y - 1, (l | q) << 1, d | q, (r | q) >> 1);
                } while (todo != 0);
            }
        }
    }

    public static int run(Query query) {
        int n = query.n;
        Base o = new Base();
        o.count(n - query.done - 1, query.left, query.down, query.right);
        return (o.N);
    }

    public static void main(String[] args) {
        System.out.println("" + run(new BaseQuery(args)));
    }
}
