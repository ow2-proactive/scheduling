/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package functionaltests.nodesource;

import static junit.framework.Assert.assertTrue;

import java.io.File;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMInfrastructure;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.resourcemanager.nodesource.policy.ReleaseResourcesWhenSchedulerIdle;
import org.ow2.proactive.utils.FileToBytesConverter;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;
import functionaltests.SchedulerTHelper;


/**
 *
 * Test checks the correct behavior of node source consisted of GCM infrastructure manager
 * and ReleaseResourcesWhenSchedulerIdle acquisition policy.
 *
 */
public class TestGCMInfrastructureReleaseWhenIdlePolicy extends FunctionalTest {

    protected byte[] GCMDeploymentData;

    protected int defaultDescriptorNodesNb = 5;
    protected static String jobDescriptor = TestGCMInfrastructureReleaseWhenIdlePolicy.class.getResource(
            "/functionaltests/descriptors/Job_PI.xml").getPath();

    protected Object[] getPolicyParams() throws Exception {
        SchedulerAuthenticationInterface auth = SchedulerConnection
                .join(SchedulerTHelper.schedulerDefaultURL);
        Credentials creds = Credentials.createCredentials(SchedulerTHelper.username,
                SchedulerTHelper.password, auth.getPublicKey());
        return new Object[] { SchedulerTHelper.schedulerDefaultURL, creds.getBase64(), "true", "30000" };
    }

    protected String getDescriptor() {
        return TestGCMInfrastructureReleaseWhenIdlePolicy.class.getResource(
                "/functionaltests/nodesource/5nodes.xml").getPath();
    }

    protected void createDefaultNodeSource(String sourceName) throws Exception {
        // creating node source
        RMTHelper.getAdminInterface().createNodesource(sourceName, GCMInfrastructure.class.getName(),
                new Object[] { GCMDeploymentData }, ReleaseResourcesWhenSchedulerIdle.class.getName(),
                getPolicyParams());

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);
    }

    protected void removeNodeSource(String sourceName) throws Exception {
        // removing node source
        RMTHelper.getAdminInterface().removeSource(sourceName, true);

        //wait for the event of the node source removal
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, sourceName);
    }

    protected void init() throws Exception {
        RMFactory.setOsJavaProperty();
        GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(getDescriptor())));
        SchedulerTHelper.startSchedulerWithEmptyResourceManager();
        RMTHelper.connectToExistingRM();
    }

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        init();

        String source1 = "Node source 1";
        SchedulerTHelper.log("Test 1");

        RMAdmin admin = RMTHelper.getAdminInterface();

        createDefaultNodeSource(source1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 0);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        System.out.println("TestGCMInfrastructureReleaseWhenIdlePolicy.action() Submit job ");
        JobId jobId = SchedulerTHelper.submitJob(jobDescriptor);
        System.out.println("TestGCMInfrastructureReleaseWhenIdlePolicy.action() job submitted ");

        SchedulerTHelper.waitForEventJobSubmitted(jobId);
        System.out.println("TestGCMInfrastructureReleaseWhenIdlePolicy.action() Submitteeddddd");

        // waiting for acquiring nodes
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        // nodes should be removed in 30 secs after job completion
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        SchedulerTHelper.waitForFinishedJob(jobId);
        removeNodeSource(source1);
    }

}
