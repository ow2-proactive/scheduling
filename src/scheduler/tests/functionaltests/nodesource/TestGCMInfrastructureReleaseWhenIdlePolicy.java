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
package functionaltests.nodesource;

import static junit.framework.Assert.assertTrue;

import java.io.File;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.GCMInfrastructure;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.resourcemanager.nodesource.policy.ReleaseResourcesWhenSchedulerIdle;
import org.ow2.proactive.utils.FileToBytesConverter;

import functionalTests.FunctionalTest;
import functionaltests.SchedulerTHelper;


/**
 *
 * Test checks the correct behavior of node source consisted of GCM infrastructure manager
 * and ReleaseResourcesWhenSchedulerIdle acquisition policy.
 *
 */
public class TestGCMInfrastructureReleaseWhenIdlePolicy extends FunctionalTest {

    protected byte[] GCMDeploymentData;
    protected RMCustomizedEventReceiver receiver;
    protected RMAdmin admin;

    protected int defaultDescriptorNodesNb = 5;
    protected static String jobDescriptor = TestGCMInfrastructureReleaseWhenIdlePolicy.class.getResource(
            "/functionaltests/descriptors/Job_PI.xml").getPath();

    protected Object[] getPolicyParams() {
        return new Object[] { SchedulerTHelper.schedulerDefaultURL, SchedulerTHelper.username,
                SchedulerTHelper.password, "true", "30000" };
    }

    protected String getDescriptor() {
        return TestGCMInfrastructureReleaseWhenIdlePolicy.class.getResource(
                "/functionaltests/nodesource/5nodes.xml").getPath();
    }

    protected void createDefaultNodeSource(String sourceName) throws Exception {
        // creating node source
        admin.createNodesource(sourceName, GCMInfrastructure.class.getName(),
                new Object[] { GCMDeploymentData }, ReleaseResourcesWhenSchedulerIdle.class.getName(),
                getPolicyParams());

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

    protected void init() throws Exception {
        RMFactory.setOsJavaProperty();

        GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(getDescriptor())));
        SchedulerTHelper.startSchedulerWithEmptyResourceManager();
        RMAuthentication auth = RMConnection.waitAndJoin(null);
        admin = auth.logAsAdmin(SchedulerTHelper.username, SchedulerTHelper.password);
        RMEventType[] eventsList = { RMEventType.NODE_ADDED, RMEventType.NODE_REMOVED,
                RMEventType.NODESOURCE_CREATED, RMEventType.NODESOURCE_REMOVED };

        RMMonitoring monitor = auth.logAsMonitor();
        receiver = (RMCustomizedEventReceiver) PAActiveObject.newActive(RMCustomizedEventReceiver.class
                .getName(), new Object[] { eventsList });
        PAFuture.waitFor(monitor.addRMEventListener(receiver, eventsList));
    }

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {
        init();

        receiver.cleanEventLists();
        String source1 = "Node source 1";

        SchedulerTHelper.log("Test 1");
        createDefaultNodeSource(source1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 0);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        JobId jobId = SchedulerTHelper.submitJob(jobDescriptor);
        // waiting for acquiring nodes
        receiver.waitForNEvent(defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == defaultDescriptorNodesNb);

        // nodes should be removed in 30 secs after job completion
        receiver.waitForNEvent(defaultDescriptorNodesNb);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == defaultDescriptorNodesNb);
        SchedulerTHelper.waitForFinishedJob(jobId);

        removeNodeSource(source1);
    }

}
