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

import functionaltests.TNode;
import org.junit.Assert;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;

import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;

import java.util.List;


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
 *
 */
public class TestNSProviderPermissions extends RMConsecutive {

    @org.junit.Test
    public void action() throws Exception {
        RMTHelper helper = RMTHelper.getDefaultInstance();

        String nsName = "ns";
        RMTHelper.log("Test1 - node providers = ME");

        ResourceManager nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        nsadmin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "ME" });

        List<TNode> nodePool =  helper.createNodes("node", 17);

        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        Node node = nodePool.remove(0).getNode();
        Node node2 = nodePool.remove(0).getNode();
        try {
            nsadmin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }

        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        ResourceManager user = helper.getResourceManager(null, "radmin", "pwd");
        try {
            // user does not allow to add nodes
            user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            // user does not allow to remove nodes
            user.removeNode(node.getNodeInformation().getURL(), true).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // AllPermission user
        ResourceManager admin = helper.getResourceManager(null, "admin", "admin");
        try {
            // user does not allow to add nodes
            admin.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }

        try {
            // user does not allow to remove nodes
            admin.removeNode(node.getNodeInformation().getURL(), true).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }

        try {
            // user does not allow to remove nodes
            admin.removeNodeSource(nsName, true).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }

        RMTHelper.log("Test2 - node providers = MY_GROUPS");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null, StaticPolicy.class
                .getName(), new Object[] { "ALL", "MY_GROUPS" });
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        node = nodePool.remove(0).getNode();
        node2 = nodePool.remove(0).getNode();
        Node node3 = nodePool.remove(0).getNode();
        try {
            admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }

        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        try {
            // nsadmin is in the same group as admin
            nsadmin.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }

        user = helper.getResourceManager(null, "radmin", "pwd");
        try {
            // user does not allow to add nodes
            user.addNode(node3.getNodeInformation().getURL(), nsName).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            // user does not allow to remove nodes
            user.removeNode(node2.getNodeInformation().getURL(), true).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        try {
            // nsadmin cannot remove foreign node
            nsadmin.removeNode(node.getNodeInformation().getURL(), true).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            // but it can remove its own
            nsadmin.removeNode(node2.getNodeInformation().getURL(), true).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            nsadmin.addNode(node3.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }

        admin = helper.getResourceManager(null, "admin", "admin");
        try {
            // admin can remove foreign node
            admin.removeNode(node3.getNodeInformation().getURL(), true).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            admin.removeNodeSource(nsName, true).getBooleanValue();
            helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }

        RMTHelper.log("Test3 - node providers = ALL");
        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        nsadmin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "ALL" });
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        node = nodePool.remove(0).getNode();
        node2 = nodePool.remove(0).getNode();
        node3 = nodePool.remove(0).getNode();
        try {
            nsadmin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }

        user = helper.getResourceManager(null, "radmin", "pwd");
        try {
            // user can add new nodes
            user.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }
        try {
            // but cannot remove a foreign node
            user.removeNode(node.getNodeInformation().getURL(), true).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            // user can remove his own node
            user.removeNode(node2.getNodeInformation().getURL(), true).getBooleanValue();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }
        try {
            // adding node3
            user.addNode(node3.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }

        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        try {
            // nsadmin can remove node3 as ns admin
            nsadmin.removeNode(node3.getNodeInformation().getURL(), true).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }

        RMTHelper.log("Test4 - admin priveleges");
        admin = helper.getResourceManager(null, "admin", "admin");
        try {
            // admin can remove anything
            admin.removeNode(node.getNodeInformation().getURL(), true).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            admin.removeNodeSource(nsName, false);
            helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }

        RMTHelper.log("Test5.1 - specific users");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null, StaticPolicy.class
                .getName(), new Object[] { "ALL", "users=nsadmin" });
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        node = nodePool.remove(0).getNode();
        node2 = nodePool.remove(0).getNode();
        node3 = nodePool.remove(0).getNode();
        try {
            admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }
        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        try {
            // nsadmin cannot remove node as he is not a node owner
            nsadmin.removeNode(node.getNodeInformation().getURL(), true).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            nsadmin.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(false);
        }
        user = helper.getResourceManager(null, "radmin", "pwd");
        try {
            // user cannot add new nodes
            user.addNode(node3.getNodeInformation().getURL(), nsName).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            // user cannot remove node as he is not a node owner
            user.removeNode(node2.getNodeInformation().getURL(), true).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        admin = helper.getResourceManager(null, "admin", "admin");
        try {
            // user does not allow to remove nodes
            admin.removeNodeSource(nsName, true).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        RMTHelper.log("Test5.2 - specific groups");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "groups=nsadmins" }).getBooleanValue();
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        node = nodePool.remove(0).getNode();
        node2 = nodePool.remove(0).getNode();
        node3 = nodePool.remove(0).getNode();
        try {
            admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        try {
            // nsadmin cannot remove node as he is not a node owner
            nsadmin.removeNode(node.getNodeInformation().getURL(), true).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            nsadmin.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
        user = helper.getResourceManager(null, "radmin", "pwd");
        try {
            // user cannot add new nodes
            user.addNode(node3.getNodeInformation().getURL(), nsName).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            // user cannot remove node as he is not a node owner
            user.removeNode(node2.getNodeInformation().getURL(), true).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        admin = helper.getResourceManager(null, "admin", "admin");
        try {
            // user does not allow to remove nodes
            admin.removeNodeSource(nsName, true).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }

        RMTHelper.log("Test5.3 - specific users/groups");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null, StaticPolicy.class
                .getName(), new Object[] { "ALL", "users=radmin;groups=nsadmins" });
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        node = nodePool.remove(0).getNode();
        node2 = nodePool.remove(0).getNode();
        node3 = nodePool.remove(0).getNode();
        try {
            admin.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
        nsadmin = helper.getResourceManager(null, "nsadmin", "pwd");
        try {
            // nsadmin cannot remove node as he is not a node owner
            nsadmin.removeNode(node.getNodeInformation().getURL(), true).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            nsadmin.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
        user = helper.getResourceManager(null, "radmin", "pwd");
        try {
            // user can add new nodes
            user.addNode(node3.getNodeInformation().getURL(), nsName).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
        try {
            // user cannot remove node as he is not a node owner
            user.removeNode(node2.getNodeInformation().getURL(), true).getBooleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        admin = helper.getResourceManager(null, "admin", "admin");
        try {
            // user does not allow to remove nodes
            admin.removeNodeSource(nsName, true).getBooleanValue();
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }

        System.out.println("Success");
    }
}
