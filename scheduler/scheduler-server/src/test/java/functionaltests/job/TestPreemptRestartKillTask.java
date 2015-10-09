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

import java.io.File;
import java.net.URL;

import org.ow2.proactive.scheduler.common.exception.TaskAbortedException;
import org.ow2.proactive.scheduler.common.exception.TaskPreemptedException;
import org.ow2.proactive.scheduler.common.exception.TaskRestartedException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;


/**
 * This class tests the preempt task, restart task, and kill task features.
 *
 * Submit a taskflow job with 4 tasks.
 * One has 4 max number of executions
 * one is failJobOnError
 * Preempt task must : (test 1)
 * - stop execution
 * - restart later without side effect
 * Restart task must :
 * - stop execution (ends like a normal termination with TaskRestartedException)
 * - restart later if possible, fails the job if 'failJobOnError'
 * Kill task must :
 * - stop execution
 * - not restart this task, fails the job if 'failJobOnError'
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
public class TestPreemptRestartKillTask extends SchedulerFunctionalTest {

    private static URL jobDescriptor = TestPreemptRestartKillTask.class
            .getResource("/functionaltests/descriptors/Job_preempt_restart_kill.xml");

    @Test
    public void run() throws Throwable {

        log("Submitting job");

        schedulerHelper.addExtraNodes(3);

        JobId id = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());
        log("Wait for event job submitted");
        schedulerHelper.waitForEventJobSubmitted(id);
        log("Wait for event t1 running");
        schedulerHelper.waitForEventTaskRunning(id, "t1");
        log("Wait for event t2 running");
        schedulerHelper.waitForEventTaskRunning(id, "t2");
        log("Wait for event t3 running");
        schedulerHelper.waitForEventTaskRunning(id, "t3");
        log("Wait for event t4 running");
        schedulerHelper.waitForEventTaskRunning(id, "t4");

        log("Preempt t1");
        schedulerHelper.getSchedulerInterface().preemptTask(id, "t1", 1);
        log("Wait for event t1 waiting for restart");
        //running jobs list must have only one job, task t1 must have number of execution to 0
        TaskInfo ti1 = schedulerHelper.waitForEventTaskWaitingForRestart(id, "t1");
        //task result for t1 must be available with TaskPreemptedException
        TaskResult tr1 = schedulerHelper.getSchedulerInterface().getTaskResult(id, "t1");

        log("Restart t2");
        schedulerHelper.getSchedulerInterface().restartTask(id, "t2", 1);
        log("Wait for event t2 waiting for restart");
        //running jobs list must have only one job, task t2 must have number of execution to 1
        TaskInfo ti2 = schedulerHelper.waitForEventTaskWaitingForRestart(id, "t2");
        //task result for t2 must be available with TaskRestartedException
        TaskResult tr2 = schedulerHelper.getSchedulerInterface().getTaskResult(id, "t2");
        log("Wait for event t2 running");

        schedulerHelper.waitForEventTaskRunning(id, "t2");

        log("Restart t2 again");
        schedulerHelper.getSchedulerInterface().restartTask(id, "t2", 1);
        log("Wait for event t2 waiting for restart again");
        //running jobs list must have only one job, task t2 must have number of execution to 2
        TaskInfo ti3 = schedulerHelper.waitForEventTaskWaitingForRestart(id, "t2");
        //task result for t2 must be available with TaskRestartedException
        TaskResult tr3 = schedulerHelper.getSchedulerInterface().getTaskResult(id, "t2");

        //ensure every tasks are running at this point
        schedulerHelper.waitForEventTaskRunning(id, "t1");
        schedulerHelper.waitForEventTaskRunning(id, "t2");

        log("Kill t3");
        schedulerHelper.getSchedulerInterface().killTask(id, "t3");
        log("Wait for event t3 finished");
        schedulerHelper.waitForEventTaskFinished(id, "t3");
        //task result for t3 must be available with TaskRestartedException
        TaskResult tr4 = schedulerHelper.getSchedulerInterface().getTaskResult(id, "t3");

        log("Kill t4");
        schedulerHelper.getSchedulerInterface().killTask(id, "t4");
        //SchedulerTHelper.log("Wait for event t4 finished");
        //schedulerHelper.waitForEventTaskFinished(id, "t4");
        log("Wait for event job finished");
        //finished jobs list must have only one job
        JobInfo ji4 = schedulerHelper.waitForEventJobFinished(id);
        //task result for t4 must be TaskRestartedException
        JobState j4 = schedulerHelper.getSchedulerInterface().getJobState(id);
        TaskResult tr5 = schedulerHelper.getSchedulerInterface().getTaskResult(id, "t4");

        //check result j1
        assertEquals(2, ti1.getNumberOfExecutionLeft());
        assertTrue(tr1.getException() instanceof TaskPreemptedException);
        //check result j2
        assertEquals(3, ti2.getNumberOfExecutionLeft());
        assertTrue(tr2.getException() instanceof TaskRestartedException);
        //check result j3
        assertEquals(2, ti3.getNumberOfExecutionLeft());
        assertTrue(tr3.getException() instanceof TaskRestartedException);
        //check result tr4
        assertTrue(tr4.getException() instanceof TaskAbortedException);
        //check result j4
        assertEquals(JobStatus.CANCELED, ji4.getStatus());

        TaskStatus t1Status = getTask(j4, "t1").getStatus();
        assertTrue(t1Status.equals(TaskStatus.ABORTED) || t1Status.equals(TaskStatus.NOT_RESTARTED));

        TaskStatus t2Status = getTask(j4, "t2").getStatus();
        assertTrue(t2Status.equals(TaskStatus.ABORTED) || t2Status.equals(TaskStatus.NOT_RESTARTED));

        assertEquals(TaskStatus.FAULTY, getTask(j4, "t3").getStatus());
        assertEquals(TaskStatus.FAULTY, getTask(j4, "t4").getStatus());
        //check result tr5
        assertTrue(tr5.getException() instanceof Exception);//
    }

    private TaskState getTask(JobState job, String taskName) {
        for (TaskState ts : job.getTasks()) {
            if (ts.getId().getReadableName().equals(taskName)) {
                return ts;
            }
        }
        assertFalse("taskName '" + taskName + "' was not found in job", true);
        return null;
    }
}
