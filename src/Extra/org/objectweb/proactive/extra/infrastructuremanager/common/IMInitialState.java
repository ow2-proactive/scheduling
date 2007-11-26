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
package org.objectweb.proactive.extra.infrastructuremanager.common;

import java.io.Serializable;
import java.util.Vector;


public class IMInitialState implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** Free Nodes */
    private Vector<NodeEvent> freeNodes = new Vector<NodeEvent>();

    /** busy Nodes */
    private Vector<NodeEvent> busyNodes = new Vector<NodeEvent>();

    /** toRelease Nodes */
    private Vector<NodeEvent> toReleaseNodes = new Vector<NodeEvent>();

    /** down Nodes */
    private Vector<NodeEvent> downNodes = new Vector<NodeEvent>();
    private Vector<NodeSourceEvent> nodeSources = new Vector<NodeSourceEvent>();

    public IMInitialState() {
    }

    public IMInitialState(Vector<NodeEvent> freeNodesVector,
        Vector<NodeEvent> busyNodesVector,
        Vector<NodeEvent> toReleaseNodesVector,
        Vector<NodeEvent> downNodesVector,
        Vector<NodeSourceEvent> nodeSourcesVector) {
        this.freeNodes = freeNodesVector;
        this.busyNodes = busyNodesVector;
        this.toReleaseNodes = toReleaseNodesVector;
        this.downNodes = downNodesVector;
        this.nodeSources = nodeSourcesVector;
    }

    // SETTERS
    public void setFreeNodes(Vector<NodeEvent> v) {
        this.freeNodes = v;
    }

    public void setBusyNodes(Vector<NodeEvent> v) {
        this.busyNodes = v;
    }

    public void setDownNodes(Vector<NodeEvent> v) {
        this.downNodes = v;
    }

    public void setToReleaseNodes(Vector<NodeEvent> v) {
        this.toReleaseNodes = v;
    }

    public void setNodeSource(Vector<NodeSourceEvent> v) {
        this.nodeSources = v;
    }

    // GETTERS
    public Vector<NodeEvent> getFreeNodes() {
        return this.freeNodes;
    }

    public Vector<NodeEvent> getBusyNodes() {
        return this.busyNodes;
    }

    public Vector<NodeEvent> getDownNodes() {
        return this.downNodes;
    }

    public Vector<NodeEvent> getToReleaseNodes() {
        return this.toReleaseNodes;
    }

    public Vector<NodeSourceEvent> getNodeSource() {
        return this.nodeSources;
    }
}
