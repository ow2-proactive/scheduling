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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
 * @author The ProActive Team
 * @see RMNodeEvent
 * @see RMNodeSourceEvent
 * @see RMMonitoring
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
@XmlRootElement
public class RMInitialState implements Serializable {

    /**
     * Nodes events
     */
    private Map<String, RMNodeEvent> nodeEvents = new ConcurrentHashMap<>();

    /**
     * Nodes sources AO living in RM
     */
    private Map<String, RMNodeSourceEvent> nodeSourceEvents = new ConcurrentHashMap<>();

    private Map<String, RMNodeSourceEvent> removeNodeSourceEvents = new ConcurrentHashMap<>();

    private long latestCounter = -1;

    /**
     * ProActive empty constructor
     */
    public RMInitialState() {

    }

    /**
     * Creates an InitialState object.
     *
     * @param nodesEventList  RM's node events.
     * @param nodeSourcesList RM's node sources list.
     */
    public RMInitialState(Map<String, RMNodeEvent> nodesEventList, Map<String, RMNodeSourceEvent> nodeSourcesList) {
        this.nodeEvents = nodesEventList;
        this.nodeSourceEvents = nodeSourcesList;
    }


    /**
     * Current version of RM portal and maybe other clients expects "nodesEvents" inside JSON
     *
     * @return
     */
    public List<RMNodeEvent> getNodesEvents() {
        return new ArrayList(this.nodeEvents.values());
    }


    /**
     * Current version of RM portal and maybe other clients expects "nodeSource" inside JSON
     *
     * @return
     */
    public List<RMNodeSourceEvent> getNodeSource() {
        return new ArrayList<>(this.nodeSourceEvents.values());
    }


    public long getLatestCounter() {
        return latestCounter;
    }


    public Collection<RMNodeSourceEvent> getRemoveNodeSourceEvents() {
        return removeNodeSourceEvents.values();
    }

    public void nodeAdded(RMNodeEvent event) {
        nodeEvents.put(event.getNodeUrl(), event);
    }

    public void nodeStateChanged(RMNodeEvent event) {
        nodeEvents.put(event.getNodeUrl(), event);
    }

    public void nodeRemoved(RMNodeEvent event) {
        nodeEvents.put(event.getNodeUrl(), event);
    }

    public void nodeSourceAdded(RMNodeSourceEvent event) {
        nodeSourceEvents.put(event.getSourceName(), event);
    }

    public void nodeSourceRemoved(RMNodeSourceEvent event) {
        nodeSourceEvents.remove(event.getSourceName());
        removeNodeSourceEvents.put(event.getSourceName(), event);
    }



    public RMInitialState cloneAndFilter(long filter) {
        RMInitialState clone = new RMInitialState();

        clone.nodeEvents = newFilteredEvents(this.nodeEvents, filter);
        clone.nodeSourceEvents = newFilteredEvents(this.nodeSourceEvents, filter);
        clone.removeNodeSourceEvents = newFilteredEvents(this.removeNodeSourceEvents, filter);

        clone.latestCounter = Math.max(
                Math.max(
                        findLargestCounter(clone.nodeEvents.values()),
                        findLargestCounter(clone.nodeSourceEvents.values())),
                Math.max(
                        filter,
                        findLargestCounter(clone.removeNodeSourceEvents.values())));

        return clone;
    }

    private <T extends RMEvent> Map<String, T> newFilteredEvents(Map<String, T> events, long filter) {
        Map<String, T> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, T> entry : events.entrySet()) {
            if(entry.getValue().getCounter() > filter){
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private <T extends RMEvent> long findLargestCounter(Collection<T> events) {
        long result = 0;
        for (T event : events) {
            if (result < event.getCounter()) {
                result = event.getCounter();
            }
        }
        return result;
    }

    public void nodeSourceStateChanged(RMNodeSourceEvent stateChangedEvent) {
        boolean existNodeSource = false;
        int size = nodeSourceEvents.size();
        for (int i = 0; i < size; i++) {
            if (stateChangedEvent.getSourceName().equals(nodeSourceEvents.get(i).getSourceName())) {
                existNodeSource = true;
                nodeSourceEvents.put(stateChangedEvent.getSourceName(), stateChangedEvent);
                break;
            }
        }
        if (!existNodeSource) {
            nodeSourceEvents.put(stateChangedEvent.getSourceName(), stateChangedEvent);
        }
    }
}
