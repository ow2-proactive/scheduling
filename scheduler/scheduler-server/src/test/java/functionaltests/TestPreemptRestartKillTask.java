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
import java.net.URL;

import org.junit.Assert;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
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
import org.junit.Assert;


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
 * @date 15 mar. 11
 * @since ProActive Scheduling 3.0
 */
public class TestPreemptRestartKillTask extends SchedulerConsecutive {

    private static URL jobDescriptor = TestPreemptRestartKillTask.class
            .getResource("/functionaltests/descriptors/Job_preempt_restart_kill.xml");

    /**
    * Tests start here.
    *
    * @throws Throwable any exception that can be thrown during the test.
    */
    @org.junit.Test
    public void run() throws Throwable {

        SchedulerTHelper.log("Submitting job");

        SchedulerTHelper.getSchedulerAuth();
        RMTHelper rmHelper = RMTHelper.getDefaultInstance();
        rmHelper.createNodeSource("extra", 3);

        JobId id = SchedulerTHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath(),
                UserType.ADMIN);
        SchedulerTHelper.log("Wait for event job submitted");
        SchedulerTHelper.waitForEventJobSubmitted(id);
        SchedulerTHelper.log("Wait for event t1 running");
        SchedulerTHelper.waitForEventTaskRunning(id, "t1");
        SchedulerTHelper.log("Wait for event t2 running");
        SchedulerTHelper.waitForEventTaskRunning(id, "t2");
        SchedulerTHelper.log("Wait for event t3 running");
        SchedulerTHelper.waitForEventTaskRunning(id, "t3");
        SchedulerTHelper.log("Wait for event t4 running");
        SchedulerTHelper.waitForEventTaskRunning(id, "t4");

        SchedulerTHelper.log("Preempt t1");
        SchedulerTHelper.getSchedulerInterface().preemptTask(id, "t1", 1);
        SchedulerTHelper.log("Wait for event t1 waiting for restart");
        //running jobs list must have only one job, task t1 must have number of execution to 0
        TaskInfo ti1 = SchedulerTHelper.waitForEventTaskWaitingForRestart(id, "t1");
        //task result for t1 must be available with TaskPreemptedException
        TaskResult tr1 = SchedulerTHelper.getSchedulerInterface().getTaskResult(id, "t1");

        SchedulerTHelper.log("Restart t2");
        SchedulerTHelper.getSchedulerInterface().restartTask(id, "t2", 1);
        SchedulerTHelper.log("Wait for event t2 waiting for restart");
        //running jobs list must have only one job, task t2 must have number of execution to 1
        TaskInfo ti2 = SchedulerTHelper.waitForEventTaskWaitingForRestart(id, "t2");
        //task result for t2 must be available with TaskRestartedException
        TaskResult tr2 = SchedulerTHelper.getSchedulerInterface().getTaskResult(id, "t2");
        SchedulerTHelper.log("Wait for event t2 running");

        SchedulerTHelper.waitForEventTaskRunning(id, "t2");

        SchedulerTHelper.log("Restart t2 again");
        SchedulerTHelper.getSchedulerInterface().restartTask(id, "t2", 1);
        SchedulerTHelper.log("Wait for event t2 waiting for restart again");
        //running jobs list must have only one job, task t2 must have number of execution to 2
        TaskInfo ti3 = SchedulerTHelper.waitForEventTaskWaitingForRestart(id, "t2");
        //task result for t2 must be available with TaskRestartedException
        TaskResult tr3 = SchedulerTHelper.getSchedulerInterface().getTaskResult(id, "t2");

        //ensure every tasks are running at this point
        SchedulerTHelper.waitForEventTaskRunning(id, "t1");
        SchedulerTHelper.waitForEventTaskRunning(id, "t2");

        SchedulerTHelper.log("Kill t3");
        SchedulerTHelper.getSchedulerInterface().killTask(id, "t3");
        SchedulerTHelper.log("Wait for event t3 finished");
        SchedulerTHelper.waitForEventTaskFinished(id, "t3");
        //task result for t3 must be available with TaskRestartedException
        TaskResult tr4 = SchedulerTHelper.getSchedulerInterface().getTaskResult(id, "t3");

        SchedulerTHelper.log("Kill t4");
        SchedulerTHelper.getSchedulerInterface().killTask(id, "t4");
        //SchedulerTHelper.log("Wait for event t4 finished");
        //SchedulerTHelper.waitForEventTaskFinished(id, "t4");
        SchedulerTHelper.log("Wait for event job finished");
        //finished jobs list must have only one job
        JobInfo ji4 = SchedulerTHelper.waitForEventJobFinished(id);
        //task result for t4 must be TaskRestartedException
        JobState j4 = SchedulerTHelper.getSchedulerInterface().getJobState(id);
        TaskResult tr5 = SchedulerTHelper.getSchedulerInterface().getTaskResult(id, "t4");

        //check result j1
        Assert.assertEquals(2, ti1.getNumberOfExecutionLeft());
        Assert.assertTrue(tr1.getException() instanceof TaskPreemptedException);
        //check result j2
        Assert.assertEquals(3, ti2.getNumberOfExecutionLeft());
        Assert.assertTrue(tr2.getException() instanceof TaskRestartedException);
        //check result j3
        Assert.assertEquals(2, ti3.getNumberOfExecutionLeft());
        Assert.assertTrue(tr3.getException() instanceof TaskRestartedException);
        //check result tr4
        Assert.assertTrue(tr4.getException() instanceof TaskAbortedException);
        //check result j4
        Assert.assertEquals(JobStatus.CANCELED, ji4.getStatus());
        Assert.assertEquals(TaskStatus.ABORTED, getTask(j4, "t1").getStatus());
        Assert.assertEquals(TaskStatus.ABORTED, getTask(j4, "t2").getStatus());
        Assert.assertEquals(TaskStatus.FAULTY, getTask(j4, "t3").getStatus());
        Assert.assertEquals(TaskStatus.FAULTY, getTask(j4, "t4").getStatus());
        //check result tr5
        Assert.assertTrue(tr5.getException() instanceof Exception);//
    }

    private TaskState getTask(JobState job, String taskName) {
        for (TaskState ts : job.getTasks()) {
            if (ts.getId().getReadableName().equals(taskName)) {
                return ts;
            }
        }
        Assert.assertFalse("taskName '" + taskName + "' was not found in job", true);
        return null;
    }
}
