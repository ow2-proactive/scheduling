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

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.tests.FunctionalTest;
import org.junit.Assert;


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
 * @since ProActive Scheduling 1.0
 */
public class TestJobRecover extends FunctionalTest {

    private static URL jobDescriptor = TestJobRecover.class
            .getResource("/functionaltests/descriptors/Job_PI_recover.xml");

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {

        JobId idJ1 = SchedulerTHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());
        JobId idJ2 = SchedulerTHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());
        JobId idJ3 = SchedulerTHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        SchedulerTHelper.waitForEventJobRunning(idJ1);

        SchedulerTHelper.log("Waiting for job 1 to finish");
        SchedulerTHelper.waitForFinishedJob(idJ1);

        SchedulerTHelper.log("Kill Scheduler");
        SchedulerTHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
                "config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        SchedulerTHelper.log("Waiting for job 2 to finish");
        SchedulerTHelper.waitForEventJobFinished(idJ2);

        SchedulerTHelper.log("Waiting for job 3 to finish");
        SchedulerTHelper.waitForFinishedJob(idJ3);

        SchedulerTHelper.log("check result job 1");
        JobResult result = SchedulerTHelper.getJobResult(idJ1);
        //        for (int i = 1; i <= 6; i++) {
        //            TaskResult tr = result.getResult("Computation" + i);
        //            System.out.println(tr.getTaskId().getReadableName() + " : " + tr.value());
        //        }
        Assert.assertEquals(6, result.getAllResults().size());
        for (int i = 1; i <= 6; i++) {
            Assert.assertNotNull(result.getResult("Computation" + i).value());
            Assert.assertNull(result.getResult("Computation" + i).getException());
        }

        SchedulerTHelper.log("check result job 2");
        result = SchedulerTHelper.getJobResult(idJ2);
        //        for (int i = 1; i <= 6; i++) {
        //            TaskResult tr = result.getResult("Computation" + i);
        //            System.out.println(tr.getTaskId().getReadableName() + " : " + tr.value());
        //        }
        Assert.assertEquals(6, result.getAllResults().size());
        for (int i = 1; i <= 6; i++) {
            Assert.assertNotNull(result.getResult("Computation" + i).value());
            Assert.assertNull(result.getResult("Computation" + i).getException());
        }

        SchedulerTHelper.log("check result job 3");
        result = SchedulerTHelper.getJobResult(idJ3);
        //        for (int i = 1; i <= 6; i++) {
        //            TaskResult tr = result.getResult("Computation" + i);
        //            System.out.println(tr.getTaskId().getReadableName() + " : " + tr.value());
        //        }
        Assert.assertEquals(6, result.getAllResults().size());
        for (int i = 1; i <= 6; i++) {
            Assert.assertNotNull(result.getResult("Computation" + i).value());
            Assert.assertNull(result.getResult("Computation" + i).getException());
        }

    }
}
