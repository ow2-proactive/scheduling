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
package functionaltests.job;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.utils.SchedulerFunctionalTestWithRestart;


/**
 * Checking that job removal works for pending/running/finished jobs
 */
public class TestJobRemoved extends SchedulerFunctionalTestWithRestart {

    private static URL pendingJob = TestJobRemoved.class.getResource("/functionaltests/descriptors/Job_pending.xml");

    private static URL runningJob = TestJobRemoved.class.getResource("/functionaltests/descriptors/Job_running.xml");

    private static URL simpleJob = TestJobRemoved.class.getResource("/functionaltests/descriptors/Job_simple.xml");

    private final static int EVENT_TIMEOUT = 5000;

    @Test
    public void testJobRemoved() throws Throwable {

        SchedulerState state = schedulerHelper.getSchedulerInterface().getState();
        int jobsNumber = state.getFinishedJobs().size() + state.getPendingJobs().size() + state.getRunningJobs().size();

        JobId id = schedulerHelper.submitJob(new File(pendingJob.toURI()).getAbsolutePath());
        log("Job submitted, id " + id.toString());
        schedulerHelper.waitForEventJobSubmitted(id);

        // removing pending job
        schedulerHelper.removeJob(id);

        int jobsDelta = state.getFinishedJobs().size() + state.getPendingJobs().size() + state.getRunningJobs().size() -
                        jobsNumber;

        assertEquals(0, jobsDelta);
        schedulerHelper.waitForEventJobRemoved(id, EVENT_TIMEOUT);

        id = schedulerHelper.submitJob(new File(runningJob.toURI()).getAbsolutePath());
        log("Job submitted, id " + id.toString());
        schedulerHelper.waitForEventJobRunning(id);

        // removing running job
        schedulerHelper.removeJob(id);
        // it should kill the job
        schedulerHelper.waitForEventJobFinished(id);

        jobsDelta = state.getFinishedJobs().size() + state.getPendingJobs().size() + state.getRunningJobs().size() -
                    jobsNumber;

        assertEquals(0, jobsDelta);

        id = schedulerHelper.submitJob(new File(simpleJob.toURI()).getAbsolutePath());
        log("Job submitted, id " + id.toString());
        schedulerHelper.waitForEventJobFinished(id);

        // removing finished job
        schedulerHelper.removeJob(id);

        jobsDelta = state.getFinishedJobs().size() + state.getPendingJobs().size() + state.getRunningJobs().size() -
                    jobsNumber;

        assertEquals(0, jobsDelta);

        schedulerHelper.checkNodesAreClean();
    }
}
