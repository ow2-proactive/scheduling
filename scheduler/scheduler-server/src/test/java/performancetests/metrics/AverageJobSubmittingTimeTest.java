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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
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
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;

import functionaltests.utils.SchedulerTHelper;
import performancetests.recovery.PeformanceTestBase;


/**
 * Measures time to submit numberOfThreads*numberOfJobSubmittedByThread jobs in parallel.
 * This checks will show improvement due to adding annotation ImmediateService to submit method.
 *
 */
@RunWith(Parameterized.class)
public class AverageJobSubmittingTimeTest extends PeformanceTestBase {

    private static final Logger LOGGER = Logger.getLogger(AverageJobSubmittingTimeTest.class);

    SchedulerTHelper schedulerHelper;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { 8, 1000, 1.0 } });
    }

    private final int numberOfThreads;

    private final int numberOfJobSubmittedByThread;

    private final double rate;

    private List<JobId> allJobs = new ArrayList<>();

    public AverageJobSubmittingTimeTest(int numberOfThreads, int numberOfJobSubmittedByThread, double rate) {
        this.numberOfThreads = numberOfThreads;
        this.numberOfJobSubmittedByThread = numberOfJobSubmittedByThread;
        this.rate = rate;
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
        long parallel = parallel();
        long sequential = sequential();

        double faster = ((double) parallel) / sequential;

        LOGGER.info(makeCSVString(AverageJobSubmittingTimeTest.class.getSimpleName(),
                                  String.format("%d * %d", numberOfThreads, numberOfJobSubmittedByThread),
                                  rate,
                                  faster,
                                  ((faster < rate) ? SUCCESS : FAILURE)));

    }

    public long parallel() throws Exception {
        ProActiveConfiguration.load();
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                                               SchedulerEfficiencyMetricsTest.SCHEDULER_CONFIGURATION_START.getPath(),
                                               SchedulerEfficiencyMetricsTest.RM_CONFIGURATION_START.getPath(),
                                               null);

        final TaskFlowJob job = SchedulerEfficiencyMetricsTest.createJob(1, 1);

        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        long startTime = System.currentTimeMillis();
        List<Future<List<JobId>>> futures = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; ++i) {
            final Future<List<JobId>> future = executorService.submit(new Callable<List<JobId>>() {
                @Override
                public List<JobId> call() throws Exception {
                    List<JobId> result = new ArrayList<>();
                    try {
                        for (int i = 0; i < numberOfJobSubmittedByThread; ++i) {
                            result.add(schedulerHelper.submitJob(job));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return result;
                }
            });
            futures.add(future);
        }

        // wait that all futures are finished
        for (Future<List<JobId>> future : futures) {
            allJobs.addAll(future.get());
        }
        long endTime = System.currentTimeMillis();
        long actualTime = endTime - startTime;

        return actualTime;
    }

    public long sequential() throws Exception {
        ProActiveConfiguration.load();
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                                               SchedulerEfficiencyMetricsTest.SCHEDULER_CONFIGURATION_START.getPath(),
                                               SchedulerEfficiencyMetricsTest.RM_CONFIGURATION_START.getPath(),
                                               null);

        final TaskFlowJob job = SchedulerEfficiencyMetricsTest.createJob(1, 1);

        final ExecutorService executorService = Executors.newFixedThreadPool(1);

        long startTime = System.currentTimeMillis();
        List<Future<List<JobId>>> futures = new ArrayList<>();
        for (int i = 0; i < 1; ++i) {
            final Future<List<JobId>> future = executorService.submit(new Callable<List<JobId>>() {
                @Override
                public List<JobId> call() throws Exception {
                    List<JobId> result = new ArrayList<>();
                    try {
                        for (int i = 0; i < numberOfThreads * numberOfJobSubmittedByThread; ++i) {
                            result.add(schedulerHelper.submitJob(job));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return result;
                }
            });
            futures.add(future);
        }

        // wait that all futures are finished
        for (Future<List<JobId>> future : futures) {
            allJobs.addAll(future.get());
        }
        long endTime = System.currentTimeMillis();
        long actualTime = endTime - startTime;
        return actualTime;
    }

    @After
    public void after() throws Exception {
        if (schedulerHelper != null) {
            for (JobId jobId : allJobs) {
                if (!schedulerHelper.getSchedulerInterface().getJobState(jobId).isFinished()) {
                    schedulerHelper.getSchedulerInterface().killJob(jobId);
                }
                schedulerHelper.getSchedulerInterface().removeJob(jobId);
            }

            schedulerHelper.log("Kill Scheduler after test.");
            schedulerHelper.killScheduler();
        }
    }
}
