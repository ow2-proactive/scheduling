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
package functionaltests.permissions;

import junit.framework.Assert;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.NodeSet;

import org.ow2.tests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 *  Test checks "node users" parameter of the node source, which defines
 *  who can use nodes from it. Possible values of this parameter:
 *  ME(ns creator), MY_GROUPS, PROVIDER, PROVIDER_GROUPS, ALL
 *
 *	1. If set to ME, only ns creator can use nodes
 *
 *	2. If set to MY_GROUPS, only people from creator groups can use nodes
 *
 *	3. If set to PROVIDER, only the node provider can use its own nodes
 *
 *  4. If set to PROVIDER_GROUPS, the node provider + people from its groups
 *     can use nodes of this provider.
 *
 *  5. If set to ALL no restriction on the nodes utilization.
 *  
 *  6. If set to a specific user/group only those users can add/remove (can remove only their nodes)
 *
 *  We suppose that the resource manager is configured in the way that
 *  3 users exist: admin, nsadmin, user
 *  admin and nsadmin are in the same group ("nsadmins")
 *
 *  Checking all possible combination of permissions.
 */
public class TestNSNodesPermissions extends FunctionalTest {

    @org.junit.Test
    public void action() throws Exception {
        RMTHelper helper = RMTHelper.getDefaultInstance();

        String nsName = "ns";
        RMTHelper.log("Test1 - node users = ME");

        ResourceManager nsadmin = helper.join("nsadmin", "pwd");
        nsadmin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ME", "ALL" }).getBooleanValue();

        Node node = helper.createNode("node1");
        Node node2 = helper.createNode("node2");
        nsadmin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        NodeSet nodes = nsadmin.getAtMostNodes(1, null);
        //we eat free -> busy
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        Assert.assertEquals(1, nodes.size());
        nsadmin.releaseNodes(nodes);
        //busy -> free
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.disconnect();

        ResourceManager user = helper.join("radmin", "pwd");
        nodes = user.getAtMostNodes(1, null);
        Assert.assertEquals("User cannot get foreign node", 0, nodes.size());

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2.getNodeInformation().getURL());
        user.disconnect();

        nsadmin = helper.join("nsadmin", "pwd");
        nodes = nsadmin.getAtMostNodes(2, null);
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        Assert.assertEquals("Did not get foreign node", 2, nodes.size());
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nsadmin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        nsadmin.disconnect();

