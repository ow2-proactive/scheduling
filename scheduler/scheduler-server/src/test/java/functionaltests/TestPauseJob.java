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

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.junit.Assert;
import org.junit.Test;

import functionaltests.utils.ProActiveLock;

import static functionaltests.TestPauseJobRecover.createJob;
import static functionaltests.TestPauseJobRecover.getTaskState;


/**
 * 
 * Test checks that once a job is paused the execution of all tasks except running
 * is postponed.
 *   
 */
public class TestPauseJob extends RMFunctionalTest {

    @Test
    public void test() throws Throwable {

        ProActiveLock communicationObject = PAActiveObject.newActive(ProActiveLock.class, new Object[] {});

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

        //let the task1 finish
        communicationObject.unlock();

        System.out.println("Checking is the status of task2 remains unchanged");
        Thread.sleep(10000);
        js = SchedulerTHelper.getSchedulerInterface().getJobState(jobId);
        Assert.assertEquals(TaskStatus.PAUSED, getTaskState("task2", js).getStatus());

    }
}
