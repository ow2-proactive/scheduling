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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.examples.EmptyTask;
import org.ow2.proactive.scripting.SelectionScript;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * Test provokes scenario when task gets 'NOT_STARTED' status
 * (job is killed when task has 'pending' or 'submitted' status) . 
 *
 */
public class TestTaskNotStarted extends SchedulerFunctionalTestNoRestart {

    @Test
    public void test() throws Exception {
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        JobId jobId;
        JobState jobState;

        jobId = scheduler.submit(createJob1());
        jobState = scheduler.getJobState(jobId);
        assertEquals(1, jobState.getTasks().size());
        assertEquals(TaskStatus.SUBMITTED, jobState.getTasks().get(0).getStatus());
        scheduler.killJob(jobId);
        jobState = scheduler.getJobState(jobId);
        assertEquals(1, jobState.getTasks().size());
        assertEquals(TaskStatus.NOT_STARTED, jobState.getTasks().get(0).getStatus());

        jobId = scheduler.submit(createJob2());
        schedulerHelper.waitForEventJobRunning(jobId);
        jobState = scheduler.getJobState(jobId);
        assertEquals(2, jobState.getTasks().size());
        assertEquals(TaskStatus.PENDING, getTask(jobState, "task2").getStatus());
        scheduler.killJob(jobId);
        jobState = scheduler.getJobState(jobId);
        assertEquals(2, jobState.getTasks().size());
        assertEquals(TaskStatus.NOT_STARTED, getTask(jobState, "task2").getStatus());
    }

    /*
     * Job with one task, task's selection script always returns 'false' so task can't start
     */
    private TaskFlowJob createJob1() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_1");

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(EmptyTask.class.getName());
        javaTask.setName("task1");
        SelectionScript selScript = new SelectionScript("selected = false;", "js");
        javaTask.setSelectionScript(selScript);

        job.addTask(javaTask);

        return job;
    }

    /*
     * Job with two task, one task without selection script, and one task with selection script
     * always returning 'false' so this task can't start
     */
    private TaskFlowJob createJob2() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_2");

        JavaTask javaTask1 = new JavaTask();
        javaTask1.setExecutableClassName(EmptyTask.class.getName());
        javaTask1.setName("task1");

        JavaTask javaTask2 = new JavaTask();
        javaTask2.setExecutableClassName(EmptyTask.class.getName());
        javaTask2.setName("task2");
        SelectionScript selScript = new SelectionScript("selected = false;", "js");
        javaTask2.setSelectionScript(selScript);

        job.addTask(javaTask1);
        job.addTask(javaTask2);

        return job;
    }

    private TaskState getTask(JobState jobState, String taskName) {
        for (TaskState task : jobState.getTasks()) {
            if (task.getName().equals(taskName)) {
                return task;
            }
        }
        fail("Failed to find task " + taskName);
        return null;
    }
}
