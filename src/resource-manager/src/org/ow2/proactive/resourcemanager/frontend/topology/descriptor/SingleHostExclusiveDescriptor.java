/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.frontend.topology.descriptor;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.SingleHostDescriptor;


/**
 * This descriptor allows to select nodes on the single hosts exclusively.
 * The host with selected nodes will be reserved for the user.
 *
 * By specifying this descriptor in {@link ResourceManager.getAtMostNodes} user may get
 * more nodes than it asked for due to the fact that total capacity of hosts is
 * bigger (even thought the resource manager tries to find the optimal host
 * minimizing the waist of resources), namely
 *
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
