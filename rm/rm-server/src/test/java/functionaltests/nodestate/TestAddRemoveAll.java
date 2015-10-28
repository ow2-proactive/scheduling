/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.junit.Test;

import functionaltests.utils.RMFunctionalTest;

import static functionaltests.utils.RMTHelper.log;


/**
 * This class tests a particular case of node add/removal where a race condition
 * exists and that lead to dead lock in the past.
 * Running this test ensure that at least the dead lock doesn't occur.
 */
public class TestAddRemoveAll extends RMFunctionalTest {

    private String nodeName = "nodeDeadLock";
    private String nsName = "TestAddRemoveAll";

    @Test
    public void action() throws Exception {

        ResourceManager resourceManager = rmHelper.getResourceManager();
        log("Add/RemoveAll");
        resourceManager.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), null);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);
        Node nodeToAdd = rmHelper.createNode(nodeName).getNode();
        resourceManager.addNode(nodeToAdd.getNodeInformation().getURL(), nsName).getBooleanValue();
        //at this time, nodes maybe fully added in the nodesource but not in the core
        //the next removal may fail for some nodes that are not known by the core...
        resourceManager.removeNodeSource(nsName, true);
        if (resourceManager.getState().getTotalNodesNumber() != 0) {
            log("The removeAll method in RMCore didn't removed all nodes");
        } else {
            log("The removeAll method in RMCore did its job well");
        }
    }
}
