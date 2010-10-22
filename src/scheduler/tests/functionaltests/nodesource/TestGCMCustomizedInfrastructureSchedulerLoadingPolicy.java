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
package functionaltests.nodesource;

import static junit.framework.Assert.assertTrue;

import java.net.InetAddress;

import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMCustomisedInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMInfrastructure;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.resourcemanager.nodesource.policy.SchedulerLoadingPolicy;

import functionaltests.RMTHelper;
import functionaltests.SchedulerTHelper;


/**
 *
 * Test checks the correct behavior of node source consisted of customized GCM infrastructure manager
 * and SchedulerLoadingPolicy acquisition policy.
 *
 */
public class TestGCMCustomizedInfrastructureSchedulerLoadingPolicy extends
        TestGCMCustomizedInfrastructureReleaseWhenIdlePolicy {

    @Override
    protected Object[] getPolicyParams() throws Exception {
        SchedulerAuthenticationInterface auth = SchedulerConnection
                .join(SchedulerTHelper.schedulerDefaultURL);
        Credentials creds = Credentials.createCredentials(new CredData(SchedulerTHelper.username,
            SchedulerTHelper.password), auth.getPublicKey());
        return new Object[] { "ALL", "ME", SchedulerTHelper.schedulerDefaultURL, creds.getBase64(), "2000",// policy period
                "0", // min modes
                "1", // max modes
                "1", // nodes per task
                "1000" // releasing period
        };
    }

    @Override
    protected String getDescriptor() {
        return TestGCMInfrastructureReleaseWhenIdlePolicy.class.getResource(
                "/functionaltests/nodesource/1node.xml").getPath();
    }

    @Override
    protected void createDefaultNodeSource(String sourceName) throws Exception {

        byte[] hosts = InetAddress.getLocalHost().getHostName().getBytes();
        // creating node source
        RMTHelper.getResourceManager().createNodeSource(sourceName,
                GCMCustomisedInfrastructure.class.getName(), new Object[] { GCMDeploymentData, hosts },
                SchedulerLoadingPolicy.class.getName(), getPolicyParams());

        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, sourceName);
    }

    @Override
    @org.junit.Test
    public void action() throws Exception {

        init();

        ResourceManager resourceManager = RMTHelper.getResourceManager();

        String source1 = "Node source 1";

        SchedulerTHelper.log("Test 1");
        createDefaultNodeSource(source1);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == 0);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);

        JobId jobId = SchedulerTHelper.testJobSubmission(jobDescriptor);

        // waiting for acquiring nodes
        RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        // nodes should be removed after scheduler becomes idle
        RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

        SchedulerTHelper.waitForFinishedJob(jobId);

        SchedulerTHelper.log("Test 2");
        try {
            // incorrect infrastructure for the policy
            resourceManager.createNodeSource(source1, GCMInfrastructure.class.getName(),
                    new Object[] { GCMDeploymentData }, SchedulerLoadingPolicy.class.getName(),
                    getPolicyParams()).booleanValue();
            assertTrue(false);
        } catch (Exception e) {
        }
    }

}
