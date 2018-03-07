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

import static functionaltests.job.TestJobSubmittedParallel.repeater;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;

import functionaltests.utils.SchedulerTHelper;
import performancetests.recovery.PerformanceTestBase;


/**
 * Test executes in two parts: parallel and sequentional .
 * In both parts it submits numberOfThreads*numberOfJobSubmittedByThread jobs. And test
 * measures time needed to submit all these jobs for each part.
 * Finally, it outputs ratio between parallel and sequentional time.
 * It always should be less or equal 1.0.
 * <p>
 * This checks will show improvement due to adding ImmediateService annotation to submit method SchedulerFrontend.
 */
@RunWith(Parameterized.class)
public class ParallelSequentionalJobSubmition extends PerformanceTestBase {

    private static final Logger LOGGER = Logger.getLogger(ParallelSequentionalJobSubmition.class);

    SchedulerTHelper schedulerHelper;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { 8, 1000 } });
    }

    private final int numberOfThreads;

    private final int numberOfJobSubmittedByThread;

    public ParallelSequentionalJobSubmition(int numberOfThreads, int numberOfJobSubmittedByThread, double rate) {
        this.numberOfThreads = numberOfThreads;
        this.numberOfJobSubmittedByThread = numberOfJobSubmittedByThread;
    }

    @Before
    public void before() {
        PrintStream dummyStream = new PrintStream(new OutputStream() {
            public void write(int b) {
                // supress all output to not lost time to print anything to console
            }
        });
        System.setOut(dummyStream);
    }

    @Test(timeout = 3600000)
    public void parallelComparingToSequntial() throws Exception {
        long parallel = submitjobs(numberOfThreads);
        long sequential = submitjobs(1);

        double faster = ((double) parallel) / sequential;

        LOGGER.info(makeCSVString(ParallelSequentionalJobSubmition.class.getSimpleName(),
                                  String.format("%d * %d", numberOfThreads, numberOfJobSubmittedByThread),
                                  1.0,
                                  faster,
                                  ((faster <= 1.0) ? SUCCESS : FAILURE)));

    }

    public long submitjobs(int threadPoolSize) throws Exception {
        List<JobId> allJobs = new LinkedList<>();

        ProActiveConfiguration.load();

        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                                               SCHEDULER_CONFIGURATION_START.getPath(),
                                               RM_CONFIGURATION_START.getPath(),
                                               null);

        final TaskFlowJob job = SchedulerEfficiencyMetricsTest.createJob(1, 1);

        final ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

        long startTime = System.currentTimeMillis();
        List<Future<List<JobId>>> futures = new ArrayList<>();
        for (int i = 0; i < threadPoolSize; ++i) {
            final Future<List<JobId>> future = executorService.submit(() -> {
                List<JobId> result = new ArrayList<>(numberOfJobSubmittedByThread);

                repeater.accept(numberOfJobSubmittedByThread, () -> {
                    result.add(schedulerHelper.submitJob(job));
                });

                return result;
            });
            futures.add(future);
        }

        // wait that all futures are finished
        for (Future<List<JobId>> future : futures) {
            allJobs.addAll(future.get());
        }
        long endTime = System.currentTimeMillis();
        long actualTime = endTime - startTime;

        // kill and remove jobs
        for (JobId jobId : allJobs) {
            if (!schedulerHelper.getSchedulerInterface().getJobState(jobId).isFinished()) {
                schedulerHelper.getSchedulerInterface().killJob(jobId);
            }
            schedulerHelper.getSchedulerInterface().removeJob(jobId);
        }

        return actualTime;
    }

    @After
    public void after() throws Exception {
        if (schedulerHelper != null) {
            schedulerHelper.log("Kill Scheduler after test.");
            schedulerHelper.killScheduler();
        }
    }
}
