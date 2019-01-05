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
package functionaltests.connectionpooling;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.utils.SchedulerFunctionalTestNonForkedModeNoRestart;


/**
 * @author ActiveEon Team
 * @since 04/01/2019
 */
public class TestSchedulerDBConnectionPooling extends SchedulerFunctionalTestNonForkedModeNoRestart {

    private static URL jobDescriptor = TestSchedulerDBConnectionPooling.class.getResource("/functionaltests/connectionpooling/DBConnectionPooling.xml");

    private static int TIMEOUT = 300; // in seconds

    /**
     * Tests that the pooled connection to the scheduler DB is done using HikariCP framework.
     *
     * @throws Exception
     */
    @Test
    public void testSchedulerDBConnectionPooling() throws Exception {
        log("test scheduler DB connection pooling");

        String jobDescriptorPath = new File(jobDescriptor.toURI()).getAbsolutePath();

        log("Test 1 : First submission...");

        //job submission
        JobId id = schedulerHelper.testJobSubmission(jobDescriptorPath);

        //check events reception
        log("Job terminated, id " + id.toString());

        assertThat(schedulerHelper.getJobResult(id).getResult("DB_task1").getOutput().getAllLogs(),
                   containsString("New connection to an external DB is created"));

        log("Test 2 : Next submissions...");

        for (int i = 0; i < 10; i++) {
            id = schedulerHelper.testJobSubmission(jobDescriptorPath);
            assertThat(schedulerHelper.getJobResult(id).getResult("DB_task1").getOutput().getAllLogs(),
                       not(containsString("New connection to an external DB is created")));
        }

    }
}
