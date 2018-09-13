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

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;
import functionaltests.utils.TestNode;
import functionaltests.utils.TestUsers;


/**
 *  Test checks "node providers" parameter of the node source.
 *  Possible values of this parameter: ME(ns creator), MY_GROUPS, ALL
 *
 *  1. If set to ME only ns creator can add/remove nodes
 *
 *  2. If set to MY_GROUPS only people from these groups can add/remove nodes
 *  (can remove only their nodes).
 *
 *  3. If set to ALL anyone can add/remove (can remove only their nodes)
 *
 *  4. Users with AllPermissions do not have any restriction described above.
 *
 *  5. If set to a specific user/group only those users can add/remove (can remove only their nodes)
 *
 *  We suppose that the resource manager is configured in the way that 3
 *  users exist: admin, nsadmin, user
 *  admin and nsadmin are in the same group ("nsadmins")
 */
public class TestNSProviderPermissions extends RMFunctionalTest {

    ResourceManager nsadmin;

    ResourceManager admin;

    ResourceManager user;

    static String nsName = "TestNSNodesPermissions";

    @Test
    public void testNodeProviderMe() throws Exception {

        RMTHelper.log("Test1 - node providers = ME");

        ResourceManager nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nsadmin.createNodeSource(nsName,
                                 DefaultInfrastructureManager.class.getName(),
                                 null,
                                 StaticPolicy.class.getName(),
                                 new Object[] { "ALL", "ME" },
                                 NODES_NOT_RECOVERABLE);

        List<TestNode> nodePool = rmHelper.createNodes("node", 2);

        testNode = nodePool.get(0);

        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        Node node1 = nodePool.remove(0).getNode();
        Node node2 = nodePool.remove(0).getNode();
        nsadmin.addNode(node1.getNodeInformation().getURL(), nsName).getBooleanValue();

        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        ResourceManager user = rmHelper.getResourceManager(TestUsers.RADMIN);
        try {
            // user does not allow to add nodes
            user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
            fail();
        } catch (Exception expected) {
        }

        try {
            // user does not allow to remove nodes
            user.removeNode(node1.getNodeInformation().getURL(), true).getBooleanValue();
            fail();
        } catch (Exception expected) {
        }

        // AllPermission user
        ResourceManager admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        // admin is allowed to add nodes
        admin.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        // admin is allowed to remove nodes
        admin.removeNode(node1.getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        // admin is allowed to remove the node source
        admin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
    }

    @Test
    public void testNodeProviderMyGroups() throws Exception {
        RMTHelper.log("Test2 - node providers = MY_GROUPS");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "ALL", "MY_GROUPS" },
                               NODES_NOT_RECOVERABLE);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        TestNode node1 = rmHelper.createNode("node1");
        TestNode node2 = rmHelper.createNode("node2");
        TestNode node3 = rmHelper.createNode("node3");

        testNodes.addAll(Arrays.asList(node1, node2, node3));

