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
package org.ow2.proactive.scheduler.core.db.schedulerdb;

import static org.hamcrest.CoreMatchers.*;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestJobRuntimeData extends BaseSchedulerDBTest {

    @Test
    public void testChangePriority() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setPriority(JobPriority.LOW);
        InternalJob jobData = defaultSubmitJobAndLoadInternal(false, new TaskFlowJob());

        dbManager.changeJobPriority(jobData.getId(), JobPriority.HIGH);

        jobData = loadInternalJob(false, jobData.getId());
        Assert.assertEquals(JobPriority.HIGH, jobData.getPriority());
    }

    @Test
    public void testJobRuntimeData() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        job.addTask(createDefaultTask("task1"));
        job.setPriority(JobPriority.LOW);

        System.out.println("Submit and load job");
        InternalJob runtimeData = defaultSubmitJobAndLoadInternal(true, job);
        Assert.assertEquals(JobStatus.PENDING, runtimeData.getStatus());
        Assert.assertEquals(DEFAULT_USER_NAME, runtimeData.getOwner());
        Assert.assertEquals(JobPriority.LOW, runtimeData.getPriority());
        Assert.assertEquals(0, runtimeData.getNumberOfPendingTasks());
        Assert.assertEquals(1, runtimeData.getTotalNumberOfTasks());
        Assert.assertEquals(0, runtimeData.getNumberOfFinishedTasks());
        Assert.assertEquals(0, runtimeData.getNumberOfRunningTasks());
        Assert.assertEquals(this.getClass().getSimpleName(), runtimeData.getName());
        Assert.assertNotNull(runtimeData.getCredentials());
        Assert.assertEquals(1, runtimeData.getITasks().size());

        runtimeData.start();
        InternalTask internalTask = startTask(runtimeData, runtimeData.getITasks().get(0));

        System.out.println("Update started task data");
        dbManager.jobTaskStarted(runtimeData, internalTask, false);

        System.out.println("Load internal job");
        runtimeData = loadInternalJob(true, runtimeData.getId());

        Assert.assertEquals(this.getClass().getSimpleName(), runtimeData.getJobInfo().getJobId().getReadableName());
        Assert.assertEquals(JobStatus.RUNNING, runtimeData.getStatus());
        Assert.assertEquals(1, runtimeData.getNumberOfRunningTasks());
        Assert.assertEquals(0, runtimeData.getNumberOfFinishedTasks());
        Assert.assertEquals(0, runtimeData.getNumberOfPendingTasks());
        Assert.assertTrue(runtimeData.getStartTime() > 0);

        internalTask = runtimeData.getITasks().get(0);
        Assert.assertEquals(TaskStatus.RUNNING, internalTask.getStatus());
        Assert.assertTrue(internalTask.getStartTime() > 0);
        Assert.assertNotNull(internalTask.getExecutionHostName());
    }

    @Test
    public void submitAndLoadJobContent() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        job.addTask(createDefaultTask("task1"));
        job.setPriority(JobPriority.LOW);

        InternalJob runtimeData = defaultSubmitJobAndLoadInternal(true, job);
        Job content = dbManager.loadInitalJobContent(runtimeData.getId());

        Assert.assertThat(content.getName(), is(job.getName()));
        Assert.assertThat(content.getPriority(), is(JobPriority.LOW));
        Assert.assertTrue(content instanceof TaskFlowJob);
        Assert.assertThat(((TaskFlowJob) content).getTasks().size(), is(1));
    }

}
