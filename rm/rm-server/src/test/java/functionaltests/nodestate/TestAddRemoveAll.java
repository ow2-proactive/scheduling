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
package functionaltests.nodestate;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;

import functionaltests.utils.RMFunctionalTest;


/**
 * This class tests a particular case of node add/removal where a race condition
 * exists and that lead to dead lock in the past.
 * Running this test ensure that at least the dead lock doesn't occur.
 * A failed connection to the junk node "nodeDeadLock" may appear in subsequent tests,
 * so it's better to restart the RM after the test.
 */
public class TestAddRemoveAll extends RMFunctionalTest {

    private String nodeName = "nodeDeadLock";

    private String nsName = "TestAddRemoveAll";

    @Test
    public void testCreateNodeSource() throws Exception {

        ResourceManager resourceManager = rmHelper.getResourceManager();

        int pingFrequency = 6000;

        log("Add/RemoveAll");
        resourceManager.createNodeSource(nsName,
                                         DefaultInfrastructureManager.class.getName(),
                                         null,
                                         StaticPolicy.class.getName(),
                                         null);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);
        resourceManager.setNodeSourcePingFrequency(pingFrequency, nsName);

        testNode = rmHelper.createNode(nodeName);
        Node nodeToAdd = testNode.getNode();
        resourceManager.addNode(nodeToAdd.getNodeInformation().getURL(), nsName).getBooleanValue();
        //at this time, nodes maybe fully added in the nodesource but not in the core
        //the next removal may fail for some nodes that are not known by the core...
        resourceManager.removeNodeSource(nsName, true);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        Thread.sleep(pingFrequency * 2);
        if (resourceManager.getState().getTotalNodesNumber() != 0) {
            log("The removeAll method in RMCore didn't removed all nodes");
            fail();
        } else {
            log("The removeAll method in RMCore did its job well");
        }
    }

    @After
    public void killRM() throws Exception {
        rmHelper.killRM();
    }
}
