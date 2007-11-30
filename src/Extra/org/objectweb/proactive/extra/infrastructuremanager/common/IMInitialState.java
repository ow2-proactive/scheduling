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
import java.util.ArrayList;
import java.util.Vector;


public class IMInitialState implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2L;

    /** Free Nodes */
    private ArrayList<IMNodeEvent> freeNodes = new ArrayList<IMNodeEvent>();

    /** busy Nodes */
    private ArrayList<IMNodeEvent> busyNodes = new ArrayList<IMNodeEvent>();

    /** toRelease Nodes */
    private ArrayList<IMNodeEvent> toReleaseNodes = new ArrayList<IMNodeEvent>();

    /** down Nodes */
    private ArrayList<IMNodeEvent> downNodes = new ArrayList<IMNodeEvent>();

    /** Nodes sources AO living in IM */
    private ArrayList<IMNodeSourceEvent> nodeSources = new ArrayList<IMNodeSourceEvent>();

    public IMInitialState() {
    }

    public IMInitialState(ArrayList<IMNodeEvent> freeNodesList,
        ArrayList<IMNodeEvent> busyNodesList,
        ArrayList<IMNodeEvent> toReleaseNodesList,
        ArrayList<IMNodeEvent> downNodesList,
        ArrayList<IMNodeSourceEvent> nodeSourcesList) {
        this.freeNodes = freeNodesList;
        this.busyNodes = busyNodesList;
        this.toReleaseNodes = toReleaseNodesList;
        this.downNodes = downNodesList;
        this.nodeSources = nodeSourcesList;
    }

    // SETTERS
    public void setFreeNodes(ArrayList<IMNodeEvent> v) {
        this.freeNodes = v;
    }

    public void setBusyNodes(ArrayList<IMNodeEvent> v) {
        this.busyNodes = v;
    }

    public void setDownNodes(ArrayList<IMNodeEvent> v) {
        this.downNodes = v;
    }

    public void setToReleaseNodes(ArrayList<IMNodeEvent> v) {
        this.toReleaseNodes = v;
    }

    public void setNodeSource(ArrayList<IMNodeSourceEvent> v) {
        this.nodeSources = v;
    }

    // GETTERS
    public ArrayList<IMNodeEvent> getFreeNodes() {
        return this.freeNodes;
    }

    public ArrayList<IMNodeEvent> getBusyNodes() {
        return this.busyNodes;
    }

    public ArrayList<IMNodeEvent> getDownNodes() {
        return this.downNodes;
    }

    public ArrayList<IMNodeEvent> getToReleaseNodes() {
        return this.toReleaseNodes;
    }

    public ArrayList<IMNodeSourceEvent> getNodeSource() {
        return this.nodeSources;
    }
}
