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
package nodestate;

import static junit.framework.Assert.assertTrue;

import java.io.File;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 * Tests adds 10 nodes by GCM deployment descriptor and
 * in parallel starts adding/removing existing nodes.
 *
 * Expected result is 10 available nodes at the end and no deadlocks.
 *
 * @author ProActive team
 *
 */
public class TestConcurrentNodesAdding extends FunctionalTDefaultRM {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        RMEventType[] eventsList = { RMEventType.NODE_ADDED, RMEventType.NODE_STATE_CHANGED,
                RMEventType.NODE_REMOVED };

        RMEventReceiver receiver = (RMEventReceiver) PAActiveObject.newActive(
                RMEventReceiver.class.getName(), new Object[] { monitor, eventsList });

        receiver.cleanEventLists();
        RMFactory.setOsJavaProperty();
        String hostName = ProActiveInet.getInstance().getHostname();

        log("Test 1 - concurrent nodes adding");
        String nodeURL = "//" + hostName + "/";

        int nodeNumber = 10;
        // creating 10 nodes
        for (int i = 0; i < nodeNumber; i++) {
            log("Creating node " + i);
            createNode("node" + i);
        }

        String dd = FunctionalTDefaultRM.class.getResource("/nodestate/10nodes.xml").getPath();
        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(dd)));
        System.out.println("Start GCM deployment of " + nodeNumber + " nodes asynchronously (1 per JVM)");
        admin.addNodes(NodeSource.DEFAULT_NAME, new Object[] { GCMDeploymentData });

        // wait for the request of node acquisition info adding
        receiver.waitForNEvent(1);

        // wait for first deployed node from GCM
        receiver.waitForNEvent(1);

        for (int i = 0; i < nodeNumber; i++) {
            String url = nodeURL + "node" + i;
            log("Adding node " + i);
            admin.addNode(url).booleanValue();
            log("Removing node " + i);
            admin.removeNode(url, true);
        }

        receiver.waitForNEvent(nodeNumber - 1 + 2 * nodeNumber);
        assertTrue(admin.getTotalNodesNumber().intValue() == nodeNumber);
    }
}
