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
package functionaltests.job.taskkill;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * This class tests the way a task can kill a node.
 * It will start 3 dependent tasks where one will kill the node.
 * The task will be restarted as defined in the Scheduler ini file.
 * The goal is to check that the killing task is restarted the expected number of time, and ensure the task and job will terminate.
 * At the end, the first task must be terminated, the second one failed, and the last one not started.
 * It will also check that it's not relative to the 'cancelJobOnError' value.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestJobKilled extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static URL jobDescriptor = TestJobKilled.class.getResource("/functionaltests/descriptors/Job_Killed.xml");

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        schedulerHelper = new SchedulerTHelper(true,
                                               new File(SchedulerTHelper.class.getResource("/functionaltests/config/scheduler-nonforkedscripttasks.ini")
                                                                              .toURI()).getAbsolutePath());
    }

    @Test
    public void testJobKilled() throws Throwable {
        String task1Name = "task1";
        String task2Name = "task2";
        String task3Name = "task3";

        // cannot use SchedulerTHelper.testJobsubmission because
        // task 3 is never executed so no event can be received
        // regarding this task.
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

        log("Waiting for task running : " + task2Name);
        schedulerHelper.waitForEventTaskRunning(id, task2Name);
        log("Waiting for task finished : " + task2Name);
        schedulerHelper.waitForEventTaskFinished(id, task2Name);

        log("Waiting for job finished");
        schedulerHelper.waitForEventJobFinished(id);

        try {
            schedulerHelper.waitForEventTaskRunning(id, task3Name, 1000);
            fail("Task 3 should not be started");
        } catch (ProActiveTimeoutException expected) {
            // expected
        }

        JobResult res = schedulerHelper.getJobResult(id);
        Map<String, TaskResult> results = res.getAllResults();
        //check that all tasks results are defined
        assertNotNull(results.get("task1").value());
        assertNotNull(results.get("task2").getException());
    }

}
