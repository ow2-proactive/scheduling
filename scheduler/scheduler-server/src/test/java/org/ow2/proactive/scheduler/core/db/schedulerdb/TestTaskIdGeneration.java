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
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.db.RecoveredSchedulerState;
import org.ow2.proactive.scheduler.core.db.SchedulerStateRecoverHelper;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

import com.google.common.collect.ImmutableSet;


public class TestTaskIdGeneration extends BaseSchedulerDBTest {

    @Test
    public void test() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();
        jobDef.addTask(createDefaultTask("task1"));
        jobDef.addTask(createDefaultTask("task2"));
        jobDef.addTask(createDefaultTask("task3"));

        InternalJob job = InternalJobFactory.createJob(jobDef, getDefaultCredentials());
        job.setOwner(DEFAULT_USER_NAME);

        dbManager.newJobSubmitted(job);

        for (InternalTask task : job.getITasks()) {
            Assert.assertSame(task, job.getIHMTasks().get(task.getId()));
        }
        for (EligibleTaskDescriptor task : job.getJobDescriptor().getEligibleTasks()) {
            Assert.assertNotNull(job.getIHMTasks().get(task.getTaskId()));
        }

        checkIds(job);

        SchedulerStateRecoverHelper recoverHelper = new SchedulerStateRecoverHelper(dbManager);
        RecoveredSchedulerState state = recoverHelper.recover(-1);
        Collection<InternalJob> jobs = state.getPendingJobs();
        Assert.assertEquals(1, jobs.size());
        job = jobs.iterator().next();
        checkIds(job);

        JobState jobState = state.getSchedulerState().getPendingJobs().get(0);
        checkIds(jobState);
    }

    private void checkIds(JobState job) throws Exception {
        Set<String> expected = ImmutableSet.of("0", "1", "2");

        Set<String> actual = ImmutableSet.of(getTaskValue(job, "task1"),
                                             getTaskValue(job, "task2"),
                                             getTaskValue(job, "task3"));

        Assert.assertEquals(expected, actual);

        actual = ImmutableSet.of(getTaskReadableName(job, "task1"),
                                 getTaskReadableName(job, "task2"),
                                 getTaskReadableName(job, "task3"));

        expected = ImmutableSet.of("task1", "task2", "task3");

        Assert.assertEquals(expected, actual);
    }

    private String getTaskReadableName(JobState job, String taskName) {
        return getTaskId(job, taskName).getReadableName();
    }

    private String getTaskValue(JobState job, String taskName) {
        return getTaskId(job, taskName).value();
    }

    private TaskId getTaskId(JobState job, String taskName) {
        return findTask(job, taskName).getId();
    }

}
