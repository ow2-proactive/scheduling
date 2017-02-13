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
package functionaltests.permissions;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;
import functionaltests.utils.TestNode;
import functionaltests.utils.TestUsers;


/**
 *  Checking node usage permissions defining by the following property in the NodeSourcePolicy
 *
 *  @Configurable(description = "ME|users=name1,name2;groups=group1,group2;tokens=t1,t2|ALL")
 *  private AccessType userAccessType = AccessType.ALL;
 *
 */
public class TestNSNodesPermissions extends RMFunctionalTest {

    private List<Node> createNodes(String name, int number) throws Exception {
        List<TestNode> nodePool = rmHelper.createNodes("node", 17);
        testNodes.addAll(nodePool);
        ArrayList<Node> answer = new ArrayList<>(nodePool.size());
        for (TestNode tn : nodePool) {
            answer.add(tn.getNode());
        }
        return answer;
    }

    @Test
    public void action() throws Exception {

        String nsName = "ns";
        RMTHelper.log("Test1 - node users = ME");

        ResourceManager nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nsadmin.createNodeSource(nsName,
                                 DefaultInfrastructureManager.class.getName(),
                                 null,
                                 StaticPolicy.class.getName(),
                                 new Object[] { "ME", "ALL" })
               .getBooleanValue();

        List<Node> nodePool = createNodes("node", 2);

        Node node = nodePool.remove(0);
        Node node2 = nodePool.remove(0);
        nsadmin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        NodeSet nodes = nsadmin.getNodes(new Criteria(1));
        //we eat free -> busy
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        assertEquals(1, nodes.size());
        nsadmin.releaseNodes(nodes);
        //busy -> free
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        ResourceManager user = rmHelper.getResourceManager(TestUsers.RADMIN);
        nodes = user.getNodes(new Criteria(1));
        assertEquals("User cannot get foreign node", 0, nodes.size());

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2.getNodeInformation().getURL());

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nodes = nsadmin.getNodes(new Criteria(2));
        //we eat free -> busy * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        assertEquals("Did not get foreign node", 2, nodes.size());
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nsadmin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test2 - node users = MY_GROUPS");
        ResourceManager admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "MY_GROUPS", "ALL" })
             .getBooleanValue();

        nodePool = createNodes("node", 2);

        node = nodePool.remove(0);
        node2 = nodePool.remove(0);
        System.out.println(System.currentTimeMillis());
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        nodes = user.getNodes(new Criteria(1));
        assertEquals("User cannot get foreign node", 0, nodes.size());

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2.getNodeInformation().getURL());
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nodes = nsadmin.getNodes(new Criteria(2));
        assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test3 - node users = PROVIDER");
        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        user.createNodeSource(nsName,
                              DefaultInfrastructureManager.class.getName(),
                              null,
                              StaticPolicy.class.getName(),
                              new Object[] { "PROVIDER", "ALL" })
            .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        node2 = nodePool.remove(0);

        user.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // admin can get 2 nodes as it has AllPermissions
        nodes = admin.getNodes(new Criteria(2));
        assertEquals("Admin did not get foreign node", 2, nodes.size());
        //we eat free -> busy * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.releaseNodes(nodes);
        //busy -> free * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        nodes = user.getNodes(new Criteria(2));
        assertEquals(1, nodes.size());
        //we eat free -> busy
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.releaseNodes(nodes);
        //busy -> free
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nodes = nsadmin.getNodes(new Criteria(2));
        assertEquals("Have got a foreign node", 0, nodes.size());
        nsadmin.releaseNodes(nodes);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test4 - node users = PROVIDER_GROUPS");
        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        user.createNodeSource(nsName,
                              DefaultInfrastructureManager.class.getName(),
                              null,
                              StaticPolicy.class.getName(),
                              new Object[] { "PROVIDER_GROUPS", "ALL" })
            .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        node2 = nodePool.remove(0);

        user.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // admin can get 2 nodes as it has AllPermissions
        nodes = admin.getNodes(new Criteria(2));
        assertEquals("Admin did not get foreign node", 2, nodes.size());
        //we eat free -> busy * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.releaseNodes(nodes);
        //we eat busy -> free * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        nodes = user.getNodes(new Criteria(2));
        assertEquals(1, nodes.size());

        //we eat free -> busy
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.releaseNodes(nodes);
        //we eat busy -> free
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nodes = nsadmin.getNodes(new Criteria(2));
        assertEquals("Have not get an admin node", 1, nodes.size());
        //we eat free -> busy
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //we eat busy -> free
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test5 - node users = ALL");
        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        user.createNodeSource(nsName,
                              DefaultInfrastructureManager.class.getName(),
                              null,
                              StaticPolicy.class.getName(),
                              new Object[] { "ALL", "ALL" })
            .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        user.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        Criteria criteria = new Criteria(1);
        nodes = nsadmin.getNodes(criteria);
        assertEquals("Have got a foreign node", 1, nodes.size());
        //we eat the free to busy
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //we eat the busy to free
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        criteria.setNodeAccessToken("non_existing_token");
        nodes = nsadmin.getNodes(criteria);
        assertEquals("Got a regular node while requesting a node with token", 0, nodes.size());

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test6.1 - specific users");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "users=nsadmin", "ALL" })
             .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        node2 = nodePool.remove(0);
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        nodes = user.getNodes(new Criteria(1));
        assertEquals("User cannot get foreign node", 0, nodes.size());

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2.getNodeInformation().getURL());
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nodes = user.getNodes(new Criteria(1));
        assertEquals("User cannot even your own node", 0, nodes.size());

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nodes = nsadmin.getNodes(new Criteria(2));
        assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test6.2 - specific groups");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "groups=nsadmins", "ALL" })
             .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        node2 = nodePool.remove(0);
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        nodes = user.getNodes(new Criteria(1));
        assertEquals("User cannot get foreign node", 0, nodes.size());

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2.getNodeInformation().getURL());
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nodes = user.getNodes(new Criteria(1));
        assertEquals("User cannot even your own node", 0, nodes.size());

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nodes = nsadmin.getNodes(new Criteria(2));
        assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        nodes = admin.getNodes(new Criteria(2));
        assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.releaseNodes(nodes);
        //busy -> free * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        admin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test6.3 - specific tokens");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "tokens=token1,token2", "ALL" })
             .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        criteria = new Criteria(1);
        criteria.setNodeAccessToken("token1");
        nodes = admin.getNodes(criteria);
        assertEquals(1, nodes.size());
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.releaseNodes(nodes);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        criteria.setNodeAccessToken("token2");
        nodes = admin.getNodes(criteria);
        assertEquals(1, nodes.size());
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        admin.releaseNodes(nodes);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // admin also does not have the right to get the node
        criteria.setNodeAccessToken("token3");
        nodes = admin.getNodes(criteria);
        assertEquals(0, nodes.size());

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        criteria = new Criteria(1);
        nodes = user.getNodes(criteria);
        assertEquals(0, nodes.size());

        criteria.setNodeAccessToken("token1");
        nodes = user.getNodes(criteria);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.releaseNodes(nodes);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test6.4 - specific users and groups");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "users=radmin;groups=nsadmins", "ALL" })
             .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        node2 = nodePool.remove(0);
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        nodes = user.getNodes(new Criteria(1));
        assertEquals("User did not get a node but had a right to get it", 1, nodes.size());
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.releaseNodes(nodes);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2.getNodeInformation().getURL());
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nodes = user.getNodes(new Criteria(2));
        assertEquals("User did not get nodes but had a right to get them", 2, nodes.size());
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.releaseNodes(nodes);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nodes = nsadmin.getNodes(new Criteria(2));
        assertEquals(2, nodes.size());
        //we eat free -> busy * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        //busy -> free * 2
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test6.5 - specific users and token");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "users=radmin;tokens=token1", "ALL" })
             .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //we eat the configuring to free nodeevent
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        criteria = new Criteria(1);
        criteria.setNodeAccessToken("token1");
        nodes = user.getNodes(criteria);
        assertEquals("User did not get a node but had a right to get it", 1, nodes.size());
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        user.releaseNodes(nodes);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        criteria.setNodeAccessToken("token2"); // will not get a node as don't have the token "token2"
        nodes = user.getNodes(criteria);
        assertEquals(0, nodes.size());

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        criteria.setNodeAccessToken("token1");
        nodes = nsadmin.getNodes(criteria);
        assertEquals(1, nodes.size());
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        nsadmin.releaseNodes(nodes);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
    }
}
