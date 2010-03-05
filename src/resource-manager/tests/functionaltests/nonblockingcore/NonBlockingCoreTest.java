/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.nonblockingcore;

import static junit.framework.Assert.assertTrue;

import java.io.File;

import javax.security.auth.login.LoginException;

import org.junit.Assert;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMUser;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;
import functionaltests.selectionscript.SelectionScriptTimeOutTest;


/**
 * Test starts node selection using timeout script. At the same time adds and removes node source and nodes to the RM.
 * Checks that blocking in the node selection mechanism does not prevent usage of RMAdmin interface.
 *
 * @author ProActice team
 *
 */
public class NonBlockingCoreTest extends FunctionalTest {

    private String selectionScriptWithtimeOutPath = SelectionScriptTimeOutTest.class.getResource(
            "selectionScriptWithtimeOut.js").getPath();

    private NodeSet nodes;

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        RMAdmin admin = RMTHelper.getAdminInterface();

        RMTHelper.log("Deployment");

        RMTHelper.createGCMLocalNodeSource();
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.GCM_LOCAL);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        int coreScriptExecutionTimeout = PAResourceManagerProperties.RM_SELECT_SCRIPT_TIMEOUT.getValueAsInt();
        int scriptSleepingTime = coreScriptExecutionTimeout * 2;

        RMTHelper.log("Selecting node with timeout script");

        //create the static selection script object
        final SelectionScript sScript = new SelectionScript(new File(selectionScriptWithtimeOutPath),
            new String[] { Integer.toString(scriptSleepingTime) }, false);

        //mandatory to use RMUser AO, otherwise, getAtMostNode we be send in RMAdmin request queue
        final RMAuthentication auth = RMConnection.waitAndJoin(null);

        final Credentials cred = Credentials.createCredentials(RMTHelper.username, RMTHelper.password, auth
                .getPublicKey());

        // cannot connect twice from the same active object
        // so creating another thread
        Thread t = new Thread() {
            public void run() {
                RMUser user;
                try {
                    user = auth.logAsUser(cred);
                    nodes = user.getAtMostNodes(2, sScript);
                } catch (LoginException e) {
                }
            }
        };
        t.start();

        String hostName = ProActiveInet.getInstance().getHostname();
        String node1Name = "node1";
        String node1URL = "//" + hostName + "/" + node1Name;
        RMTHelper.createNode(node1Name);

        RMTHelper.log("Adding node " + node1URL);
        admin.addNode(node1URL, NodeSource.GCM_LOCAL);

        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);

        assertTrue(admin.getTotalNodesNumber().intValue() == RMTHelper.defaultNodesNumber + 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber + 1);

        //preemptive removal is useless for this case, because node is free
        RMTHelper.log("Removing node " + node1URL);
        admin.removeNode(node1URL, false);

        RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node1URL);

        assertTrue(admin.getTotalNodesNumber().intValue() == RMTHelper.defaultNodesNumber);
        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber);

        String nsName = "myNS";
        RMTHelper.log("Creating GCM node source " + nsName);
        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(
            RMTHelper.defaultDescriptor)));
        admin.createNodesource(nsName, GCMInfrastructure.class.getName(), new Object[] { GCMDeploymentData },
                StaticPolicy.class.getName(), null);

        //wait for creation of GCM Node Source event, and deployment of its nodes
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);
        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        assertTrue(admin.getTotalNodesNumber().intValue() == RMTHelper.defaultNodesNumber * 2);
        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber * 2);

        RMTHelper.log("Removing node source " + nsName);
        admin.removeSource(nsName, true);

        boolean selectionInProgress = PAFuture.isAwaited(nodes);

        if (!selectionInProgress) {
            // normally we are looking for 2 nodes with timeout script,
            // so the selection procedure has to finish not earlier than
            // coreScriptExecutionTimeout * RMTHelper.defaultNodesNumber
            // it should be sufficient to perform all admin operations concurrently
            //
            // if the core is blocked admin operations will be performed after selection
            Assert.assertTrue("Blocked inside RMCore", false);
        } else {
            RMTHelper.log("No blocking inside RMCore");
        }
    }
}
