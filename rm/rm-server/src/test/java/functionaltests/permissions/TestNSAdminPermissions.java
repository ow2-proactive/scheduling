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

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.NodeSet;
import org.junit.Assert;
import org.junit.Test;

import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;

import static org.junit.Assert.fail;


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
public class TestNSAdminPermissions extends RMConsecutive {

    @Test
    public void action() throws Exception {
        String nsName = "ns";
        ResourceManager adminRMAccess = rmHelper.getResourceManager(null, "admin", "admin");

        RMTHelper.log("Test1 - node source removal");
        adminRMAccess.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "ME" });

        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        // user does not have an access to change the node source
        ResourceManager userRMAccess = rmHelper.getResourceManager(null, "radmin", "pwd");
        try {
            userRMAccess.removeNodeSource(nsName, true).getBooleanValue();
            fail();
        } catch (Exception e) {
        }

        // admin and provider are in "nsadmins" group
        ResourceManager providerRMAccess = rmHelper.getResourceManager(null, "nsadmin", "pwd");
        try {
            providerRMAccess.removeNodeSource(nsName, true).getBooleanValue();
            fail();
        } catch (Exception e) {
        }

        adminRMAccess = rmHelper.getResourceManager(null, "admin", "admin");

        adminRMAccess.removeNodeSource(nsName, true).getBooleanValue();

        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);

        RMTHelper.log("Test2 - ns admin can remove foreign nodes");
        userRMAccess = rmHelper.getResourceManager(null, "radmin", "pwd");
        userRMAccess.createNodeSource(nsName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "PROVIDER", "ALL" });
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        providerRMAccess = rmHelper.getResourceManager(null, "nsadmin", "pwd");
        Node node = rmHelper.createNode("node1").getNode();

        // adding the node as provider
        providerRMAccess.addNode(node.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        // node becomes free
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        userRMAccess = rmHelper.getResourceManager(null, "radmin", "pwd");
        // this is an administrator of the node source, so it can remove the foreign node
        userRMAccess.removeNode(node.getNodeInformation().getURL(), true).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        RMTHelper.log("Test3 - ns admin cannot get the foreign node");
        providerRMAccess = rmHelper.getResourceManager(null, "nsadmin", "pwd");
        Node node2 = rmHelper.createNode("node2").getNode();
        // adding the node as provider
        providerRMAccess.addNode(node2.getNodeInformation().getURL(), nsName).getBooleanValue();
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        // node becomes free
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        userRMAccess = rmHelper.getResourceManager(null, "radmin", "pwd");
        // this is an administrator of the node source, so it can remove the foreign node
        NodeSet nodes = userRMAccess.getAtMostNodes(1, null);
        Assert.assertEquals("NS admin cannot get nodes as the get level is set to PROVIDER", 0, nodes.size());

        RMTHelper.log("Test4 - user with AllPermisssion can remove any node sources");

        adminRMAccess = rmHelper.getResourceManager(null, "admin", "admin");
        adminRMAccess.removeNodeSource(nsName, true).getBooleanValue();

        RMTHelper.log("Success");
    }
}
