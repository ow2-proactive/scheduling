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
package functionaltests;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 * Checking that job with token is deployed on nodes with the corresponding token.
 * 
 */
public class TestJobNodeAccess extends SchedulerConsecutive {

    private static URL simpleJob = TestJobNodeAccess.class
            .getResource("/functionaltests/descriptors/Job_simple.xml");
    private static URL simpleJobWithToken = TestJobNodeAccess.class
            .getResource("/functionaltests/descriptors/Job_simple_with_token.xml");

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {

        JobId id = SchedulerTHelper.submitJob(new File(simpleJob.toURI()).getAbsolutePath(),
                ExecutionMode.normal);
        SchedulerTHelper.log("Job submitted, id " + id.toString());
        SchedulerTHelper.waitForEventJobSubmitted(id);

        JobId id2 = SchedulerTHelper.submitJob(new File(simpleJobWithToken.toURI()).getAbsolutePath(),
                ExecutionMode.normal);
        SchedulerTHelper.log("Job submitted, id " + id2.toString());
        SchedulerTHelper.waitForEventJobSubmitted(id2);

        SchedulerTHelper.waitForEventJobFinished(id);
        Thread.sleep(10000);

        JobState js = SchedulerTHelper.getSchedulerInterface().getJobState(id2);
        Assert.assertEquals(JobStatus.PENDING, js.getStatus());

        // adding node with the token "test_token"
        ResourceManager rm = RMTHelper.getDefaultInstance().getResourceManager();
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties
                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
        String nsName = "NodeSourceWithToken";
        rm.createNodeSource(nsName, LocalInfrastructure.class.getName(), new Object[] { "", creds, 1,
                RMTHelper.defaultNodesTimeout, "-D" + RMNodeStarter.NODE_ACCESS_TOKEN + "=test_token" },
                StaticPolicy.class.getName(), null);

        // ns created
        RMTHelper.getDefaultInstance().waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);
        // deploying
        RMTHelper.getDefaultInstance().waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        // from deploying 
        RMTHelper.getDefaultInstance().waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        // to configuring
        RMTHelper.getDefaultInstance().waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        // free
        RMTHelper.getDefaultInstance().waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        SchedulerTHelper.waitForEventJobFinished(id2);
    }
}
