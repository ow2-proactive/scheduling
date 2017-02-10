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
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;


/**
 * The parallel environment of the multi-nodes tasks that defines
 * the number of nodes needed by task to be executed and topology
 * descriptor optimizing its execution.
 *
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class ParallelEnvironment implements Serializable {

    /** Number of nodes asked by the user. */
    private int nodesNumber;

    /**
     * The topology descriptor of the task
     */
    private TopologyDescriptor topologyDescriptor = null;

    /**
     * Default constructor for hibernate
     */
    protected ParallelEnvironment() {
    }

    /**
     * Creates new instance of the parallel environment.
     *
     * @param nodesNumber the number of nodes required by task.
     */
    public ParallelEnvironment(int nodesNumber) {
        if (nodesNumber < 1) {
            throw new IllegalArgumentException("nodesNumber must be greater than 1");
        }
        this.nodesNumber = nodesNumber;
    }

    /**
     * Creates new instance of the parallel environment.
     *
     * @param nodesNumber the number of nodes required by task.
     * @param topologyDescriptor nodes topology that will be used for this task.
     */
    public ParallelEnvironment(int nodesNumber, TopologyDescriptor topologyDescriptor) {
        this(nodesNumber);
        this.topologyDescriptor = topologyDescriptor;
    }

    /**
     * Copy constructor
     * <p>
     * Does not properly copy the topology descriptor, shares the same reference
     * 
     * @param original the original parallel env to copy
     */
    public ParallelEnvironment(ParallelEnvironment original) {
        this.nodesNumber = original.getNodesNumber();
        this.topologyDescriptor = original.topologyDescriptor;
    }

    /**
     * Returns the node number of the parallel task.
     * @return the node number of the parallel task.
     */
    public int getNodesNumber() {
        return nodesNumber;
    }

    /**
     * Returns the topology descriptor of the parallel task.
     * @return the topology descriptor of the parallel task.
     */
    public TopologyDescriptor getTopologyDescriptor() {
        return topologyDescriptor;
    }

    public void setTopologyDescriptor(TopologyDescriptor topologyDescriptor) {
        this.topologyDescriptor = topologyDescriptor;
    }

    @Override
    public String toString() {
        return "ParallelEnvironment { " + " nodesNumber = " + nodesNumber + ", topologyDescriptor = " +
               topologyDescriptor + '}';
    }
}
