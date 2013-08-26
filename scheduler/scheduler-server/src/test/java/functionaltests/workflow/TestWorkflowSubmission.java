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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.workflow;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;

import functionaltests.SchedulerConsecutive;
import functionaltests.SchedulerTHelper;


/**
 * Tests that valid workflow-enabled jobs are accepted by the scheduler,
 * and invalid workflows-enabled jobs rejected
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowSubmission extends SchedulerConsecutive {

    private static final URL jobs_path = TestWorkflowSubmission.class
            .getResource("/functionaltests/workflow/descriptors/");

    private static final int jobs_valid = 17;

    private static final int jobs_fail = 44;

    @org.junit.Test
    public void run() throws Throwable {
        testFail();
        testValid();
    }

    /**
     * Submits a batch of illegal workflow jobs,
     * checks for failure at the job creation
     * 
     * @throws Throwable
     */
    private static void testFail() throws Throwable {
        Scheduler userInt = SchedulerTHelper.getSchedulerInterface();

        for (int i = 0; i < jobs_fail; i++) {
            String job_path = new File(jobs_path.toURI()).getAbsolutePath() + "/flow_fail_" + (i + 1) +
                ".xml";

            Exception exc = null;
            JobId job = null;
            try {
                job = userInt.submit(JobFactory.getFactory().createJob(job_path));
            } catch (Exception e) {
                exc = e;
            }
            Assert
                    .assertTrue("Job " + job_path + " was supposed to be rejected but was created",
                            job == null);
            Assert.assertTrue(exc != null);
        }
        SchedulerTHelper.log(jobs_fail + " invalid jobs successfully rejected");
    }

    /**
     * Submits a batch of valid workflow jobs,
     * checks for success at the job creation
     * 
     * @throws Throwable
     */
    private static void testValid() throws Throwable {
        Scheduler userInt = SchedulerTHelper.getSchedulerInterface();

        for (int i = 0; i < jobs_valid; i++) {
            String job_path = new File(jobs_path.toURI()).getAbsolutePath() + "/flow_valid_" + (i + 1) +
                ".xml";

            Exception exc = null;
            JobId job = null;
            try {
                job = userInt.submit(JobFactory.getFactory().createJob(job_path));
            } catch (Exception e) {
                exc = e;
            } finally {
                userInt.removeJob(job);
            }
            Assert.assertTrue("JobFactory returned null for valid job " + job_path, job != null);
            Assert.assertTrue("JobFactory threw an exception for valid job " + job_path, exc == null);
        }
        SchedulerTHelper.log(jobs_valid + " valid jobs successfully created");
    }
}
