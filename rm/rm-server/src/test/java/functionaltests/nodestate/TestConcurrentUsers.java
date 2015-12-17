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

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.TestUsers;


/**
 *
 * @author ProActive team
 *
 */
public class TestConcurrentUsers extends RMFunctionalTest {

    @Test
    public void testConcurrency() throws Exception {

        ResourceManager resourceManager = rmHelper.getResourceManager();
        String nsName = "TestConcurrentUsers";
        String node1Name = "node1";
        String node1URL = rmHelper.createNode(node1Name).getNode().getNodeInformation().getURL();
        resourceManager.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), null);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);
        resourceManager.addNode(node1URL, nsName);

        // waiting for node adding event
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        // waiting for the node to be free
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        assertTrue(resourceManager.getState().getTotalNodesNumber() == 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 1);

        log("Test 1 - releasing of the foreign node");
        // acquiring a node
        final NodeSet ns = resourceManager.getAtMostNodes(1, null);

        // waiting for node busy event
        RMNodeEvent evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        assertEquals(evt.getNodeState(), NodeState.BUSY);
        assertEquals(ns.size(), 1);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Credentials cred = Credentials.createCredentials(new CredData(
                        CredData.parseLogin("user"), CredData.parseDomain("user"), "pwd"),
                            TestConcurrentUsers.this.rmHelper.getRMAuth().getPublicKey());

                    ResourceManager rm2 = TestConcurrentUsers.this.rmHelper.getRMAuth().login(cred);
                    rm2.releaseNode(ns.get(0)).getBooleanValue();
                    Assert.assertTrue("Should not be able to release foreign node", false);
                } catch (Exception e) {
                    log(e.getMessage());
                }
            }
        };
        t.start();
        t.join();

        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        resourceManager.releaseNodes(ns);
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        assertEquals(evt.getNodeState(), NodeState.FREE);

        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        log("Test 2 - releasing node twice");
        resourceManager.releaseNodes(ns);

        // to make sure everything has been processed
        Thread.sleep(1000);
        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        log("Test 3 - client crash detection");
        JVMProcessImpl nodeProcess = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        nodeProcess.setJvmOptions(Collections.singletonList(PAResourceManagerProperties.RM_HOME.getCmdLine() +
            PAResourceManagerProperties.RM_HOME.getValueAsString()));
        nodeProcess.setClassname(GetAllNodes.class.getName());
        nodeProcess.startProcess();

        // node busy event
        for (int i = 0; i < 1; i++) {
            evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(evt.getNodeState(), NodeState.BUSY);
        }
        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        // client does not exist anymore
        log("Client does not exist anymore. Waiting for client crash detection.");
        // it should be detected by RM
        // waiting for node free event
        for (int i = 0; i < 1; i++) {
            evt = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            assertEquals(NodeState.FREE, evt.getNodeState());
        }
        assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        log("Test 4 - disconnecting");

        NodeSet ns2 = resourceManager.getAtMostNodes(1, null);
        RMNodeEvent event = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, 10000);
        Assert.assertTrue(event.getNodeState() == NodeState.BUSY);
        PAFuture.waitFor(ns2);
        log("Number of found nodes " + ns2.size());
        assertEquals(1, ns2.size());

        t = new Thread() {
            public void run() {
                try {
                    RMAuthentication auth = rmHelper.getRMAuth();
                    Credentials cred = Credentials.createCredentials(new CredData(
                        TestUsers.TEST.username, TestUsers.TEST.password), auth.getPublicKey());
                    ResourceManager rm = auth.login(cred);
                    rm.disconnect().getBooleanValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        t.join();

        try {
            event = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, 10000);
            fail("Unexpected event: " + event);
        } catch (ProActiveTimeoutException e) {
        }
    }
}
