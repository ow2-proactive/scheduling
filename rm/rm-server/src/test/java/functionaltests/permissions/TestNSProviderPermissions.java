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

    ResourceManager nsadmin2;

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
        addNodeToNodeSource(nsadmin, node1);

        user = rmHelper.getResourceManager(TestUsers.PROVIDER);
        failIfNodeCanBeAdded(user, node2, "provider is not node source owner");

        failIfNodeCanBeRemoved(user, node1, "provider is not node source owner");

        // AllPermission user
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        // admin is allowed to add nodes
        addNodeToNodeSource(admin, node2);

        // admin is allowed to remove nodes
        removeNodeAndWait(node1, admin);

        // admin is allowed to remove the node source
        removeNodeSourceWithNodes(admin, 1);
    }

    private void removeNodeAndWait(Node node, ResourceManager user) {
        user.removeNode(node.getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
    }

    @Test
    public void testNodeProviderMyGroups() throws Exception {
        RMTHelper.log("Test2 - node providers = MY_GROUPS");
        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nsadmin.createNodeSource(nsName,
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

        addNodeToNodeSource(nsadmin, node1.getNode());

        nsadmin2 = rmHelper.getResourceManager(TestUsers.NSADMIN2);
        // nsadmin2 is in the same group as nsadmin
        addNodeToNodeSource(nsadmin2, node2.getNode());

        user = rmHelper.getResourceManager(TestUsers.PROVIDER);
        failIfNodeCanBeAdded(user, node3.getNode(), "provider is not in node source owner group");

        failIfNodeCanBeRemoved(user, node2.getNode(), "provider is not in node source owner group");

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);

        // nsadmin can remove node in the node source as a node source provider
        removeNodeAndWait(node1.getNode(), nsadmin);

        // but it can remove its own
        removeNodeAndWait(node2.getNode(), nsadmin);
        addNodeToNodeSource(nsadmin, node3.getNode());

        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        // admin can remove foreign node
        removeNodeAndWait(node3.getNode(), admin);

        removeNodeSourceWithNodes(admin, 0);
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

        addNodeToNodeSource(nsadmin, node1.getNode());

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        // user can add new nodes
        addNodeToNodeSource(user, node2.getNode());
        // the user should have the right to remove the Node, as the provider of the node source is "ALL"
        // so the user should be considered as a node source provider
        removeNodeAndWait(node1.getNode(), user);

        // user can remove his own node
        removeNodeAndWait(node2.getNode(), user);
        // adding node3
        addNodeToNodeSource(user, node3.getNode());

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        // nsadmin can remove node3 as ns admin
        removeNodeAndWait(node3.getNode(), nsadmin);

        removeNodeSourceWithNodes(nsadmin, 0);
    }

    @Test
    public void testAdminPriviledge() throws Exception {
        RMTHelper.log("Test4 - admin priviledges");

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nsadmin.createNodeSource(nsName,
                                 DefaultInfrastructureManager.class.getName(),
                                 null,
                                 StaticPolicy.class.getName(),
                                 new Object[] { "ALL", "ME" },
                                 NODES_NOT_RECOVERABLE);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        List<TestNode> nodePool = rmHelper.createNodes("node", 2);
        testNode = nodePool.get(0);
        Node node1 = nodePool.remove(0).getNode();

        addNodeToNodeSource(nsadmin, node1);

        admin = rmHelper.getResourceManager(TestUsers.ADMIN);

        // admin can remove anything
        removeNodeAndWait(node1, admin);

        removeNodeSourceWithNodes(admin, 0);
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

        addNodeToNodeSource(admin, node1.getNode());
        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);

        // nsadmin can remove node as he is a node source provider
        removeNodeAndWait(node1.getNode(), nsadmin);

        addNodeToNodeSource(nsadmin, node2.getNode());
        user = rmHelper.getResourceManager(TestUsers.PROVIDER);
        failIfNodeCanBeAdded(user, node3.getNode(), "provider is not a specified user");
        failIfNodeCanBeRemoved(user, node2.getNode(), "provider is not a specified user");

        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        removeNodeSourceWithNodes(admin, 1);
    }

    @Test
    public void testSpecificGroups() throws Exception {

        RMTHelper.log("Test5.2 - specific groups");
        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nsadmin.createNodeSource(nsName,
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

        addNodeToNodeSource(nsadmin, node1.getNode());
        nsadmin2 = rmHelper.getResourceManager(TestUsers.NSADMIN2);

        // nsadmin2 can remove the node as he is a node source provider
        removeNodeAndWait(node1.getNode(), nsadmin2);
        addNodeToNodeSource(nsadmin2, node2.getNode());

        user = rmHelper.getResourceManager(TestUsers.PROVIDER);
        failIfNodeCanBeAdded(user, node3.getNode(), "provider is not in the specified group");
        failIfNodeCanBeRemoved(user, node2.getNode(), "provider is not in the specified group");

        // admin can remove the NS
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        // user does not allow to remove nodes
        removeNodeSourceWithNodes(admin, 1);
    }

    @Test
    public void testSpecificUsersGroups() throws Exception {
        RMTHelper.log("Test5.3 - specific users/groups");
        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nsadmin.createNodeSource(nsName,
                                 DefaultInfrastructureManager.class.getName(),
                                 null,
                                 StaticPolicy.class.getName(),
                                 new Object[] { "ALL", "users=provider;groups=nsadmins" },
                                 NODES_NOT_RECOVERABLE);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        TestNode node1 = rmHelper.createNode("node1");
        TestNode node2 = rmHelper.createNode("node2");
        TestNode node3 = rmHelper.createNode("node3");

        testNodes.addAll(Arrays.asList(node1, node2, node3));

        addNodeToNodeSource(nsadmin, node1.getNode());
        nsadmin2 = rmHelper.getResourceManager(TestUsers.NSADMIN2);

        // nsadmin2 can remove the node as he is a node source provider
        removeNodeAndWait(node1.getNode(), nsadmin2);
        addNodeToNodeSource(nsadmin2, node2.getNode());

        user = rmHelper.getResourceManager(TestUsers.PROVIDER);
        // user can add new nodes
        addNodeToNodeSource(user, node3.getNode());

        //user can remove nodes as a node source provider
        removeNodeAndWait(node2.getNode(), user);

        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        removeNodeSourceWithNodes(admin, 1);
    }

    private void removeNodeSourceWithNodes(ResourceManager user, int nbNodes) {
        try {
            user.removeNodeSource(nsName, true).getBooleanValue();
            for (int i = 0; i < nbNodes; i++) {
                rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            }
            rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void addNodeToNodeSource(ResourceManager user, Node node) {
        user.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();

        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
    }

    private static void failIfNodeCanBeAdded(ResourceManager user, Node node, String message) {
        try {
            // user cannot add new nodes
            user.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
            fail(message);
        } catch (Exception expected) {
        }
    }

    private static void failIfNodeCanBeRemoved(ResourceManager user, Node node, String message) {
        try {
            user.removeNode(node.getNodeInformation().getURL(), true).getBooleanValue();
            fail(message);
        } catch (Exception expected) {
        }
    }
}
