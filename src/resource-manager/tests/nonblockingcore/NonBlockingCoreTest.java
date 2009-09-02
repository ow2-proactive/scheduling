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
package nonblockingcore;

import static junit.framework.Assert.assertTrue;

import java.io.File;

import nodestate.FunctionalTDefaultRM;
import nodestate.RMEventReceiver;

import org.junit.Assert;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMUser;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.FileToBytesConverter;

import selectionscript.SelectionScriptTimeOutTest;


/**
 * Test starts node selection using timeout script. At the same time adds and removes node / standard node source to the RM.
 * Checks that blocking in the node selection does not prevent to use RMAdmin interface (means that core is not blocked by selection).
 *
 * @author ProActice team
 *
 */
public class NonBlockingCoreTest extends FunctionalTDefaultRM {

    private String selectionScriptWithtimeOutPath = SelectionScriptTimeOutTest.class.getResource(
            "selectionScriptWithtimeOut.js").getPath();

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        log("Deployment");

        System.out.println(monitor.isAlive());
        System.out.println(admin.isAlive());

        RMEventType[] eventsList = { RMEventType.NODE_ADDED, RMEventType.NODE_REMOVED,
                RMEventType.NODESOURCE_CREATED, RMEventType.NODE_STATE_CHANGED };

        RMEventReceiver receiver = (RMEventReceiver) PAActiveObject.newActive(
                RMEventReceiver.class.getName(), new Object[] { monitor, eventsList });

        receiver.cleanEventLists();
        super.deployDefault();

        //wait for creation of GCM Node Source event, and deployment of its nodes
        receiver.waitForNEvent(defaultDescriptorNodesNb + 1);
        receiver.cleanEventLists();

        int coreScriptExecutionTimeout = PAResourceManagerProperties.RM_SELECT_SCRIPT_TIMEOUT.getValueAsInt();
        int scriptSleepingTime = coreScriptExecutionTimeout * 2;

        log("Selecting node with timeout script");

        //create the static selection script object
        SelectionScript sScript = new SelectionScript(new File(selectionScriptWithtimeOutPath),
            new String[] { Integer.toString(scriptSleepingTime) }, false);

        long startTime = System.currentTimeMillis();
        RMAuthentication auth = RMConnection.waitAndJoin(null);
        RMUser user = auth.logAsUser(username, password);
        user.getAtMostNodes(2, sScript);

        String hostName = ProActiveInet.getInstance().getHostname();
        String node1Name = "node1";
        String node1URL = "//" + hostName + "/" + node1Name;
        createNode(node1Name);

        log("Adding node " + node1URL);
        admin.addNode(node1URL);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb + 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb + 1);

        //preemptive removal is useless for this case, because node is free
        log("Removing node " + node1URL);
        admin.removeNode(node1URL, false);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);

        String nsName = "myNS";
        log("Creating GCM node source " + nsName);
        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(defaultDescriptor)));
        admin.createNodesource(nsName, GCMInfrastructure.class.getName(), new Object[] { GCMDeploymentData },
                StaticPolicy.class.getName(), null);

        //wait for creation of GCM Node Source event, and deployment of its nodes
        receiver.waitForNEvent(defaultDescriptorNodesNb + 1);
        assertTrue(receiver.cleanNgetNodeSourcesCreatedEvents().size() == 1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == defaultDescriptorNodesNb);
        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb * 2);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb * 2);

        log("Removing node source " + nsName);
        admin.removeSource(nsName, true);

        long endTime = System.currentTimeMillis();

        if ((endTime - startTime) > coreScriptExecutionTimeout) {
            Assert.assertTrue("Blocked inside RMCore", false);
        } else {
            log("No blocking inside RMCore");
        }
    }
}
