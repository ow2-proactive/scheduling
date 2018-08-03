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
package functionaltests.job.taskkill;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.ow2.proactive.utils.Lambda.silent;

import java.io.File;
import java.io.Serializable;
import java.net.URL;

import com.jayway.awaitility.Duration;
import functionaltests.job.error.TestTaskRestartOnNodeFailure;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobStatus;

import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.util.FileLock;


/**
 * This test checks that when we mark failed task as finished,
 * Scheduler does not release node, which is busy with other task.
 */
public class TestMarkedAsFinished extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static final URL failingJob = TestMarkedAsFinished.class.getResource("/functionaltests/descriptors/Job_failing.xml");

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        schedulerHelper = new SchedulerTHelper(false,
                new File(SchedulerTHelper.class.getResource("/functionaltests/config/scheduler-nonforkedscripttasks.ini")
                        .toURI()).getAbsolutePath(),
                null,
                null);
        schedulerHelper.createNodeSource("local", 1);
    }

    @Test
    public void test() throws Throwable {

        JobId failingJobId = schedulerHelper.submitJob(new File(failingJob.toURI()).getAbsolutePath());

        // task failure will make whole job paused. Because onTaskError="pauseJob"
        await().timeout(Duration.ONE_MINUTE)
               .until(silent(() -> schedulerHelper.getSchedulerInterface().getJobState(failingJobId).getStatus()),
                equalTo(JobStatus.PAUSED));

        final FileLock fileLock = new FileLock();
        fileLock.lock();

        // submit normal job which should execute for a quite long time
        JobId normalJobId = schedulerHelper.submitJob(createJob(fileLock.toString()));

        // wait until normal job is running
        schedulerHelper.waitForEventJobRunning(normalJobId);

        assertEquals(JobStatus.PAUSED, schedulerHelper.getSchedulerInterface().getJobState(failingJobId).getStatus());
        assertEquals(JobStatus.RUNNING, schedulerHelper.getSchedulerInterface().getJobState(normalJobId).getStatus());

        // after we mark as finished failed task, failing job should become FINISHED
        // however, even more important, that other job is continue to run
//        schedulerHelper.getSchedulerInterface().finishInErrorTask(failingJobId.value(), "Error_Task");
        Thread.sleep(10000);

        assertEquals(TaskStatus.FINISHED, schedulerHelper.getSchedulerInterface().getJobState(failingJobId.value()).getTasks().get(0).getStatus());

        fileLock.unlock();

        schedulerHelper.waitForEventJobFinished(failingJobId);
        schedulerHelper.waitForEventJobFinished(normalJobId);

        assertEquals(JobStatus.FINISHED, schedulerHelper.getSchedulerInterface().getJobState(failingJobId).getStatus());
        assertEquals(JobStatus.FINISHED, schedulerHelper.getSchedulerInterface().getJobState(normalJobId).getStatus());

    }


    public static class TestJavaTask extends JavaExecutable {

        private String fileLockPath;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            getOut().println("OK");
            FileLock.waitUntilUnlocked(fileLockPath);
            return "OK";
        }

    }

    private TaskFlowJob createJob(String communicationObjectUrl) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        job.setOnTaskError(OnTaskError.CANCEL_JOB);
        job.setMaxNumberOfExecution(1);

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestTaskRestartOnNodeFailure.TestJavaTask.class.getName());
        javaTask.setMaxNumberOfExecution(1);
        javaTask.setOnTaskError(OnTaskError.CANCEL_JOB);
        javaTask.setName("Test task");
        javaTask.addArgument("fileLockPath", communicationObjectUrl);
        job.addTask(javaTask);

        return job;
    }

}
