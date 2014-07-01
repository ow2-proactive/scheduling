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
package functionaltests.monitor;

import java.util.Set;

import org.junit.Assert;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;


/**
 * Test checks the subscription to RM events.
 * When user subscribes to a particular event he must receive
 * only events of this type.
 *
 */
public class TestRMMonitoring extends RMConsecutive {

    @org.junit.Test
    public void action() throws Exception {
        RMTHelper helper = RMTHelper.getDefaultInstance();

        RMTHelper.log("Deployment");

        ResourceManager resourceManager = helper.getResourceManager();
        helper.createNodeSource("TestRMMonitoring");

        Set<String> nodesUrls = RMTHelper.getDefaultInstance().listAliveNodesUrls();

        // we received all event as we are here
        RMTHelper.log("Test 1 - subscribing only to 'node remove' event");
        resourceManager.getMonitoring().removeRMEventListener();
        resourceManager.getMonitoring().addRMEventListener(helper.getEventReceiver(),
                RMEventType.NODE_REMOVED);

        String url = nodesUrls.iterator().next();
        BooleanWrapper status = resourceManager.removeNode(url, true);
        if (status.getBooleanValue()) {
            try {
                helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED, 10000);
            } catch (ProActiveTimeoutException ex) {
                Assert.assertTrue("Did not receive NODE_REMOVE event", false);
            }
            RMTHelper.log("Test 1 - success");
        }

        NodeSet ns = resourceManager.getAtMostNodes(5, null);
        RMTHelper.log("Got " + ns.size() + " nodes");

        // must not receive "node busy" event
        try {
            helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, 5000);
            Assert.assertTrue("Must not recive this type of event", false);
        } catch (ProActiveTimeoutException ex) {
            RMTHelper.log("Test 2 - success");
        }

        resourceManager.releaseNodes(ns).getBooleanValue();
        RMTHelper.log("End of test");

    }

}
