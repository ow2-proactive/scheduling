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

import java.net.InetAddress;

import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.GCMCustomisedInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.manager.GCMInfrastructure;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.resourcemanager.nodesource.policy.SchedulerLoadingPolicy;

import functionaltests.SchedulerTHelper;


/**
 *
 * Test checks the correct behavior of node source consisted of customized GCM infrastructure manager
 * and SchedulerLoadingPolicy acquisition policy.
 *
 */
public class TestGCMCustomizedInfrastructureSchedulerLoadingPolicy extends
        TestGCMCustomizedInfrastructureReleaseWhenIdlePolicy {

    protected Object[] getPolicyParams() {
        return new Object[] { SchedulerTHelper.schedulerDefaultURL, SchedulerTHelper.username,
                SchedulerTHelper.password, "true", "0", // min modes
                "1", // max modes
                "1", // nodes per task
                "1000" // releasing period
        };
    }

    protected String getDescriptor() {
        return TestGCMInfrastructureReleaseWhenIdlePolicy.class.getResource(
                "/functionaltests/nodesource/1node.xml").getPath();
    }

    protected void createDefaultNodeSource(String sourceName) throws Exception {

        byte[] hosts = InetAddress.getLocalHost().getHostName().getBytes();
        // creating node source
        admin.createNodesource(sourceName, GCMCustomisedInfrastructure.class.getName(), new Object[] {
                GCMDeploymentData, hosts }, SchedulerLoadingPolicy.class.getName(), getPolicyParams());

        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodeSourcesCreatedEvents().size() == 1);
    }

    @org.junit.Test
    public void action() throws Exception {
        init();

        receiver.cleanEventLists();
        String source1 = "Node source 1";

        SchedulerTHelper.log("Test 1");
        createDefaultNodeSource(source1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 0);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        JobId jobId = SchedulerTHelper.testJobSubmission(jobDescriptor);
        // waiting for acquiring nodes
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);

        // nodes should be removed in 1 sec after job completion
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 1);
        SchedulerTHelper.waitForFinishedJob(jobId);

        SchedulerTHelper.log("Test 2");
        try {
            // incorrect infrastructure for the policy
            admin.createNodesource(source1, GCMInfrastructure.class.getName(),
                    new Object[] { GCMDeploymentData }, SchedulerLoadingPolicy.class.getName(),
                    getPolicyParams());
            assertTrue(false);
        } catch (Exception e) {
        }
    }

}
