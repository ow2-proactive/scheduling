/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests.job.error;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * This class tests the cancelJobOnError feature of a task.
 * It will start 2 tasks.
 * The first task last 10s, the second one will throw an exception after 3 seconds.
 * This last task has its flag 'cancelJobOnError' as true.
 * The goal is to check that this job will terminate with the exception.
 * The first task must be canceled.
 * The number of finish task will be 1 at the end.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestJobAborted extends SchedulerFunctionalTestNoRestart {

    private static URL jobDescriptor = TestJobAborted.class.getResource("/functionaltests/descriptors/Job_Aborted.xml");

    private static URL jobDescriptor33 = TestJobAborted.class.getResource("/functionaltests/descriptors/Job_Aborted_Schema33.xml");

    @Test
    public void testJobAborted() throws Throwable {
        String jobDescriptorPath = new File(jobDescriptor.toURI()).getAbsolutePath();
        testJobAborted(jobDescriptorPath);
    }

    @Test
    public void testJobAbortedCompatibilitySchema33() throws Throwable {
        String jobDescriptorPath = new File(jobDescriptor33.toURI()).getAbsolutePath();
        testJobAborted(jobDescriptorPath);
    }

    private void testJobAborted(String jobDescriptorPath) throws Exception {
        String task1Name = "task1";
        String task2Name = "task2";

        log("Test 1 : Submitting job...");

        //job submission
        JobId id = schedulerHelper.submitJob(jobDescriptorPath);

        //check events reception
        log("Job submitted, id " + id.toString());

        log("Waiting for jobSubmitted");

        Job receivedJob = schedulerHelper.waitForEventJobSubmitted(id);
        assertEquals(receivedJob.getId(), id);

        log("Waiting for job running");
        JobInfo jInfo = schedulerHelper.waitForEventJobRunning(id);
        assertEquals(jInfo.getJobId(), id);

        log("Waiting for task running : " + task1Name);
        schedulerHelper.waitForEventTaskRunning(id, task1Name);
        log("Waiting for task running : " + task2Name);
        schedulerHelper.waitForEventTaskRunning(id, task2Name);

        log("Waiting for task finished : " + task2Name);
        schedulerHelper.waitForEventTaskFinished(id, task2Name);

        log("Waiting for job finished");
        jInfo = schedulerHelper.waitForEventJobFinished(id);
        assertEquals(JobStatus.CANCELED, jInfo.getStatus());

        log("Test 7 : Getting job result...");
        JobResult res = schedulerHelper.getJobResult(id);

        Map<String, TaskResult> results = res.getAllResults();

        //check that number of results correspond to 1
        assertEquals(1, results.size());
        assertNotNull(results.get("task2").getException());

        //remove jobs and check its event
        schedulerHelper.removeJob(id);
        log("Waiting for job removed");
        jInfo = schedulerHelper.waitForEventJobRemoved(id);
        assertEquals(JobStatus.CANCELED, jInfo.getStatus());
        assertEquals(jInfo.getJobId(), id);
    }
}
