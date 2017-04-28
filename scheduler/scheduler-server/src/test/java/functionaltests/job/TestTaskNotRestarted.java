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

import java.io.Serializable;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import functionaltests.utils.SchedulerFunctionalTestWithRestart;


/**
 * Test provokes scenario when task gets 'NOT_RESTARTED' status:
 * - task is submitted and starts execution
 * - user requests to restart task with some delay
 * - before task was restarted job is killed
 *
 */
public class TestTaskNotRestarted extends SchedulerFunctionalTestWithRestart {

    public static class TestJavaTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            Thread.sleep(Long.MAX_VALUE);
            return "OK";
        }
    }

    @Test
    public void test() throws Exception {
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        JobId jobId = scheduler.submit(createJob());

        JobState jobState;

        schedulerHelper.waitForEventTaskRunning(jobId, "task1");
        jobState = scheduler.getJobState(jobId);
        assertEquals(1, jobState.getTasks().size());
        assertEquals(TaskStatus.RUNNING, jobState.getTasks().get(0).getStatus());

        scheduler.restartTask(jobId, "task1", Integer.MAX_VALUE);
        jobState = scheduler.getJobState(jobId);
        assertEquals(1, jobState.getTasks().size());
        assertEquals(TaskStatus.WAITING_ON_ERROR, jobState.getTasks().get(0).getStatus());

        scheduler.killJob(jobId);

        jobState = scheduler.getJobState(jobId);
        assertEquals(1, jobState.getTasks().size());
        assertEquals(TaskStatus.NOT_RESTARTED, jobState.getTasks().get(0).getStatus());
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.setName("task1");
        javaTask.setMaxNumberOfExecution(10);

        job.addTask(javaTask);

        return job;
    }
}
