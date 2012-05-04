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
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.NodeSet;

import org.ow2.tests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 *
 * Test checks the correct behavior of node source consisted of Local infrastructure manager
 * and static acquisition policy.
 *
 */
public class TestLocalInfrastructureStaticPolicy extends FunctionalTest {

    protected int defaultDescriptorNodesNb = 3;

    private RMTHelper helper = RMTHelper.getDefaultInstance();

    protected void createEmptyNodeSource(String sourceName) throws Exception {
        //first parameter of im is empty default rm url
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        helper.getResourceManager().createNodeSource(sourceName, LocalInfrastructure.class.getName(),
                new Object[] { "", creds, 0, RMTHelper.defaultNodesTimeout, "" },
                StaticPolicy.class.getName(), null);

        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);
    }

    protected void createNodeSourceWithNodes(String sourceName) throws Exception {
        RMTHelper helper = RMTHelper.getDefaultInstance();

        // creating node source
        // first parameter of im is empty default rm url
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        helper.getResourceManager().createNodeSource(sourceName, LocalInfrastructure.class.getName(),
                new Object[] { "", creds, defaultDescriptorNodesNb, RMTHelper.defaultNodesTimeout, "" },
                StaticPolicy.class.getName(), null);

        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }
        // once added, wait for nodes to be configured
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }

    protected void removeNodeSource(String sourceName) throws Exception {
        // removing node source
        helper.getResourceManager().removeNodeSource(sourceName, true);

        //wait the n events of the n nodes removals of the node source
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        //wait for the event of the node source removal
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, sourceName);
    }

    protected void init() throws Exception {
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

        RMTHelper helper = RMTHelper.getDefaultInstance();

        ResourceManager resourceManager = helper.getResourceManager();

        RMTHelper.log("Test 1 - creation/removal of empty node source");

        RMTHelper.log("creation");
        createEmptyNodeSource(source1);
        RMTHelper.log("removal");
        resourceManager.removeNodeSource(source1, true);

        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, source1);

        RMTHelper.log("Test 2 - creation/removal of the node source with nodes");
        createNodeSourceWithNodes(source1);

        RMState s = resourceManager.getState();
        assertTrue(s.getTotalNodesNumber() == defaultDescriptorNodesNb);
        assertTrue(s.getFreeNodesNumber() == defaultDescriptorNodesNb);
        // releasing some nodes
        NodeSet ns = resourceManager.getAtMostNodes(defaultDescriptorNodesNb, null);

        for (Node n : ns) {
            // eat the freeToBusy event
            helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            resourceManager.removeNode(n.getNodeInformation().getURL(), true);
            helper.waitForNodeEvent(RMEventType.NODE_REMOVED, n.getNodeInformation().getURL());
        }

        assertTrue(resourceManager.getState().getTotalNodesNumber() == 0);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);
        resourceManager.removeNodeSource(source1, true);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, source1);

        RMTHelper.log("Test 3 - several node sources");
        createNodeSourceWithNodes(source1);
        createNodeSourceWithNodes(source2);

        assertTrue(resourceManager.getState().getTotalNodesNumber() == 2 * defaultDescriptorNodesNb);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 2 * defaultDescriptorNodesNb);

        resourceManager.removeNodeSource(source1, true);
        //wait the n events of the n nodes removals of the node source
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        //wait for the event of the node source removal
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, source1);

        resourceManager.removeNodeSource(source2, true);
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        //wait for the event of the node source removal
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, source2);

    }
}
