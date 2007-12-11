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


public class DiagQuery extends Query {
    public int sym;
    public int scale;

    public DiagQuery(int n, int done, int sym, int s, int l, int d, int r) {
        super(n, done, l, d, r);
        this.sym = sym;
        this.scale = s;
    }

    @Override
    public long run() {
        return Diag.run(this) * scale;
    }

    private DiagQuery next(int q) {
        int l = (left | q) << 1;
        int d = (down | q);
        int r = (right | q) >> 1;
        return (new DiagQuery(n, done + 1, sym, scale, l, d, r));
    }

    @Override
    public Vector split(Vector v) {
        int y = n - done - 1;
        int todo = ~(left | down | right | ((y >= sym) ? 2 : 0));
        while (todo != 0) {
            int q = -todo & todo;
            todo ^= q;
            v.add(next(q));
        }
        return (v);
    }
}
