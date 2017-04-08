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
package functionaltests.job.log;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.rm.nodesource.TestJobNodeAccessToken;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * Checks that if listening to jobs is disabled in the configuration 
 * file, the {@link Scheduler#listenJobLogs} method throws an exception 
 */
public class TestDisabledListenJobLogs extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static URL simpleJob = TestJobNodeAccessToken.class.getResource("/functionaltests/descriptors/Job_simple.xml");

    private static final String EXPECTED_MESSAGE = "Listening to job logs is disabled by administrator";

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        log("Creating the scheduler");
        schedulerHelper = new SchedulerTHelper(true,
                                               new File(SchedulerTHelper.class.getResource("/functionaltests/config/scheduler-disablelistenjoblogs.ini")
                                                                              .toURI()).getAbsolutePath());

    }

    @Test
    public void test() throws Throwable {

        log("Submitting job");
        JobId id = schedulerHelper.submitJob(new File(simpleJob.toURI()).getAbsolutePath());
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        try {
            scheduler.listenJobLogs(id, null);
            fail("listenJobLogs should throw an exception");
        } catch (PermissionException ex) {
            assertEquals(EXPECTED_MESSAGE, ex.getMessage());
        }

        try {
            scheduler.listenJobLogs(id.value(), null);
            fail("listenJobLogs should throw an exception");
        } catch (PermissionException ex) {
            assertEquals(EXPECTED_MESSAGE, ex.getMessage());
        }

    }
}
