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
package functionaltests.job.error;

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
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;


public class TestTaskNotExecuted extends SchedulerFunctionalTest {

    private static URL jobDescriptor1 = TestTaskNotExecuted.class
            .getResource("/functionaltests/descriptors/Job_TaskCouldNotStart.xml");
    private static URL jobDescriptor2 = TestTaskNotExecuted.class
            .getResource("/functionaltests/descriptors/Job_TaskCouldNotRestart.xml");
    private static URL jobDescriptor3 = TestTaskNotExecuted.class
            .getResource("/functionaltests/descriptors/Job_TaskSkipped.xml");

    @Test
    public void run() throws Throwable {

        log("Submitting job 1");
        JobId id1 = schedulerHelper.submitJob(new File(jobDescriptor1.toURI()).getAbsolutePath());
        log("Wait for event job 1 submitted");
        schedulerHelper.waitForEventJobSubmitted(id1);
        log("Wait for event t1 running");
        schedulerHelper.waitForEventTaskRunning(id1, "t0");
        schedulerHelper.waitForEventTaskFinished(id1, "t0");
        schedulerHelper.waitForEventJobFinished(id1);

        TaskResult tr1 = schedulerHelper.getSchedulerInterface().getTaskResult(id1, "t1");
        assertTrue(tr1.getException() instanceof TaskCouldNotStartException);

        log("Submitting job 2");
        JobId id2 = schedulerHelper.submitJob(new File(jobDescriptor2.toURI()).getAbsolutePath());
        log("Wait for event job 2 submitted");
        schedulerHelper.waitForEventJobSubmitted(id2);
        log("Wait for event t2 running");
        schedulerHelper.waitForEventTaskRunning(id2, "t2");
        log("Restarting task");
        schedulerHelper.getSchedulerInterface().restartTask(id2, "t2", Integer.MAX_VALUE);

        TaskInfo ti2 = schedulerHelper.waitForEventTaskWaitingForRestart(id2, "t2");
        schedulerHelper.getSchedulerInterface().killJob(id2);

        schedulerHelper.waitForEventJobFinished(id2);

        JobState jobState = schedulerHelper.getSchedulerInterface().getJobState(id2);
        Assert.assertEquals(TaskStatus.NOT_RESTARTED, jobState.getTasks().get(0).getStatus());
        TaskResult tr2 = schedulerHelper.getSchedulerInterface().getTaskResult(id2, "t2");
        assertTrue(tr2.getException() instanceof TaskCouldNotRestartException);

        log("Submitting job 3");
        JobId id3 = schedulerHelper.submitJob(new File(jobDescriptor3.toURI()).getAbsolutePath());
        log("Wait for event job 3 submitted");
        schedulerHelper.waitForEventJobSubmitted(id3);
        log("Wait for event T T1 T2 finished");
        schedulerHelper.waitForEventTaskFinished(id3, "T");
        schedulerHelper.waitForEventTaskFinished(id3, "T1");

        TaskResult tr3_1 = schedulerHelper.getSchedulerInterface().getTaskResult(id3, "T1");
        TaskResult tr3_2 = schedulerHelper.getSchedulerInterface().getTaskResult(id3, "T2");

        assertNull(tr3_1.getException());
        assertTrue(tr3_2.getException() instanceof TaskSkippedException);

    }

}