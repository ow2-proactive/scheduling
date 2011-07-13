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
package functionaltests.nonblockingcore;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import javax.security.auth.login.LoginException;

import org.junit.Assert;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
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

    private URL selectionScriptWithtimeOutPath = SelectionScriptTimeOutTest.class
            .getResource("selectionScriptWithtimeOut.js");

    private NodeSet nodes;

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        ResourceManager resourceManager = RMTHelper.getResourceManager();

        RMTHelper.log("Deployment");

        RMTHelper.createGCMLocalNodeSource();
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.GCM_LOCAL);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            //waiting for node to be in free state
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }

        int coreScriptExecutionTimeout = PAResourceManagerProperties.RM_SELECT_SCRIPT_TIMEOUT.getValueAsInt();
        int scriptSleepingTime = coreScriptExecutionTimeout * 2;

        RMTHelper.log("Selecting node with timeout script");

        //create the static selection script object
        final SelectionScript sScript = new SelectionScript(new File(selectionScriptWithtimeOutPath.toURI()),
            new String[] { Integer.toString(scriptSleepingTime) }, false);

        //mandatory to use RMUser AO, otherwise, getAtMostNode we be send in RMAdmin request queue
        final RMAuthentication auth = RMConnection.waitAndJoin(null);

        final Credentials cred = Credentials.createCredentials(new CredData(RMTHelper.username,
            RMTHelper.password), auth.getPublicKey());

        // cannot connect twice from the same active object
        // so creating another thread
        Thread t = new Thread() {
            public void run() {
                ResourceManager rm2;
                try {
                    rm2 = auth.login(cred);
                    nodes = rm2.getAtMostNodes(2, sScript);
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
        resourceManager.addNode(node1URL, NodeSource.GCM_LOCAL);

        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        //waiting for node to be in free state, it is in configuring state when added...
        RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber + 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber + 1);

        //preemptive removal is useless for this case, because node is free
        RMTHelper.log("Removing node " + node1URL);
        resourceManager.removeNode(node1URL, false);

        RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node1URL);

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        String nsName = "myNS";
        RMTHelper.log("Creating GCM node source " + nsName);
        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(
            RMTHelper.defaultDescriptor.toURI())));
        //first im parameter is default rm url
        resourceManager.createNodeSource(nsName, GCMInfrastructure.class.getName(), new Object[] { "",
                GCMDeploymentData }, StaticPolicy.class.getName(), null);

        //wait for creation of GCM Node Source event, and deployment of its nodes
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);
        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            //waiting for the node to be in free state
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber * 2);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber * 2);

        RMTHelper.log("Removing node source " + nsName);
        resourceManager.removeNodeSource(nsName, true);

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
