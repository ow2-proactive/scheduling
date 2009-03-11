/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import junit.framework.Assert;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;

import functionalTests.FunctionalTest;


/**
 * This class tests the failure of the scheduler.
 * Even if it is in pending, running, or finished list, the scheduled jobs must be restarted
 * as expected after the scheduler restart.
 * This test case is about the behavior of the scheduler after a failure.
 *
 * This test will first commit 3 jobs.
 * When the each one will be in each list (pending, running, finished), the scheduler will
 * be interrupt abnormally.
 * After restart, It will check that every data, tags, status are those expected.
 * It will finally check if the scheduling process will terminate.
 *
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive 4.0
 */
public class TestJobRecover extends FunctionalTest {

    private static String jobDescriptor = TestJobRecover.class.getResource(
            "/functionaltests/descriptors/Job_PI_recover.xml").getPath();

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {

        JobId idJ1 = SchedulerTHelper.submitJob(jobDescriptor);
        JobId idJ2 = SchedulerTHelper.submitJob(jobDescriptor);
        JobId idJ3 = SchedulerTHelper.submitJob(jobDescriptor);

        SchedulerTHelper.waitForEventJobRunning(idJ1);

        SchedulerTHelper.log("Waiting for job 1 to finished");
        SchedulerTHelper.waitForFinishedJob(idJ1);

        SchedulerTHelper.log("Kill Scheduler");
        SchedulerTHelper.killAndRestartScheduler(true);

        SchedulerTHelper.getUserInterface();

        SchedulerTHelper.log("Waiting for job 2 to finished");
        SchedulerTHelper.waitForFinishedJob(idJ2);
        SchedulerTHelper.log("Waiting for job 3 to finished");
        SchedulerTHelper.waitForFinishedJob(idJ3);

        SchedulerTHelper.log("check result job 1");
        JobResult result = SchedulerTHelper.getJobResult(idJ1);
        Assert.assertEquals(6, result.getAllResults().size());
        for (int i = 1; i <= 6; i++) {
            Assert.assertNotNull(result.getAllResults().get("Computation" + i).value());
            Assert.assertNull(result.getAllResults().get("Computation" + i).getException());
        }
        SchedulerTHelper.log("check result job 2");
        result = SchedulerTHelper.getJobResult(idJ2);
        Assert.assertEquals(6, result.getAllResults().size());
        for (int i = 1; i <= 6; i++) {
            Assert.assertNotNull(result.getAllResults().get("Computation" + i).value());
            Assert.assertNull(result.getAllResults().get("Computation" + i).getException());
        }
        SchedulerTHelper.log("check result job 3");
        result = SchedulerTHelper.getJobResult(idJ3);
        Assert.assertEquals(6, result.getAllResults().size());
        for (int i = 1; i <= 6; i++) {
            Assert.assertNotNull(result.getAllResults().get("Computation" + i).value());
            Assert.assertNull(result.getAllResults().get("Computation" + i).getException());
        }

    }
}
