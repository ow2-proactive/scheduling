/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.job;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.utils.SchedulerFunctionalTest;


/**
 * Checking that job removal works for pending/running/finished jobs
 *
 */
public class TestJobRemoved extends SchedulerFunctionalTest {

    private static URL pendingJob = TestJobRemoved.class
            .getResource("/functionaltests/descriptors/Job_pending.xml");
    private static URL runningJob = TestJobRemoved.class
            .getResource("/functionaltests/descriptors/Job_running.xml");
    private static URL simpleJob = TestJobRemoved.class
            .getResource("/functionaltests/descriptors/Job_simple.xml");

    private final static int EVENT_TIMEOUT = 5000;

    @Test
    public void testJobRemoved() throws Throwable {

        SchedulerState state = schedulerHelper.getSchedulerInterface().getState();
        int jobsNumber = state.getFinishedJobs().size() + state.getPendingJobs().size() +
            state.getRunningJobs().size();

        JobId id = schedulerHelper.submitJob(new File(pendingJob.toURI()).getAbsolutePath());
        log("Job submitted, id " + id.toString());
        schedulerHelper.waitForEventJobSubmitted(id);

        // removing pending job
        schedulerHelper.removeJob(id);

        int jobsDelta = state.getFinishedJobs().size() + state.getPendingJobs().size() +
            state.getRunningJobs().size() - jobsNumber;

        assertEquals(0, jobsDelta);
        schedulerHelper.waitForEventJobRemoved(id, EVENT_TIMEOUT);

        id = schedulerHelper.submitJob(new File(runningJob.toURI()).getAbsolutePath());
        log("Job submitted, id " + id.toString());
        schedulerHelper.waitForEventJobRunning(id);

        // removing running job
        schedulerHelper.removeJob(id);
        // it should kill the job
        schedulerHelper.waitForEventJobFinished(id);

        jobsDelta = state.getFinishedJobs().size() + state.getPendingJobs().size() +
            state.getRunningJobs().size() - jobsNumber;

        assertEquals(0, jobsDelta);

        id = schedulerHelper.submitJob(new File(simpleJob.toURI()).getAbsolutePath());
        log("Job submitted, id " + id.toString());
        schedulerHelper.waitForEventJobFinished(id);

        // removing finished job
        schedulerHelper.removeJob(id);

        jobsDelta = state.getFinishedJobs().size() + state.getPendingJobs().size() +
            state.getRunningJobs().size() - jobsNumber;

        assertEquals(0, jobsDelta);

        schedulerHelper.checkNodesAreClean();
    }
}
