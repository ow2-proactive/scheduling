/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests.rm.nodesource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.After;
import org.junit.Test;
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

import functionaltests.utils.RMTHelper;
import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * Checking that job with token is deployed on nodes with the corresponding token.
 * 
 */
public class TestJobNodeAccessToken extends SchedulerFunctionalTestNoRestart {

    private static URL simpleJob = TestJobNodeAccessToken.class.getResource("/functionaltests/descriptors/Job_simple.xml");

    private static URL simpleJobWithToken = TestJobNodeAccessToken.class.getResource("/functionaltests/descriptors/Job_simple_with_token.xml");

    private boolean nsCreated = false;

    String nsName = "NodeSourceWithToken";

    ResourceManager rm;

    @Test
    public void testJobNodeAccessToken() throws Throwable {

        JobId id = schedulerHelper.submitJob(new File(simpleJob.toURI()).getAbsolutePath());
        SchedulerTHelper.log("Job submitted, id " + id.toString());
        schedulerHelper.waitForEventJobSubmitted(id);

        JobId id2 = schedulerHelper.submitJob(new File(simpleJobWithToken.toURI()).getAbsolutePath());
        SchedulerTHelper.log("Job submitted, id " + id2.toString());
        schedulerHelper.waitForEventJobSubmitted(id2);

        schedulerHelper.waitForEventJobFinished(id);

        JobState js = schedulerHelper.getSchedulerInterface().getJobState(id2);
        assertEquals(JobStatus.PENDING, js.getStatus());

        // adding node with the token "test_token"
        rm = schedulerHelper.getResourceManager();
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));

        String nsProps = "-D" + RMNodeStarter.NODE_ACCESS_TOKEN + "=test_token";
        assertTrue(rm.createNodeSource(nsName,
                                       LocalInfrastructure.class.getName(),
                                       new Object[] { creds, 1, RMTHelper.DEFAULT_NODES_TIMEOUT, nsProps },
                                       StaticPolicy.class.getName(),
                                       null)
                     .getBooleanValue());

        nsCreated = true;

        // ns created
        schedulerHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);
        // deploying
        schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        // from deploying 
        schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        // to configuring
        schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        // free
        schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        schedulerHelper.waitForEventJobFinished(id2);

    }

    @After
    public void cleanUp() {
        if (nsCreated) {
            rm.removeNodeSource(nsName, false);
        }
    }
}
