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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.nodestate;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;

import org.ow2.tests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 * This class tests a particular case of node add/removal where a race condition
 * exists and that lead to dead lock in the past.
 * Running this test ensure that at least the dead lock doesn't occur.
 */
public class TestAddRemoveAll extends FunctionalTest {

    String nodeName = "nodeDeadLock";

    @org.junit.Test
    public void action() throws Exception {
        ResourceManager resourceManager = RMTHelper.getResourceManager();
        RMTHelper.log("Add/RemoveAll");
        resourceManager.createNodeSource(NodeSource.DEFAULT, DefaultInfrastructureManager.class.getName(),
                null, StaticPolicy.class.getName(), null);
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.DEFAULT);
        Node nodeToAdd = RMTHelper.createNode(nodeName);
        resourceManager.addNode(nodeToAdd.getNodeInformation().getURL(), NodeSource.DEFAULT)
                .getBooleanValue();
        //at this time, nodes maybe fully added in the nodesource but not in the core
        //the next removal may fail for some nodes that are not known by the core...
        RMCore resourceManagerIMPL = (RMCore) resourceManager;
        resourceManagerIMPL.removeAllNodes(NodeSource.DEFAULT, true);
        if (resourceManager.getState().getTotalNodesNumber() != 0) {
            RMTHelper.log("The removeAll method in RMCore didn't removed all nodes");
        } else {
            RMTHelper.log("The removeAll method in RMCore did its job well");
        }
    }
}
