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
package functionaltests.job.multinodes;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.utils.SchedulerFunctionalTestWithRestart;


/**
 * Test checks that walltime parameter of multinode job is correctly
 * taken into account.
 */
public class TestJobMultiNodesWalltime extends SchedulerFunctionalTestWithRestart {

    private static URL jobDescriptor = TestJobMultiNodesWalltime.class.getResource("/functionaltests/descriptors/Job_MultiNodes_walltime.xml");

    private static final long TIMEOUT = 30000;

    @Test
    public void testJobMultiNodesWalltime() throws Throwable {
        //submit job
        JobId id = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());
        //connect to RM
        schedulerHelper.addExtraNodes(3);

        //wait job is running
        schedulerHelper.waitForEventJobRunning(id);

        //wait for job to be finished
        schedulerHelper.waitForEventJobFinished(id, TIMEOUT);

        //remove job
        schedulerHelper.removeJob(id);
        schedulerHelper.waitForEventJobRemoved(id);
    }
}
