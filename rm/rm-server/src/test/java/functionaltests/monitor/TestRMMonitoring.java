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
package functionaltests.monitor;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Test;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.utils.RMFunctionalTest;


/**
 * Test checks the subscription to RM events.
 * When user subscribes to a particular event he must receive
 * only events of this type.
 *
 */
public class TestRMMonitoring extends RMFunctionalTest {

    @Test
    public void action() throws Exception {
        log("Deployment");

        RMMonitorEventReceiver resourceManager = (RMMonitorEventReceiver) rmHelper.getResourceManager();
        rmHelper.createNodeSource("TestRMMonitoring");

        Set<String> nodesUrls = rmHelper.listAliveNodesUrls();

        // we received all event as we are here
        log("Test 1 - subscribing only to 'node remove' event");
        resourceManager.removeRMEventListener();
        resourceManager.addRMEventListener(rmHelper.getEventReceiver(), RMEventType.NODE_REMOVED);

        String url = nodesUrls.iterator().next();
        BooleanWrapper status = resourceManager.removeNode(url, true);
        if (status.getBooleanValue()) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED, 10000);
            log("Test 1 - success");
        }

        NodeSet ns = resourceManager.getAtMostNodes(5, null);
        log("Got " + ns.size() + " nodes");

        // must not receive "node busy" event
        try {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, 2000);
            fail("Must not receive this type of event");
        } catch (ProActiveTimeoutException ex) {
            log("Test 2 - success");
        }

        resourceManager.releaseNodes(ns).getBooleanValue();
    }

}
