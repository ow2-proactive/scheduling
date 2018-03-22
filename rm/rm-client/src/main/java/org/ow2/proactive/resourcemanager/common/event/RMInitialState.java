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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
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

    public static final Long EMPTY_STATE = -1l;

    private static final Logger LOGGER = Logger.getLogger(RMInitialState.class);

    /**
     * Node events are stored in sorted collection,
     * where nodeUrl is unique (key),
     * but events are sorted based on counter.
     * Because main usage suggests that
     * there will be more read the writes to this collection
     * thus when we add/update event, we spend O(length) time
     * to remove previous event with the same key (nodeUrl),
     * and after this we sort collection to keep it always sorted.
     * PS: Starting from Java 8 AraryList are faster to sort than LinkedList
     */
    private List<RMNodeEvent> nodeEvents = new ArrayList<>();

    /**
     * Nodesource events are stored in sorted collection,
     * where sourceName is unique (key),
     * but events are sorted based on counter.
     * Because main usage suggests that
     * there will be more read the writes to this collection
     * thus when we add/update event, we spend O(length) time
     * to remove previous event with the same key (nodeUrl),
     * and after this we sort collection to keep it always sorted.
     * PS: Starting from Java 8 AraryList are faster to sort than LinkedList
     */
    private List<RMNodeSourceEvent> nodeSourceEvents = new ArrayList<>();

    /**
     * keeps track of the latest (biggest) counter among the 'nodeEvents' and 'nodeSourceEvents'
     */
    private AtomicLong latestCounter = new AtomicLong(0);

    /**
     * ProActive empty constructor
     */
    public RMInitialState() {

    }

    public RMInitialState(List<RMNodeEvent> nodeEvents, List<RMNodeSourceEvent> nodeSourceEvents) {
        this.nodeEvents = nodeEvents;
        this.nodeSourceEvents = nodeSourceEvents;
        Collections.sort(nodeEvents, Comparator.comparingLong(RMEvent::getCounter));
        Collections.sort(nodeSourceEvents, Comparator.comparingLong(RMEvent::getCounter));
    }

    /**
     * Current version of RM portal and maybe other clients expects "nodesEvents" inside JSON
     *
     * @return
     */
    public List<RMNodeEvent> getNodesEvents() {
        return Collections.unmodifiableList(this.nodeEvents);
    }

    /**
     * Current version of RM portal and maybe other clients expects "nodeSource" inside JSON
     *
     * @return
     */
    public List<RMNodeSourceEvent> getNodeSource() {
        return Collections.unmodifiableList(this.nodeSourceEvents);
    }

    public long getLatestCounter() {
        return latestCounter.get();
    }

    public void nodeAdded(RMNodeEvent event) {
        updateNode(event);
    }

    public void nodeStateChanged(RMNodeEvent event) {
        updateNode(event);
    }

    public void nodeRemoved(RMNodeEvent event) {
        updateNode(event);
    }

    protected void updateNode(RMNodeEvent event) {
        updateCounter(event);

        // try to remove existing node with the same node url
        // PS: I do not see how I can extract method without changing RMNodeEvent and RMNodeSourceEvent types.
        final Iterator<RMNodeEvent> iterator = nodeEvents.iterator();
        while (iterator.hasNext()) {
            final RMNodeEvent existedEvent = iterator.next();
            final String keyExistedEvent = existedEvent.getNodeUrl();
            final String keyEvent = event.getNodeUrl();
            if (keyExistedEvent.equals(keyEvent)) {
                iterator.remove();
                break;
            }
        }

        nodeEvents.add(event);

        // keep list sorted by counter
        Collections.sort(nodeEvents, Comparator.comparingLong(RMEvent::getCounter));
    }

    public void nodeSourceAdded(RMNodeSourceEvent event) {
        updateNodeSource(event);
    }

    public void nodeSourceRemoved(RMNodeSourceEvent event) {
        updateNodeSource(event);
    }

    public void nodeSourceStateChanged(RMNodeSourceEvent event) {
        updateNodeSource(event);
    }

    protected void updateNodeSource(RMNodeSourceEvent event) {
        updateCounter(event);

        // try to remove existing node with the same source name
        // PS: I do not see how I can extract method without changing RMNodeEvent and RMNodeSourceEvent types.
        final Iterator<RMNodeSourceEvent> iterator = nodeSourceEvents.iterator();
        while (iterator.hasNext()) {
            final RMNodeSourceEvent existedEvent = iterator.next();
            final String keyExistedEvent = existedEvent.getSourceName();
            final String keyEvent = event.getSourceName();
            if (keyExistedEvent.equals(keyEvent)) {
                iterator.remove();
                break;
            }
        }

        nodeSourceEvents.add(event);

        // keep list sorted by counter
        Collections.sort(nodeSourceEvents, Comparator.comparingLong(RMEvent::getCounter));
    }

    private void updateCounter(RMEvent event) {
        latestCounter.set(Math.max(latestCounter.get(), event.getCounter()));
    }

    /**
     * Clones current state events, but keep only those events which has counter bigger than provided 'filter'
     * Event counter can take values [0, +).
     * So if filter is '-1' then all events will returned.
     * @param filter
     * @return rmInitialState where all the events bigger than 'filter'
     */
    public RMInitialState cloneAndFilter(long filter) {
        long actualFilter;
        if (filter <= latestCounter.get()) {
            actualFilter = filter;
        } else {
            LOGGER.info(String.format("Client is aware of %d but server knows only about %d counter. " +
                                      "Probably because there was network server restart.",
                                      filter,
                                      latestCounter.get()));
            actualFilter = EMPTY_STATE; // reset filter to default  value
        }
        RMInitialState clone = new RMInitialState();

        int indexNode = 0;
        int indexNodeSoruce = 0;

        // move indexes to skip all node and nodesource events which has counter smaller or equal to filter,
        // i.e. client is already aware about them
        for (; indexNode < this.nodeEvents.size() &&
               this.nodeEvents.get(indexNode).getCounter() <= actualFilter; ++indexNode) {
        }
        for (; indexNodeSoruce < this.nodeSourceEvents.size() &&
               this.nodeSourceEvents.get(indexNodeSoruce).getCounter() <= actualFilter; ++indexNodeSoruce) {
        }

        // this loop finds at most 'RM_REST_MONITORING_MAXIMUM_CHUNK_SIZE' events with the smallest existing counter
        for (int elementInChunk = 0; elementInChunk < PAResourceManagerProperties.RM_REST_MONITORING_MAXIMUM_CHUNK_SIZE.getValueAsInt(); ++elementInChunk) {

            // these are candidates to add to chunk that will be send to client
            Optional<RMNodeEvent> candidateNode = indexNode < nodeEvents.size() ? Optional.of(nodeEvents.get(indexNode))
                                                                                : Optional.empty();

            Optional<RMNodeSourceEvent> candidateNodeSource = indexNodeSoruce < nodeSourceEvents.size() ? Optional.of(nodeSourceEvents.get(indexNodeSoruce))
                                                                                                        : Optional.empty();

            // if both candidates are present we take that with smallest counter
            if (candidateNode.isPresent() && candidateNodeSource.isPresent()) {
                if (candidateNode.get().getCounter() < candidateNodeSource.get().getCounter()) {
                    clone.nodeEvents.add(candidateNode.get());
                    ++indexNode;
                } else {
                    clone.nodeSourceEvents.add(candidateNodeSource.get());
                    ++indexNodeSoruce;
                }
                // in case only one candidate is present than we have to add it to the chunk
            } else if (candidateNode.isPresent() && !candidateNodeSource.isPresent()) {
                clone.nodeEvents.add(candidateNode.get());
                ++indexNode;
            } else if (!candidateNode.isPresent() && candidateNodeSource.isPresent()) {
                clone.nodeSourceEvents.add(candidateNodeSource.get());
                ++indexNodeSoruce;
            } else { // in case there is no more candidates, we stop our search
                break;
            }
        }

        clone.latestCounter.set(Math.max(actualFilter,
                                         Math.max(findLargestCounter(clone.nodeEvents),
                                                  findLargestCounter(clone.nodeSourceEvents))));

        LOGGER.debug(String.format("Provided %d, return #nodes %d, #nodesource %d, latest %d",
                actualFilter,
                clone.nodeEvents.size(),
                clone.nodeSourceEvents.size(),
                clone.latestCounter.get()));
        return clone;
    }

    private <T extends RMEvent> long findLargestCounter(Collection<T> events) {
        final Optional<T> max = events.stream().max(Comparator.comparing(RMEvent::getCounter));
        return max.map(RMEvent::getCounter).orElse(0l);
    }
}
