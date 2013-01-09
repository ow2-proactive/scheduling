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
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;


/**
 *  Checking node usage permissions defining by the following property in the NodeSourcePolicy
 *
 *  @Configurable(description = "ME|users=name1,name2;groups=group1,group2;tokens=t1,t2|ALL")
 *  private AccessType userAccessType = AccessType.ALL;
 *
 */
public class TestNSNodesPermissions extends RMConsecutive {

    @org.junit.Test
    public void action() throws Exception {
        RMTHelper helper = RMTHelper.getDefaultInstance();
        helper.getResourceManager();

        String nsName = "ns";
        RMTHelper.log("Test1 - node users = ME");

        ResourceManager nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        nsadmin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ME", "ALL" }).getBooleanValue();

        Node node = helper.createNode("node1").getNode();
        Node node2 = helper.createNode("node2").getNode();
        nsadmin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        NodeSet nodes = nsadmin.getNodes(new Criteria(1));
        //we eat free -> busy
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        Assert.assertEquals(1, nodes.size());
        nsadmin.releaseNodes(nodes);
        //busy -> free
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        ResourceManager user = helper.getResourceManager(null, "radmin", "pwd");
        nodes = user.getNodes(new Criteria(1));
        Assert.assertEquals("User cannot get foreign node", 0, nodes.size());

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2.getNodeInformation().getURL());

        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        nodes = nsadmin.getNodes(new Criteria(2));
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

