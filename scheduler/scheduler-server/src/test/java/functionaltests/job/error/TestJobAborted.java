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
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import java.io.File;
import java.net.URL;
import java.util.Map;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


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

    private static URL jobDescriptor = TestJobAborted.class
            .getResource("/functionaltests/descriptors/Job_Aborted.xml");

    @Test
    public void testJobAborted() throws Throwable {

        String task1Name = "task1";
        String task2Name = "task2";

        log("Test 1 : Submitting job...");

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
