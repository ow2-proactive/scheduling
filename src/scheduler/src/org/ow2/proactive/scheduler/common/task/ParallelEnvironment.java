/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;


/**
 * The parallel environment of the multi-nodes tasks that defines
 * the number of nodes needed by task to be executed and topology
 * descriptor optimizing its execution.
 *
 */
@PublicAPI
@Entity
@Table(name = "PARALLEL_ENVIRONMENT")
@AccessType("field")
@Proxy(lazy = false)
@XmlAccessorType(XmlAccessType.FIELD)
public class ParallelEnvironment implements Serializable {

    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hibernateId;

    /** Number of nodes asked by the user. */
    @Column(name = "NODES_NUMBER")
    private int nodesNumber;

    /**
     * The topology descriptor of the task
     */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER)
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
        if (nodesNumber <= 1) {
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
}
