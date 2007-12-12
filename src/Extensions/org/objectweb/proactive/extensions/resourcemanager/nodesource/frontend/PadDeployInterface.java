/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend;

import org.objectweb.proactive.core.node.Node;


/**
 * Interface between RMDeploy objects and a NodeSource that performs
 * deployment by ProActive descriptors.
 *
 * @see PADNodesource
 *
 * @author ProActive team.
 * @version 3.9
 * @since ProActive 3.9
 *
 */
public interface PadDeployInterface {

    /**
     * Things to do when a new node has been deployed by a ProActivedescriptor
     * and is now available.
     * Called by RMDeploy.
     */
    public void receiveDeployedNode(Node node, String VnName, String PADName);
}
