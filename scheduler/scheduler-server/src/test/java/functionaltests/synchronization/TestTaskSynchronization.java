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
package functionaltests.synchronization;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * This test submits a job which performs task synchronization using the Synchronization API script bindings.
 *
 * The test simply ensures that the job terminates without errors and that operations on the synchronization store are found in the job server logs.
 * @author ActiveEon Team
 * @since 11/04/2018
 */
public class TestTaskSynchronization extends SchedulerFunctionalTestNoRestart {
    @Test
    public void testTaskSynchronization() throws Exception {
        JobId jobId = schedulerHelper.testJobSubmission(new File(TestTaskSynchronization.class.getResource("/functionaltests/descriptors/Job_TaskSynchronization.xml")
                                                                                              .toURI()).getAbsolutePath());

        String serverLogs = schedulerHelper.getJobServerLogs(jobId);
        // test that operation initiated by task script on the store is printed in the logs
        Assert.assertTrue(serverLogs.contains("Put true on key 'lock1'"));
        // test that operation initiated by clean script on the store is printed in the logs
        Assert.assertTrue(serverLogs.contains("Put true on key 'lock2'"));
    }

}
