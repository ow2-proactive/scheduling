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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.topology.descriptor;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 *
 * Class represents the descriptor of the nodes topology (network location)
 * which could be used with {@link ResourceManager.getAtMostNodes} request.
 *
 * User may create its own instance of available descriptors and parameterize it or
 * use one of predefined constants when it is sufficient.
 */
@PublicAPI
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "TOPOLOGY_DESCRIPTOR")
@AccessType("field")
@Proxy(lazy = false)
public class TopologyDescriptor implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 30L;
    /** no constraint on node location */
    public static final TopologyDescriptor ARBITRARY = new ArbitraryTopologyDescriptor();
    /** the set of closest nodes */
    public static final TopologyDescriptor BEST_PROXIMITY = new BestProximityDescriptor();
    /** the set of nodes on a single host */
    public static final TopologyDescriptor SINGLE_HOST = new SingleHostDescriptor();
    /**
     * the set of nodes of a single host exclusively
     * (the host is reserved for the user)
     */
    public static final TopologyDescriptor SINGLE_HOST_EXCLUSIVE = new SingleHostExclusiveDescriptor();
    /**
     * the set of nodes of several hosts exclusively
     * (hosts are reserved for the user)
     */
    public static final TopologyDescriptor MULTIPLE_HOSTS_EXCLUSIVE = new MultipleHostsExclusiveDescriptor();
    /**
     * the set of nodes one per host exclusively
     * (hosts are reserved for the user)
     */
    public static final TopologyDescriptor DIFFERENT_HOSTS_EXCLUSIVE = new DifferentHostsExclusiveDescriptor();

    @Id
    @GeneratedValue
    private long hibernateId;

    /**
     * the flag indicated that descriptor requires the topology information in the resource manager.
     * It affects the scripts execution strategy: if true selection scripts are executed on all nodes
     * first and then the topology information is taken into account. If false scripts are executed
     * only on required number of nodes.
     * If this field is set to false the descriptor could be used even when the topology is
     * disabled in the resource manager.
     */
    @Column(name = "TOPOLOGY_BASED")
    private boolean topologyBased;

    /**
     * Creates the descriptor.
     * @param topologyBased indicates that descriptor requires the topology information in the resource manager.
     * It affects the scripts execution strategy: if true selection scripts are executed on all nodes
     * first and then the topology information is taken into account. If false scripts are executed
     * only on required number of nodes.
     * If this field is set to false the descriptor could be used even when the topology is
     * disabled in the resource manager.
     */
    protected TopologyDescriptor(boolean topologyBased) {
        this.topologyBased = topologyBased;
    }

    /**
     * Defines if selection scripts have to be executed on all
     * available nodes before the topology information will be processed.
     *
     * @return true in case of greedy behavior false otherwise
     */
    public boolean isTopologyBased() {
        return topologyBased;
    }

    /**
     * Returns the string representation of the descriptor.
     */
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
