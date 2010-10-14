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

import junit.framework.Assert;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


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
 *  We suppose that the resource manager is configured in the way that 3
 *  users exist: admin, nsadmin, user
 *  admin and nsadmin are in the same group ("nsadmins")
 *
 */
public class TestNSProviderPermissions extends FunctionalTest {

    @org.junit.Test
    public void action() throws Exception {

        String nsName = "ns";
        RMTHelper.log("Test1 - node providers = ME");

        ResourceManager nsadmin = RMTHelper.join("nsadmin", "pwd");
        nsadmin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "ME" });

        Node node = RMTHelper.createNode("node1");
        Node node2 = RMTHelper.createNode("node2");
        try {
            nsadmin.addNode(node.getNodeInformation().getURL(), nsName).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        nsadmin.disconnect();

        ResourceManager user = RMTHelper.join("radmin", "pwd");
        try {
            // user does not allow to add nodes
            user.addNode(node2.getNodeInformation().getURL(), nsName).booleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            // user does not allow to remove nodes
            user.removeNode(node.getNodeInformation().getURL(), true).booleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        user.disconnect();

        // AllPermission user
        ResourceManager admin = RMTHelper.join("admin", "admin");
        try {
            // user does not allow to add nodes
            admin.addNode(node2.getNodeInformation().getURL(), nsName).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            // user does not allow to remove nodes
            admin.removeNode(node.getNodeInformation().getURL(), true).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            // user does not allow to remove nodes
            admin.removeNodeSource(nsName, true).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        RMTHelper.log("Test2 - node providers = MY_GROUPS");
        admin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null, StaticPolicy.class
                .getName(), new Object[] { "ALL", "MY_GROUPS" });

        node = RMTHelper.createNode("node1");
        node2 = RMTHelper.createNode("node2");
        Node node3 = RMTHelper.createNode("node3");
        try {
            admin.addNode(node.getNodeInformation().getURL(), nsName).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        admin.disconnect();

        nsadmin = RMTHelper.join("nsadmin", "pwd");
        try {
            // nsadmin is in the same group as admin
            nsadmin.addNode(node2.getNodeInformation().getURL(), nsName).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        nsadmin.disconnect();

        user = RMTHelper.join("radmin", "pwd");
        try {
            // user does not allow to add nodes
            user.addNode(node3.getNodeInformation().getURL(), nsName).booleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            // user does not allow to remove nodes
            user.removeNode(node2.getNodeInformation().getURL(), true).booleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        user.disconnect();

        nsadmin = RMTHelper.join("nsadmin", "pwd");
        try {
            // nsadmin cannot remove foreign node
            nsadmin.removeNode(node.getNodeInformation().getURL(), true).booleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            // but it can remove its own
            nsadmin.removeNode(node2.getNodeInformation().getURL(), true).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            nsadmin.addNode(node3.getNodeInformation().getURL(), nsName).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        nsadmin.disconnect();

        admin = RMTHelper.join("admin", "admin");
        try {
            // admin can remove foreign node
            admin.removeNode(node3.getNodeInformation().getURL(), true).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            admin.removeNodeSource(nsName, true).booleanValue();
            RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        admin.disconnect();

        RMTHelper.log("Test3 - node providers = ALL");
        nsadmin = RMTHelper.join("nsadmin", "pwd");
        nsadmin.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "ALL" });

        node = RMTHelper.createNode("node1");
        node2 = RMTHelper.createNode("node2");
        node3 = RMTHelper.createNode("node3");
        try {
            nsadmin.addNode(node.getNodeInformation().getURL(), nsName).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        nsadmin.disconnect();

        user = RMTHelper.join("radmin", "pwd");
        try {
            // user can add new nodes
            user.addNode(node2.getNodeInformation().getURL(), nsName).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        try {
            // but cannot remove a foreign node
            user.removeNode(node.getNodeInformation().getURL(), true).booleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            // user can remove his own node
            user.removeNode(node2.getNodeInformation().getURL(), true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        try {
            // adding node3
            user.addNode(node3.getNodeInformation().getURL(), nsName).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        user.disconnect();
        nsadmin = RMTHelper.join("nsadmin", "pwd");
        try {
            // nsadmin can remove node3 as ns admin
            nsadmin.removeNode(node3.getNodeInformation().getURL(), true).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        nsadmin.disconnect();
        admin = RMTHelper.join("admin", "admin");
        try {
            // admin can remove anything
            admin.removeNode(node.getNodeInformation().getURL(), true).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            admin.removeNodeSource(nsName, false);
            RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        System.out.println("Success");
    }
}
