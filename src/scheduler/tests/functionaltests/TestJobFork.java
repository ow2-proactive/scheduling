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
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory_stax;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;


/**
 * This class tests the forked feature of a task.
 * It will start 2 couples of equal tasks. The goal is to ensure that the first couple
 * will last more than the time define in the walltime.
 * If the walltime is respected it is that the fork operation has succeed and the walltime too.
 *
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive Scheduling 1.0
 */
public class TestJobFork extends SchedulerConsecutive {

    private static URL jobDescriptor = TestJobFork.class
            .getResource("/functionaltests/descriptors/Job_fork.xml");

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
        TaskFlowJob job = (TaskFlowJob) JobFactory_stax.getFactory().createJob(
                new File(jobDescriptor.toURI()).getAbsolutePath());
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.windows) {
            ((NativeTask) job.getTask(task1Name)).setCommandLine("cmd", "/C", "ping", "127.0.0.1", "-n",
                    "20", ">", "NUL");
            ((NativeTask) job.getTask(taskForked1Name)).setCommandLine("cmd", "/C", "ping", "127.0.0.1",
                    "-n", "20", ">", "NUL");
        }
        JobId id = SchedulerTHelper.submitJob(job);

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

        Assert.assertFalse(res.getResult(task1Name).hadException());
        Assert.assertNull(res.getResult(task1Name).getException());

        Assert.assertFalse(res.getResult(task2Name).hadException());
        Assert.assertNull(res.getResult(task2Name).getException());

        Assert.assertFalse(res.getResult(taskForked1Name).hadException());
        Assert.assertNull(res.getResult(taskForked1Name).getException());

        Assert.assertTrue(res.getResult(taskForked2Name).hadException());
        Assert.assertNotNull(res.getResult(taskForked2Name).getException());

        //remove job
        SchedulerTHelper.removeJob(id);
        SchedulerTHelper.waitForEventJobRemoved(id);
    }
}
