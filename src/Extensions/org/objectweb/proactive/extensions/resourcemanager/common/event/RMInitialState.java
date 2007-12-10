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
package org.objectweb.proactive.extensions.resourcemanager.common.event;

import java.io.Serializable;
import java.util.ArrayList;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMEventListener;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMMonitoring;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource;


/**
 * Defines a state of the Resource Manager for a Monitor.
 * In order to receive Resource Manager events,
 * a monitor register itself to {@link RMMonitoring} by
 * the method {@link RMMonitoring#addRMEventListener(RMEventListener, IMEvent...)},
 * and get an InitialState which is the snapshot of InfrastrcutureManager state, with its
 * nodes and NodeSources.
 *
 * @see RMNodeEvent
 * @see RMNodeSourceEvent
 * @see RMMonitoring
 *
 * @author ProActive team
 *
 */
@PublicAPI
public class RMInitialState implements Serializable {

    /** serial version UID */
    private static final long serialVersionUID = 2L;

    /** Free Nodes */
    private ArrayList<RMNodeEvent> freeNodes = new ArrayList<RMNodeEvent>();

    /** busy Nodes */
    private ArrayList<RMNodeEvent> busyNodes = new ArrayList<RMNodeEvent>();

    /** toRelease Nodes */
    private ArrayList<RMNodeEvent> toReleaseNodes = new ArrayList<RMNodeEvent>();

    /** down Nodes */
    private ArrayList<RMNodeEvent> downNodes = new ArrayList<RMNodeEvent>();

    /** Nodes sources AO living in RM */
    private ArrayList<RMNodeSourceEvent> nodeSources = new ArrayList<RMNodeSourceEvent>();

    /**
     * ProActive empty constructor
     */
    public RMInitialState() {
    }

    /**
     * Creates an InitialState object.
     * @param freeNodesList RM's free nodes.
     * @param busyNodesList RM's busy nodes.
     * @param toReleaseNodesList RM's 'to release' nodes.
     * @param downNodesList RM's down nodes.
     * @param nodeSourcesList RM's node sources list.
     */
    public RMInitialState(ArrayList<RMNodeEvent> freeNodesList,
        ArrayList<RMNodeEvent> busyNodesList,
        ArrayList<RMNodeEvent> toReleaseNodesList,
        ArrayList<RMNodeEvent> downNodesList,
        ArrayList<RMNodeSourceEvent> nodeSourcesList) {
        this.freeNodes = freeNodesList;
        this.busyNodes = busyNodesList;
        this.toReleaseNodes = toReleaseNodesList;
        this.downNodes = downNodesList;
        this.nodeSources = nodeSourcesList;
    }

    /**
     * set the free nodes list.
     * @param v the list of free nodes
     */
    public void setFreeNodes(ArrayList<RMNodeEvent> v) {
        this.freeNodes = v;
    }

    /**
     * set the busy nodes list.
     * @param v the list of busy nodes
     */
    public void setBusyNodes(ArrayList<RMNodeEvent> v) {
        this.busyNodes = v;
    }

    /**
     * set the free nodes list.
     * @param v the list of down nodes.
     */
    public void setDownNodes(ArrayList<RMNodeEvent> v) {
        this.downNodes = v;
    }

    /**
     * set the 'to be released' nodes list.
     * @param v the list of free nodes.
     */
    public void setToReleaseNodes(ArrayList<RMNodeEvent> v) {
        this.toReleaseNodes = v;
    }

    /**
     * set the {@link NodeSource} objects list.
     * @param v the list of free nodes.
     */
    public void setNodeSource(ArrayList<RMNodeSourceEvent> v) {
        this.nodeSources = v;
    }

    /**
     * Returns the free Nodes list.
     * @return the free Nodes list.
     */
    public ArrayList<RMNodeEvent> getFreeNodes() {
        return this.freeNodes;
    }

    /**
     * Returns the busy Nodes list.
     * @return the busy Nodes list.
     */
    public ArrayList<RMNodeEvent> getBusyNodes() {
        return this.busyNodes;
    }

    /**
     * Returns the down Nodes list.
     * @return the down Nodes list.
     */
    public ArrayList<RMNodeEvent> getDownNodes() {
        return this.downNodes;
    }

    /**
     * Returns the 'to release' Nodes list.
     * @return the 'to release' Nodes list.
     */
    public ArrayList<RMNodeEvent> getToReleaseNodes() {
        return this.toReleaseNodes;
    }

    /**
     * Returns the NodeSources list.
     * @return the NodeSources list.
     */
    public ArrayList<RMNodeSourceEvent> getNodeSource() {
        return this.nodeSources;
    }
}
