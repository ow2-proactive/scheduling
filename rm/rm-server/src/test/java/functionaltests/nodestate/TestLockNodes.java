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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.nodestate;

import java.util.Set;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.utils.NodeSet;
import org.junit.Test;

import functionaltests.utils.RMFunctionalTest;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.*;


/**
 * Test checks the correctness of nodes locking.
 * Locking is applicable only for free nodes. An attempt to lock them in
 * other states must lead to an exception.
 *
 * Unlock changes node states to "free".
 *
 */
public class TestLockNodes extends RMFunctionalTest {

    @Test
    public void action() throws Exception {

        log("Deployment");

        ResourceManager resourceManager = rmHelper.getResourceManager();
        int nodesNumber = rmHelper.createNodeSource("TestLockNodes");

        Set<String> nodesUrls = rmHelper.listAliveNodesUrls();

        log("Test 1 - locking");

        BooleanWrapper status = resourceManager.lockNodes(nodesUrls);
        if (status.getBooleanValue()) {

            for (int i = 0; i < nodesNumber; i++) {
                RMNodeEvent nodeEvent = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
                assertEquals(nodeEvent.getNodeState(), NodeState.LOCKED);
            }

            log("Test 1 - success");
        }

        log("Test 2 - unlocking");

        status = resourceManager.unlockNodes(nodesUrls);
        if (status.getBooleanValue()) {

            for (int i = 0; i < nodesNumber; i++) {
                RMNodeEvent nodeEvent = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
                assertEquals(nodeEvent.getNodeState(), NodeState.FREE);
            }

            log("Test 2 - success");
        }

        log("Test 3 - locking non-free nodes");

        NodeSet nodes = resourceManager.getAtMostNodes(nodesNumber, null);

        PAFuture.waitFor(nodes);
        assertEquals(nodes.size(), nodesNumber);
        assertEquals(resourceManager.getState().getFreeNodesNumber(), 0);

        for (int i = 0; i < nodesNumber; i++) {
            RMNodeEvent evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        BooleanWrapper res = resourceManager.lockNodes(nodesUrls);
        assertFalse("Successfully locked non-free nodes", res.getBooleanValue());

        log("Test 4 - unlocking nodes which are not locked");

        res = resourceManager.unlockNodes(nodesUrls);
        assertFalse("Successfully locked non-free nodes", res.getBooleanValue());
        log("End of test");
    }

}