        RMTHelper.log("Test2 - node users = MY_GROUPS");
        ResourceManager admin = helper.join("admin", "admin");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "MY_GROUPS", "ALL" }).getBooleanValue();

        node = helper.createNode("node1");
        node2 = helper.createNode("node2");
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.disconnect();

        user = helper.join("radmin", "pwd");
        nodes = user.getAtMostNodes(1, null);
        Assert.assertEquals("User cannot get foreign node", 0, nodes.size());

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2.getNodeInformation().getURL());
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.disconnect();

        nsadmin = helper.join("nsadmin", "pwd");
        nodes = nsadmin.getAtMostNodes(2, null);
        Assert.assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.disconnect();

        // removing the node source
        admin = helper.join("admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        admin.disconnect();

        RMTHelper.log("Test3 - node users = PROVIDER");
        user = helper.join("radmin", "pwd");
        user.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "PROVIDER", "ALL" }).getBooleanValue();

        node = helper.createNode("node1");
        node2 = helper.createNode("node2");

        user.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.disconnect();

        admin = helper.join("admin", "admin");
        admin.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // admin can get 2 nodes as it has AllPermissions
        nodes = admin.getAtMostNodes(2, null);
        Assert.assertEquals("Admin did not get foreign node", 2, nodes.size());
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.releaseNodes(nodes);
        //busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.disconnect();

        user = helper.join("radmin", "pwd");
        nodes = admin.getAtMostNodes(2, null);
        Assert.assertEquals(1, nodes.size());
        //we eat free -> busy
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.releaseNodes(nodes);
        //busy -> free
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.disconnect();

        nsadmin = helper.join("nsadmin", "pwd");
        nodes = nsadmin.getAtMostNodes(2, null);
        Assert.assertEquals("Have got a foreign node", 0, nodes.size());
        nsadmin.releaseNodes(nodes);
        nsadmin.disconnect();

        // removing the node source
        admin = helper.join("admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        admin.disconnect();

        RMTHelper.log("Test4 - node users = PROVIDER_GROUPS");
        user = helper.join("radmin", "pwd");
        user.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "PROVIDER_GROUPS", "ALL" }).getBooleanValue();

        node = helper.createNode("node1");
        node2 = helper.createNode("node2");

        user.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.disconnect();

        admin = helper.join("admin", "admin");
        admin.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // admin can get 2 nodes as it has AllPermissions
        nodes = admin.getAtMostNodes(2, null);
        Assert.assertEquals("Admin did not get foreign node", 2, nodes.size());
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.releaseNodes(nodes);
        //we eat busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.disconnect();

        user = helper.join("radmin", "pwd");
        nodes = admin.getAtMostNodes(2, null);
        Assert.assertEquals(1, nodes.size());
        Assert.assertTrue(nodes.get(0).getNodeInformation().getURL().contains("node1"));
        //we eat free -> busy
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.releaseNodes(nodes);
        //we eat busy -> free
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.disconnect();

        nsadmin = helper.join("nsadmin", "pwd");
        nodes = nsadmin.getAtMostNodes(2, null);
        Assert.assertEquals("Have not get an admin node", 1, nodes.size());
        Assert.assertTrue(nodes.get(0).getNodeInformation().getURL().contains("node2"));
        //we eat free -> busy
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //we eat busy -> free
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.disconnect();

        // removing the node source
        admin = helper.join("admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        admin.disconnect();

        RMTHelper.log("Test5 - node users = ALL");
        user = helper.join("radmin", "pwd");
        user.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "ALL" }).getBooleanValue();

        node = helper.createNode("node1");
        user.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.disconnect();

        nsadmin = helper.join("nsadmin", "pwd");
        nodes = nsadmin.getAtMostNodes(1, null);
        Assert.assertEquals("Have got a foreign node", 1, nodes.size());
        //we eat the free to busy
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //we eat the busy to free
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.disconnect();

        // removing the node source
        admin = helper.join("admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        admin.disconnect();

        RMTHelper.log("Test6.1 - specific users");
        admin = helper.join("admin", "admin");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "users=nsadmin", "ALL" }).getBooleanValue();

        node = helper.createNode("node1");
        node2 = helper.createNode("node2");
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.disconnect();

        user = helper.join("radmin", "pwd");
        nodes = user.getAtMostNodes(1, null);
        Assert.assertEquals("User cannot get foreign node", 0, nodes.size());

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2.getNodeInformation().getURL());
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nodes = user.getAtMostNodes(1, null);
        Assert.assertEquals("User cannot even your own node", 0, nodes.size());
        user.disconnect();

        nsadmin = helper.join("nsadmin", "pwd");
        nodes = nsadmin.getAtMostNodes(2, null);
        Assert.assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.disconnect();

        // removing the node source
        admin = helper.join("admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        admin.disconnect();

        RMTHelper.log("Test6.2 - specific groups");
        admin = helper.join("admin", "admin");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "groups=nsadmins", "ALL" }).getBooleanValue();

        node = helper.createNode("node1");
        node2 = helper.createNode("node2");
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.disconnect();

        user = helper.join("radmin", "pwd");
        nodes = user.getAtMostNodes(1, null);
        Assert.assertEquals("User cannot get foreign node", 0, nodes.size());

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2.getNodeInformation().getURL());
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nodes = user.getAtMostNodes(1, null);
        Assert.assertEquals("User cannot even your own node", 0, nodes.size());
        user.disconnect();

        nsadmin = helper.join("nsadmin", "pwd");
        nodes = nsadmin.getAtMostNodes(2, null);
        Assert.assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.disconnect();

        // removing the node source
        admin = helper.join("admin", "admin");
        nodes = admin.getAtMostNodes(2, null);
        Assert.assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.releaseNodes(nodes);
        //busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        admin.disconnect();

        RMTHelper.log("Test6.3 - specific users and groups");
        admin = helper.join("admin", "admin");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "users=radmin;groups=nsadmins", "ALL" })
                .getBooleanValue();

        node = helper.createNode("node1");
        node2 = helper.createNode("node2");
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.disconnect();

        user = helper.join("radmin", "pwd");
        nodes = user.getAtMostNodes(1, null);
        Assert.assertEquals("User did not get a node but had a right to get it", 1, nodes.size());
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2.getNodeInformation().getURL());
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nodes = user.getAtMostNodes(2, null);
        Assert.assertEquals("User did not get nodes but had a right to get them", 2, nodes.size());
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.disconnect();

        nsadmin = helper.join("nsadmin", "pwd");
        nodes = nsadmin.getAtMostNodes(2, null);
        Assert.assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.disconnect();

        // removing the node source
        admin = helper.join("admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        admin.disconnect();

        System.out.println("Success");
    }
}
