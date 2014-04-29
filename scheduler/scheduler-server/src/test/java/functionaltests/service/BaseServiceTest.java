package functionaltests.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.ow2.proactive.scheduler.task.internal.ExecuterInformations;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.tests.ProActiveTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;


public class BaseServiceTest extends ProActiveTest {

    protected SchedulerDBManager dbManager;
    protected MockSchedulingInfrastructure infrastructure;
    protected MockSchedulingListener listener;
    protected SchedulingService service;

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
        task.setExecuterInformations(new ExecuterInformations(launcher, NodeFactory.getDefaultNode()));
        service.taskStarted(jobDesc.getInternal(), task, launcher);
    }

    static InternalJob createJob(TaskFlowJob job) throws Exception {
        InternalJob internalJob = InternalJobFactory.createJob(job, getDefaultCredentials());
        internalJob.setOwner(DEFAULT_USER_NAME);
        return internalJob;
    }

    private static Credentials defaultCredentials;

    static final String DEFAULT_USER_NAME = "admin";

    static Credentials getDefaultCredentials() throws Exception {
        if (defaultCredentials == null) {
            defaultCredentials = Credentials.createCredentials(DEFAULT_USER_NAME, "admin",
                    PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_AUTH_PUBKEY_PATH
                            .getValueAsString()));
        }
        return defaultCredentials;
    }

    static ExecutorService executorService = Executors.newCachedThreadPool();

    static interface TestRunnable {
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
