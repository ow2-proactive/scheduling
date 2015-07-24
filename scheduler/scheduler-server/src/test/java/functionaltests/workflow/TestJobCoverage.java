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
package functionaltests.workflow;

import java.io.File;
import java.net.URL;

import org.objectweb.proactive.utils.StackTraceUtil;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.job.error.TestJobAborted;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;


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
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestJobCoverage extends SchedulerFunctionalTest {

    private static URL jobDescriptor = TestJobAborted.class
            .getResource("/functionaltests/descriptors/Job_Coverage.xml");

    @Test
    public void run() throws Throwable {
        JobState jstate;
        TaskInfo tinfo;
        JobInfo jinfo;

        // removing temp file if existing
        File w3File = new File(System.getProperty("java.io.tmpdir"), "WorkingAt3rdT2_13031984.tmp");
        if (w3File.exists()) {
            FileUtils.forceDelete(w3File);
        }

        //job submission
        log("Submitting job...");
        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory().createJob(
                new File(jobDescriptor.toURI()).getAbsolutePath());
        JobId id = schedulerHelper.submitJob(job);

        //waiting for job termination
        log("Waiting for job to finish...");
        jinfo = schedulerHelper.waitForEventJobFinished(id);

        //checking results
        log("Checking results...");
        JobResult result = schedulerHelper.getJobResult(id);
        assertEquals(8, result.getAllResults().size());
        assertEquals(2, result.getPreciousResults().size());
        assertNotNull(result.getPreciousResults().get("task1"));
        assertNotNull(result.getPreciousResults().get("task6"));

        assertEquals("Working", result.getPreciousResults().get("task1").value());
        assertTrue(StackTraceUtil.getStackTrace(result.getResult("task2").getException()).contains(
                "WorkingAt3rd - Status : Number is 1"));
        assertTrue(result.getResult("task3").value().toString()
                .contains("WorkingAt3rd - Status : OK / File deleted :"));
        assertTrue(result.getResult("task4").getException().getCause().getMessage().contains("Throwing"));
        assertTrue(result.getResult("task5").getException().getCause().getMessage().contains("Throwing"));
        assertNotNull(result.getResult("task7").getException());
        assertNotNull(result.getResult("task8").getException());

        //checking all processes
        log("Checking all received events :");
        jstate = schedulerHelper.waitForEventJobSubmitted(id);
        assertEquals(JobStatus.PENDING, jstate.getStatus());

        //checking task 1
        log("Checking task1 process...");
        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task1");
        jstate.update(tinfo);
        jstate.update(tinfo.getJobInfo());
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());
        assertEquals(JobStatus.RUNNING, jstate.getStatus());

        tinfo = schedulerHelper.waitForEventTaskFinished(id, "task1");
        jstate.update(tinfo);
        assertEquals(TaskStatus.FINISHED, tinfo.getStatus());

        //checking task 2
        log("Checking task2 process...");
        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task2");
        jstate.update(tinfo);
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskWaitingForRestart(id, "task2");
        jstate.update(tinfo);
        assertEquals(TaskStatus.WAITING_ON_ERROR, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task2");
        jstate.update(tinfo);
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskFinished(id, "task2");
        jstate.update(tinfo);
        assertEquals(TaskStatus.FAULTY, tinfo.getStatus());

        //checking task 3
        log("Checking task3 process...");
        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task3");
        jstate.update(tinfo);
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskWaitingForRestart(id, "task3");
        jstate.update(tinfo);
        assertEquals(TaskStatus.WAITING_ON_ERROR, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task3");
        jstate.update(tinfo);
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskWaitingForRestart(id, "task3");
        jstate.update(tinfo);
        assertEquals(TaskStatus.WAITING_ON_ERROR, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task3");
        jstate.update(tinfo);
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskFinished(id, "task3");
        jstate.update(tinfo);
        assertEquals(TaskStatus.FINISHED, tinfo.getStatus());

        //checking task 4
        log("Checking task4 process...");
        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task4");
        jstate.update(tinfo);
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskFinished(id, "task4");
        jstate.update(tinfo);
        assertEquals(TaskStatus.FAULTY, tinfo.getStatus());

        //checking task 5
        log("Checking task5 process...");
        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task5");
        jstate.update(tinfo);
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());
        String hostName = tinfo.getExecutionHostName();

        tinfo = schedulerHelper.waitForEventTaskWaitingForRestart(id, "task5");
        jstate.update(tinfo);
        assertEquals(TaskStatus.WAITING_ON_ERROR, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task5");
        jstate.update(tinfo);
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());
        Assert.assertFalse(hostName.equals(tinfo.getExecutionHostName()));
        hostName = tinfo.getExecutionHostName();

        tinfo = schedulerHelper.waitForEventTaskWaitingForRestart(id, "task5");
        jstate.update(tinfo);
        assertEquals(TaskStatus.WAITING_ON_ERROR, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task5");
        jstate.update(tinfo);
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());
        Assert.assertFalse(hostName.equals(tinfo.getExecutionHostName()));

        tinfo = schedulerHelper.waitForEventTaskFinished(id, "task5");
        jstate.update(tinfo);
        assertEquals(TaskStatus.FAULTY, tinfo.getStatus());

        //checking task 6
        log("Checking task6 process...");
        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task6");
        jstate.update(tinfo);
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskFinished(id, "task6");
        jstate.update(tinfo);
        assertEquals(TaskStatus.FINISHED, tinfo.getStatus());

        //checking task 7
        log("Checking task7 process...");
        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task7");
        jstate.update(tinfo);
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskFinished(id, "task7");
        jstate.update(tinfo);
        assertEquals(TaskStatus.FAULTY, tinfo.getStatus());

        //checking task 8
        log("Checking task8 process...");
        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task8");
        jstate.update(tinfo);
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskWaitingForRestart(id, "task8");
        jstate.update(tinfo);
        assertEquals(TaskStatus.WAITING_ON_ERROR, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskRunning(id, "task8");
        jstate.update(tinfo);
        assertEquals(TaskStatus.RUNNING, tinfo.getStatus());

        tinfo = schedulerHelper.waitForEventTaskFinished(id, "task8");
        jstate.update(tinfo);
        assertEquals(TaskStatus.FAULTY, tinfo.getStatus());

        //checking end of the job...
        jstate.update(jinfo);
        assertEquals(0, jinfo.getNumberOfPendingTasks());
        assertEquals(0, jinfo.getNumberOfRunningTasks());
        assertEquals(8, jinfo.getNumberOfFinishedTasks());
        assertEquals(8, jinfo.getTotalNumberOfTasks());
        assertEquals(JobStatus.FINISHED, jinfo.getStatus());

        assertEquals(0, jstate.getNumberOfPendingTasks());
        assertEquals(0, jstate.getNumberOfRunningTasks());
        assertEquals(8, jstate.getNumberOfFinishedTasks());
        assertEquals(8, jstate.getTotalNumberOfTasks());
        assertEquals(JobStatus.FINISHED, jstate.getStatus());

    }

}
