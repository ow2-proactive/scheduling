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
import org.ow2.tests.FunctionalTest;
import org.junit.Assert;
import org.junit.Test;


/**
 * Submits a job with 2 tasks, task 2 depends on task1.
 * It pauses the job while task1 is running. Kills the Scheduler after task 1 finishes.
 * Expects task 2 to be executed after restart.
 * @author esalagea
 *
 */
public class TestPauseJobRecover extends FunctionalTest {

    @Test
    public void test() throws Throwable {

        CommunicationObject communicationObject = PAActiveObject.newActive(CommunicationObject.class,
                new Object[] {});

        TaskFlowJob job = createJob(PAActiveObject.getUrl(communicationObject));

        System.out.println("Submit job");
        JobId jobId = SchedulerTHelper.submitJob(job);
        System.out.println("Submitted job " + jobId);

        System.out.println("Waiting for task1 to start");
        SchedulerTHelper.waitForEventTaskRunning(jobId, "task1");

        JobState js = SchedulerTHelper.getSchedulerInterface().getJobState(jobId);
        Assert.assertEquals(JobStatus.RUNNING, js.getStatus());
        Assert.assertEquals(TaskStatus.RUNNING, getTaskState("task1", js).getStatus());
        Assert.assertEquals(TaskStatus.PENDING, getTaskState("task2", js).getStatus());

        System.out.println("Pause the job " + jobId);
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
        communicationObject.setCanFinish(true);

        System.out.println("Waiting for task1 to finish");
        SchedulerTHelper.waitForEventTaskFinished(jobId, "task1");

        System.out.println("Kill&Restart the scheduler");

        SchedulerTHelper.killAndRestartScheduler(new File(SchedulerTHelper.class.getResource(
                "config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        System.out.println("Resume the job " + jobId);
        SchedulerTHelper.getSchedulerInterface().resumeJob(jobId);

        System.out.println("Waiting for job " + jobId + " to finish");
        SchedulerTHelper.waitForEventJobFinished(jobId);

        JobResult jobResult = SchedulerTHelper.getSchedulerInterface().getJobResult(jobId);
        Assert.assertEquals(2, jobResult.getAllResults().size());
        Assert.assertEquals("OK", jobResult.getResult("task1").value().toString());
        Assert.assertEquals("OK", jobResult.getResult("task2").value().toString());
    }

    protected TaskState getTaskState(String taskName, JobState jobState) {
        for (TaskState ts : jobState.getTasks()) {
            if (ts.getName().equals(taskName)) {
                return ts;
            }
        }
        return null;
    }

    protected TaskFlowJob createJob(String communicationObjectUrl) throws Exception {
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
            CommunicationObject communicationObject = PAActiveObject.lookupActive(CommunicationObject.class,
                    communicationObjectUrl);

            while (true) {
                if (!communicationObject.canFinish()) {
                    Thread.sleep(5000);
                } else {
                    break;
                }
            }
            return "OK";
        }
    }

    public static class CommunicationObject {

        private boolean canFinish;

        public CommunicationObject() {
        }

        public void setCanFinish(boolean value) {
            canFinish = value;
        }

        public boolean canFinish() {
            return canFinish;
        }

    }

}
