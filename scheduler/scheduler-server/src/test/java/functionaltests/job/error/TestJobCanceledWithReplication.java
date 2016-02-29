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

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import org.junit.Test;
import org.objectweb.proactive.utils.StackTraceUtil;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.examples.FailTaskConditionally;

import java.io.File;
import java.net.URL;
import java.util.Map;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;


/**
 * This class tests the cancelJobOnError feature of a task together with task
 * replication. It will start task2, which
 * has "cancelJobOnError" attribute set to true and is also replicated with
 * factor 3 (tesk2replicate and task2merge are necessary because for replication
 * to work). Tasks task2 and task2*2 will sleep for 10 seconds, task2*1
 * will throw an exception after 3 seconds. We check that the job is canceled
 * and that it is possible to retrieve the result of the faulty task (the
 * exception which caused the failure).
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 3.1
 */
public class TestJobCanceledWithReplication extends SchedulerFunctionalTestNoRestart {

    private static URL jobDescriptor = TestJobCanceledWithReplication.class
            .getResource("/functionaltests/descriptors/Job_Aborted_With_Replication.xml");

    @Test
    public void testJobCanceledWithReplication() throws Throwable {
        schedulerHelper.addExtraNodes(3);

        String faultyTaskName = "task2*1";

        log("Submitting job...");

        //job submission
        JobId id = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        //check events reception
        log("Job submitted, id " + id.toString());

        log("Waiting for jobSubmitted");

        Job receivedJob = schedulerHelper.waitForEventJobSubmitted(id);
        assertEquals(receivedJob.getId(), id);

        log("Waiting for job running");
        JobInfo jInfo = schedulerHelper.waitForEventJobRunning(id);
        assertEquals(jInfo.getJobId(), id);

        log("Waiting for job finished");
        jInfo = schedulerHelper.waitForEventJobFinished(id);
        assertEquals("Job status should be CANCELED", JobStatus.CANCELED, jInfo.getStatus());

        log("Getting job result...");
        JobResult res = schedulerHelper.getJobResult(id);

        Map<String, TaskResult> results = res.getAllResults();

        assertTrue("The number of results should be 2 [task2replicate, task2*1] but was " + results.size() + " : " + results, 2 == results.size());
        assertNotNull("Faulty task result should be an exception", results.get(faultyTaskName).getException
          ());

        String stackTrace = StackTraceUtil.getStackTrace(results.get(faultyTaskName).getException());

        assertTrue(
          "The exception message extracted from the result should match the exception thrown by the task",
          stackTrace.contains(FailTaskConditionally.EXCEPTION_MESSAGE));

        //remove jobs and check its event
        schedulerHelper.removeJob(id);
        log("Waiting for job removed");
        jInfo = schedulerHelper.waitForEventJobRemoved(id);
        assertEquals(JobStatus.CANCELED, jInfo.getStatus());
        assertEquals(jInfo.getJobId(), id);
    }
}
