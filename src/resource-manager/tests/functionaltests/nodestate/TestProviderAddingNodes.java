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
package functionaltests.nodestate;

import static junit.framework.Assert.assertTrue;
import junit.framework.Assert;

import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMProvider;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 * Test checks the provider role
 * - an ability to add remove node to the resource manager
 * - call get/free nodes
 * - cannot remove foreign node
 *
 * @author ProActive team
 *
 */
public class TestProviderAddingNodes extends FunctionalTest {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        RMAdmin admin = RMTHelper.getAdminInterface();

        admin.createNodesource(NodeSource.DEFAULT, GCMInfrastructure.class.getName(), null,
                StaticPolicy.class.getName(), null);
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.DEFAULT);

        String hostName = ProActiveInet.getInstance().getHostname();
        String node1Name = "node1";
        String node1URL = "//" + hostName + "/" + node1Name;

        admin.disconnect();
        RMAuthentication auth = RMTHelper.getRMAuth();
        Credentials cred = Credentials.createCredentials("provider", "provider", auth.getPublicKey());
        RMProvider provider = auth.logAsProvider(cred);

        RMTHelper.log("Test 1 - adding node as provider");
        RMTHelper.createNode(node1Name);
        provider.addNode(node1URL, NodeSource.DEFAULT);
        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        Assert.assertEquals(1, provider.getTotalAliveNodesNumber().intValue());

        RMTHelper.log("Test 2 - getting/freeing node");
        NodeSet ns = provider.getAtMostNodes(1, null);
        Assert.assertEquals(1, ns.size());

        RMNodeEvent evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);

        provider.freeNodes(ns);
        evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);

        RMTHelper.log("Test 2 - removing node");
        provider.removeNode(node1URL, false);

        RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node1URL);

        assertTrue(provider.getTotalNodesNumber().intValue() == 0);
        assertTrue(provider.getTotalAliveNodesNumber().intValue() == 0);
        assertTrue(provider.getFreeNodesNumber().intValue() == 0);

        RMTHelper.log("Test 3 - creating node as provider removing as another provider");
        RMTHelper.createNode(node1Name);

        provider.addNode(node1URL, NodeSource.DEFAULT);

        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        //wait the node added event
        Assert.assertEquals(1, provider.getTotalAliveNodesNumber().intValue());

        provider.disconnect();
        Credentials cred2 = Credentials.createCredentials("provider2", "provider2", auth.getPublicKey());
        RMProvider provider2 = auth.logAsProvider(cred2);

        provider2.removeNode(node1URL, true);

        try {
            RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node1URL, 5000);
            assertTrue("provider2 successfully removed node owned by provider", false);
        } catch (ProActiveTimeoutException e) {
        }

        provider2.disconnect();
        RMTHelper.log("Test 4 - removing provider's node as admin");
        Credentials credAdmin = Credentials.createCredentials(RMTHelper.username, RMTHelper.password, auth
                .getPublicKey());
        admin = auth.logAsAdmin(credAdmin);
        admin.removeNode(node1URL, true);
        RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node1URL);

        assertTrue(admin.getTotalNodesNumber().intValue() == 0);
        assertTrue(admin.getTotalAliveNodesNumber().intValue() == 0);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);
    }
}
