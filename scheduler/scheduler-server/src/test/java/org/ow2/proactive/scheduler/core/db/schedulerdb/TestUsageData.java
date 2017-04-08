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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.common.usage.TaskUsage;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestUsageData extends BaseSchedulerDBTest {

    private static final String USER_WITH_JOBS = "bob";

    private static final String USER_WITHOUT_JOBS = "albert";

    @Test
    public void testEmptyDatabase() throws Exception {
        List<JobUsage> usages = dbManager.getUsage(USER_WITH_JOBS, new Date(), new Date());
        assertTrue(usages.isEmpty());
    }

    @Test(expected = DatabaseManagerException.class)
    public void testNullDatesDatabase() throws Exception {
        dbManager.getUsage(USER_WITH_JOBS, null, null);
    }

    @Test
    public void testNonEmptyDatabase() throws Exception {
        Date beforeJobExecution = new Date();

        InternalJob job = defaultSubmitJob(createJob("job", "task1", "task2", "task3"), USER_WITH_JOBS);

        // not started and killed job, should not appear in usage data
        InternalJob jobToBeKilled = defaultSubmitJob(createJob("job2", "task1"), USER_WITH_JOBS);
        killJob(jobToBeKilled);

        job.start();
        for (InternalTask task : job.getITasks()) {
            startTask(job, task);
            finishTask(job, task);
        }

        Date afterJobExecution = new Date();

        List<JobUsage> usagesBeforeJobRan = dbManager.getUsage(USER_WITH_JOBS, beforeJobExecution, beforeJobExecution);
        assertTrue(usagesBeforeJobRan.isEmpty());

        List<JobUsage> usagesAfterJobRan = dbManager.getUsage(USER_WITH_JOBS, afterJobExecution, afterJobExecution);
        assertTrue(usagesAfterJobRan.isEmpty());

        List<JobUsage> usagesForDifferentUser = dbManager.getUsage(USER_WITHOUT_JOBS,
                                                                   beforeJobExecution,
                                                                   afterJobExecution);
        assertTrue(usagesForDifferentUser.isEmpty());

        List<JobUsage> usagesWithinJobRun = dbManager.getUsage(USER_WITH_JOBS, beforeJobExecution, afterJobExecution);
        assertEquals(1, usagesWithinJobRun.size());
        assertEquals(3, usagesWithinJobRun.get(0).getTaskUsages().size());

        JobUsage onlyOneUsage = usagesWithinJobRun.get(0);
        assertEquals("job", onlyOneUsage.getJobName());
        assertTrue(onlyOneUsage.getJobDuration() > 0);

        TaskUsage onlyOneTaskUsage = onlyOneUsage.getTaskUsages().get(0);
        assertTrue(onlyOneTaskUsage.getTaskName().contains("task"));
        assertEquals(1, onlyOneTaskUsage.getTaskNodeNumber());
        assertTrue(onlyOneTaskUsage.getTaskExecutionDuration() > 0);
    }

    private TaskFlowJob createJob(String name, String... taskNames) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(name);
        job.setPriority(JobPriority.IDLE);
        for (String taskName : taskNames) {
            JavaTask task = new JavaTask();
            task.setName(taskName);
            task.setExecutableClassName("className");
            job.addTask(task);
        }
        return job;
    }

    private void killJob(InternalJob job) {
        job.setFinishedTime(System.currentTimeMillis());
        dbManager.updateAfterJobKilled(job, Collections.<TaskId> emptySet());
    }

    protected InternalTask startTask(InternalJob job, InternalTask task) throws Exception {
        super.startTask(job, task);
        dbManager.jobTaskStarted(job, task, false);
        return task;
    }

    private void finishTask(InternalJob job, InternalTask task) throws Exception {
        Thread.sleep(10);
        TaskResultImpl res = new TaskResultImpl(null, "ok", null, 42);
        job.terminateTask(false, task.getId(), null, null, res);
        if (job.isFinished()) {
            job.terminate();
        }
        dbManager.updateAfterTaskFinished(job, task, res);
    }
}
