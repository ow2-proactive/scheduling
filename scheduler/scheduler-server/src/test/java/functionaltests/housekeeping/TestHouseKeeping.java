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
package functionaltests.housekeeping;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * @author ActiveEon Team
 * @since 20/06/2020
 */
public class TestHouseKeeping extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static URL simpleJob = TestHouseKeeping.class.getResource("/functionaltests/descriptors/Job_houseKeeping.xml");

    private static URL jobWithRemoveTimeGI = TestHouseKeeping.class.getResource("/functionaltests/descriptors/Job_houseKeeping_With_GI.xml");

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        schedulerHelper.log("Start Scheduler in non-fork mode.");
        schedulerHelper = new SchedulerTHelper(false,
                                               new File(TestHouseKeeping.class.getResource("/functionaltests/config/functionalTSchedulerProperties-houseKeeping.ini")
                                                                              .toURI()).getAbsolutePath());
    }

    @Test(timeout = 900000)
    public void testHouseKeeping() throws Exception {

        JobId jobWithRemoveTimeGIJobId = schedulerHelper.submitJob(new File(jobWithRemoveTimeGI.toURI()).getAbsolutePath());
        JobId simpleJobJobId = schedulerHelper.submitJob(new File(simpleJob.toURI()).getAbsolutePath());

        schedulerHelper.waitForEventJobFinished(jobWithRemoveTimeGIJobId);
        schedulerHelper.waitForEventJobFinished(simpleJobJobId);

        // Wait at most two minutes before the first job is removed (configured time = 1 minute + cron loop= every minute)
        schedulerHelper.waitForEventJobRemoved(jobWithRemoveTimeGIJobId, (60 + 70) * 1000);
        JobResult notYetRemovedJobResult = schedulerHelper.getJobResult(simpleJobJobId);
        Assert.assertNotNull("Job " + simpleJobJobId + " should not be removed yet", notYetRemovedJobResult);

        // Wait another two minutes before the second job is removed (configured time = 3 minutes - previous wait + cron loop = every minute)
        schedulerHelper.waitForEventJobRemoved(simpleJobJobId, (60 + 70) * 1000);
    }
}
