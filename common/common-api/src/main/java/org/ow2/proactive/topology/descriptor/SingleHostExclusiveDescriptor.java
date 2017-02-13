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
 * This descriptor allows to select nodes on the single hosts exclusively.
 * The host with selected nodes will be reserved for the user.
 * <p>
 * By specifying this descriptor in {@code ResourceManager.getAtMostNodes} user may get
 * more nodes than it asked for due to the fact that total capacity of hosts is
 * bigger (even thought the resource manager tries to find the optimal host
 * minimizing the waist of resources), namely
 * <p>
 * if user requested k nodes
 *
 * - the machine with exact capacity will be selected if exists
 * - the machine with bigger capacity will be selected if exists.
 *   The capacity of the selected machine will be the closest to k.
 * - the machine with smaller capacity than k will be selected.
 *   In this case the capacity of selected host will be the biggest among all other.
 *
 */
@PublicAPI
public class SingleHostExclusiveDescriptor extends SingleHostDescriptor {

    /**
     * Constructs the new instance of this class.
     */
    public SingleHostExclusiveDescriptor() {
        super();
    }
}
