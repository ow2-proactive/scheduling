/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.common.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;


/**
 * Defines a state of the Resource Manager for a Monitor.
 * In order to receive Resource Manager events,
 * a monitor register itself to {@link RMMonitoring} by
 * the method {@link RMMonitoring#addRMEventListener(RMEventListener listener, RMEventType... events)},
 * and get an initial state which is the snapshot of Resource Manager state, with its
 * nodes and NodeSources.
 *
 * @see RMNodeEvent
 * @see RMNodeSourceEvent
 * @see RMMonitoring
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
@XmlRootElement
public class RMInitialState implements Serializable {

    /** Nodes events */
    private List<RMNodeEvent> nodesList = new ArrayList<>();

    /** Nodes sources AO living in RM */
    private List<RMNodeSourceEvent> nodeSources = new ArrayList<>();

    private final long latestCounter;

    public long getLatestCounter() {
        return latestCounter;
    }

    /**
     * ProActive empty constructor
     */
    public RMInitialState() {
        latestCounter = 0;
    }

    /**
     * Creates an InitialState object.
     *
     * @param nodesEventList RM's node events.
     * @param nodeSourcesList RM's node sources list.
     */
    public RMInitialState(List<RMNodeEvent> nodesEventList, List<RMNodeSourceEvent> nodeSourcesList) {
        this.nodesList = nodesEventList;
        this.nodeSources = nodeSourcesList;
        this.latestCounter = 0;
    }

    public RMInitialState(List<RMNodeEvent> nodesEventList, List<RMNodeSourceEvent> nodeSourcesList, long latestCounter) {
        this.nodesList = nodesEventList;
        this.nodeSources = nodeSourcesList;
        this.latestCounter = latestCounter;
    }


    /**
     * Returns the 'to release' Nodes list.
     * @return the 'to release' Nodes list.
     */
    public List<RMNodeEvent> getNodesEvents() {
        return this.nodesList;
    }

    /**
     * Returns the NodeSources list.
     * @return the NodeSources list.
     */
    public List<RMNodeSourceEvent> getNodeSource() {
        return this.nodeSources;
    }

    public void nodeStateChanged(RMNodeEvent stateChangedEvent) {
        int size = nodesList.size();
        for (int i = 0; i < size; i++) {
            if (stateChangedEvent.getNodeUrl().equals(nodesList.get(i).getNodeUrl())) {
                nodesList.set(i, stateChangedEvent);
                break;
            }
        }
    }

    public void nodeRemoved(RMNodeEvent removedEvent) {
        Iterator<RMNodeEvent> events = nodesList.iterator();
        while (events.hasNext()) {
            RMNodeEvent nodeEvent = events.next();
            if (removedEvent.getNodeUrl().equals(nodeEvent.getNodeUrl())) {
                events.remove();
                break;
            }
        }
    }

    public void nodeAdded(RMNodeEvent event) {
        nodesList.add(event);
    }

    public void nodeSourceStateChanged(RMNodeSourceEvent stateChangedEvent) {
        boolean existNodeSource = false;
        int size = nodeSources.size();
        for (int i = 0; i < size; i++) {
            if (stateChangedEvent.getSourceName().equals(nodeSources.get(i).getSourceName())) {
                existNodeSource = true;
                nodeSources.set(i, stateChangedEvent);
                break;
            }
        }
        if (!existNodeSource) {
            nodeSources.add(stateChangedEvent);
        }
    }
}
