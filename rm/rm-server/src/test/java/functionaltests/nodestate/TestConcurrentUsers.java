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
package functionaltests.nodestate;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.NodeSet;
import junit.framework.Assert;

import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 *
 * @author ProActive team
 *
 */
public class TestConcurrentUsers extends RMConsecutive {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {
        final RMTHelper helper = RMTHelper.getDefaultInstance();

        ResourceManager resourceManager = helper.getResourceManager();
        String nsName = "TestConcurrentUsers";
        String node1Name = "node1";
        String node1URL = helper.createNode(node1Name).getNode().getNodeInformation().getURL();
        resourceManager.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), null);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);
        resourceManager.addNode(node1URL, nsName);

        // waiting for node adding event
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        // waiting for the node to be free
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        assertTrue(resourceManager.getState().getTotalNodesNumber() == 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 1);

        RMTHelper.log("Test 1 - releasing of the foreign node");
        // acquiring a node
        final NodeSet ns = resourceManager.getAtMostNodes(1, null);

        // waiting for node busy event
        RMNodeEvent evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        assertTrue(ns.size() == 1);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Credentials cred = Credentials.createCredentials(new CredData(
                        CredData.parseLogin("user"), CredData.parseDomain("user"), "pwd"), helper.getRMAuth()
                            .getPublicKey());

                    ResourceManager rm2 = helper.getRMAuth().login(cred);
                    rm2.releaseNode(ns.get(0)).getBooleanValue();
                    Assert.assertTrue("Should not be able to release foreign node", false);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        t.start();
        t.join();

        Assert.assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        resourceManager.releaseNodes(ns);
        evt = helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);

        Assert.assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        RMTHelper.log("Test 2 - releasing node twice");
        resourceManager.releaseNodes(ns);

        // to make sure everything has been processed
        Thread.sleep(1000);
        Assert.assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        RMTHelper.log("Test 3 - client crash detection");
        JVMProcessImpl nodeProcess = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        nodeProcess.setClassname("functionaltests.nodestate.GetAllNodes");
        nodeProcess.startProcess();

        // node busy event
        for (int i = 0; i < 1; i++) {
            evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }
        Assert.assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        // client does not exist anymore
        RMTHelper.log("Client does not exist anymore. Waiting for client crash detection.");
        // it should be detected by RM
        // waiting for node free event
        for (int i = 0; i < 1; i++) {
            evt = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(NodeState.FREE, evt.getNodeState());
        }
        Assert.assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        RMTHelper.log("Test 4 - disconnecting");

        NodeSet ns2 = resourceManager.getAtMostNodes(1, null);
        RMNodeEvent event = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, 10000);
        Assert.assertTrue(event.getNodeState() == NodeState.BUSY);
        PAFuture.waitFor(ns2);
        System.out.println("Number of found nodes " + ns2.size());
        Assert.assertEquals(1, ns2.size());

        t = new Thread() {
            public void run() {
                try {
                    RMAuthentication auth = RMTHelper.getDefaultInstance().getRMAuth();
                    Credentials cred = Credentials.createCredentials(new CredData(RMTHelper.defaultUserName,
                        RMTHelper.defaultUserPassword), auth.getPublicKey());
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
            event = helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, 10000);
            fail("Unexpected event: " + event);
        } catch (ProActiveTimeoutException e) {
        }
    }
}
