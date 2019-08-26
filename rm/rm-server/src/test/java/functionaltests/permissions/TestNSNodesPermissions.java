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
import static org.junit.Assert.assertFalse;

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
        List<TestNode> nodePool = rmHelper.createNodes(name, number);
        testNodes.addAll(nodePool);
        ArrayList<Node> answer = new ArrayList<>(nodePool.size());
        for (TestNode tn : nodePool) {
            answer.add(tn.getNode());
        }
        return answer;
    }

    @Test
    public void action() throws Exception {

        List<Node> nodePool;

        String nsName = "TestNSNodesPermissions";
        RMTHelper.log("Test1 - node users = ME");

        ResourceManager nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        nsadmin.createNodeSource(nsName,
                                 DefaultInfrastructureManager.class.getName(),
                                 null,
                                 StaticPolicy.class.getName(),
                                 new Object[] { "ME", "ALL" },
                                 NODES_NOT_RECOVERABLE)
               .getBooleanValue();

        nodePool = createNodes("node", 2);
        Node node = nodePool.remove(0);
        Node node2 = nodePool.remove(0);

        addOneNodeToNodeSource(nsName, node, nsadmin);

        NodeSet nodes;

        getAndReleaseNodes(nsadmin, "Node source creator should be able to get nodes", new Criteria(1), 1);

        ResourceManager user = rmHelper.getResourceManager(TestUsers.RADMIN);
        nodes = user.getNodes(new Criteria(1));
        assertEquals("User cannot get foreign node", 0, nodes.size());

        addOneNodeToNodeSource(nsName, node2, user);

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);

        getAndReleaseNodes(nsadmin,
                           "Node source creator should be able to get nodes created by a different user",
                           new Criteria(2),
                           2);

        removeNodeSourceAndNodes(nsName, nsadmin, 2);

        RMTHelper.log("Test2 - node users = MY_GROUPS");
        ResourceManager admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "MY_GROUPS", "ALL" },
                               NODES_NOT_RECOVERABLE)
             .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        node2 = nodePool.remove(0);

        System.out.println(System.currentTimeMillis());
        addOneNodeToNodeSource(nsName, node, admin);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);

        addOneNodeToNodeSource(nsName, node2, user);

        getAndReleaseNodes(user,
                           "user from the same group as the owner should be able to get nodes",
                           new Criteria(2),
                           2);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        removeNodeSourceAndNodes(nsName, admin, 2);

        RMTHelper.log("Test3 - node users = PROVIDER");
        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        user.createNodeSource(nsName,
                              DefaultInfrastructureManager.class.getName(),
                              null,
                              StaticPolicy.class.getName(),
                              new Object[] { "PROVIDER", "ALL" },
                              NODES_NOT_RECOVERABLE)
            .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        node2 = nodePool.remove(0);

        addOneNodeToNodeSource(nsName, node, user);

        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        addOneNodeToNodeSource(nsName, node2, admin);

        ResourceManager superAdmin = rmHelper.getResourceManager(TestUsers.TEST);

        // super admin can get 2 nodes as it has AllPermissions
        getAndReleaseNodes(superAdmin, "Super admin should be able to get foreign nodes", new Criteria(2), 2);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        getAndReleaseNodes(user, "node source owner should be able to get his own nodes only", new Criteria(2), 1);

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        getAndReleaseNodes(nsadmin, "other users should not be able to get nodes", new Criteria(2), 0);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        removeNodeSourceAndNodes(nsName, admin, 2);

        RMTHelper.log("Test4 - node users = PROVIDER_GROUPS");
        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        user.createNodeSource(nsName,
                              DefaultInfrastructureManager.class.getName(),
                              null,
                              StaticPolicy.class.getName(),
                              new Object[] { "PROVIDER_GROUPS", "ALL" },
                              NODES_NOT_RECOVERABLE)
            .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        node2 = nodePool.remove(0);

        addOneNodeToNodeSource(nsName, node, user);

        superAdmin = rmHelper.getResourceManager(TestUsers.TEST);
        addOneNodeToNodeSource(nsName, node2, superAdmin);

        // admin can get 2 nodes as it has AllPermissions
        getAndReleaseNodes(superAdmin, "superAdmin did not get foreign node", new Criteria(2), 2);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        getAndReleaseNodes(user, "node source owner should get nodes he added", new Criteria(2), 1);

        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        getAndReleaseNodes(admin,
                           "user with the same group as the node source owner should get the same nodes",
                           new Criteria(2),
                           1);

        // removing the node source
        superAdmin = rmHelper.getResourceManager(TestUsers.TEST);
        removeNodeSourceAndNodes(nsName, superAdmin, 2);

        RMTHelper.log("Test5 - node users = ALL");
        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        user.createNodeSource(nsName,
                              DefaultInfrastructureManager.class.getName(),
                              null,
                              StaticPolicy.class.getName(),
                              new Object[] { "ALL", "ALL" },
                              NODES_NOT_RECOVERABLE)
            .getBooleanValue();

        nodePool = createNodes("node", 1);
        node = nodePool.remove(0);

        addOneNodeToNodeSource(nsName, node, user);

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        Criteria criteria = new Criteria(1);
        getAndReleaseNodes(nsadmin, "standard user should use a node", criteria, 1);

        criteria.setNodeAccessToken("non_existing_token");
        nodes = nsadmin.getNodes(criteria);
        assertEquals("Got a regular node while requesting a node with token", 0, nodes.size());

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        removeNodeSourceAndNodes(nsName, admin, 1);

        RMTHelper.log("Test6.1 - specific users");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "users=nsadmin", "ALL" },
                               NODES_NOT_RECOVERABLE)
             .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        node2 = nodePool.remove(0);

        addOneNodeToNodeSource(nsName, node, admin);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        nodes = user.getNodes(new Criteria(1));
        assertEquals("unspecified user cannot use its own node", 0, nodes.size());

        addOneNodeToNodeSource(nsName, node2, user);

        nodes = user.getNodes(new Criteria(1));
        assertEquals("unspecified user cannot use its own node", 0, nodes.size());

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        getAndReleaseNodes(nsadmin, "specified user should use nodes", new Criteria(2), 2);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        removeNodeSourceAndNodes(nsName, admin, 2);

        RMTHelper.log("Test6.2 - specific groups");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "groups=nsadmins", "ALL" },
                               NODES_NOT_RECOVERABLE)
             .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        node2 = nodePool.remove(0);

        addOneNodeToNodeSource(nsName, node, admin);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        nodes = user.getNodes(new Criteria(1));
        assertEquals("User not in specified group should not get foreign node", 0, nodes.size());

        addOneNodeToNodeSource(nsName, node2, user);

        nodes = user.getNodes(new Criteria(1));
        assertEquals("User not in specified group should not get his own node", 0, nodes.size());

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        getAndReleaseNodes(nsadmin, "user in specified group should get nodes", new Criteria(2), 2);

        // removing the node source
        superAdmin = rmHelper.getResourceManager(TestUsers.TEST);
        getAndReleaseNodes(superAdmin, "superAdmin should get nodes", new Criteria(2), 2);

        removeNodeSourceAndNodes(nsName, superAdmin, 2);

        RMTHelper.log("Test6.3 - specific tokens");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "tokens=token1,token2", "ALL" },
                               NODES_NOT_RECOVERABLE)
             .getBooleanValue();

        nodePool = createNodes("node", 1);
        node = nodePool.remove(0);

        addOneNodeToNodeSource(nsName, node, admin);

        criteria = new Criteria(1);
        criteria.setNodeAccessToken("token1");
        getAndReleaseNodes(admin, "token1 should use the node", criteria, 1);

        criteria.setNodeAccessToken("token2");
        getAndReleaseNodes(admin, "token2 should use the node", criteria, 1);

        // admin also does not have the right to get the node
        criteria.setNodeAccessToken("token3");
        getAndReleaseNodes(admin, "token3 should not use the node", criteria, 0);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        criteria = new Criteria(1);
        getAndReleaseNodes(user, "no token should not use the node", criteria, 0);

        criteria.setNodeAccessToken("token1");
        getAndReleaseNodes(user, "token1 should use the node", criteria, 1);

        superAdmin = rmHelper.getResourceManager(TestUsers.TEST);
        getAndReleaseNodes(superAdmin, "superAdmin should use the node", new Criteria(1), 1);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        removeNodeSourceAndNodes(nsName, admin, 1);

        RMTHelper.log("Test6.4 - specific users and groups");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "users=radmin;groups=nsadmins", "ALL" },
                               NODES_NOT_RECOVERABLE)
             .getBooleanValue();

        nodePool = createNodes("node", 2);
        node = nodePool.remove(0);
        node2 = nodePool.remove(0);

        addOneNodeToNodeSource(nsName, node, admin);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        getAndReleaseNodes(user, "specified user should get node", new Criteria(1), 1);

        addOneNodeToNodeSource(nsName, node2, user);
        getAndReleaseNodes(user, "specified user should get nodes", new Criteria(2), 2);

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        getAndReleaseNodes(nsadmin, "user from specified group should get nodes", new Criteria(2), 2);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        removeNodeSourceAndNodes(nsName, admin, 2);

        RMTHelper.log("Test6.5 - specific users and token");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "users=radmin;tokens=token1", "ALL" },
                               NODES_NOT_RECOVERABLE)
             .getBooleanValue();

        nodePool = createNodes("node", 1);
        node = nodePool.remove(0);

        addOneNodeToNodeSource(nsName, node, admin);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        criteria = new Criteria(1);
        criteria.setNodeAccessToken("token1");
        getAndReleaseNodes(user, "token1 should allow getting nodes", criteria, 1);

        criteria.setNodeAccessToken("token2"); // will not get a node as don't have the token "token2"
        getAndReleaseNodes(user, "token2 should not allow getting nodes", criteria, 0);

        nsadmin = rmHelper.getResourceManager(TestUsers.NSADMIN);
        criteria.setNodeAccessToken("token1");
        getAndReleaseNodes(nsadmin, "token1 should allow getting nodes", criteria, 1);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        removeNodeSourceAndNodes(nsName, admin, 1);

        RMTHelper.log("Test7.1 - excluding specific user");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "users=!radmin", "ALL" },
                               NODES_NOT_RECOVERABLE)
             .getBooleanValue();

        nodePool = createNodes("node", 1);
        node = nodePool.remove(0);

        addOneNodeToNodeSource(nsName, node, admin);

        user = rmHelper.getResourceManager(TestUsers.RADMIN);
        boolean canComputeOnIt = user.isNodeUser(node.getNodeInformation().getURL()).getBooleanValue();
        assertFalse("User is not supposed to get this compute node", canComputeOnIt);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        removeNodeSourceAndNodes(nsName, admin, 1);

        RMTHelper.log("Test7.2 - excluding specific group");
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        admin.createNodeSource(nsName,
                               DefaultInfrastructureManager.class.getName(),
                               null,
                               StaticPolicy.class.getName(),
                               new Object[] { "groups=!nsadmins", "ALL" },
                               NODES_NOT_RECOVERABLE)
             .getBooleanValue();

        nodePool = createNodes("node", 1);
        node = nodePool.remove(0);

        addOneNodeToNodeSource(nsName, node, admin);

        user = rmHelper.getResourceManager(TestUsers.NSADMIN);
        canComputeOnIt = user.isNodeUser(node.getNodeInformation().getURL()).getBooleanValue();
        assertFalse("User is not supposed to get this compute node", canComputeOnIt);

        // removing the node source
        admin = rmHelper.getResourceManager(TestUsers.ADMIN);
        removeNodeSourceAndNodes(nsName, admin, 1);
    }

    private void addOneNodeToNodeSource(String nsName, Node node, ResourceManager admin) {
        String url = node.getNodeInformation().getURL();
        admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, url);
        //we eat the configuring to free nodeevent
        rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, url);
    }

    private void removeNodeSourceAndNodes(String nsName, ResourceManager nsadmin, int nbNodes) {
        nsadmin.removeNodeSource(nsName, true).getBooleanValue();
        for (int i = 0; i < nbNodes; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
    }

    private void getAndReleaseNodes(ResourceManager user, String message, Criteria criteria, int expectedNodes) {
        NodeSet nodes;
        nodes = user.getNodes(criteria);
        assertEquals(message, expectedNodes, nodes.size());
        //we eat free -> busy * expectedNodes
        for (int i = 0; i < expectedNodes; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }

        if (expectedNodes > 0) {
            user.releaseNodes(nodes);
        }
        //we eat busy -> free * expectedNodes
        for (int i = 0; i < expectedNodes; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }
}
