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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This descriptor allows to select one node per host exclusively.
 * Hosts with selected nodes will be reserved for the user.
 *
 * By specifying this descriptor in {@link ResourceManager.getAtMostNodes} user may get
 * more nodes than it asked for due to the fact that total capacity of all machines is
 * bigger.
 *
 * The resource manager first will try to find hosts with only one node
 * to optimize the utilization of resources. If there no such hosts (or their quantity is
 * below required) it will continue looking for hosts with 2 nodes and so on.
 *
 * If number of hosts is not enough the found subset will be provided.
 *
 */
@PublicAPI
@Entity
@DiscriminatorValue("DifferentHostsExclusive")
@AccessType("field")
@Proxy(lazy = false)
public class DifferentHostsExclusiveDescriptor extends TopologyDescriptor {

    /**
     * Constructs a new instance of the class.
     */
    public DifferentHostsExclusiveDescriptor() {
        super(true);
    }
}
