/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package functionaltests;

import java.io.File;
import java.net.URL;

import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.junit.Assert;


/**
 * Checking that job removal works for pending/running/finished jobs
 * 
 */
public class TestJobRemoved extends SchedulerConsecutive {

    private static URL pendingJob = TestJobRemoved.class
            .getResource("/functionaltests/descriptors/Job_pending.xml");
    private static URL runningJob = TestJobRemoved.class
            .getResource("/functionaltests/descriptors/Job_running.xml");
    private static URL simpleJob = TestJobRemoved.class
            .getResource("/functionaltests/descriptors/Job_simple.xml");

    private final static int EVENT_TIMEOUT = 5000;

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void action() throws Throwable {

        SchedulerState state = SchedulerTHelper.getSchedulerInterface().getState();
        int jobsNumber = state.getFinishedJobs().size() + state.getPendingJobs().size() +
            state.getRunningJobs().size();

        JobId id = SchedulerTHelper.submitJob(new File(pendingJob.toURI()).getAbsolutePath(),
                ExecutionMode.normal);
        SchedulerTHelper.log("Job submitted, id " + id.toString());
        SchedulerTHelper.waitForEventJobSubmitted(id);

        // removing pending job
        SchedulerTHelper.removeJob(id);

        int jobsDelta = state.getFinishedJobs().size() + state.getPendingJobs().size() +
            state.getRunningJobs().size() - jobsNumber;

        Assert.assertEquals(0, jobsDelta);
        SchedulerTHelper.waitForEventJobRemoved(id, EVENT_TIMEOUT);

        id = SchedulerTHelper.submitJob(new File(runningJob.toURI()).getAbsolutePath(), ExecutionMode.normal);
        SchedulerTHelper.log("Job submitted, id " + id.toString());
        SchedulerTHelper.waitForEventJobRunning(id);

        // removing running job
        SchedulerTHelper.removeJob(id);
        // it should kill the job
        SchedulerTHelper.waitForEventJobFinished(id);

        jobsDelta = state.getFinishedJobs().size() + state.getPendingJobs().size() +
            state.getRunningJobs().size() - jobsNumber;

        Assert.assertEquals(0, jobsDelta);

        id = SchedulerTHelper.submitJob(new File(simpleJob.toURI()).getAbsolutePath(), ExecutionMode.normal);
        SchedulerTHelper.log("Job submitted, id " + id.toString());
        SchedulerTHelper.waitForEventJobFinished(id);

        // removing finished job
        SchedulerTHelper.removeJob(id);

        jobsDelta = state.getFinishedJobs().size() + state.getPendingJobs().size() +
            state.getRunningJobs().size() - jobsNumber;

        Assert.assertEquals(0, jobsDelta);
    }
}
