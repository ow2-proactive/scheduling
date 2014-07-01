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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.resourcemanager.nodesource.policy.ReleaseResourcesWhenSchedulerIdle;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.tests.FunctionalTest;
import org.junit.Ignore;

import functionaltests.RMTHelper;
import functionaltests.SchedulerTHelper;

import static org.junit.Assert.assertTrue;


/**
 *
 * Test checks the correct behavior of node source consisted of GCM infrastructure manager
 * and ReleaseResourcesWhenSchedulerIdle acquisition policy.
 *
 */
@Ignore
public class TestGCMInfrastructureReleaseWhenIdlePolicy extends FunctionalTest {

    protected byte[] GCMDeploymentData;

    protected int defaultDescriptorNodesNb = 5;
    protected static URL jobDescriptor = TestGCMInfrastructureReleaseWhenIdlePolicy.class
            .getResource("/functionaltests/descriptors/Job_PI.xml");

    private RMTHelper helper = RMTHelper.getDefaultInstance();

    protected Object[] getPolicyParams() throws Exception {
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(SchedulerTHelper.schedulerUrl);
        Credentials creds = Credentials.createCredentials(new CredData(SchedulerTHelper.admin_username,
            SchedulerTHelper.admin_password), auth.getPublicKey());
        return new Object[] { "ALL", "ME", SchedulerTHelper.schedulerUrl, creds.getBase64(), "30000" };
    }

    protected String getDescriptor() throws URISyntaxException {
        return new File(TestGCMInfrastructureReleaseWhenIdlePolicy.class.getResource(
                "/functionaltests/nodesource/5nodes.xml").toURI()).getAbsolutePath();
    }

    protected void createDefaultNodeSource(String sourceName) throws Exception {
        // creating node source
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        helper.getResourceManager().createNodeSource(sourceName, LocalInfrastructure.class.getName(),
                new Object[] { creds, defaultDescriptorNodesNb, RMTHelper.defaultNodesTimeout, "" },
                ReleaseResourcesWhenSchedulerIdle.class.getName(), getPolicyParams());

        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);
    }

    protected void removeNodeSource(String sourceName) throws Exception {
        // removing node source
        helper.getResourceManager().removeNodeSource(sourceName, true);

        //wait for the event of the node source removal
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, sourceName);
    }

    protected void init() throws Exception {
        RMFactory.setOsJavaProperty();
        GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(getDescriptor())));
        SchedulerTHelper.startSchedulerWithEmptyResourceManager();
        helper.getResourceManager(null, RMTHelper.defaultUserName, RMTHelper.defaultUserPassword);
    }

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        init();

        String source1 = "Node_source_1";
        SchedulerTHelper.log("Test 1");

        ResourceManager resourceManager = helper.getResourceManager();

        createDefaultNodeSource(source1);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == 0);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);

        System.out.println("TestGCMInfrastructureReleaseWhenIdlePolicy.action() Submit job ");
        JobId jobId = SchedulerTHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());
        System.out.println("TestGCMInfrastructureReleaseWhenIdlePolicy.action() job submitted ");

        SchedulerTHelper.waitForEventJobSubmitted(jobId);
        System.out.println("TestGCMInfrastructureReleaseWhenIdlePolicy.action() Submitteeddddd");

        // waiting for acquiring nodes
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        // nodes should be removed in 30 secs after job completion
        for (int i = 0; i < defaultDescriptorNodesNb; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        SchedulerTHelper.waitForFinishedJob(jobId);
        removeNodeSource(source1);
    }

}
