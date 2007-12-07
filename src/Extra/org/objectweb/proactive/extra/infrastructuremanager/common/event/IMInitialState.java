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
package org.objectweb.proactive.extra.infrastructuremanager.common.event;

import java.io.Serializable;
import java.util.ArrayList;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMEventListener;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource;


/**
 * Defines a state of he infrastructure Manager for a Monitor.
 * In order to receive Infrastructure Manager events,
 * a monitor register itself to {@link IMMonitoring} by
 * the method {@link IMMonitoring#addIMEventListener(IMEventListener, IMEvent...)},
 * and get an InitialState which is the snapshot of InfrastrcutureManager state, with its
 * nodes and NodeSources.
 *
 * @see IMNodeEvent
 * @see IMNodeSourceEvent
 * @see IMMonitoring
 *
 * @author ProActive team
 *
 */
@PublicAPI
public class IMInitialState implements Serializable {

    /** serial version UID */
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

    /**
     * ProActive empty constructor
     */
    public IMInitialState() {
    }

    /**
     * Creates an InitialState object.
     * @param freeNodesList IM's free nodes.
     * @param busyNodesList IM's busy nodes.
     * @param toReleaseNodesList IM's 'to release' nodes.
     * @param downNodesList IM's down nodes.
     * @param nodeSourcesList IM's node sources list.
     */
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

    /**
     * set the free nodes list.
     * @param v the list of free nodes
     */
    public void setFreeNodes(ArrayList<IMNodeEvent> v) {
        this.freeNodes = v;
    }

    /**
     * set the busy nodes list.
     * @param v the list of busy nodes
     */
    public void setBusyNodes(ArrayList<IMNodeEvent> v) {
        this.busyNodes = v;
    }

    /**
     * set the free nodes list.
     * @param v the list of down nodes.
     */
    public void setDownNodes(ArrayList<IMNodeEvent> v) {
        this.downNodes = v;
    }

    /**
     * set the 'to be released' nodes list.
     * @param v the list of free nodes.
     */
    public void setToReleaseNodes(ArrayList<IMNodeEvent> v) {
        this.toReleaseNodes = v;
    }

    /**
     * set the {@link NodeSource} objects list.
     * @param v the list of free nodes.
     */
    public void setNodeSource(ArrayList<IMNodeSourceEvent> v) {
        this.nodeSources = v;
    }

    /**
     * Returns the free Nodes list.
     * @return the free Nodes list.
     */
    public ArrayList<IMNodeEvent> getFreeNodes() {
        return this.freeNodes;
    }

    /**
     * Returns the busy Nodes list.
     * @return the busy Nodes list.
     */
    public ArrayList<IMNodeEvent> getBusyNodes() {
        return this.busyNodes;
    }

    /**
     * Returns the down Nodes list.
     * @return the down Nodes list.
     */
    public ArrayList<IMNodeEvent> getDownNodes() {
        return this.downNodes;
    }

    /**
     * Returns the 'to release' Nodes list.
     * @return the 'to release' Nodes list.
     */
    public ArrayList<IMNodeEvent> getToReleaseNodes() {
        return this.toReleaseNodes;
    }

    /**
     * Returns the NodeSources list.
     * @return the NodeSources list.
     */
    public ArrayList<IMNodeSourceEvent> getNodeSource() {
        return this.nodeSources;
    }
}
