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


public class FirstOutQuery extends Query {
    public FirstOutQuery(int n) {
        super(n, 0, 0, ~((1 << n) - 1), 0);
    }

    public long run() {
        Vector v = split(new Vector());
        int n = v.size();
        long r = 0;
        for (int i = 0; i < n; i++) {
            r += ((Query) v.get(i)).run();
        }
        return (r);
    }

    public Vector split(Vector v) {
        int nq1 = n - 1;
        int j = n - 2;
        int LASTMASK = (1 << nq1) | 1;
        int ENDBIT = (1 << nq1) >> 1;
        for (int i = 1; i < j; i++, j--) {
            v.add(new OutQuery(1 << i, n, i, j, LASTMASK, ENDBIT));
            LASTMASK |= ((LASTMASK >> 1) | (LASTMASK << 1));
            ENDBIT >>= 1;
        }
        // And the diag
        v.add(new FirstDiagQuery(n, 8));
        return (v);
    }
}
