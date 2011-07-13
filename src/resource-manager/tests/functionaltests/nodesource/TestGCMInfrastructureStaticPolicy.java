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
package functionaltests.nodesource;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMInfrastructure;
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

    protected byte[] emptyGCMD;
    protected byte[] GCMDeploymentData;

    protected static URL defaultDescriptor = TestGCMInfrastructureStaticPolicy.class
            .getResource("/functionaltests/nodesource/3nodes.xml");
    protected int defaultDescriptorNodesNb = 3;

    protected void createEmptyNodeSource(String sourceName) throws Exception {
        //first parameter of im is empty default rm url
        RMTHelper.getResourceManager().createNodeSource(sourceName, GCMInfrastructure.class.getName(),
                new Object[] { "", emptyGCMD }, StaticPolicy.class.getName(), null);

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);
    }

    protected void createNodeSourceWithNodes(String sourceName) throws Exception {
        // creating node source
        // first parameter of im is empty default rm url
        RMTHelper.getResourceManager().createNodeSource(sourceName, GCMInfrastructure.class.getName(),
                new Object[] { "", GCMDeploymentData }, StaticPolicy.class.getName(), null);

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }
        // once added, wait for nodes to be configured
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }

    protected void removeNodeSource(String sourceName) throws Exception {
        // removing node source
        RMTHelper.getResourceManager().removeNodeSource(sourceName, true);

        //wait the n events of the n nodes removals of the node source
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        //wait for the event of the node source removal
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, sourceName);
    }

    protected void init() throws Exception {
        GCMDeploymentData = FileToBytesConverter
                .convertFileToByteArray((new File(defaultDescriptor.toURI())));
        URL emptyNodeDescriptor = TestGCMInfrastructureTimeSlotPolicy.class
                .getResource("/functionaltests/nodesource/empty_gcmd.xml");
        emptyGCMD = FileToBytesConverter.convertFileToByteArray((new File(emptyNodeDescriptor.toURI())));
    }

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        init();
        String source1 = "Node_source_1";
        String source2 = "Node_source_2";

        ResourceManager resourceManager = RMTHelper.getResourceManager();

        RMTHelper.log("Test 1 - creation/removal of empty node source");

        RMTHelper.log("creation");
        createEmptyNodeSource(source1);
        RMTHelper.log("removal");
        resourceManager.removeNodeSource(source1, true);

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, source1);

        RMTHelper.log("Test 2 - creation/removal of the node source with nodes");
        createNodeSourceWithNodes(source1);

        RMState s = resourceManager.getState();
        assertTrue(s.getTotalNodesNumber() == defaultDescriptorNodesNb);
        assertTrue(s.getFreeNodesNumber() == defaultDescriptorNodesNb);
        // releasing some nodes
        NodeSet ns = resourceManager.getAtMostNodes(defaultDescriptorNodesNb, null);

        for (Node n : ns) {
            // eat the freeToBusy event
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            resourceManager.removeNode(n.getNodeInformation().getURL(), true);
            RMTHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, n.getNodeInformation().getURL());
        }

        assertTrue(resourceManager.getState().getTotalNodesNumber() == 0);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);
        resourceManager.removeNodeSource(source1, true);
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, source1);

        RMTHelper.log("Test 3 - several node sources");
        createNodeSourceWithNodes(source1);
        createNodeSourceWithNodes(source2);

        assertTrue(resourceManager.getState().getTotalNodesNumber() == 2 * defaultDescriptorNodesNb);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 2 * defaultDescriptorNodesNb);

        resourceManager.removeNodeSource(source1, true);
        //wait the n events of the n nodes removals of the node source
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        //wait for the event of the node source removal
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, source1);

        resourceManager.removeNodeSource(source2, true);
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        //wait for the event of the node source removal
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, source2);

    }
}
