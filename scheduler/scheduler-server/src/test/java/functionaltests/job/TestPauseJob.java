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

import java.nio.file.Path;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.util.FileLock;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;

import static functionaltests.utils.SchedulerTHelper.log;
import static functionaltests.job.recover.TestPauseJobRecover.createJob;
import static functionaltests.job.recover.TestPauseJobRecover.getTaskState;
import static org.junit.Assert.*;


/**
 * Test checks that once a job is paused the execution of all tasks except running
 * is postponed.
 */
public class TestPauseJob extends SchedulerFunctionalTest {

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

}
