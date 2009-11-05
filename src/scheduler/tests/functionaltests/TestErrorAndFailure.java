/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.util.Map;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import functionalTests.FunctionalTest;


/**
 * This class tests a basic actions of a job submission to ProActive scheduler :
 * Connection to scheduler, with authentication
 * Register a monitor to Scheduler in order to receive events concerning
 * job submission.
 * 
 * This test will try many kind of possible errors.
 * The goal for this test is to terminate. If the Test timeout is reached, it is considered as failed.
 * Possible problems may come from many error count. If this job finish in a
 * reasonable time, it is considered that it passed the test.
 * Every events coming from the scheduler are also checked.
 * 
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive Scheduling 1.0
 */
public class TestErrorAndFailure extends FunctionalTest {

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {

        String URLbegin = System.getProperty("pa.scheduler.home") + "/";
        String javaCmd = System.getProperty("java.home") + "/bin/java";
        SchedulerTHelper.log("Test 1 : Creating job...");
        //creating job
        TaskFlowJob submittedJob = new TaskFlowJob();
        submittedJob.setName("Test 12 tasks");
        submittedJob
                .setDescription("12 tasks job testing the behavior of error code and normal task ending.");
        submittedJob.setMaxNumberOfExecution(10);
        NativeTask finalTask = new NativeTask();
        finalTask.setName("TestMerge");
        finalTask.setCommandLine(new String[] { javaCmd, "-cp", URLbegin + "classes/scheduler/",
                "org.ow2.proactive.scheduler.examples.NativeTestWithRandomDefault", "final" });
        for (int i = 1; i < 12; i++) {
            NativeTask task = new NativeTask();
            task.setName("Test" + i);
            task.setCommandLine(new String[] { javaCmd, "-cp", URLbegin + "classes/scheduler/",
                    "org.ow2.proactive.scheduler.examples.NativeTestWithRandomDefault", "0" });
            finalTask.addDependence(task);
            submittedJob.addTask(task);
        }

        submittedJob.addTask(finalTask);

        //test submission and event reception
        JobId id = SchedulerTHelper.submitJob(submittedJob);
        SchedulerTHelper.log("Job submitted, id " + id.toString());
        SchedulerTHelper.log("Waiting for jobSubmitted Event");
        Job receivedJob = SchedulerTHelper.waitForEventJobSubmitted(id);
        Assert.assertEquals(receivedJob.getId(), id);
        SchedulerTHelper.log("Waiting for job running");
        JobInfo jInfo = SchedulerTHelper.waitForEventJobRunning(id);

        Assert.assertEquals(jInfo.getJobId(), id);

        //task running event may occurs several time for this test
        //TODO how to check that ?
        for (Task t : ((TaskFlowJob) submittedJob).getTasks()) {
            SchedulerTHelper.log("Waiting for task running : " + t.getName());
            SchedulerTHelper.waitForEventTaskRunning(id, t.getName());
        }

        for (Task t : ((TaskFlowJob) submittedJob).getTasks()) {
            SchedulerTHelper.log("Waiting for task finished : " + t.getName());
            SchedulerTHelper.waitForEventTaskFinished(id, t.getName());

        }

        SchedulerTHelper.log("Waiting for job finished");
        jInfo = SchedulerTHelper.waitForEventJobFinished(id);

        Assert.assertEquals(JobStatus.FINISHED, jInfo.getStatus());

        //check job results
        JobResult res = SchedulerTHelper.getJobResult(id);
        //Check the results
        Map<String, TaskResult> results = res.getAllResults();
        //check that number of results correspond to number of tasks
        Assert.assertEquals(submittedJob.getTasks().size(), results.size());

        //remove jobs and check its event
        SchedulerTHelper.removeJob(id);
        SchedulerTHelper.log("Waiting for job removed");
        jInfo = SchedulerTHelper.waitForEventJobRemoved(id);
        Assert.assertEquals(JobStatus.FINISHED, jInfo.getStatus());
        Assert.assertEquals(jInfo.getJobId(), id);

    }
}
