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
package functionaltests.workflow.startat;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.CommonAttribute;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.examples.EmptyTask;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.SchedulerTHelper;


public class TestStartAt extends SchedulerFunctionalTestNoRestart {

    private final String JOB_NAME = this.getClass().getSimpleName();

    private final String FUTURE_START_AT = "3000-01-01T00:00:00+00:00";

    private final String OLD_START_AT = "1970-01-01T00:00:00+00:00";

    @Test
    public void testChangeStartAt() throws Throwable {

        JobId jobId = schedulerHelper.submitJob(createJob());
        SchedulerTHelper.log("Job submitted, id " + jobId.toString());
        JobState js = schedulerHelper.waitForEventJobSubmitted(jobId);

        Assert.assertThat(js.getJobInfo().getStartAt(), Matchers.greaterThan(System.currentTimeMillis()));

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        scheduler.changeStartAt(jobId, OLD_START_AT);

        js = scheduler.getJobState(jobId);

        Assert.assertThat(js.getJobInfo().getStartAt(), Matchers.lessThan(System.currentTimeMillis()));
        Assert.assertEquals(js.getJobInfo().getGenericInformation().get(CommonAttribute.GENERIC_INFO_START_AT_KEY),
                            OLD_START_AT);
        schedulerHelper.waitForEventJobFinished(jobId);
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(JOB_NAME);
        setStartAt(job);

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(EmptyTask.class.getName());
        String TASK_NAME = "task name";
        javaTask.setName(TASK_NAME);
        job.addTask(javaTask);

        return job;
    }

    private void setStartAt(TaskFlowJob job) {
        job.addGenericInformation(CommonAttribute.GENERIC_INFO_START_AT_KEY, FUTURE_START_AT);
    }

}
