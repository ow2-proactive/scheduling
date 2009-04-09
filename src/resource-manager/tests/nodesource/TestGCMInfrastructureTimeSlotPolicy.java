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
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.TimeSlotPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 *
 * Test checks the correct behavior of node source consisted of GCM infrastructure manager
 * and time slot acquisition policy.
 *
 * This test may failed by timeout if the machine is too slow, so gcm deployment takes more than 15 secs
 *
 */
public class TestGCMInfrastructureTimeSlotPolicy extends FunctionalTDefaultRM {

    protected byte[] GCMDeploymentData;
    protected RMEventReceiver receiver;
    protected int descriptorNodeNumber = 1;

    protected Object[] getPolicyParams() {
        return new Object[] { TimeSlotPolicy.dateFormat.format(System.currentTimeMillis()),
                TimeSlotPolicy.dateFormat.format(System.currentTimeMillis() + 15000), "0", "true" };
    }

    protected void init() throws Exception {
        String oneNodeescriptor = FunctionalTDefaultRM.class.getResource("/nodesource/1node.xml").getPath();
        GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(oneNodeescriptor)));
    }

    protected void createEmptyNodeSource(String sourceName) throws Exception {
        admin.createNodesource(sourceName, GCMInfrastructure.class.getName(), null, StaticPolicy.class
                .getName(), null);

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodeSourcesCreatedEvents().size() == 1);
    }

    protected void createDefaultNodeSource(String sourceName) throws Exception {
        // creating node source
        admin.createNodesource(sourceName, GCMInfrastructure.class.getName(),
                new Object[] { GCMDeploymentData }, TimeSlotPolicy.class.getName(), getPolicyParams());

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodeSourcesCreatedEvents().size() == 1);
    }

    protected void removeNodeSource(String sourceName) throws Exception {
        // removing node source
        admin.removeSource(sourceName, true);
        //wait for the event of the node source removal
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodeSourcesRemovedEvents().size() == 1);
    }

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {
        init();
        RMFactory.setOsJavaProperty();

        RMEventType[] eventsList = { RMEventType.NODE_ADDED, RMEventType.NODE_STATE_CHANGED,
                RMEventType.NODE_REMOVED, RMEventType.NODESOURCE_CREATED, RMEventType.NODESOURCE_REMOVED };

        receiver = (RMEventReceiver) PAActiveObject.newActive(RMEventReceiver.class.getName(), new Object[] {
                monitor, eventsList });

        receiver.cleanEventLists();
        String source1 = "Node source 1";

        log("Test 1 - creation/removal of empty node source");
        createEmptyNodeSource(source1);
        removeNodeSource(source1);

        log("Test 2 - creation/removal of the node source with nodes");
        createDefaultNodeSource(source1);

        receiver.waitForNEvent(descriptorNodeNumber);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == descriptorNodeNumber);
        log("Test 2 - nodes will be removed in 15 secs");

        // wait for the nodes release
        receiver.waitForNEvent(descriptorNodeNumber);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == descriptorNodeNumber);

        removeNodeSource(source1);
    }
}