        RMTHelper.log("Test2 - node users = MY_GROUPS");
        ResourceManager admin = helper.getResourceManager(null, "admin", "admin");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "MY_GROUPS", "ALL" }).getBooleanValue();

        node = helper.createNode("node1").getNode();
        node2 = helper.createNode("node2").getNode();
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = helper.getResourceManager(null, "radmin", "pwd");
        nodes = user.getNodes(new Criteria(1));
        Assert.assertEquals("User cannot get foreign node", 0, nodes.size());

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2.getNodeInformation().getURL());
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        nodes = nsadmin.getNodes(new Criteria(2));
        Assert.assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = helper.getResourceManager(null, "admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test3 - node users = PROVIDER");
        user = helper.getResourceManager(null, "radmin", "pwd");
        user.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "PROVIDER", "ALL" }).getBooleanValue();

        node = helper.createNode("node1").getNode();
        node2 = helper.createNode("node2").getNode();

        user.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        admin = helper.getResourceManager(null, "admin", "admin");
        admin.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // admin can get 2 nodes as it has AllPermissions
        nodes = admin.getNodes(new Criteria(2));
        Assert.assertEquals("Admin did not get foreign node", 2, nodes.size());
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.releaseNodes(nodes);
        //busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = helper.getResourceManager(null, "radmin", "pwd");
        nodes = admin.getNodes(new Criteria(2));
        Assert.assertEquals(1, nodes.size());
        //we eat free -> busy
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.releaseNodes(nodes);
        //busy -> free
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        nodes = nsadmin.getNodes(new Criteria(2));
        Assert.assertEquals("Have got a foreign node", 0, nodes.size());
        nsadmin.releaseNodes(nodes);

        // removing the node source
        admin = helper.getResourceManager(null, "admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test4 - node users = PROVIDER_GROUPS");
        user = helper.getResourceManager(null, "radmin", "pwd");
        user.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "PROVIDER_GROUPS", "ALL" }).getBooleanValue();

        node = helper.createNode("node1").getNode();
        node2 = helper.createNode("node2").getNode();

        user.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        admin = helper.getResourceManager(null, "admin", "admin");
        admin.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // admin can get 2 nodes as it has AllPermissions
        nodes = admin.getNodes(new Criteria(2));
        Assert.assertEquals("Admin did not get foreign node", 2, nodes.size());
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.releaseNodes(nodes);
        //we eat busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = helper.getResourceManager(null, "radmin", "pwd");
        nodes = admin.getNodes(new Criteria(2));
        Assert.assertEquals(1, nodes.size());
        Assert.assertTrue(nodes.get(0).getNodeInformation().getURL().contains("node1"));
        //we eat free -> busy
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.releaseNodes(nodes);
        //we eat busy -> free
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        nodes = nsadmin.getNodes(new Criteria(2));
        Assert.assertEquals("Have not get an admin node", 1, nodes.size());
        Assert.assertTrue(nodes.get(0).getNodeInformation().getURL().contains("node2"));
        //we eat free -> busy
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //we eat busy -> free
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = helper.getResourceManager(null, "admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test5 - node users = ALL");
        user = helper.getResourceManager(null, "radmin", "pwd");
        user.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "ALL" }).getBooleanValue();

        node = helper.createNode("node1").getNode();
        user.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        Criteria criteria = new Criteria(1);
        nodes = nsadmin.getNodes(criteria);
        Assert.assertEquals("Have got a foreign node", 1, nodes.size());
        //we eat the free to busy
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //we eat the busy to free
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        criteria.setNodeAccessToken("non_existing_token");
        nodes = nsadmin.getNodes(criteria);
        Assert.assertEquals("Got a regular node while requesting a node with token", 0, nodes.size());

        // removing the node source
        admin = helper.getResourceManager(null, "admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test6.1 - specific users");
        admin = helper.getResourceManager(null, "admin", "admin");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "users=nsadmin", "ALL" }).getBooleanValue();

        node = helper.createNode("node1").getNode();
        node2 = helper.createNode("node2").getNode();
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = helper.getResourceManager(null, "radmin", "pwd");
        nodes = user.getNodes(new Criteria(1));
        Assert.assertEquals("User cannot get foreign node", 0, nodes.size());

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2.getNodeInformation().getURL());
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nodes = user.getNodes(new Criteria(1));
        Assert.assertEquals("User cannot even your own node", 0, nodes.size());

        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        nodes = nsadmin.getNodes(new Criteria(2));
        Assert.assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = helper.getResourceManager(null, "admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test6.2 - specific groups");
        admin = helper.getResourceManager(null, "admin", "admin");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "groups=nsadmins", "ALL" }).getBooleanValue();

        node = helper.createNode("node1").getNode();
        node2 = helper.createNode("node2").getNode();
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = helper.getResourceManager(null, "radmin", "pwd");
        nodes = user.getNodes(new Criteria(1));
        Assert.assertEquals("User cannot get foreign node", 0, nodes.size());

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2.getNodeInformation().getURL());
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nodes = user.getNodes(new Criteria(1));
        Assert.assertEquals("User cannot even your own node", 0, nodes.size());

        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        nodes = nsadmin.getNodes(new Criteria(2));
        Assert.assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = helper.getResourceManager(null, "admin", "admin");
        nodes = admin.getNodes(new Criteria(2));
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

        RMTHelper.log("Test6.3 - specific tokens");
        admin = helper.getResourceManager(null, "admin", "admin");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "tokens=token1,token2", "ALL" })
                .getBooleanValue();

        node = helper.createNode("node1").getNode();
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        criteria = new Criteria(1);
        criteria.setNodeAccessToken("token1");
        nodes = admin.getNodes(criteria);
        Assert.assertEquals(1, nodes.size());
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.releaseNodes(nodes);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        criteria.setNodeAccessToken("token2");
        nodes = admin.getNodes(criteria);
        Assert.assertEquals(1, nodes.size());
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.releaseNodes(nodes);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // admin has the right to get the node anyway
        criteria.setNodeAccessToken("token3");
        nodes = admin.getNodes(criteria);
        Assert.assertEquals(1, nodes.size());
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.releaseNodes(nodes);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = helper.getResourceManager(null, "radmin", "pwd");
        criteria = new Criteria(1);
        nodes = user.getNodes(criteria);
        Assert.assertEquals(0, nodes.size());

        criteria.setNodeAccessToken("token1");
        nodes = user.getNodes(criteria);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.releaseNodes(nodes);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = helper.getResourceManager(null, "admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test6.4 - specific users and groups");
        admin = helper.getResourceManager(null, "admin", "admin");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "users=radmin;groups=nsadmins", "ALL" })
                .getBooleanValue();

        node = helper.createNode("node1").getNode();
        node2 = helper.createNode("node2").getNode();
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = helper.getResourceManager(null, "radmin", "pwd");
        nodes = user.getNodes(new Criteria(1));
        Assert.assertEquals("User did not get a node but had a right to get it", 1, nodes.size());
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForNodeEvent(RMEventType.NODE_ADDED, node2.getNodeInformation().getURL());
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nodes = user.getNodes(new Criteria(2));
        Assert.assertEquals("User did not get nodes but had a right to get them", 2, nodes.size());
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        nodes = nsadmin.getNodes(new Criteria(2));
        Assert.assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = helper.getResourceManager(null, "admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test6.5 - specific users and token");
        admin = helper.getResourceManager(null, "admin", "admin");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "users=radmin;tokens=token1", "ALL" })
                .getBooleanValue();

        node = helper.createNode("node1").getNode();
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = helper.getResourceManager(null, "radmin", "pwd");
        criteria = new Criteria(1);
        criteria.setNodeAccessToken("token1");
        nodes = user.getNodes(criteria);
        Assert.assertEquals("User did not get a node but had a right to get it", 1, nodes.size());
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        criteria.setNodeAccessToken("token2"); // still will get a node as radmin user
        nodes = nsadmin.getNodes(criteria);
        Assert.assertEquals(1, nodes.size());
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        criteria.setNodeAccessToken("token1");
        nodes = nsadmin.getNodes(criteria);
        Assert.assertEquals(1, nodes.size());
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = helper.getResourceManager(null, "admin", "admin");
        admin.removeNodeSource(nsName, true).getBooleanValue();
        helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        System.out.println("Success");
    }
}
