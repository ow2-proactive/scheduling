/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.nodestate;

import static junit.framework.Assert.assertTrue;

import java.io.File;

import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.utils.FileToBytesConverter;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 * Tests adds 10 nodes by GCM deployment descriptor and
 * in parallel starts adding/removing existing nodes.
 *
 * Expected result is 10 available nodes at the end and no deadlocks.
 *
 * @author ProActive team
 *
 */
public class TestConcurrentNodesAdding extends FunctionalTest {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        RMAdmin admin = RMTHelper.getAdminInterface();
        String hostName = ProActiveInet.getInstance().getHostname();

        RMTHelper.log("Test 1 - concurrent nodes adding");
        String nodeURL = "//" + hostName + "/";

        int nodeNumber = 10;
        // creating 10 nodes
        for (int i = 0; i < nodeNumber; i++) {
            RMTHelper.log("Creating node " + i);
            RMTHelper.createNode("node" + i);
        }

        String dd = TestConcurrentNodesAdding.class.getResource("/functionaltests/nodestate/10nodes.xml")
                .getPath();
        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(dd)));
        System.out.println("Start GCM deployment of " + nodeNumber + " nodes asynchronously (1 per JVM)");
        admin.addNodes(NodeSource.DEFAULT_NAME, new Object[] { GCMDeploymentData });

        // wait for the request of node acquisition info adding
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_NODES_ACQUISTION_INFO_ADDED,
                NodeSource.DEFAULT_NAME);

        // wait for first deployed node from GCM
        RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        for (int i = 0; i < nodeNumber; i++) {
            String url = nodeURL + "node" + i;
            RMTHelper.log("Adding node " + i);
            admin.addNode(url).booleanValue();
            RMTHelper.log("Removing node " + i);
            admin.removeNode(url, true);
        }

        for (int i = 0; i < nodeNumber; i++) {
            String url = nodeURL + "node" + i;
            RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, url);
            RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, url);
        }

        assertTrue(admin.getTotalNodesNumber().intValue() == nodeNumber);
    }
}
