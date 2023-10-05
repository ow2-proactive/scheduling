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
package functionaltests.job;

import static functionaltests.utils.SchedulerTHelper.log;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.task.TaskIdImpl;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * Checking that getTaskState and getJobState return generic info that use variable propagation
 */
public class TestGetTaskStateDynamicGenericInfo extends SchedulerFunctionalTestNoRestart {

    private static URL simpleJob = TestGetTaskStateDynamicGenericInfo.class.getResource("/functionaltests/descriptors/Job_GI_using_propagated_var.xml");

    @Test
    public void testGenericInfoWithPropagatedVariable() throws Throwable {

        JobId id = schedulerHelper.submitJob(new File(simpleJob.toURI()).getAbsolutePath());
        log("Job submitted, id " + id.toString());
        schedulerHelper.waitForEventJobSubmitted(id);
        schedulerHelper.waitForEventTaskRunning(id, "Task_GI");
        TaskState taskState = schedulerHelper.getSchedulerInterface().getTaskState(id, "Task_GI");
        String genericInfoValue = taskState.getRuntimeGenericInformation().get("MY_GI");
        Assert.assertNotNull(genericInfoValue);
        Assert.assertEquals("value", genericInfoValue);
        JobState jobState = schedulerHelper.getSchedulerInterface().getJobState(id);
        genericInfoValue = jobState.getHMTasks()
                                   .get(TaskIdImpl.makeTaskId(id.toString() + "t1"))
                                   .getRuntimeGenericInformation()
                                   .get("MY_GI");
        Assert.assertNotNull(genericInfoValue);
        Assert.assertEquals("value", genericInfoValue);
        schedulerHelper.waitForEventJobFinished(id);
    }
}
