/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.nodesource;

import static junit.framework.Assert.assertTrue;

import java.io.File;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 *
 * Test checks the correct behavior of node source consisted of GCM infrastructure manager
 * and static acquisition policy.
 *
 */
public class TestGCMInfrastructureStaticPolicy extends FunctionalTest {

    protected byte[] GCMDeploymentData;

    protected static String defaultDescriptor = TestGCMInfrastructureStaticPolicy.class.getResource(
            "/functionaltests/nodesource/3nodes.xml").getPath();
    protected int defaultDescriptorNodesNb = 3;

    protected void createEmptyNodeSource(String sourceName) throws Exception {
        RMTHelper.getAdminInterface().createNodesource(sourceName, GCMInfrastructure.class.getName(), null,
                StaticPolicy.class.getName(), null);

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);
    }

    protected void createDefaultNodeSource(String sourceName) throws Exception {
        // creating node source
        RMTHelper.getAdminInterface().createNodesource(sourceName, GCMInfrastructure.class.getName(),
                new Object[] { GCMDeploymentData }, StaticPolicy.class.getName(), null);

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }
    }

    protected void removeNodeSource(String sourceName) throws Exception {
        // removing node source
        RMTHelper.getAdminInterface().removeSource(sourceName, true);

        //wait the n events of the n nodes removals of the node source
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        //wait for the event of the node source removal
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, sourceName);
    }

    protected void init() throws Exception {
        GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(defaultDescriptor)));
    }

    protected BooleanWrapper addNodes(String sourceName) throws Exception {
        BooleanWrapper result = RMTHelper.getAdminInterface().addNodes(sourceName,
                new Object[] { GCMDeploymentData });
        if (result.booleanValue()) {
            // waiting for adding nodes acquisition info event
            RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_NODES_ACQUISTION_INFO_ADDED, sourceName);
        }
        return result;
    }

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        init();
        String source1 = "Node source 1";
        String source2 = "Node source 2";

        RMAdmin admin = RMTHelper.getAdminInterface();

        RMTHelper.log("Test 1 - creation/removal of empty node source");

        RMTHelper.log("creation");
        createEmptyNodeSource(source1);
        RMTHelper.log("removal");
        admin.removeSource(source1, true);

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, source1);

        RMTHelper.log("Test 2 - creation/removal of the node source with nodes");
        createDefaultNodeSource(source1);
        removeNodeSource(source1);

        RMTHelper.log("Test 3 - adding nodes to existing node source");
        createDefaultNodeSource(source1);

        // adding nodes
        addNodes(source1);

        //wait the n events of the n nodes added to the node source
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }
        assertTrue(admin.getTotalNodesNumber().intValue() == 2 * defaultDescriptorNodesNb);
        assertTrue(admin.getFreeNodesNumber().intValue() == 2 * defaultDescriptorNodesNb);

        // releasing some nodes
        NodeSet ns = admin.getAtMostNodes(defaultDescriptorNodesNb, null);

        for (Node n : ns) {
            admin.removeNode(n.getNodeInformation().getURL(), true, false);
            RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n.getNodeInformation().getURL());
        }

        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);
        removeNodeSource(source1);

        RMTHelper.log("Test 4 - adding nodes to non existing node source");
        try {
            BooleanWrapper result = addNodes(source1);
            PAFuture.waitFor(result);
            assertTrue(false);
        } catch (AddingNodesException e) {
            System.out.println("Expected exception");
        }

        RMTHelper.log("Test 5 - several node sources");
        createDefaultNodeSource(source1);
        createDefaultNodeSource(source2);

        addNodes(source1);
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }
        assertTrue(admin.getTotalNodesNumber().intValue() == 3 * defaultDescriptorNodesNb);
        assertTrue(admin.getFreeNodesNumber().intValue() == 3 * defaultDescriptorNodesNb);

        addNodes(source2);
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }
        assertTrue(admin.getTotalNodesNumber().intValue() == 4 * defaultDescriptorNodesNb);
        assertTrue(admin.getFreeNodesNumber().intValue() == 4 * defaultDescriptorNodesNb);

        admin.removeSource(source1, true);
        //wait the n events of the n nodes removals of the node source
        for (int i = 0; i < defaultDescriptorNodesNb * 2; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        //wait for the event of the node source removal
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, source1);

        admin.removeSource(source2, true);
        for (int i = 0; i < defaultDescriptorNodesNb * 2; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        //wait for the event of the node source removal
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, source2);
    }
}
