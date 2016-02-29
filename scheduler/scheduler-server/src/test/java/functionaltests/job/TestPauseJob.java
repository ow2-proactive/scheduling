/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.job;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.util.FileLock;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;


/**
 * Test checks that once a job is paused the execution of all tasks except running
 * is postponed.
 */
public class TestPauseJob extends SchedulerFunctionalTestNoRestart {

    @Test
    public void test() throws Throwable {
        FileLock fileLock = new FileLock();
        Path fileLockPath = fileLock.lock();

        TaskFlowJob job = createJob(fileLockPath.toString());

        log("Submit job");
        JobId jobId = schedulerHelper.submitJob(job);
        log("Submitted job " + jobId);

        log("Waiting for task1 to start");
        schedulerHelper.waitForEventTaskRunning(jobId, "task1");

        JobState js = schedulerHelper.getSchedulerInterface().getJobState(jobId);
        assertEquals(JobStatus.RUNNING, js.getStatus());
        assertEquals(TaskStatus.RUNNING, getTaskState("task1", js).getStatus());
        assertEquals(TaskStatus.PENDING, getTaskState("task2", js).getStatus());

        log("Pause the job " + jobId);
        schedulerHelper.getSchedulerInterface().pauseJob(jobId);

        js = schedulerHelper.getSchedulerInterface().getJobState(jobId);
        assertEquals(JobStatus.PAUSED, js.getStatus());
        assertEquals(TaskStatus.RUNNING, getTaskState("task1", js).getStatus());
        assertEquals(TaskStatus.PAUSED, getTaskState("task2", js).getStatus());

        // let the task1 finish
        fileLock.unlock();

        log("Checking is the status of task2 remains unchanged");
        Thread.sleep(5000);
        js = schedulerHelper.getSchedulerInterface().getJobState(jobId);
        assertEquals(TaskStatus.PAUSED, getTaskState("task2", js).getStatus());
    }

    public static TaskState getTaskState(String taskName, JobState jobState) {
        for (TaskState ts : jobState.getTasks()) {
            if (ts.getName().equals(taskName)) {
                return ts;
            }
        }
        return null;
    }

    public static TaskFlowJob createJob(String fileLockPath) throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask task1 = new JavaTask();
        task1.setName("task1");
        task1.setExecutableClassName(TestJavaTask.class.getName());
        task1.addArgument("fileLockPath", fileLockPath);

        JavaTask task2 = new JavaTask();
        task2.setName("task2");
        task2.setExecutableClassName(SleepTask.class.getName());

        task2.addDependence(task1);
        job.addTask(task1);
        job.addTask(task2);
        return job;
    }

    public static class SleepTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            TimeUnit.MILLISECONDS.sleep(1);
            return "OK";
        }
    }

    public static class TestJavaTask extends JavaExecutable {

        private String fileLockPath;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            System.out.println("OK");
            FileLock.waitUntilUnlocked(fileLockPath);
            return "OK";
        }
    }

}
