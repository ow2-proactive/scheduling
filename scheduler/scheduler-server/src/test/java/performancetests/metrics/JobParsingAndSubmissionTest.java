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
package performancetests.metrics;

import static org.ow2.proactive.utils.Lambda.repeater;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;

import functionaltests.utils.SchedulerTHelper;
import performancetests.recovery.PerformanceTestBase;


@RunWith(Parameterized.class)
public class JobParsingAndSubmissionTest extends PerformanceTestBase {

    private static URL simpleJob = JobParsingAndSubmissionTest.class.getResource("/functionaltests/descriptors/Job_simple.xml");

    /**
     * @return number of jobs and limit for average job submission time
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { 100, 200 } });
    }

    private final int jobsNumber;

    private final long timeLimit;

    public JobParsingAndSubmissionTest(int jobsNumber, long timeLimit) {
        this.jobsNumber = jobsNumber;
        this.timeLimit = timeLimit;
    }

    @Test(timeout = 3600000)
    public void submitAlotOfJobs() throws Exception {
        ProActiveConfiguration.load();
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                                               SCHEDULER_CONFIGURATION_START.getPath(),
                                               RM_CONFIGURATION_START.getPath(),
                                               null);

        // submit a first job to load factories (not counted in the total time)
        schedulerHelper.submitJob(new File(simpleJob.toURI()).getAbsolutePath());

        long start = System.currentTimeMillis();

        repeater.accept(jobsNumber, () -> schedulerHelper.submitJob(new File(simpleJob.toURI()).getAbsolutePath()));

        long anActualTime = System.currentTimeMillis() - start;

        LOGGER.info(makeCSVString(JobParsingAndSubmissionTest.class.getSimpleName(),
                                  jobsNumber,
                                  timeLimit,
                                  anActualTime,
                                  ((anActualTime < timeLimit * jobsNumber) ? SUCCESS : FAILURE)));
    }

}
