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
package functionaltests.policy.license;

import static functionaltests.utils.SchedulerTHelper.log;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskState;

import functionaltests.utils.SchedulerFunctionalTestLicensePolicy;


public class TestLicensePolicy extends SchedulerFunctionalTestLicensePolicy {

    private static URL jobDescriptor = TestLicensePolicy.class.getResource("/functionaltests/descriptors/Job_simple_license_policy.xml");

    /**
     * Tests that two independent tasks do not run at the same time, due to license limitation.
     *
     * @throws Exception
     */
    /*
     * @Test
     * public void testLicensePolicy() throws Throwable {
     * 
     * JobId jobId = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());
     * 
     * log("Waiting for job finished");
     * schedulerHelper.waitForEventJobFinished(jobId);
     * 
     * Scheduler scheduler = schedulerHelper.getSchedulerInterface();
     * TaskState taskState0 = scheduler.getJobState(jobId).getTasks().get(0);
     * TaskState taskState1 = scheduler.getJobState(jobId).getTasks().get(1);
     * 
     * boolean tasksExecutedOneByOne = (taskState0.getFinishedTime() < taskState1.getStartTime()) ||
     * (taskState1.getFinishedTime() < taskState0.getStartTime());
     * Assert.assertTrue(tasksExecutedOneByOne);
     * }
     */

}
