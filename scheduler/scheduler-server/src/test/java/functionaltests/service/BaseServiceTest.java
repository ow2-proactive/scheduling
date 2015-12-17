package functionaltests.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import functionaltests.utils.Jobs;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.core.SchedulingMethod;
import org.ow2.proactive.scheduler.core.SchedulingService;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.policy.DefaultPolicy;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.ExecuterInformation;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.tests.ProActiveTest;


public class BaseServiceTest extends ProActiveTest {

    protected SchedulerDBManager dbManager;
    protected MockSchedulingInfrastructure infrastructure;
    protected MockSchedulingListener listener;
    protected SchedulingService service;

    private static ExecutorService executorService = Executors.newCachedThreadPool();

    @Before
    public void init() throws Exception {
        dbManager = SchedulerDBManager.createInMemorySchedulerDBManager();
        infrastructure = new MockSchedulingInfrastructure(dbManager);
        listener = new MockSchedulingListener();
        service = new SchedulingService(infrastructure, listener, null, DefaultPolicy.class.getName(),
            Mockito.mock(SchedulingMethod.class));
    }

    @After
    public void cleanup() {
        service.kill();
    }

    void taskStarted(JobDescriptor jobDesc, EligibleTaskDescriptor taskDesc) throws Exception {
        InternalTask task = taskDesc.getInternal();
        TaskLauncher launcher = Mockito.mock(TaskLauncher.class);
        task.setExecuterInformation(new ExecuterInformation(launcher, NodeFactory.getDefaultNode()));
        service.taskStarted(jobDesc.getInternal(), task, launcher);
    }

    public static InternalJob createJob(TaskFlowJob job) throws Exception {
        return Jobs.createJob(job);
    }

    interface TestRunnable {
        void run() throws Exception;
    }

    static void runInAnotherThread(final TestRunnable runnable) throws Exception {
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    Assert.fail("Unexpected exception: " + t);
                    t.printStackTrace(System.out);
                }
            }
        }).get();
    }

    static void runInAnotherThreadNoWait(final TestRunnable runnable) throws Exception {
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    Assert.fail("Unexpected exception: " + t);
                    t.printStackTrace(System.out);
                }
            }
        });
    }
}
