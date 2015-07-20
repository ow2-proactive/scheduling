/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package functionaltests;

import java.io.File;
import java.io.Serializable;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.junit.Assert;
import org.junit.Test;

import functionaltests.utils.ProActiveLock;

import static functionaltests.SchedulerTHelper.log;


public class TestPauseJobRecover extends RMFunctionalTest {

    @Test
    public void runTest() throws Throwable {
        SchedulerTHelper.startScheduler(new File(SchedulerTHelper.class.getResource(
          "config/scheduler-nonforkedscripttasks.ini").toURI()).getAbsolutePath());

        pause_resume_recover();
        recover_paused_job_with_finished_tasks();
    }

    /**
     * Submits a job with 2 tasks, task 2 depends on task1.
     * It pauses the job while task1 is running. Kills the Scheduler after task 1 finishes.
     * Expects task 2 to be executed after restart.
     */
    public void pause_resume_recover() throws Throwable {

        ProActiveLock communicationObject = PAActiveObject.newActive(ProActiveLock.class, new Object[] {});

        TaskFlowJob job = createJob(PAActiveObject.getUrl(communicationObject));

        log("Submit job");
        JobId jobId = SchedulerTHelper.submitJob(job);
        log("Submitted job " + jobId);

        log("Waiting for task1 to start");
        SchedulerTHelper.waitForEventTaskRunning(jobId, "task1");

        JobState js = SchedulerTHelper.getSchedulerInterface().getJobState(jobId);
        Assert.assertEquals(JobStatus.RUNNING, js.getStatus());
        Assert.assertEquals(TaskStatus.RUNNING, getTaskState("task1", js).getStatus());
        Assert.assertEquals(TaskStatus.PENDING, getTaskState("task2", js).getStatus());

        log("Pause the job " + jobId);
        SchedulerTHelper.getSchedulerInterface().pauseJob(jobId);

        js = SchedulerTHelper.getSchedulerInterface().getJobState(jobId);
        Assert.assertEquals(JobStatus.PAUSED, js.getStatus());
        Assert.assertEquals(TaskStatus.RUNNING, getTaskState("task1", js).getStatus());
        Assert.assertEquals(TaskStatus.PAUSED, getTaskState("task2", js).getStatus());

        SchedulerTHelper.getSchedulerInterface().resumeJob(jobId);

        js = SchedulerTHelper.getSchedulerInterface().getJobState(jobId);
        Assert.assertEquals(JobStatus.RUNNING, js.getStatus());
        Assert.assertEquals(TaskStatus.RUNNING, getTaskState("task1", js).getStatus());
        Assert.assertEquals(TaskStatus.PENDING, getTaskState("task2", js).getStatus());

        //let the task1 finish
        communicationObject.unlock();

        log("Waiting for task1 to finish");
        SchedulerTHelper.waitForEventTaskFinished(jobId, "task1");

        log("Kill&Restart the scheduler");

        SchedulerTHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
                "config/functionalTSchedulerProperties-updateDB-nonforkedtasks.ini").toURI()).getAbsolutePath());

        log("Resume the job " + jobId);
        SchedulerTHelper.getSchedulerInterface().resumeJob(jobId);

        log("Waiting for job " + jobId + " to finish");
        SchedulerTHelper.waitForEventJobFinished(jobId);

        JobResult jobResult = SchedulerTHelper.getSchedulerInterface().getJobResult(jobId);
        Assert.assertEquals(2, jobResult.getAllResults().size());
        Assert.assertEquals("OK", jobResult.getResult("task1").value().toString());
        Assert.assertEquals("OK", jobResult.getResult("task2").value().toString());
    }

    // SCHEDULING-1924 SCHEDULING-2030
    public void recover_paused_job_with_finished_tasks() throws Throwable {

        ProActiveLock controlTask2 = PAActiveObject.newActive(ProActiveLock.class, new Object[] {});
        ProActiveLock controlTask3 = PAActiveObject.newActive(ProActiveLock.class, new Object[] {});

        TaskFlowJob job = createJob3TasksPauseTask2AndTask3(PAActiveObject.getUrl(controlTask2),
                PAActiveObject.getUrl(controlTask3));

        log("Submit job");
        JobId jobId = SchedulerTHelper.submitJob(job);
        log("Submitted job " + jobId);

        log("Waiting for task1 to finish and task2 to start");
        SchedulerTHelper.waitForEventTaskFinished(jobId, "task1");
        SchedulerTHelper.waitForEventTaskRunning(jobId, "task2");

        JobState js = SchedulerTHelper.getSchedulerInterface().getJobState(jobId);
        Assert.assertEquals(JobStatus.RUNNING, js.getStatus());
        Assert.assertEquals(TaskStatus.FINISHED, getTaskState("task1", js).getStatus());
        Assert.assertEquals(TaskStatus.RUNNING, getTaskState("task2", js).getStatus());
        Assert.assertEquals(TaskStatus.PENDING, getTaskState("task3", js).getStatus());

        log("Pause the job " + jobId);
        SchedulerTHelper.getSchedulerInterface().pauseJob(jobId);

        js = SchedulerTHelper.getSchedulerInterface().getJobState(jobId);
        Assert.assertEquals(JobStatus.PAUSED, js.getStatus());

        controlTask2.unlock();

        log("Waiting for task2 to finish");
        SchedulerTHelper.waitForEventTaskFinished(jobId, "task2");

        log("Kill&Restart the scheduler");

        SchedulerTHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
                "config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        log("Resume the job " + jobId);
        SchedulerTHelper.getSchedulerInterface().resumeJob(jobId);

        controlTask3.unlock();

        log("Waiting for task3 to finish");
        SchedulerTHelper.waitForEventTaskFinished(jobId, "task3");

        log("Waiting for job " + jobId + " to finish");
        SchedulerTHelper.waitForEventJobFinished(jobId);

        JobResult jobResult = SchedulerTHelper.getSchedulerInterface().getJobResult(jobId);
        Assert.assertEquals(3, jobResult.getAllResults().size());
        Assert.assertEquals("OK", jobResult.getResult("task1").value().toString());
        Assert.assertEquals("OK", jobResult.getResult("task2").value().toString());
        Assert.assertEquals("OK", jobResult.getResult("task3").value().toString());
    }

    public static TaskState getTaskState(String taskName, JobState jobState) {
        for (TaskState ts : jobState.getTasks()) {
            if (ts.getName().equals(taskName)) {
                return ts;
            }
        }
        return null;
    }

    public static TaskFlowJob createJob(String communicationObjectUrl) throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask task1 = new JavaTask();
        task1.setName("task1");
        task1.setExecutableClassName(TestJavaTask.class.getName());
        task1.addArgument("communicationObjectUrl", communicationObjectUrl);

        JavaTask task2 = new JavaTask();
        task2.setName("task2");
        task2.setExecutableClassName(SleepTask.class.getName());

        task2.addDependence(task1);
        job.addTask(task1);
        job.addTask(task2);
        return job;
    }

    protected TaskFlowJob createJob3TasksPauseTask2AndTask3(String communicationObjectUrlTask2,
            String communicationObjectUrlTask3) throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask task1 = new JavaTask();
        task1.setName("task1");
        task1.setExecutableClassName(SleepTask.class.getName());

        JavaTask task2 = new JavaTask();
        task2.setName("task2");
        task2.setExecutableClassName(TestJavaTask.class.getName());
        task2.addArgument("communicationObjectUrl", communicationObjectUrlTask2);

        JavaTask task3 = new JavaTask();
        task3.setName("task3");
        task3.setExecutableClassName(TestJavaTask.class.getName());
        task3.addArgument("communicationObjectUrl", communicationObjectUrlTask3);

        task2.addDependence(task1);
        task3.addDependence(task2);
        job.addTask(task1);
        job.addTask(task2);
        job.addTask(task3);
        return job;
    }

    public static class SleepTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            Thread.sleep(1);
            return "OK";
        }
    }

    public static class TestJavaTask extends JavaExecutable {

        private String communicationObjectUrl;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            System.out.println("OK");
            ProActiveLock communicationObject = PAActiveObject.lookupActive(ProActiveLock.class,
                    communicationObjectUrl);

            ProActiveLock.waitUntilUnlocked(communicationObject);
            return "OK";
        }
    }

}
