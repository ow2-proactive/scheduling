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

import java.io.Serializable;
import java.util.Vector;


public abstract class Query implements Serializable {
    public int n;
    public int done;
    public int left;
    public int down;
    public int right;
    public int queryId;

    // static generation of task id, may fail if more than 1 manager on the same jvm 
    public static int taskId = 0;
    public boolean resolve = false;

    public Query(int n, int done, int left, int down, int right) {
        this.n = n;
        this.done = done;
        this.left = left;
        this.down = down;
        this.right = right;

        this.queryId = Query.taskId++;
    }

    @Override
    public String toString() {
        //return(mask((left | down | right), n));
        return ("<" + n + "," + done + type() + "{" + mask(left, n) + "," + mask(down, n) + "," +
            mask(right, n) + "}>");
    }

    public String type() {
        if (this instanceof OutQuery) {
            return ("Out");
        }
        if (this instanceof DiagQuery) {
            return ("Diag");
        }
        if (this instanceof BaseQuery) {
            return ("Base");
        }
        if (this instanceof FirstOutQuery) {
            return ("FirstOut");
        }
        if (this instanceof FirstDiagQuery) {
            return ("firstDiag");
        }
        return ("?");
    }

    public String mask(int n, int l) {
        if (l == 0) {
            return ("");
        }
        return (mask(n >>> 1, l - 1) + (((n % 2) == 0) ? "." : "*"));
    }

    public abstract long run();

    public abstract Vector split(Vector v);

    /**
     * @param buffer
     */
    public void toStringBuffer(StringBuffer buffer) {
        // TODO some day
    }
}
