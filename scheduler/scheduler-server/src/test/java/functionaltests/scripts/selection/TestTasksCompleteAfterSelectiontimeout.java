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
package functionaltests.scripts.selection;

import static functionaltests.utils.SchedulerTHelper.log;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;

/**
 * This class tests the way a task can kill a node. It will start 3 dependent
 * tasks where one will kill the node. The task will be restarted as defined in
 * the Scheduler ini file. The goal is to check that the killing task is
 * restarted the expected number of time, and ensure the task and job will
 * terminate. At the end, the first task must be terminated, the second one
 * failed, and the last one not started. It will also check that it's not
 * relative to the 'cancelJobOnError' value.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestTasksCompleteAfterSelectiontimeout extends SchedulerFunctionalTestWithCustomConfigAndRestart {

	private static URL jobDescriptor = TestTasksCompleteAfterSelectiontimeout.class
			.getResource("/functionaltests/descriptors/timeoutSelection.xml");

	@BeforeClass
	public static void startDedicatedScheduler() throws Exception {
		schedulerHelper = new SchedulerTHelper(true,
				new File(SchedulerTHelper.class
						.getResource("/functionaltests/config/scheduler-selectionWithShortTimeout.ini").toURI())
								.getAbsolutePath());
	}

	@Test(timeout = 30000)
	public void testJobCompleteAfterTimeout() throws Throwable {

		String task1Name = "toBeCompletetd1";
		String task2Name = "toBeCompletetd2";
		String task3Name = "toBeCompletetd3";
		String task4Name = "toBeCompletetd4";
		String task5Name = "toBeCompletetd5";

		// cannot use SchedulerTHelper.testJobsubmission because
		// task 3 is never executed so no event can be received
		// regarding this task.
		JobId id = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());
		// check events reception
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

		log("Waiting for task running : " + task3Name);
		schedulerHelper.waitForEventTaskRunning(id, task3Name);
		log("Waiting for task finished : " + task3Name);
		schedulerHelper.waitForEventTaskFinished(id, task3Name);

		log("Waiting for task running : " + task4Name);
		schedulerHelper.waitForEventTaskRunning(id, task4Name);
		log("Waiting for task finished : " + task4Name);
		schedulerHelper.waitForEventTaskFinished(id, task4Name);

		log("Waiting for task running : " + task5Name);
		schedulerHelper.waitForEventTaskRunning(id, task5Name);
		log("Waiting for task finished : " + task5Name);
		schedulerHelper.waitForEventTaskFinished(id, task5Name);

	}

}
