/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package nodesource;

import static junit.framework.Assert.assertTrue;

import java.io.File;

import nodestate.FunctionalTDefaultRM;
import nodestate.RMEventReceiver;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.NodeSet;


/**
 *
 * Test checks the correct behavior of node source consisted of GCM infrastructure manager
 * and static acquisition policy.
 *
 */
public class TestGCMInfrastructureStaticPolicy extends FunctionalTDefaultRM {

    protected byte[] GCMDeploymentData;
    protected RMEventReceiver receiver;

    protected void createEmptyNodeSource(String sourceName) throws Exception {
        admin.createNodesource(sourceName, GCMInfrastructure.class.getName(), null, StaticPolicy.class
                .getName(), null);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodeSourcesCreatedEvents().size() == 1);
    }

    protected void createDefaultNodeSource(String sourceName) throws Exception {
        // creating node source
        admin.createNodesource(sourceName, GCMInfrastructure.class.getName(),
                new Object[] { GCMDeploymentData }, StaticPolicy.class.getName(), null);

        receiver.waitForNEvent(defaultDescriptorNodesNb + 1);
        assertTrue(receiver.cleanNgetNodeSourcesCreatedEvents().size() == 1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == defaultDescriptorNodesNb);
    }

    protected void removeNodeSource(String sourceName) throws Exception {
        // removing node source
        admin.removeSource(sourceName, true);
        //wait the n events of the n nodes removals of the node source
        receiver.waitForNEvent(defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == defaultDescriptorNodesNb);

        //wait for the event of the node source removal
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodeSourcesRemovedEvents().size() == 1);
    }

    protected void init() throws Exception {
        GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(defaultDescriptor)));
    }

    protected void addNodes(String sourceName) throws Exception {
        admin.addNodes(sourceName, new Object[] { GCMDeploymentData });
        // waiting for adding nodes acquisition info event
        receiver.waitForNEvent(1);
    }

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {
        RMFactory.setOsJavaProperty();
        init();

        RMEventType[] eventsList = { RMEventType.NODE_ADDED, RMEventType.NODE_BUSY, RMEventType.NODE_DOWN,
                RMEventType.NODE_FREE, RMEventType.NODE_REMOVED, RMEventType.NODE_TO_RELEASE,
                RMEventType.NODESOURCE_CREATED, RMEventType.NODESOURCE_REMOVED };

        receiver = (RMEventReceiver) PAActiveObject.newActive(RMEventReceiver.class.getName(), new Object[] {
                monitor, eventsList });

        receiver.cleanEventLists();
        String source1 = "Node source 1";
        String source2 = "Node source 2";

        log("Test 1 - creation/removal of empty node source");

        createEmptyNodeSource(source1);
        admin.removeSource(source1, true);
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodeSourcesRemovedEvents().size() == 1);

        log("Test 2 - creation/removal of the node source with nodes");
        createDefaultNodeSource(source1);
        removeNodeSource(source1);

        log("Test 3 - adding nodes to existing node source");
        createDefaultNodeSource(source1);

        // adding nodes
        addNodes(source1);
        receiver.waitForNEvent(defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == defaultDescriptorNodesNb);
        assertTrue(admin.getTotalNodesNumber().intValue() == 2 * defaultDescriptorNodesNb);
        assertTrue(admin.getFreeNodesNumber().intValue() == 2 * defaultDescriptorNodesNb);

        // releasing some nodes
        NodeSet ns = admin.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb), null);
        receiver.waitForNEvent(defaultDescriptorNodesNb);
        for (Node n : ns)
            admin.removeNode(n.getNodeInformation().getURL(), true, false);

        receiver.waitForNEvent(defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == defaultDescriptorNodesNb);
        assertTrue(admin.getTotalNodesNumber().intValue() == defaultDescriptorNodesNb);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);

        removeNodeSource(source1);

        log("Test 4 - adding nodes to non existing node source");
        try {
            addNodes(source1);
            assertTrue(false);
        } catch (Exception e) {
            System.out.println("Expected exception");
        }

        log("Test 5 - several node sources");
        createDefaultNodeSource(source1);
        createDefaultNodeSource(source2);

        addNodes(source1);
        receiver.waitForNEvent(defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == defaultDescriptorNodesNb);
        assertTrue(admin.getTotalNodesNumber().intValue() == 3 * defaultDescriptorNodesNb);
        assertTrue(admin.getFreeNodesNumber().intValue() == 3 * defaultDescriptorNodesNb);

        addNodes(source2);
        receiver.waitForNEvent(defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == defaultDescriptorNodesNb);
        assertTrue(admin.getTotalNodesNumber().intValue() == 4 * defaultDescriptorNodesNb);
        assertTrue(admin.getFreeNodesNumber().intValue() == 4 * defaultDescriptorNodesNb);

        admin.removeSource(source1, true);
        //wait the n events of the n nodes removals of the node source
        receiver.waitForNEvent(2 * defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 2 * defaultDescriptorNodesNb);

        //wait for the event of the node source removal
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodeSourcesRemovedEvents().size() == 1);

        admin.removeSource(source2, true);
        receiver.waitForNEvent(2 * defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 2 * defaultDescriptorNodesNb);

        //wait for the event of the node source removal
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodeSourcesRemovedEvents().size() == 1);
    }
}
