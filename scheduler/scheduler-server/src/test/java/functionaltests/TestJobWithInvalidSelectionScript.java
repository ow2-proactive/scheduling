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

import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.junit.Assert;

import static org.junit.Assert.fail;


/**
 * This class tests the behavior when giving an invalid selection script
 * It will start 3 dependent tasks where one will use an invalid selection script
 * when detecting an invalid script, the scheduler can't acquire the corresponding node.
 * The goal is to check that the task is not started, and the job is canceled.
 * At the end, the first task must be terminated, the second one faulty, and the last one not started.
 *
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive Scheduling 1.0
 */
public class TestJobWithInvalidSelectionScript extends SchedulerConsecutive {

    private static URL jobDescriptor = TestJobWithInvalidSelectionScript.class
            .getResource("/functionaltests/descriptors/Job_invalidSS.xml");

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {

        String task1Name = "task1";
        String task2Name = "task2";
        String task3Name = "task3";
        //cannot use SchedulerTHelper.testJobsubmission because
        //task 3 is never executed so no event can received
        //regarding this task.
        JobId id = SchedulerTHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());
        //check events reception
        SchedulerTHelper.log("Job submitted, id " + id.toString());
        SchedulerTHelper.log("Waiting for jobSubmitted");
        SchedulerTHelper.waitForEventJobSubmitted(id);
        SchedulerTHelper.log("Waiting for job running");
        SchedulerTHelper.waitForEventJobRunning(id);
        SchedulerTHelper.log("Waiting for task running : " + task1Name);
        SchedulerTHelper.waitForEventTaskRunning(id, task1Name);
        SchedulerTHelper.log("Waiting for task finished : " + task1Name);
        SchedulerTHelper.waitForEventTaskFinished(id, task1Name);

        //second task will not even start
        try {
            SchedulerTHelper.log("Waiting for task *running : " + task2Name);
            SchedulerTHelper.waitForEventTaskRunning(id, task2Name, 2000);
            SchedulerTHelper.log("Waiting for task *finished : " + task2Name);
            SchedulerTHelper.waitForEventTaskFinished(id, task2Name, 2000);
            //should always go in the catch
            fail();
        } catch (ProActiveTimeoutException expected) {
        }

        //task 3 should not be started
        boolean task3Started = false;

        try {
            SchedulerTHelper.waitForEventTaskRunning(id, task3Name, 1000);
            //should always go in the catch
            task3Started = true;
        } catch (ProActiveTimeoutException e) {
        }

        Assert.assertFalse(task3Started);

        SchedulerTHelper.killJob(id.toString());
        JobInfo jobInfo = SchedulerTHelper.waitForEventJobFinished(id);

        JobResult res = SchedulerTHelper.getJobResult(id);
        Map<String, TaskResult> results = res.getAllResults();
        //check that all tasks results are defined
        Assert.assertNotNull(results.get("task1").value());
        Assert.assertNull(results.get("task2"));
        Assert.assertNull(results.get("task3"));

        Assert.assertEquals(JobStatus.KILLED, jobInfo.getStatus());
        Assert.assertEquals(1, jobInfo.getNumberOfFinishedTasks());
        Assert.assertEquals(0, jobInfo.getNumberOfRunningTasks());
        Assert.assertEquals(0, jobInfo.getNumberOfPendingTasks());
    }
}
