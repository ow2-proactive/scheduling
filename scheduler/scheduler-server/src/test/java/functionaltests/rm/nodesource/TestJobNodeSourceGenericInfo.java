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

import static functionaltests.nodesrecovery.RecoverInfrastructureTestHelper.NODES_RECOVERABLE;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.utils.FileToBytesConverter;

import com.google.common.collect.ImmutableMap;

import functionaltests.utils.RMTHelper;
import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * Checking that a job with node source generic info
 * is deployed on nodes within the corresponding node source.
 * 
 */
public class TestJobNodeSourceGenericInfo extends SchedulerFunctionalTestNoRestart {

    private static URL simpleJob = TestJobNodeAccessToken.class.getResource("/functionaltests/descriptors/Job_simple.xml");

    private boolean nsCreated = false;

    String nsName = "NodeSourceWithGI";

    ResourceManager rm;

    @Test
    public void testJobNodeSourceGenericInfo() throws Throwable {

        // adding node source
        rm = schedulerHelper.getResourceManager();
        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));

        assertTrue(rm.createNodeSource(nsName,
                                       LocalInfrastructure.class.getName(),
                                       new Object[] { creds, 2, RMTHelper.DEFAULT_NODES_TIMEOUT, "" },
                                       StaticPolicy.class.getName(),
                                       null,
                                       NODES_RECOVERABLE)
                     .getBooleanValue());

        nsCreated = true;

        // ns created
        schedulerHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nsName);

        Map<String, String> genericInfo = ImmutableMap.of(SchedulerConstants.NODE_SOURCE_GENERIC_INFO, nsName);

        JobId id = schedulerHelper.submitJobWithGI(new File(simpleJob.toURI()).getAbsolutePath(), genericInfo);
        SchedulerTHelper.log("Job submitted, id " + id.toString());
        schedulerHelper.waitForEventJobSubmitted(id);
        schedulerHelper.waitForEventJobFinished(id);
        TaskState state1 = schedulerHelper.getSchedulerInterface().getTaskState(id, "task1");
        Assert.assertTrue("Execution must contain the node source name",
                          state1.getExecutionHostName().contains(nsName));
        TaskState state2 = schedulerHelper.getSchedulerInterface().getTaskState(id, "task2");
        Assert.assertTrue("Execution must contain the node source name",
                          state2.getExecutionHostName().contains(nsName));

    }

    @After
    public void cleanUp() {
        if (nsCreated) {
            rm.removeNodeSource(nsName, false);
        }
    }
}
