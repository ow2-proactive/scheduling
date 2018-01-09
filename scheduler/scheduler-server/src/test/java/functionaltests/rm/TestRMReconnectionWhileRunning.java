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
package functionaltests.rm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;

import functionaltests.utils.*;
import performancetests.helper.LogProcessor;


public class TestRMReconnectionWhileRunning extends MultipleRMTBase {

    static final Logger logger = Logger.getLogger(TestRMReconnectionWhileRunning.class);

    private SchedulerTHelper schedulerHelper;

    private static URL runningJob = TestRMReconnectionWhileRunning.class.getResource("/functionaltests/descriptors/Job_20s.xml");

    private static URL runningJob1 = TestRMReconnectionWhileRunning.class.getResource("/functionaltests/descriptors/Job_20s-1.xml");

    private static URL runningJob2 = TestRMReconnectionWhileRunning.class.getResource("/functionaltests/descriptors/Job_20s-2.xml");

    @BeforeClass
    public static void setUp() throws Exception {
        if (TestScheduler.isStarted()) {
            SchedulerTHelper.log("Killing previous scheduler.");
            TestScheduler.kill();
        }
        initConfigs();
    }

    @Test
    public void testTaskIsNotStuckRunning() throws Exception {
        ProActiveConfiguration.load();
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false, true);
        schedulerHelper.createNodeSource("local", 3);
        schedulerHelper.getSchedulerInterface().changePolicy("functionaltests.rm.PolicyWhichThrowsExceptions");
        JobId jobId = schedulerHelper.submitJob(new File(runningJob.toURI()).getAbsolutePath());
        JobId jobId2 = schedulerHelper.submitJob(new File(runningJob1.toURI()).getAbsolutePath());
        JobId jobId3 = schedulerHelper.submitJob(new File(runningJob2.toURI()).getAbsolutePath());

        schedulerHelper.waitForEventTaskRunning(jobId, "running_task_for20s");

        Thread.sleep(200000); // sleep enough that all jobs should be able to finish

        assertJobFinished(jobId2);
        assertJobFinished(jobId3);
        assertJobFinished(jobId);
    }

    @Test
    public void testReconnectionIsNotHappen() throws Exception {
        ProActiveConfiguration.load();
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false, true);
        schedulerHelper.createNodeSource("local", 3);
        schedulerHelper.getSchedulerInterface().changePolicy("functionaltests.rm.PolicyWhichThrowsExceptions");
        JobId jobId = schedulerHelper.submitJob(new File(runningJob.toURI()).getAbsolutePath());
        JobId jobId2 = schedulerHelper.submitJob(new File(runningJob1.toURI()).getAbsolutePath());
        JobId jobId3 = schedulerHelper.submitJob(new File(runningJob2.toURI()).getAbsolutePath());

        schedulerHelper.waitForEventTaskRunning(jobId, "running_task_for20s");

        schedulerHelper.disconnect();
        schedulerHelper.getSchedulerInterface().disconnect();
        schedulerHelper.getResourceManager().disconnect();

        Thread.sleep(200000); // sleep enough that all jobs should be able to finish

        assertJobFinished(jobId);
        assertJobFinished(jobId2);
        assertJobFinished(jobId3);

        int numberOfAttemptsToReconnect = LogProcessor.linesThatMatch("Successfully reconnected to Resource Manager ")
                                                      .size();
        int attemptsThatDidNotCauseReconnection = LogProcessor.linesThatMatch("Do not reconnect to the RM as connection is active for ")
                                                              .size();
        int actualNumberOfReconnections = numberOfAttemptsToReconnect - attemptsThatDidNotCauseReconnection;

        assertEquals(0, actualNumberOfReconnections);
    }


    private void assertJobFinished(JobId jobId) throws Exception {
        final JobResult result0 = schedulerHelper.getJobResult(jobId);
        assertNotNull(result0);
        final JobInfo jobInfo0 = result0.getJobInfo();
        final JobStatus status0 = jobInfo0.getStatus();
        assertFalse(status0.isJobAlive());
    }

    @After
    public void stopRMs() throws Exception {
        if (schedulerHelper != null) {
            schedulerHelper.killScheduler();
        }
    }
}
