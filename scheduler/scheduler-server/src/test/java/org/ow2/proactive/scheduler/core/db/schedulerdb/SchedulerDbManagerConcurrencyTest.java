package org.ow2.proactive.scheduler.core.db.schedulerdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class SchedulerDbManagerConcurrencyTest extends BaseSchedulerDBTest {

    private Scenario scenario;

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                        { new ConcurrentJobInsertionScenario(),
                                ConcurrentJobInsertionScenario.class.getSimpleName() },
                        { new ConcurrentJobDeletionScenario(true),
                                ConcurrentJobDeletionScenario.class.getSimpleName() + "DeleteData" },
                        { new ConcurrentJobDeletionScenario(false),
                                ConcurrentJobDeletionScenario.class.getSimpleName() + "DoNotDeleteData" },
                        { new ConcurrentJobInsertionAndDeletionScenario(),
                                ConcurrentJobInsertionAndDeletionScenario.class.getSimpleName() } });
    }

    public SchedulerDbManagerConcurrencyTest(Scenario scenario, String testName) {
        // second parameter 'testName' is used by the @Parameterized.Parameters annotation
        this.scenario = scenario;
    }

    @Test(timeout = 60000)
    public void test() throws Exception {
        scenario.doTest(this);
    }

    private static class ConcurrentJobDeletionScenario extends PreInsertJobScenario {

        private final boolean deleteData;

        public ConcurrentJobDeletionScenario(boolean deleteData) {
            super(10);
            this.deleteData = deleteData;
        }

        @Override
        public void execute(final SchedulerDbManagerConcurrencyTest test) throws InterruptedException {
            for (int i = 0; i < nbJobs; i++) {
                final int finalI = i;
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            test.deleteJob(jobIds.get(finalI), deleteData);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        @Override
        protected int getExpectedNumberOfJobInDb() {
            return 0;
        }

    }

    private static abstract class PreInsertJobScenario extends Scenario {

        protected final int nbJobs;

        protected final List<JobId> jobIds;

        public PreInsertJobScenario(int nbJobs) {
            this.nbJobs = nbJobs;
            this.jobIds = new ArrayList<JobId>(nbJobs);
        }

        @Override
        protected void setUp(final SchedulerDbManagerConcurrencyTest test) throws Exception {
            super.setUp(test);

            for (int i = 0; i < nbJobs; i++) {
                jobIds.add(test.createAndInsertJob().getId());
            }
        }
    }

    private static class ConcurrentJobInsertionAndDeletionScenario extends PreInsertJobScenario {

        private AtomicInteger expectedNumberOfJobs;

        public ConcurrentJobInsertionAndDeletionScenario() {
            super(30);

            expectedNumberOfJobs = new AtomicInteger(nbJobs);
        }

        @Override
        public void execute(final SchedulerDbManagerConcurrencyTest test) throws InterruptedException {
            for (int i = 0; i < nbJobs; i++) {
                final int iCopy = i;

                threadPool.submit(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        int value = iCopy % 3;

                        switch (value) {
                            case 0:
                                test.createAndInsertJob();
                                expectedNumberOfJobs.getAndIncrement();
                                break;
                            case 1:
                                test.deleteJob(jobIds.get(iCopy), false);
                                expectedNumberOfJobs.getAndDecrement();
                                break;
                            case 2:
                                test.deleteJob(jobIds.get(iCopy), true);
                                expectedNumberOfJobs.getAndDecrement();
                                break;
                        }

                        return null;
                    }
                });
            }

            awaitTermination();
        }

        @Override
        protected int getExpectedNumberOfJobInDb() {
            return expectedNumberOfJobs.get();
        }

    }

    private static class ConcurrentJobInsertionScenario extends Scenario {

        @Override
        public void execute(final SchedulerDbManagerConcurrencyTest test) throws InterruptedException {
            for (int i = 0; i < 10; i++) {
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            test.createAndInsertJob();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        @Override
        protected int getExpectedNumberOfJobInDb() {
            return 10;
        }

    }

    private static abstract class Scenario {

        protected ExecutorService threadPool = Executors.newFixedThreadPool(10);

        public void doTest(SchedulerDbManagerConcurrencyTest manager) throws Exception {
            setUp(manager);

            execute(manager);
            awaitTermination();
            assertTermination(manager);

            tearDown(manager);
        }

        protected void setUp(SchedulerDbManagerConcurrencyTest test) throws Exception {
        }

        protected abstract void execute(SchedulerDbManagerConcurrencyTest test) throws InterruptedException;

        protected void awaitTermination() throws InterruptedException {
            threadPool.shutdown();
            threadPool.awaitTermination(1, TimeUnit.MINUTES);
        }

        protected void assertTermination(SchedulerDbManagerConcurrencyTest test) {
            Assert.assertEquals(getExpectedNumberOfJobInDb(), test.getDbManager().getTotalJobsCount());
        }

        protected void tearDown(SchedulerDbManagerConcurrencyTest test) throws InterruptedException {
        }

        protected abstract int getExpectedNumberOfJobInDb();

    }

    public InternalJob createAndInsertJob() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();

        JavaTask javaTask = createDefaultTask("java task");
        javaTask.setExecutableClassName(TestDummyExecutable.class.getName());
        jobDef.addTask(javaTask);

        return defaultSubmitJob(jobDef);
    }

    public void deleteJob(JobId jobId, boolean deleteData) {
        dbManager.removeJob(jobId, System.currentTimeMillis(), deleteData);
    }

}
