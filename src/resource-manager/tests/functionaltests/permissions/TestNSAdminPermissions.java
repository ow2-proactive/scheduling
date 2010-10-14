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
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 *  Test checks that admin permission of the node source (which the creator has) allows to
 *
 *  1. remove the node source,
 *
 *  2. add/remove nodes to/from node sources,
 *
 *  3. does not allow to use nodes if node access is set to PROVIDER/PROVIDER_GROUPS
 *
 *  4. user with AllPermission can remove any node sources
 *
 *  We suppose that the resource manager is configured in the way that 3
 *  users exist: admin, nsadmin, user
 *  admin and nsadmin are in the same group ("nsadmins")
 *
 */
public class TestNSAdminPermissions extends FunctionalTest {

    @org.junit.Test
    public void action() throws Exception {

        String nsName = "ns";
        ResourceManager adminRMAccess = RMTHelper.join("admin", "admin");

        RMTHelper.log("Test1 - node source removal");
        adminRMAccess.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "ME" });

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
        adminRMAccess.disconnect();

        RMTHelper.log("Test2 - ns admin can remove foreign nodes");
        userRMAccess = RMTHelper.join("radmin", "pwd");
        userRMAccess.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "PROVIDER", "ALL" });
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);
        userRMAccess.disconnect();

        providerRMAccess = RMTHelper.join("nsadmin", "pwd");
        Node node = RMTHelper.createNode("node1");
        try {
            // adding the node as provider
            providerRMAccess.addNode(node.getNodeInformation().getURL(), nsName).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        providerRMAccess.disconnect();

        userRMAccess = RMTHelper.join("radmin", "pwd");
        try {
            // this is an administrator of the node source, so it can remove the foreign node
            userRMAccess.removeNode(node.getNodeInformation().getURL(), true).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        userRMAccess.disconnect();

        RMTHelper.log("Test3 - ns admin cannot get the foreign node");
        providerRMAccess = RMTHelper.join("nsadmin", "pwd");
        Node node2 = RMTHelper.createNode("node2");
        try {
            // adding the node as provider
            providerRMAccess.addNode(node2.getNodeInformation().getURL(), nsName).booleanValue();
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        providerRMAccess.disconnect();

        userRMAccess = RMTHelper.join("radmin", "pwd");
        try {
            // this is an administrator of the node source, so it can remove the foreign node
            NodeSet nodes = userRMAccess.getAtMostNodes(1, null);
            Assert.assertEquals("NS admin cannot get nodes as the get level is set to PROVIDER", 0, nodes
                    .size());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
        userRMAccess.disconnect();

        RMTHelper.log("Test4 - user with AllPermisssion can remove any node sources");

        adminRMAccess = RMTHelper.join("admin", "admin");
        try {
            adminRMAccess.removeNodeSource(nsName, true).booleanValue();
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        System.out.println("Success");
    }
}
