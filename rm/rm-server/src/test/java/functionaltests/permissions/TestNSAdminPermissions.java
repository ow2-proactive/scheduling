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

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;
import functionaltests.utils.TestNode;
import functionaltests.utils.TestUsers;


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
public class TestNSAdminPermissions extends RMFunctionalTest {

    @Test
    public void action() throws Exception {
        String nsName = "TestNSAdminPermissions";
        ResourceManager adminRMAccess = rmHelper.getResourceManager(TestUsers.ADMIN);

        RMTHelper.log("Test1 - node source removal");
        adminRMAccess.createNodeSource(nsName,
                                       DefaultInfrastructureManager.class.getName(),
                                       null,
                                       StaticPolicy.class.getName(),
                                       new Object[] { "ALL", "ME" },
                                       NODES_NOT_RECOVERABLE);

        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        ResourceManager providerRMAccess = rmHelper.getResourceManager(TestUsers.NSADMIN);
        try {
            providerRMAccess.removeNodeSource(nsName, true).getBooleanValue();
            fail("nsadmin is not the node source owner");
        } catch (Exception e) {
        }

        adminRMAccess = rmHelper.getResourceManager(TestUsers.RADMIN);

        adminRMAccess.removeNodeSource(nsName, true).getBooleanValue();

        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test2 - ns admin can remove foreign nodes");
        ResourceManager userRMAccess = rmHelper.getResourceManager(TestUsers.NSADMIN);
        userRMAccess.createNodeSource(nsName,
                                      DefaultInfrastructureManager.class.getName(),
                                      null,
                                      StaticPolicy.class.getName(),
                                      new Object[] { "PROVIDER", "ALL" },
                                      NODES_NOT_RECOVERABLE);
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        providerRMAccess = rmHelper.getResourceManager(TestUsers.PROVIDER);
        TestNode testNode1 = rmHelper.createNode("node1");
        testNodes.add(testNode1);
        Node node = testNode1.getNode();

        // adding the node as provider
        providerRMAccess.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        // node becomes free
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        userRMAccess = rmHelper.getResourceManager(TestUsers.NSADMIN);
        // this is an administrator of the node source, so it can remove the foreign node
        userRMAccess.removeNode(node.getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        RMTHelper.log("Test3 - ns admin cannot get the foreign node");
        providerRMAccess = rmHelper.getResourceManager(TestUsers.PROVIDER);
        TestNode testNode2 = rmHelper.createNode("node2");
        testNodes.add(testNode2);
        Node node2 = testNode2.getNode();
        // adding the node as provider
        providerRMAccess.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        // node becomes free
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        userRMAccess = rmHelper.getResourceManager(TestUsers.NSADMIN);
        // this is an administrator of the node source, but it cannot use the node
        NodeSet nodes = userRMAccess.getAtMostNodes(1, null);
        Assert.assertEquals("NS admin cannot get nodes as the get level is set to PROVIDER", 0, nodes.size());

        RMTHelper.log("Test4 - user with AllPermisssion can remove any node sources");

        adminRMAccess = rmHelper.getResourceManager(TestUsers.ADMIN);
        adminRMAccess.removeNodeSource(nsName, true).getBooleanValue();
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Success");
    }
}
