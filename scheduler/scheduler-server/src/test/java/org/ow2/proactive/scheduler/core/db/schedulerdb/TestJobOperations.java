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

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.db.SchedulerStateRecoverHelper;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;


public class TestJobOperations extends BaseSchedulerDBTest {

    @Test
    public void testLoadJobWithTasksIfNotRemoved() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();
        jobDef.addTask(createDefaultTask("task1"));
        jobDef.addTask(createDefaultTask("task2"));
        jobDef.addTask(createDefaultTask("task3"));
        jobDef.setPriority(JobPriority.HIGHEST);

        InternalJob job = defaultSubmitJob(jobDef, "user1");

        job = dbManager.loadJobWithTasksIfNotRemoved(job.getId());
        Assert.assertEquals(3, job.getTasks().size());
        Assert.assertEquals("user1", job.getOwner());
        Assert.assertEquals(3, job.getJobInfo().getTotalNumberOfTasks());
        Assert.assertEquals(0, job.getJobInfo().getNumberOfFinishedTasks());
        Assert.assertEquals(0, job.getJobInfo().getNumberOfRunningTasks());
        Assert.assertEquals(0, job.getJobInfo().getNumberOfPendingTasks());
        Assert.assertEquals(JobStatus.PENDING, job.getJobInfo().getStatus());
        Assert.assertEquals(JobPriority.HIGHEST, job.getJobInfo().getPriority());

        dbManager.removeJob(job.getId(), System.currentTimeMillis(), false);
        Assert.assertNull(dbManager.loadJobWithTasksIfNotRemoved(job.getId()));

        Assert.assertNull(dbManager.loadJobWithTasksIfNotRemoved(JobIdImpl.makeJobId("123456789")));
    }

    @Test
    public void testPause() throws Exception {
        InternalJob job = defaultSubmitJobAndLoadInternal(false, new TaskFlowJob());
        Assert.assertEquals(JobStatus.PENDING, job.getStatus());

        job.setPaused();
        dbManager.updateJobAndTasksState(job);

        job = recoverJob();

        Assert.assertEquals(JobStatus.PAUSED, job.getStatus());
        Assert.assertEquals(TaskStatus.PAUSED, job.getTasks().get(0).getStatus());
    }

    @Test
    public void testSetToBeRemoved() throws Exception {
        InternalJob job = defaultSubmitJobAndLoadInternal(false, new TaskFlowJob());
        Assert.assertFalse(job.isToBeRemoved());

        job.setToBeRemoved();
        System.out.println("Set job to be removed");
        dbManager.jobSetToBeRemoved(job.getId());

        job = recoverJob();

        Assert.assertTrue(job.isToBeRemoved());
    }

    private InternalJob recoverJob() {
        SchedulerStateRecoverHelper recoverHelper = new SchedulerStateRecoverHelper(dbManager);
        Collection<InternalJob> jobs = recoverHelper.recover(-1).getPendingJobs();
        Assert.assertEquals(1, jobs.size());
        return jobs.iterator().next();
    }
}