        admin.addNode(node1.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        // nsadmin is in the same group as admin
        nsadmin.addNode(node2.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        try {
            // user does not allow to add nodes
            user.addNode(node3.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
            fail();
        } catch (Exception expected) {
        }

        try {
            // user does not allow to remove nodes
            user.removeNode(node2.getNode().getNodeInformation().getURL(), true).getBooleanValue();
            fail();
        } catch (Exception expected) {
        }

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);

        // nsadmin can remove node in the node source as a node source provider
        nsadmin.removeNode(node1.getNode().getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        // but it can remove its own
        nsadmin.removeNode(node2.getNode().getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        nsadmin.addNode(node3.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        // admin can remove foreign node
        admin.removeNode(node3.getNode().getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        admin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
    }

    @Test
    public void testNodeProviderAll() throws Exception {
        RMTHelper.log("Test3 - node providers = ALL");
        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nsadmin.createNodeSource(nsName,
                                 DefaultInfrastructureManager.class.getName(),
                                 null,
                                 StaticPolicy.class.getName(),
                                 new Object[] { "ALL", "ALL" },
                                 NODES_NOT_RECOVERABLE);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        TestNode node1 = rmHelper.createNode("node1");
        TestNode node2 = rmHelper.createNode("node2");
        TestNode node3 = rmHelper.createNode("node3");

        testNodes.addAll(Arrays.asList(node1, node2, node3));

        nsadmin.addNode(node1.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        // user can add new nodes
        user.addNode(node2.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        // the user should have the right to remove the Node, as the provider of the node source is "ALL"
        // so the user should be considered as a node source provider
        user.removeNode(node1.getNode().getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        // user can remove his own node
        user.removeNode(node2.getNode().getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        // adding node3
        user.addNode(node3.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        // nsadmin can remove node3 as ns admin
        nsadmin.removeNode(node3.getNode().getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        nsadmin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
    }

    @Test
    public void testAdminPriviledge() throws Exception {
        RMTHelper.log("Test4 - admin priviledges");

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nsadmin.createNodeSource(nsName,
                                 DefaultInfrastructureManager.class.getName(),
                                 null,
                                 StaticPolicy.class.getName(),
                                 new Object[] { "ALL", "ALL" },
                                 NODES_NOT_RECOVERABLE);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        List<TestNode> nodePool = rmHelper.createNodes("node", 2);
        testNode = nodePool.get(0);
        Node node1 = nodePool.remove(0).getNode();

        nsadmin.addNode(node1.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        admin = rmHelper.getResourceManager(TestUsers.ADMIN);

        // admin can remove anything
        admin.removeNode(node1.getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        admin.removeNodeSource(nsName, true);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
    }

    @Test
    public void testSpecificUsers() throws Exception {
        RMTHelper.log("Test5.1 - specific users");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "ALL", "users=nsadmin" },
                               NODES_NOT_RECOVERABLE);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        TestNode node1 = rmHelper.createNode("node1");
        TestNode node2 = rmHelper.createNode("node2");
        TestNode node3 = rmHelper.createNode("node3");

        testNodes.addAll(Arrays.asList(node1, node2, node3));

        admin.addNode(node1.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);

        // nsadmin can remove node as he is a node source provider
        nsadmin.removeNode(node1.getNode().getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        nsadmin.addNode(node2.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        try {
            // user cannot add new nodes
            user.addNode(node3.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
            fail();
        } catch (Exception expected) {
        }
        try {
            // user cannot remove node as he is not a node owner
            user.removeNode(node2.getNode().getNodeInformation().getURL(), true).getBooleanValue();
            fail();
        } catch (Exception expected) {
        }

        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        try {
            // user does not allow to remove nodes
            admin.removeNodeSource(nsName, true).getBooleanValue();
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testSpecificGroups() throws Exception {

        RMTHelper.log("Test5.2 - specific groups");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "ALL", "groups=nsadmins" },
                               NODES_NOT_RECOVERABLE)
             .getBooleanValue();
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        TestNode node1 = rmHelper.createNode("node1");
        TestNode node2 = rmHelper.createNode("node2");
        TestNode node3 = rmHelper.createNode("node3");

        testNodes.addAll(Arrays.asList(node1, node2, node3));

        admin.addNode(node1.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);

        // nsadmin can remove the node as he is a node source provider
        nsadmin.removeNode(node1.getNode().getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        nsadmin.addNode(node2.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        try {
            // user cannot add new nodes
            user.addNode(node3.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
            fail();
        } catch (Exception expected) {
        }
        try {
            // user cannot remove node as he is not a node owner
            user.removeNode(node2.getNode().getNodeInformation().getURL(), true).getBooleanValue();
            fail();
        } catch (Exception expected) {
        }

        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        // user does not allow to remove nodes
        admin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
    }

    @Test
    public void testSpecificUsersGroups() throws Exception {
        RMTHelper.log("Test5.3 - specific users/groups");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "ALL", "users=radmin;groups=nsadmins" },
                               NODES_NOT_RECOVERABLE);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        TestNode node1 = rmHelper.createNode("node1");
        TestNode node2 = rmHelper.createNode("node2");
        TestNode node3 = rmHelper.createNode("node3");

        testNodes.addAll(Arrays.asList(node1, node2, node3));

        admin.addNode(node1.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);

        // nsadmin can remove the node as he is a node source provider
        nsadmin.removeNode(node1.getNode().getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        nsadmin.addNode(node2.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        // user can add new nodes
        user.addNode(node3.getNode().getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        //user can remove nodes as a node source provider
        user.removeNode(node2.getNode().getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        // user does not allow to remove nodes
        admin.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
    }
}
