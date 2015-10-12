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
package functionaltests.monitor;

import java.util.Set;

import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.utils.NodeSet;
import org.junit.Test;

import functionaltests.utils.RMFunctionalTest;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.*;


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

        ResourceManager resourceManager = rmHelper.getResourceManager();
        rmHelper.createNodeSource("TestRMMonitoring");

        Set<String> nodesUrls = rmHelper.listAliveNodesUrls();

        // we received all event as we are here
        log("Test 1 - subscribing only to 'node remove' event");
        resourceManager.getMonitoring().removeRMEventListener();
        resourceManager.getMonitoring().addRMEventListener(rmHelper.getEventReceiver(), RMEventType.NODE_REMOVED);

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
