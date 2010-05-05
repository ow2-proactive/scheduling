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
package functionaltests.permissions;

import static junit.framework.Assert.assertTrue;

import java.io.File;

import junit.framework.Assert;

import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 *  Checks node source admin/user permissions.
 *  We suppose that the resource manager is configured in the way that
 *  3 users exist: admin, nsadmin, user
 *  admin and nsadmin are in the same group ("nsadmins")
 *
 *  Checking all possible combination of permissions.
 */
public class TestNodeSourcesPermissions extends FunctionalTest {

    @org.junit.Test
    public void action() throws Exception {

        String nsName = "ns";
        ResourceManager adminRMAccess = RMTHelper.join("admin", "admin");

        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(
            RMTHelper.defaultDescriptor)));

        RMTHelper.log("Test1 - node source admin permission is limited to USER");
        adminRMAccess.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "USER" });

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);
        adminRMAccess.disconnect();

        // user does not have an access to change the node source
        ResourceManager userRMAccess = RMTHelper.join("radmin", "pwd");
        try {
            userRMAccess.removeNodeSource(nsName, true).booleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        userRMAccess.disconnect();

        // admin and provider are in "nsadmins" group
        ResourceManager providerRMAccess = RMTHelper.join("nsadmin", "pwd");
        try {
            providerRMAccess.removeNodeSource(nsName, true).booleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        providerRMAccess.disconnect();

        adminRMAccess = RMTHelper.join("admin", "admin");
        try {
            adminRMAccess.removeNodeSource(nsName, true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test2 - node source admin permission is limited to GROUP");
        adminRMAccess.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "GROUP" });
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);
        adminRMAccess.disconnect();

        // admin and user are in different groups
        userRMAccess = RMTHelper.join("radmin", "pwd");
        try {
            userRMAccess.removeNodeSource(nsName, true).booleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        userRMAccess.disconnect();

        // admin and provider are in "nsadmins" group
        providerRMAccess = RMTHelper.join("nsadmin", "pwd");
        try {
            // should be able to remove it
            providerRMAccess.removeNodeSource(nsName, true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        providerRMAccess.disconnect();

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test3 - node source admin permission is not limited");
        adminRMAccess = RMTHelper.join("admin", "admin");
        adminRMAccess.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "ALL" });

        adminRMAccess.disconnect();
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        // admin and user are in different groups
        userRMAccess = RMTHelper.join("radmin", "pwd");
        try {
            userRMAccess.removeNodeSource(nsName, true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        userRMAccess.disconnect();
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test4 - node source usage permission is limited to USER");
        adminRMAccess = RMTHelper.join("admin", "admin");
        adminRMAccess.createNodeSource(nsName, GCMInfrastructure.class.getName(),
                new Object[] { GCMDeploymentData }, StaticPolicy.class.getName(), new Object[] { "USER",
                        "ALL" });

        // wait for creation of GCM Node Source event, and deployment of its nodes
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        assertTrue(adminRMAccess.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber);
        assertTrue(adminRMAccess.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        // geting all nodes
        NodeSet nodes = adminRMAccess.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }
        Assert.assertEquals(RMTHelper.defaultNodesNumber, nodes.size());
        Assert.assertEquals(0, adminRMAccess.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, adminRMAccess.getState().getTotalNodesNumber());

        adminRMAccess.releaseNodes(nodes).booleanValue();

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        adminRMAccess.disconnect();

        providerRMAccess = RMTHelper.join("nsadmin", "pwd");
        nodes = providerRMAccess.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        Assert.assertEquals(0, nodes.size());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, adminRMAccess.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, adminRMAccess.getState().getTotalNodesNumber());

        providerRMAccess.disconnect();
        adminRMAccess = RMTHelper.join("admin", "admin");

        try {
            adminRMAccess.removeNodeSource(nsName, true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test5 - node source usage permission is limited to GROUP");
        adminRMAccess.createNodeSource(nsName, GCMInfrastructure.class.getName(),
                new Object[] { GCMDeploymentData }, StaticPolicy.class.getName(), new Object[] { "GROUP",
                        "ALL" });

        // wait for creation of GCM Node Source event, and deployment of its nodes
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        assertTrue(adminRMAccess.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber);
        assertTrue(adminRMAccess.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        // geting all nodes
        nodes = adminRMAccess.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Assert.assertEquals(RMTHelper.defaultNodesNumber, nodes.size());
        Assert.assertEquals(0, adminRMAccess.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, adminRMAccess.getState().getTotalNodesNumber());

        adminRMAccess.releaseNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        adminRMAccess.disconnect();

        // geting all nodes
        providerRMAccess = RMTHelper.join("nsadmin", "pwd");
        nodes = providerRMAccess.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Assert.assertEquals(RMTHelper.defaultNodesNumber, nodes.size());
        Assert.assertEquals(0, providerRMAccess.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, providerRMAccess.getState().getTotalNodesNumber());

        providerRMAccess.releaseNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        providerRMAccess.disconnect();

        userRMAccess = RMTHelper.join("radmin", "pwd");
        nodes = userRMAccess.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        Assert.assertEquals(0, nodes.size());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, userRMAccess.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, userRMAccess.getState().getTotalNodesNumber());

        userRMAccess.disconnect();
        adminRMAccess = RMTHelper.join("admin", "admin");

        try {
            adminRMAccess.removeNodeSource(nsName, true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test6 - node source usage permission is not limited");
        adminRMAccess.createNodeSource(nsName, GCMInfrastructure.class.getName(),
                new Object[] { GCMDeploymentData }, StaticPolicy.class.getName(),
                new Object[] { "ALL", "ALL" });

        // wait for creation of GCM Node Source event, and deployment of its nodes
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        assertTrue(adminRMAccess.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber);
        assertTrue(adminRMAccess.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        // geting all nodes
        nodes = adminRMAccess.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Assert.assertEquals(RMTHelper.defaultNodesNumber, nodes.size());
        Assert.assertEquals(0, adminRMAccess.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, adminRMAccess.getState().getTotalNodesNumber());

        adminRMAccess.releaseNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        adminRMAccess.disconnect();

        // geting all nodes
        providerRMAccess = RMTHelper.join("nsadmin", "pwd");
        nodes = providerRMAccess.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Assert.assertEquals(RMTHelper.defaultNodesNumber, nodes.size());
        Assert.assertEquals(0, providerRMAccess.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, providerRMAccess.getState().getTotalNodesNumber());

        providerRMAccess.releaseNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        providerRMAccess.disconnect();

        // geting all nodes
        userRMAccess = RMTHelper.join("radmin", "pwd");
        nodes = userRMAccess.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Assert.assertEquals(RMTHelper.defaultNodesNumber, nodes.size());
        Assert.assertEquals(0, userRMAccess.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, userRMAccess.getState().getTotalNodesNumber());

        userRMAccess.releaseNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        userRMAccess.disconnect();
        adminRMAccess = RMTHelper.join("admin", "admin");

        try {
            adminRMAccess.removeNodeSource(nsName, true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        adminRMAccess.disconnect();

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
    }
}
