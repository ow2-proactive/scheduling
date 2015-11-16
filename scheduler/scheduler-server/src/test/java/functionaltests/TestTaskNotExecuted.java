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

import org.ow2.proactive.scheduler.common.exception.TaskCouldNotRestartException;
import org.ow2.proactive.scheduler.common.exception.TaskCouldNotStartException;
import org.ow2.proactive.scheduler.common.exception.TaskSkippedException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
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
public class TestTaskNotExecuted extends SchedulerConsecutive {

    private static URL jobDescriptor1 = TestTaskNotExecuted.class
            .getResource("/functionaltests/descriptors/Job_TaskCouldNotStart.xml");
    private static URL jobDescriptor2 = TestTaskNotExecuted.class
            .getResource("/functionaltests/descriptors/Job_TaskCouldNotRestart.xml");
    private static URL jobDescriptor3 = TestTaskNotExecuted.class
            .getResource("/functionaltests/descriptors/Job_TaskSkipped.xml");

    /**
    * Tests start here.
    *
    * @throws Throwable any exception that can be thrown during the test.
    */
    @org.junit.Test
    public void action() throws Throwable {

        SchedulerTHelper.log("Submitting job 1");
        JobId id1 = SchedulerTHelper.submitJob(new File(jobDescriptor1.toURI()).getAbsolutePath(),
                UserType.ADMIN);
        SchedulerTHelper.log("Wait for event job 1 submitted");
        SchedulerTHelper.waitForEventJobSubmitted(id1);
        SchedulerTHelper.log("Wait for event t1 running");
        SchedulerTHelper.waitForEventTaskRunning(id1, "t0");
        SchedulerTHelper.waitForEventTaskFinished(id1, "t0");
        SchedulerTHelper.waitForEventJobFinished(id1);

        TaskResult tr1 = SchedulerTHelper.getSchedulerInterface().getTaskResult(id1, "t1");
        System.out.println(tr1.getException());
        Assert.assertTrue(tr1.getException() instanceof TaskCouldNotStartException);

        SchedulerTHelper.log("Submitting job 2");
        JobId id2 = SchedulerTHelper.submitJob(new File(jobDescriptor2.toURI()).getAbsolutePath(),
                UserType.ADMIN);
        SchedulerTHelper.log("Wait for event job 2 submitted");
        SchedulerTHelper.waitForEventJobSubmitted(id2);
        SchedulerTHelper.log("Wait for event t2 running");
        SchedulerTHelper.waitForEventTaskRunning(id2, "t2");
        SchedulerTHelper.log("Restarting task");
        SchedulerTHelper.getSchedulerInterface().restartTask(id2, "t2", Integer.MAX_VALUE);

        //TaskInfo ti2 = SchedulerTHelper.waitForEventTaskWaitingForRestart(id2, "t2");

        TaskInfo ti2 = SchedulerTHelper.waitForEventTaskWaitingForRestart(id2, "t2");
        SchedulerTHelper.getSchedulerInterface().killJob(id2);

        SchedulerTHelper.waitForEventJobFinished(id2);

        JobState jobState = SchedulerTHelper.getSchedulerInterface().getJobState(id2);
        Assert.assertEquals(TaskStatus.NOT_RESTARTED, jobState.getTasks().get(0).getStatus());
        TaskResult tr2 = SchedulerTHelper.getSchedulerInterface().getTaskResult(id2, "t2");
        System.out.println(tr2.getException());
        Assert.assertTrue(tr2.getException() instanceof TaskCouldNotRestartException);

        SchedulerTHelper.log("Submitting job 3");
        JobId id3 = SchedulerTHelper.submitJob(new File(jobDescriptor3.toURI()).getAbsolutePath(),
                UserType.ADMIN);
        SchedulerTHelper.log("Wait for event job 3 submitted");
        SchedulerTHelper.waitForEventJobSubmitted(id3);
        SchedulerTHelper.log("Wait for event T T1 T2 finished");
        SchedulerTHelper.waitForEventTaskFinished(id3, "T");
        SchedulerTHelper.waitForEventTaskFinished(id3, "T1");
        //SchedulerTHelper.waitForEventTaskFinished(id3, "T2");

        TaskResult tr3_1 = SchedulerTHelper.getSchedulerInterface().getTaskResult(id3, "T1");
        TaskResult tr3_2 = SchedulerTHelper.getSchedulerInterface().getTaskResult(id3, "T2");

        Assert.assertTrue(tr3_1.getException() == null);

        Assert.assertTrue(tr3_2.getException() instanceof TaskSkippedException);

    }

}