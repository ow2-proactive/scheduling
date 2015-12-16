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
package functionaltests.scripts.selection;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.Test;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import functionaltests.utils.SchedulerFunctionalTest;


/**
 * This class tests the behavior when giving an invalid selection script
 * It will start 3 dependent tasks where one will use an invalid selection script
 * when detecting an invalid script, the scheduler can't acquire the corresponding node.
 * The goal is to check that the task is not started, and the job is canceled.
 * At the end, the first task must be terminated, the second one faulty, and the last one not started.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestJobWithInvalidSelectionScript extends SchedulerFunctionalTest {

    private static URL jobDescriptor = TestJobWithInvalidSelectionScript.class
            .getResource("/functionaltests/descriptors/Job_invalidSS.xml");

    @Test
    public void testJobWithInvalidSelectionScript() throws Throwable {

        String task1Name = "task1";
        String task2Name = "task2";
        String task3Name = "task3";
        //cannot use SchedulerTHelper.testJobsubmission because
        //task 3 is never executed so no event can received
        //regarding this task.
        JobId id = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());
        //check events reception
        log("Job submitted, id " + id.toString());
        log("Waiting for jobSubmitted");
        schedulerHelper.waitForEventJobSubmitted(id);
        log("Waiting for job running");
        schedulerHelper.waitForEventJobRunning(id);
        log("Waiting for task running : " + task1Name);
        schedulerHelper.waitForEventTaskRunning(id, task1Name);
        log("Waiting for task finished : " + task1Name);
        schedulerHelper.waitForEventTaskFinished(id, task1Name);

        //second task will not even start
        try {
            log("Waiting for task *running : " + task2Name);
            schedulerHelper.waitForEventTaskRunning(id, task2Name, 2000);
            log("Waiting for task *finished : " + task2Name);
            schedulerHelper.waitForEventTaskFinished(id, task2Name, 2000);
            //should always go in the catch
            fail();
        } catch (ProActiveTimeoutException expected) {
        }

        //task 3 should not be started
        boolean task3Started = false;

        try {
            schedulerHelper.waitForEventTaskRunning(id, task3Name, 1000);
            //should always go in the catch
            fail();
        } catch (ProActiveTimeoutException e) {
        }

        schedulerHelper.killJob(id.toString());
        JobInfo jobInfo = schedulerHelper.waitForEventJobFinished(id);

        JobResult res = schedulerHelper.getJobResult(id);
        Map<String, TaskResult> results = res.getAllResults();
        //check that all tasks results are defined
        assertNotNull(results.get("task1").value());
        assertNull(results.get("task2"));
        assertNull(results.get("task3"));

        assertEquals(JobStatus.KILLED, jobInfo.getStatus());
        assertEquals(1, jobInfo.getNumberOfFinishedTasks());
        assertEquals(0, jobInfo.getNumberOfRunningTasks());
        assertEquals(0, jobInfo.getNumberOfPendingTasks());
    }
}
