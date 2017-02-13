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
package org.ow2.proactive.topology.descriptor;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This descriptor allows to select one node per host exclusively.
 * Hosts with selected nodes will be reserved for the user.
 * <p>
 * By specifying this descriptor in {@code ResourceManager.getAtMostNodes} user may get
 * more nodes than it asked for due to the fact that total capacity of all machines is
 * bigger.
 * <p>
 * The resource manager first will try to find hosts with only one node
 * to optimize the utilization of resources. If there no such hosts (or their quantity is
 * below required) it will continue looking for hosts with 2 nodes and so on.
 *
 * If number of hosts is not enough the found subset will be provided.
 */
@PublicAPI
public class DifferentHostsExclusiveDescriptor extends TopologyDescriptor {

    /**
     * Constructs a new instance of the class.
     */
    public DifferentHostsExclusiveDescriptor() {
        super(true);
    }
}
