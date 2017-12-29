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
package performancetests.recovery;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.core.SchedulingService;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;

import functionaltests.utils.*;
import performancetests.helper.LogProcessor;


/**
 * @author ActiveEon Team
 * @since 01/12/17
 */
@RunWith(Parameterized.class)
public class JobRecoveryTest extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static final String SCHEDULER_CONFIGURATION_START = JobRecoveryTest.class.getResource("/performancetests/config/scheduler-start.ini")
                                                                                     .getPath();

    private static final String SCHEDULER_CONFIGURATION_RESTART = JobRecoveryTest.class.getResource("/performancetests/config/scheduler-restart.ini")
                                                                                       .getPath();

    private static URL runningJob = JobRecoveryTest.class.getResource("/functionaltests/descriptors/Job_running.xml");

    private static final Logger LOGGER = Logger.getLogger(JobRecoveryTest.class);

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { 5, 1000 }, { 100, 5000 } });
    }

    // number of jobs
    int jobsNumber;

    // time limit in milliseconds for test to pass
    int timeLimit;

    public JobRecoveryTest(int jobsNumber, int timeLimit) {
        this.jobsNumber = jobsNumber;
        this.timeLimit = timeLimit;
    }

    /**
     * This method tests performance of jobs recovery using local infrastructure.
     *
     * @throws Exception
     */
    @Before
    public void startKillStartScheduler() throws Exception {
        ProActiveConfiguration.load();
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                                               SCHEDULER_CONFIGURATION_START,
                                               NodeRecoveryTest.RM_CONFIGURATION_START,
                                               null);

        schedulerHelper.createNodeSource("local", jobsNumber);
        JobId[] jobIds = new JobId[jobsNumber];
        for (int i = 0; i < jobsNumber; ++i) {
            jobIds[i] = schedulerHelper.submitJob(new File(runningJob.toURI()).getAbsolutePath());
        }

        for (int i = 0; i < jobsNumber; ++i) {
            schedulerHelper.waitForEventJobRunning(jobIds[i]);
        }

        NodesRecoveryProcessHelper.findPidAndSendSigKill(SchedulerStartForFunctionalTest.class.getSimpleName());

        schedulerHelper = new SchedulerTHelper(false,
                                               SCHEDULER_CONFIGURATION_RESTART,
                                               NodeRecoveryTest.RM_CONFIGURATION_RESTART,
                                               null);

    }

    @Test
    public void test() {
        long recovered = numberOfJobsRecovered();
        long timeSpent = timeSpentToRecoverJobs();
        LOGGER.info(NodeRecoveryTest.makeCSVString("JobRecoveryTest",
                                                   jobsNumber,
                                                   timeLimit,
                                                   recovered,
                                                   timeSpent,
                                                   ((timeSpent < timeLimit) ? "SUCCES" : "FAILURE")));
        assertEquals(jobsNumber, recovered);
        assertThat("Jobs recovery time for " + jobsNumber + " jobs", (int) timeSpent, lessThan(timeLimit));
    }

    @After
    public void after() throws Exception {
        if (schedulerHelper != null) {
            schedulerHelper.log("Kill Scheduler after test.");
            schedulerHelper.killScheduler();
        }
    }

    private long numberOfJobsRecovered() {
        return LogProcessor.numberOfLinesWhichMatch(JobDescriptorImpl.STARTING_TASK_RECOVERY_FOR_JOB);
    }

    private long timeSpentToRecoverJobs() {
        final long startedToRecover = LogProcessor.getDateOfLine(LogProcessor.getFirstLineThatMatch(SchedulingService.SCHEDULING_SERVICE_RECOVER_TASKS_STATE_STARTED))
                                                  .getTime();
        final long recoveryEnded = LogProcessor.getDateOfLine(LogProcessor.getLastLineThatMatch(SchedulingService.SCHEDULING_SERVICE_RECOVER_TASKS_STATE_FINISHED))
                                               .getTime();

        long time = recoveryEnded - startedToRecover;
        if (time < 0) {
            throw new RuntimeException("First occurence of " +
                                       SchedulingService.SCHEDULING_SERVICE_RECOVER_TASKS_STATE_STARTED +
                                       " goes after " +
                                       SchedulingService.SCHEDULING_SERVICE_RECOVER_TASKS_STATE_FINISHED);
        }

        return time;
    }

}
