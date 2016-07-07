package functionaltests.job.taskkill;

import org.objectweb.proactive.api.PAFuture;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;
import functionaltests.utils.TestNode;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;


public class TestChildProcessOfNodeKilled extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        schedulerHelper = new SchedulerTHelper(true, true);
    }

    @Test
    public void childProcessesForkedByTaskAreCleanedUpWhenRMNodeStarterIsKilled() throws Throwable {
        TestNode tNode = startSchedulerAndRMWithOneNode();
        startJobForkingProcesses();

        schedulerHelper.killNode(tNode.getNode().getNodeInformation().getURL());

        TestProcessTreeKiller.waitUntilAllForkedProcessesAreKilled();
    }

    private void startJobForkingProcesses() throws Exception {
        TaskFlowJob jobForkingProcesses = TestProcessTreeKiller.createJavaExecutableJob("test", false);
        schedulerHelper.submitJob(jobForkingProcesses);
        TestProcessTreeKiller.waitUntilForkedProcessesAreRunning(TestProcessTreeKiller.DETACHED_PROC_NUMBER);
    }

    private TestNode startSchedulerAndRMWithOneNode() throws Exception {
        ResourceManager resourceManager = schedulerHelper.getResourceManager();
        testNode = schedulerHelper.createRMNodeStarterNode("test1");
        PAFuture.waitFor(resourceManager.addNode(testNode.getNode().getNodeInformation().getURL()));
        return testNode;
    }

}