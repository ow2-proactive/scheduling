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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 * Test checks the correctness of nodes locking.
 * Locking is applicable only for free nodes. An attempt to lock them in
 * other states must lead to an exception.
 *
 * Unlock changes node states to "free".
 *
 */
public class TestLockNodes extends FunctionalTest {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        RMTHelper.log("Deployment");

        ResourceManager resourceManager = RMTHelper.getResourceManager();

        RMTHelper.createLocalNodeSource();
        RMTHelper
                .waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.LOCAL_INFRASTRUCTURE_NAME);

        Set<String> nodesUrls = new HashSet<String>();
        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            //wait for the nodes to be in free state
            RMNodeEvent nodeEvent = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            nodesUrls.add(nodeEvent.getNodeUrl());
        }

        RMTHelper.log("Test 1 - locking");

        BooleanWrapper status = resourceManager.lockNodes(nodesUrls);
        if (status.getBooleanValue()) {

            for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
                RMNodeEvent nodeEvent = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
                Assert.assertEquals(nodeEvent.getNodeState(), NodeState.LOCKED);
            }

            RMTHelper.log("Test 1 - success");
        }

        RMTHelper.log("Test 2 - unlocking");

        status = resourceManager.unlockNodes(nodesUrls);
        if (status.getBooleanValue()) {

            for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
                RMNodeEvent nodeEvent = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
                Assert.assertEquals(nodeEvent.getNodeState(), NodeState.FREE);
            }

            RMTHelper.log("Test 2 - success");
        }

        RMTHelper.log("Test 3 - locking non-free nodes");

        NodeSet nodes = resourceManager.getAtMostNodes(RMTHelper.defaultNodesNumber, null);

        PAFuture.waitFor(nodes);
        assertTrue(nodes.size() == RMTHelper.defaultNodesNumber);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        try {
            resourceManager.lockNodes(nodesUrls).getBooleanValue();
            Assert.assertTrue("Successfully locked non-free nodes", false);
        } catch (RuntimeException e) {
            RMTHelper.log(e.getMessage());
            RMTHelper.log("Test 3 - success");
        }

        RMTHelper.log("Test 4 - unlocking nodes which are not locked");

        try {
            resourceManager.unlockNodes(nodesUrls).getBooleanValue();
            Assert.assertTrue("Successfully unlocked busy nodes", false);
        } catch (RuntimeException e) {
            RMTHelper.log(e.getMessage());
            RMTHelper.log("Test 4 - success");
        }
        RMTHelper.log("End of test");
    }

}
