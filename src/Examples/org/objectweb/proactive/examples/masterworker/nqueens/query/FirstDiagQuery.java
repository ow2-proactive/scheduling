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

import java.util.Vector;


public class FirstDiagQuery extends Query {
    int scale;

    public FirstDiagQuery(int n, int s) {
        super(n, 1, 2, 1, 0);
        down |= ~((1 << n) - 1);
        scale = s;
    }

    @Override
    public long run() {
        Vector v = split(new Vector());
        int n = v.size();
        long r = 0;
        for (int i = 0; i < n; i++) {
            r += ((Query) v.get(i)).run();
        }
        return (r);
    }

    private DiagQuery next(int q, int sym) {
        int l = (left | q) << 1;
        int d = (down | q);
        int r = (right | q) >> 1;
        return (new DiagQuery(n, 2, sym, scale, l, d, r));
    }

    @Override
    public Vector split(Vector v) {
        int nq1 = n - 1;
        for (int i = 2; i < nq1; i++)
            v.add(next(1 << i, nq1 - i));
        return (v);
    }
}
