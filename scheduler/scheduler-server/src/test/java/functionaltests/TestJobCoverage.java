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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory_stax;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.tests.FunctionalTest;


/**
 * TestJobCoverage check every states, changes, intermediate states of every tasks.
 *
 * <p>
 * task1 : working at first time, RESULT is placed in the precious list at the end.
 * </p>
 * <p>
 * task2 : working at 3rd time, max number of execution is 2, so EXCEPTION is expected at the end.
 * </p>
 * <p>
 * task3 : working at 3rd time, max number of execution is 4, so RESULT is expected at the end.
 * </p>
 * <p>
 * task4 : Throw an exception, expected number of execution is 1, EXCEPTION is expected at the end.
 * </p>
 * <p>
 * task5 : Throw an exception, expected number of execution is 3, EXCEPTION is expected at the end.
 * 			This task must restart on a different host each time.
 * </p>
 * <p>
 * task6 : Native task that end normally, RESULT (CODE 0) is expected at the end.
 * </p>
 * <p>
 * task7 : Native task that end with error code 12, expected number of execution is 1, RESULT (CODE 12) is expected at the end.
 * </p>
 * <p>
 * task8 : Native task that end with error code 12, expected number of execution is 2, RESULT (CODE 12) is expected at the end.
 * </p>
 * <p>
 * task9 : Started after every other tasks, Kill the runtime, expected number of execution is 2 (set by admin)
 * 			EXCEPTION is expected at the end.
 * </p>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestJobCoverage extends FunctionalTest {

    private static URL jobDescriptor = TestJobAborted.class
            .getResource("/functionaltests/descriptors/Job_Coverage.xml");

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {
        JobState jstate;
        TaskInfo tinfo;
        JobInfo jinfo;

        // removing temp file if existing
        File w3File = new File(System.getProperty("java.io.tmpdir"), "WorkingAt3rdT2_13031984.tmp");
        if (w3File.exists()) {
            w3File.delete();
        }

        //job submission
        SchedulerTHelper.log("Submitting job...");
        TaskFlowJob job = (TaskFlowJob) JobFactory_stax.getFactory().createJob(
                new File(jobDescriptor.toURI()).getAbsolutePath());
        JobId id = SchedulerTHelper.submitJob(job);

        //waiting for job termination
        SchedulerTHelper.log("Waiting for job to finish...");
        jinfo = SchedulerTHelper.waitForEventJobFinished(id);

        //checking results
        SchedulerTHelper.log("Checking results...");
        JobResult result = SchedulerTHelper.getJobResult(id);
        Assert.assertEquals(9, result.getAllResults().size());
        Assert.assertEquals(2, result.getPreciousResults().size());
        Assert.assertNotNull(result.getPreciousResults().get("task1"));
        Assert.assertNotNull(result.getPreciousResults().get("task6"));

        Assert.assertEquals("Working", result.getPreciousResults().get("task1").value());
        Assert.assertTrue(result.getResult("task2").getException().getMessage().contains(
                "WorkingAt3rd - Status : Number is 1"));
        Assert.assertTrue(result.getResult("task3").value().toString().contains(
                "WorkingAt3rd - Status : OK / File deleted :"));
        Assert.assertEquals("Throwing", result.getResult("task4").getException().getMessage());
        Assert.assertEquals("Throwing", result.getResult("task5").getException().getMessage());
        Assert.assertEquals(0, result.getPreciousResults().get("task6").value());
        Assert.assertEquals(12, result.getResult("task7").value());
        Assert.assertEquals(12, result.getResult("task8").value());
        Assert.assertNotNull(result.getResult("task9").getException());

        //checking all processes
        SchedulerTHelper.log("Checking all received events :");
        jstate = SchedulerTHelper.waitForEventJobSubmitted(id);
        Assert.assertEquals(JobStatus.PENDING, jstate.getStatus());

        //checking task 1
        SchedulerTHelper.log("Checking task1 process...");
        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task1");
        jstate.update(tinfo);
        jstate.update(tinfo.getJobInfo());
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());
        Assert.assertEquals(JobStatus.RUNNING, jstate.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskFinished(id, "task1");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.FINISHED, tinfo.getStatus());

        //checking task 2
        SchedulerTHelper.log("Checking task2 process...");
        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task2");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskWaitingForRestart(id, "task2");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.WAITING_ON_ERROR, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task2");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskFinished(id, "task2");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.FAULTY, tinfo.getStatus());

        //checking task 3
        SchedulerTHelper.log("Checking task3 process...");
        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task3");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskWaitingForRestart(id, "task3");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.WAITING_ON_ERROR, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task3");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskWaitingForRestart(id, "task3");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.WAITING_ON_ERROR, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task3");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskFinished(id, "task3");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.FINISHED, tinfo.getStatus());

        //checking task 4
        SchedulerTHelper.log("Checking task4 process...");
        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task4");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskFinished(id, "task4");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.FAULTY, tinfo.getStatus());

        //checking task 5
        SchedulerTHelper.log("Checking task5 process...");
        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task5");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());
        String hostName = tinfo.getExecutionHostName();

        tinfo = SchedulerTHelper.waitForEventTaskWaitingForRestart(id, "task5");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.WAITING_ON_ERROR, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task5");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());
        Assert.assertFalse(hostName.equals(tinfo.getExecutionHostName()));
        hostName = tinfo.getExecutionHostName();

        tinfo = SchedulerTHelper.waitForEventTaskWaitingForRestart(id, "task5");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.WAITING_ON_ERROR, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task5");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());
        Assert.assertFalse(hostName.equals(tinfo.getExecutionHostName()));
        hostName = tinfo.getExecutionHostName();

        tinfo = SchedulerTHelper.waitForEventTaskFinished(id, "task5");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.FAULTY, tinfo.getStatus());

        //checking task 6
        SchedulerTHelper.log("Checking task6 process...");
        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task6");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskFinished(id, "task6");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.FINISHED, tinfo.getStatus());

        //checking task 7
        SchedulerTHelper.log("Checking task7 process...");
        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task7");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskFinished(id, "task7");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.FAULTY, tinfo.getStatus());

        //checking task 8
        SchedulerTHelper.log("Checking task8 process...");
        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task8");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskWaitingForRestart(id, "task8");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.WAITING_ON_ERROR, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task8");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = SchedulerTHelper.waitForEventTaskFinished(id, "task8");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.FAULTY, tinfo.getStatus());

        //checking task 9
        SchedulerTHelper.log("Checking task9 process...");
        tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task9");
        jstate.update(tinfo);
        Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        if (!((JavaTask) job.getTask("task9")).isFork()) {
            tinfo = SchedulerTHelper.waitForEventTaskWaitingForRestart(id, "task9");
            jstate.update(tinfo);
            Assert.assertEquals(TaskStatus.WAITING_ON_FAILURE, tinfo.getStatus());

            tinfo = SchedulerTHelper.waitForEventTaskRunning(id, "task9");
            jstate.update(tinfo);
            Assert.assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

            tinfo = SchedulerTHelper.waitForEventTaskFinished(id, "task9");
            jstate.update(tinfo);
            Assert.assertEquals(TaskStatus.FAILED, tinfo.getStatus());
        } else {
            tinfo = SchedulerTHelper.waitForEventTaskFinished(id, "task9");
            jstate.update(tinfo);
            Assert.assertEquals(TaskStatus.FAULTY, tinfo.getStatus());
        }

        //checking end of the job...
        jstate.update(jinfo);
        Assert.assertEquals(0, jinfo.getNumberOfPendingTasks());
        Assert.assertEquals(0, jinfo.getNumberOfRunningTasks());
        Assert.assertEquals(9, jinfo.getNumberOfFinishedTasks());
        Assert.assertEquals(9, jinfo.getTotalNumberOfTasks());
        if (!((JavaTask) job.getTask("task9")).isFork()) {
            Assert.assertEquals(JobStatus.FAILED, jinfo.getStatus());
        } else {
            Assert.assertEquals(JobStatus.FINISHED, jinfo.getStatus());
        }

        Assert.assertEquals(0, jstate.getNumberOfPendingTasks());
        Assert.assertEquals(0, jstate.getNumberOfRunningTasks());
        Assert.assertEquals(9, jstate.getNumberOfFinishedTasks());
        Assert.assertEquals(9, jstate.getTotalNumberOfTasks());
        if (!((JavaTask) job.getTask("task9")).isFork()) {
            Assert.assertEquals(JobStatus.FAILED, jstate.getStatus());
        } else {
            Assert.assertEquals(JobStatus.FINISHED, jstate.getStatus());
        }

    }

}
