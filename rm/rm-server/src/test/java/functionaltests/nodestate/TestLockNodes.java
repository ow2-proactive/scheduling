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
package functionaltests.nodestate;

import static junit.framework.Assert.assertTrue;

import java.util.Set;

import junit.framework.Assert;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;


/**
 * Test checks the correctness of nodes locking.
 * Locking is applicable only for free nodes. An attempt to lock them in
 * other states must lead to an exception.
 *
 * Unlock changes node states to "free".
 *
 */
public class TestLockNodes extends RMConsecutive {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {
        RMTHelper helper = RMTHelper.getDefaultInstance();

        RMTHelper.log("Deployment");

        ResourceManager resourceManager = helper.getResourceManager();
        int nodesNumber = helper.createNodeSource("TestLockNodes");

        Set<String> nodesUrls = RMTHelper.getDefaultInstance().listAliveNodesUrls();

        RMTHelper.log("Test 1 - locking");

        BooleanWrapper status = resourceManager.lockNodes(nodesUrls);
        if (status.getBooleanValue()) {

            for (int i = 0; i < nodesNumber; i++) {
                RMNodeEvent nodeEvent = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
                Assert.assertEquals(nodeEvent.getNodeState(), NodeState.LOCKED);
            }

            RMTHelper.log("Test 1 - success");
        }

        RMTHelper.log("Test 2 - unlocking");

        status = resourceManager.unlockNodes(nodesUrls);
        if (status.getBooleanValue()) {

            for (int i = 0; i < nodesNumber; i++) {
                RMNodeEvent nodeEvent = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
                Assert.assertEquals(nodeEvent.getNodeState(), NodeState.FREE);
            }

            RMTHelper.log("Test 2 - success");
        }

        RMTHelper.log("Test 3 - locking non-free nodes");

        NodeSet nodes = resourceManager.getAtMostNodes(nodesNumber, null);

        PAFuture.waitFor(nodes);
        assertTrue(nodes.size() == nodesNumber);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);

        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        BooleanWrapper res = resourceManager.lockNodes(nodesUrls);
        if (res.getBooleanValue()) {
            Assert.assertTrue("Successfully locked non-free nodes", false);
        } else {
            RMTHelper.log("Test 3 - success");
        }

        RMTHelper.log("Test 4 - unlocking nodes which are not locked");

        res = resourceManager.unlockNodes(nodesUrls);
        if (res.getBooleanValue()) {
            Assert.assertTrue("Successfully unlocked busy nodes", false);
        } else {
            RMTHelper.log("Test 4 - success");
        }
        RMTHelper.log("End of test");
    }

}
