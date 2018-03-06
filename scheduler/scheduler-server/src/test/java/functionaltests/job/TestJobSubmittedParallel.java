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
package functionaltests.job;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * Submitting plenty of empty jobs in parallel.
 * in the end total amount of job should be correct.
 */
public class TestJobSubmittedParallel extends SchedulerFunctionalTestNoRestart {

    private static URL simpleJob = TestJobSubmittedParallel.class.getResource("/functionaltests/descriptors/Job_simple.xml");

    private static final int THREAD_POOL_SIZE = 8;

    private static final int NUMBER_OF_JOBS_PER_THREAD = 50;

    @Test
    public void testJobRemoved() throws Throwable {

        final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        List<Future<List<JobId>>> futures = new ArrayList<>();

        // submit all jobs
        repeater.accept(THREAD_POOL_SIZE, () -> futures.add(executorService.submit(() -> {
            List<JobId> result = new ArrayList<>();
            repeater.accept(NUMBER_OF_JOBS_PER_THREAD, () -> {
                final JobId jobId = schedulerHelper.submitJob(simpleJob.getPath());
                final JobState jobState = schedulerHelper.getSchedulerInterface().getJobState(jobId);
                jobState.getId().equals(jobId);
                jobState.getSubmittedTime();
                result.add(jobId);
            });
            return result;
        })));

        List<JobId> allJobId = new LinkedList<>();

        // wait until all jobs are finished
        silentMap(futures.stream(), future -> future.get()).forEach(col -> allJobId.addAll(col));

        int EXPECTER_NUMBER_OF_JOBS = THREAD_POOL_SIZE * NUMBER_OF_JOBS_PER_THREAD;

        assertEquals(EXPECTER_NUMBER_OF_JOBS, allJobId.size());

        assertEquals(EXPECTER_NUMBER_OF_JOBS, GetTotalJobCount());

    }

    /**
     * repeats func function limit number of times
     */
    private static BiConsumer<Integer, RunnableThatThrows> repeater = (limit, func) -> {
        for (int i = 0; i < limit; ++i) {
            try {
                func.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private int GetTotalJobCount() throws Exception {
        final SchedulerState schedulerState = schedulerHelper.getSchedulerInterface().getState();
        return schedulerState.getFinishedJobs().size() + schedulerState.getPendingJobs().size() +
               schedulerState.getRunningJobs().size();
    }

    private static <S, T> Stream<T> silentMap(Stream<S> stream, FunctionThatThrows<S, T> op) {
        return stream.map(item -> {
            try {
                return op.apply(item);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @FunctionalInterface
    private interface RunnableThatThrows<T, R> {
        void run() throws Exception;

    }

    /**
     * How come java does not have Function that throws Exception
     */
    @FunctionalInterface
    private interface FunctionThatThrows<T, R> {
        R apply(T var1) throws Exception;

    }

}
