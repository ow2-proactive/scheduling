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
package functionaltests.nonblockingcore;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;

import javax.security.auth.login.LoginException;

import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.selectionscript.SelectionScriptTimeOutTest;
import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;
import functionaltests.utils.TestUsers;


/**
 * Test starts node selection using timeout script. At the same time adds and
 * removes node source and nodes to the RM. Checks that blocking in the node
 * selection mechanism does not prevent usage of RMAdmin interface.
 *
 * @author ProActive team
 *
 */
public class NonBlockingCoreTest extends RMFunctionalTest {

    private URL selectionScriptWithtimeOutPath = SelectionScriptTimeOutTest.class.getResource("selectionScriptWithtimeOut.groovy");

    private NodeSet nodes;

    @Test
    public void action() throws Exception {
        String rmconf = new File(PAResourceManagerProperties.getAbsolutePath(getClass().getResource("/functionaltests/config/functionalTRMProperties-long-selection-script-timeout.ini")
                                                                                       .getFile())).getAbsolutePath();
        rmHelper.startRM(rmconf);

        ResourceManager resourceManager = rmHelper.getResourceManager();
        int initialNodeNumber = 2;
        rmHelper.createNodeSource("NonBlockingCoreTest1", initialNodeNumber);
        long coreScriptExecutionTimeout = PAResourceManagerProperties.RM_SELECT_SCRIPT_TIMEOUT.getValueAsLong();
        long scriptSleepingTime = coreScriptExecutionTimeout * 2;

        log("Selecting node with timeout script");

        // create the static selection script object
        final SelectionScript sScript = new SelectionScript(new File(selectionScriptWithtimeOutPath.toURI()),
                                                            new String[] { Long.toString(scriptSleepingTime) },
                                                            false);

        // mandatory to use RMUser AO, otherwise, getAtMostNode we be send in
        // RMAdmin request queue
        final RMAuthentication auth = rmHelper.getRMAuth();

        final Credentials cred = Credentials.createCredentials(new CredData(TestUsers.TEST.username,
                                                                            TestUsers.TEST.password),
                                                               auth.getPublicKey());

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

        String nodeName = "node_non_blocking_test";
        testNode = RMTHelper.createNode(nodeName);
        String nodeUrl = testNode.getNode().getNodeInformation().getURL();

        log("Adding node " + nodeUrl);
        resourceManager.addNode(nodeUrl);

        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, nodeUrl);
        // waiting for node to be in free state, it is in configuring state when
        // added...
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        assertEquals(resourceManager.getState().getTotalNodesNumber(), initialNodeNumber + 1);
        assertEquals(resourceManager.getState().getFreeNodesNumber(), initialNodeNumber + 1);

        // preemptive removal is useless for this case, because node is free
        log("Removing node " + nodeUrl);
        resourceManager.removeNode(nodeUrl, false);

        rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, nodeUrl);

        assertEquals(resourceManager.getState().getTotalNodesNumber(), initialNodeNumber);
        assertEquals(resourceManager.getState().getFreeNodesNumber(), initialNodeNumber);

        String nsName = "NonBlockingCoreTest";
        log("Creating a node source " + nsName);
        int nsNodesNumber = 1;
        rmHelper.createNodeSource(nsName, nsNodesNumber);

        assertEquals(resourceManager.getState().getTotalNodesNumber(), nsNodesNumber + initialNodeNumber);
        assertEquals(resourceManager.getState().getFreeNodesNumber(), nsNodesNumber + initialNodeNumber);

        log("Removing node source " + nsName);
        resourceManager.removeNodeSource(nsName, true);

        boolean selectionInProgress = PAFuture.isAwaited(nodes);

        if (!selectionInProgress) {
            // normally we are looking for 2 nodes with timeout script,
            // so the selection procedure has to finish not earlier than
            // coreScriptExecutionTimeout * nodesNumber
            // it should be sufficient to perform all admin operations
            // concurrently
            //
            // if the core is blocked admin operations will be performed after
            // selection
            fail("Blocked inside RMCore");
        } else {
            log("No blocking inside RMCore");
        }
        t.interrupt();
    }
}
