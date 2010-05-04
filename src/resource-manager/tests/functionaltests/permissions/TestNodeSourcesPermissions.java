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
 *  We suppose that 3 users exists admin, nsadmin, user
 *  admin and nsadmin are in the same group ("nsadmins")
 *
 *  Checking all possible combination of permissions
 */
public class TestNodeSourcesPermissions extends FunctionalTest {

    @org.junit.Test
    public void action() throws Exception {

        String nsName = "ns";
        ResourceManager resourceManager = RMTHelper.join("admin", "admin");

        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(
            RMTHelper.defaultDescriptor)));

        RMTHelper.log("Test1 - node source admin permission is limited to USER");
        resourceManager.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "USER" });

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);
        resourceManager.disconnect();

        // admin and user are in different groups
        ResourceManager user = RMTHelper.join("radmin", "pwd");
        try {
            user.removeNodeSource(nsName, true).booleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
        }
        user.disconnect();

        // admin and provider are in "nsadmins" group
        ResourceManager provider = RMTHelper.join("nsadmin", "pwd");
        try {
            provider.removeNodeSource(nsName, true).booleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
        }
        provider.disconnect();

        resourceManager = RMTHelper.join("admin", "admin");
        try {
            resourceManager.removeNodeSource(nsName, true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test2 - node source admin permission is limited to GROUP");
        resourceManager.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "GROUP" });
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);
        resourceManager.disconnect();

        // admin and user are in different groups
        user = RMTHelper.join("radmin", "pwd");
        try {
            user.removeNodeSource(nsName, true).booleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
        }
        user.disconnect();

        // admin and provider are in "nsadmins" group
        provider = RMTHelper.join("nsadmin", "pwd");
        try {
            // should be able to remove it
            provider.removeNodeSource(nsName, true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        provider.disconnect();

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test3 - node source admin permission is not limited");
        resourceManager = RMTHelper.join("admin", "admin");
        resourceManager.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "ALL" });

        resourceManager.disconnect();
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        // admin and user are in different groups
        user = RMTHelper.join("radmin", "pwd");
        try {
            user.removeNodeSource(nsName, true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        user.disconnect();
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test4 - node source usage permission is limited to USER");
        resourceManager = RMTHelper.join("admin", "admin");
        resourceManager.createNodeSource(nsName, GCMInfrastructure.class.getName(),
                new Object[] { GCMDeploymentData }, StaticPolicy.class.getName(), new Object[] { "USER",
                        "ALL" });

        // wait for creation of GCM Node Source event, and deployment of its nodes
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        // geting all nodes
        NodeSet nodes = resourceManager.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }
        Assert.assertEquals(RMTHelper.defaultNodesNumber, nodes.size());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, resourceManager.getState().getTotalNodesNumber());

        resourceManager.releaseNodes(nodes).booleanValue();

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        resourceManager.disconnect();

        provider = RMTHelper.join("nsadmin", "pwd");
        nodes = provider.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        Assert.assertEquals(0, nodes.size());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, resourceManager.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, resourceManager.getState().getTotalNodesNumber());

        try {
            // should be able to remove it
            provider.removeNodeSource(nsName, true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        provider.disconnect();

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test5 - node source usage permission is limited to GROUP");
        resourceManager = RMTHelper.join("admin", "admin");
        resourceManager.createNodeSource(nsName, GCMInfrastructure.class.getName(),
                new Object[] { GCMDeploymentData }, StaticPolicy.class.getName(), new Object[] { "GROUP",
                        "ALL" });

        // wait for creation of GCM Node Source event, and deployment of its nodes
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        // geting all nodes
        nodes = resourceManager.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Assert.assertEquals(RMTHelper.defaultNodesNumber, nodes.size());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, resourceManager.getState().getTotalNodesNumber());

        resourceManager.releaseNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        resourceManager.disconnect();

        // geting all nodes
        provider = RMTHelper.join("nsadmin", "pwd");
        nodes = provider.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Assert.assertEquals(RMTHelper.defaultNodesNumber, nodes.size());
        Assert.assertEquals(0, provider.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, provider.getState().getTotalNodesNumber());

        provider.releaseNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        provider.disconnect();

        user = RMTHelper.join("radmin", "pwd");
        nodes = user.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        Assert.assertEquals(0, nodes.size());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, user.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, user.getState().getTotalNodesNumber());

        try {
            // should be able to remove it
            user.removeNodeSource(nsName, true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        user.disconnect();

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test6 - node source usage permission is not limited");
        resourceManager = RMTHelper.join("admin", "admin");
        resourceManager.createNodeSource(nsName, GCMInfrastructure.class.getName(),
                new Object[] { GCMDeploymentData }, StaticPolicy.class.getName(),
                new Object[] { "ALL", "ALL" });

        // wait for creation of GCM Node Source event, and deployment of its nodes
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        assertTrue(resourceManager.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        // geting all nodes
        nodes = resourceManager.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Assert.assertEquals(RMTHelper.defaultNodesNumber, nodes.size());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, resourceManager.getState().getTotalNodesNumber());

        resourceManager.releaseNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        resourceManager.disconnect();

        // geting all nodes
        provider = RMTHelper.join("nsadmin", "pwd");
        nodes = provider.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Assert.assertEquals(RMTHelper.defaultNodesNumber, nodes.size());
        Assert.assertEquals(0, provider.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, provider.getState().getTotalNodesNumber());

        provider.releaseNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        provider.disconnect();

        // geting all nodes
        user = RMTHelper.join("radmin", "pwd");
        nodes = user.getAtMostNodes(RMTHelper.defaultNodesNumber, null);
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        Assert.assertEquals(RMTHelper.defaultNodesNumber, nodes.size());
        Assert.assertEquals(0, user.getState().getFreeNodesNumber());
        Assert.assertEquals(RMTHelper.defaultNodesNumber, user.getState().getTotalNodesNumber());

        user.releaseNodes(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        try {
            // should be able to remove it
            user.removeNodeSource(nsName, true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        user.disconnect();

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
    }
}
