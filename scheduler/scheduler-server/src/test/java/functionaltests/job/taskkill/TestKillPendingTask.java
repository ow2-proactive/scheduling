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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * This class tests the ability to kill a task in pending state.
 *
 * In order to do that it starts a scheduler with no local nodes, submits a job, kill its two tasks
 * and make sure the job terminates.
 *
 * @author The ProActive Team
 */
public class TestKillPendingTask extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static URL jobDescriptor = TestKillPendingTask.class.getResource("/functionaltests/descriptors/Job_simple.xml");

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        schedulerHelper = new SchedulerTHelper(true, true);
    }

    @Test
    public void testKillPendingTasks() throws Throwable {
        String task1Name = "task1";
        String task2Name = "task2";

        JobId id = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());
        //check events reception
        log("Job submitted, id " + id.toString());
        log("Waiting for jobSubmitted");
        schedulerHelper.waitForEventJobSubmitted(id);

        schedulerHelper.killTask(id.toString(), task1Name);
        log("Waiting for task finished : " + task1Name);
        schedulerHelper.waitForEventTaskFinished(id, task1Name);

        schedulerHelper.killTask(id.toString(), task2Name);
        log("Waiting for task finished : " + task2Name);
        schedulerHelper.waitForEventTaskFinished(id, task2Name);

        log("Waiting for job finished");
        schedulerHelper.waitForEventJobFinished(id);

        JobResult res = schedulerHelper.getJobResult(id);
        Map<String, TaskResult> results = res.getAllResults();
        //check that all tasks results are defined
        assertTrue(results.get(task1Name).hadException());
        assertNotNull(results.get(task1Name).getException());
        assertTrue(results.get(task2Name).hadException());
        assertNotNull(results.get(task2Name).getException());
    }

}
