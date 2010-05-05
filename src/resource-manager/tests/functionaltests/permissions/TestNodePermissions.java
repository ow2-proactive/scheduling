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

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.LinkedList;

import junit.framework.Assert;

import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 *  Test checks that node can be removed only by owner or
 *  user having AllPermissions.
 *  We suppose that the resource manager is configured in the way that
 *  3 users exist: admin, nsadmin, user
 *  admin and nsadmin are in the same group ("nsadmins")
 *
 */
public class TestNodePermissions extends FunctionalTest {

    @org.junit.Test
    public void action() throws Exception {

        String nsName = "ns";
        ResourceManager powerUserRMAccess = RMTHelper.join("radmin", "pwd");

        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(
            RMTHelper.defaultDescriptor)));

        RMTHelper.log("Test1 - checking that only owner or admin can remove nodes");
        powerUserRMAccess.createNodeSource(nsName, GCMInfrastructure.class.getName(),
                new Object[] { GCMDeploymentData }, StaticPolicy.class.getName(), new Object[] { "USER",
                        "ALL" });

        // wait for creation of GCM Node Source event, and deployment of its nodes
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        LinkedList<String> nodes = new LinkedList<String>();
        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMNodeEvent ev = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            nodes.add(ev.getNodeUrl());
        }

        assertTrue(powerUserRMAccess.getState().getTotalNodesNumber() == RMTHelper.defaultNodesNumber);
        assertTrue(powerUserRMAccess.getState().getFreeNodesNumber() == RMTHelper.defaultNodesNumber);

        powerUserRMAccess.disconnect();

        System.out.println("All the nodes are available");
        ResourceManager nsAdminRMAccess = RMTHelper.join("nsadmin", "pwd");
        try {
            // trying to remove foreign node
            System.out.println("Removing foreign node");
            nsAdminRMAccess.removeNode(nodes.getFirst(), true).booleanValue();
            System.out.println("Error: foreign node removed");
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println("Success: " + e.getMessage());
        }

        try {
            // trying to the node source
            // the action is allowed but node source won't be removed until owners
            // remove their own nodes
            System.out.println("Removing node source with foreign nodes");
            nsAdminRMAccess.removeNodeSource(nsName, true).booleanValue();
            Assert.assertTrue(false);
        } catch (Exception e) {
            System.out.println("Expected error: " + e.getMessage());
        }

        try {
            RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName, 5000);
            System.out.println("Error: node source was removed");
            Assert.assertTrue(false);
        } catch (ProActiveTimeoutException ex) {
            System.out
                    .println("Success: the request has been sent but node source won't be removed until owners remove their nodes");
        }

        nsAdminRMAccess.disconnect();

        ResourceManager adminRMAccess = RMTHelper.join("admin", "admin");
        try {
            // trying to the node source
            // the action is allowed but node source won't be removed until owners
            // remove their nodes
            System.out.println("Removing foreign node with AllPermission");
            adminRMAccess.removeNode(nodes.getFirst(), true).booleanValue();
            nodes.removeFirst();
            System.out.println("Success");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            Assert.assertTrue(false);
        }

        RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        adminRMAccess.disconnect();
        powerUserRMAccess = RMTHelper.join("radmin", "pwd");
        try {
            System.out.println("Removing my own node");
            powerUserRMAccess.removeNode(nodes.getFirst(), true).booleanValue();
            System.out.println("Success");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            Assert.assertTrue(false);
        }

        RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        try {
            // trying to the node source
            // the action is allowed but node source won't be removed until owners
            // remove their own nodes
            System.out.println("Removing node source");
            powerUserRMAccess.removeNodeSource(nsName, true).booleanValue();
            System.out.println("Success");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            Assert.assertTrue(false);
        }

        for (int i = 0; i < RMTHelper.defaultNodesNumber - 2; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        System.out.println("The node source will be removed by previous request");
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        System.out.println("Success");
    }
}
