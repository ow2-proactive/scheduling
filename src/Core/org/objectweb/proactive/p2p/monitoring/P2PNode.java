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
package org.objectweb.proactive.p2p.monitoring;

public class P2PNode {
    protected String name;

    //the current node index
    //if -1, indicates that this node has not been seen
    //as a sender
    protected int index;
    protected int maxNOA;
    protected int noa;

    public P2PNode(String name) {
        this.name = name;
        this.index = -1;
    }

    public P2PNode(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public P2PNode(String name, int index, int noa, int maxNOA) {
        this(name, index);
        this.noa = noa;
        this.maxNOA = maxNOA;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public int getMaxNOA() {
        return maxNOA;
    }

    public int getNoa() {
        return noa;
    }

    public void setIndex(int i) {
        this.index = i;
    }

    public void setMaxNOA(int maxNOA) {
        this.maxNOA = maxNOA;
    }

    public void setNoa(int noa) {
        this.noa = noa;
    }
}
