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
import java.util.Map;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.examples.FailTaskConditionally;


/**
 * This class tests the cancelJobOnError feature of a task together with task
 * replication. It will start task1, which is an ordinary task, and task2, which
 * has "cancelJobOnError" attribute set to true and is also replicated with
 * factor 3 (tesk2replicate and task2merge are necessary because for replication
 * to work). Tasks task1, task2 and task2*2 will sleep for 30 seconds, task2*1
 * will throw an exception after 3 seconds. We check that the job is canceled
 * and that it is possible to retrieve the result of the faulty task (the
 * exception which caused the failure).
 * 
 * @author The ProActive Team
 * @date 18 08 2011
 * @since ProActive Scheduling 3.1
 */
public class TestJobCanceledWithReplication extends SchedulerConsecutive {

    private static URL jobDescriptor = TestJobCanceledWithReplication.class
            .getResource("/functionaltests/descriptors/Job_Aborted_With_Replication.xml");

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {
        SchedulerTHelper.getSchedulerAuth();
        RMTHelper rmHelper = RMTHelper.getDefaultInstance();
        rmHelper.createNodeSource("extra", 3);

        String faultyTaskName = "task2*1";
        String abortedTaskName = "task2*2";

        SchedulerTHelper.log("Submitting job...");

        //job submission
        JobId id = SchedulerTHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        //check events reception
        SchedulerTHelper.log("Job submitted, id " + id.toString());

        SchedulerTHelper.log("Waiting for jobSubmitted");

        Job receivedJob = SchedulerTHelper.waitForEventJobSubmitted(id);
        Assert.assertEquals(receivedJob.getId(), id);

        SchedulerTHelper.log("Waiting for job running");
        JobInfo jInfo = SchedulerTHelper.waitForEventJobRunning(id);
        Assert.assertEquals(jInfo.getJobId(), id);

        SchedulerTHelper.log("Waiting for job finished");
        jInfo = SchedulerTHelper.waitForEventJobFinished(id);
        Assert.assertEquals("Job status should be CANCELED", JobStatus.CANCELED, jInfo.getStatus());

        SchedulerTHelper.log("Getting job result...");
        JobResult res = SchedulerTHelper.getJobResult(id);

        Map<String, TaskResult> results = res.getAllResults();

        Assert.assertEquals("The number of results should be 2 (task2replicate and task2*1)", 2, results
                .size());
        Assert.assertNotNull("Faulty task result should be an exception", results.get(faultyTaskName)
                .getException());

        Assert
                .assertEquals(
                        "The exception message extracted from the result should match the exception thrown by the task",
                        FailTaskConditionally.EXCEPTION_MESSAGE, results.get(faultyTaskName).getException()
                                .getMessage());

        //remove jobs and check its event
        SchedulerTHelper.removeJob(id);
        SchedulerTHelper.log("Waiting for job removed");
        jInfo = SchedulerTHelper.waitForEventJobRemoved(id);
        Assert.assertEquals(JobStatus.CANCELED, jInfo.getStatus());
        Assert.assertEquals(jInfo.getJobId(), id);
    }
}
