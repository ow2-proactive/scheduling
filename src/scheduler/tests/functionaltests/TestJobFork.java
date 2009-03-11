/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskStatus;

import functionalTests.FunctionalTest;


/**
 * This class tests the forked feature of a task.
 * It will start 2 couples of equal tasks. The goal is to ensure that the first couple
 * will last more than the time define in the walltime.
 * If the walltime is respected it is that the fork operation has succeed and the walltime too.
 *
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive 4.0
 */
public class TestJobFork extends FunctionalTest {

    private static String jobDescriptor = TestJobFork.class.getResource(
            "/functionaltests/descriptors/Job_fork.xml").getPath();

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {

        String task1Name = "Task1";
        String task2Name = "Task2";

        String taskForked1Name = "Fork1";
        String taskForked2Name = "Fork2";

        JobId id = SchedulerTHelper.submitJob(jobDescriptor);

        SchedulerTHelper.log("Job submitted, id " + id.toString());

        SchedulerTHelper.log("Waiting for jobSubmitted Event");
        Job receivedJob = SchedulerTHelper.waitForEventJobSubmitted(id);
        Assert.assertEquals(receivedJob.getId(), id);

        SchedulerTHelper.log("Waiting for job running");
        JobInfo jInfo = SchedulerTHelper.waitForEventJobRunning(id);
        Assert.assertEquals(jInfo.getJobId(), id);
        Assert.assertEquals(JobStatus.RUNNING, jInfo.getStatus());

        SchedulerTHelper.log("check events for task " + task1Name);
        TaskInfo tInfo = SchedulerTHelper.waitForEventTaskRunning(id, task1Name);
        Assert.assertEquals(TaskStatus.RUNNING, tInfo.getStatus());
        tInfo = SchedulerTHelper.waitForEventTaskFinished(id, task1Name);
        Assert.assertEquals(TaskStatus.FINISHED, tInfo.getStatus());

        SchedulerTHelper.log("check events for task " + task2Name);
        tInfo = SchedulerTHelper.waitForEventTaskRunning(id, task2Name);
        Assert.assertEquals(TaskStatus.RUNNING, tInfo.getStatus());
        tInfo = SchedulerTHelper.waitForEventTaskFinished(id, task2Name);
        Assert.assertEquals(TaskStatus.FINISHED, tInfo.getStatus());

        //this task reaches wall time, so finishes with faulty state
        SchedulerTHelper.log("check events for task " + taskForked1Name);
        tInfo = SchedulerTHelper.waitForEventTaskRunning(id, taskForked1Name);
        Assert.assertEquals(TaskStatus.RUNNING, tInfo.getStatus());
        tInfo = SchedulerTHelper.waitForEventTaskFinished(id, taskForked1Name);
        Assert.assertEquals(TaskStatus.FAULTY, tInfo.getStatus());

        //this task reaches wall time, so finishes with faulty state
        SchedulerTHelper.log("check events for task " + taskForked2Name);
        tInfo = SchedulerTHelper.waitForEventTaskRunning(id, taskForked2Name);
        Assert.assertEquals(TaskStatus.RUNNING, tInfo.getStatus());
        tInfo = SchedulerTHelper.waitForEventTaskFinished(id, taskForked2Name);
        Assert.assertEquals(TaskStatus.FAULTY, tInfo.getStatus());

        SchedulerTHelper.log("Waiting for job finished");
        jInfo = SchedulerTHelper.waitForEventJobFinished(id);
        Assert.assertEquals(jInfo.getJobId(), id);
        Assert.assertEquals(JobStatus.FINISHED, jInfo.getStatus());

        // check result are not null
        JobResult res = SchedulerTHelper.getJobResult(id);
        Assert.assertTrue(res.hadException());
        
        Assert.assertFalse(res.getAllResults().get(task1Name).hadException());
        Assert.assertNotNull(res.getAllResults().get(task1Name).value());
        Assert.assertNull(res.getAllResults().get(task1Name).getException());
        
        Assert.assertFalse(res.getAllResults().get(task2Name).hadException());
        Assert.assertNotNull(res.getAllResults().get(task2Name).value());
        Assert.assertNull(res.getAllResults().get(task2Name).getException());
        
        Assert.assertTrue(res.getAllResults().get(taskForked1Name).hadException());
        Assert.assertNull(res.getAllResults().get(taskForked1Name).value());
        Assert.assertNotNull(res.getAllResults().get(taskForked1Name).getException());
        
        Assert.assertTrue(res.getAllResults().get(taskForked2Name).hadException());
        Assert.assertNull(res.getAllResults().get(taskForked2Name).value());
        Assert.assertNotNull(res.getAllResults().get(taskForked2Name).getException());

        //remove job
        SchedulerTHelper.removeJob(id);
        SchedulerTHelper.waitForEventJobRemoved(id);
    }
}
