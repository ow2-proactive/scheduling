package functionaltests.taskkill;

import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import functionaltests.RMFunctionalTest;

import org.junit.Test;

import functionaltests.RMTHelper;
import functionaltests.SchedulerTHelper;
import functionaltests.TestNode;


public class TestChildProcessOfNodeKilled extends RMFunctionalTest {

    @Test
    public void childProcessesForkedByTaskAreCleanedUpWhenRMNodeStarterIsKilled() throws Throwable {
        TestNode tNode = startSchedulerAndRMWithOneNode();
        startJobForkingProcesses();

        RMTHelper.getDefaultInstance(SchedulerTHelper.PNP_PORT).killNode(tNode.getNode().getNodeInformation().getURL());

        TestProcessTreeKiller.waitUntilAllForkedProcessesAreKilled();
    }

    private static void startJobForkingProcesses() throws Exception {
        TaskFlowJob jobForkingProcesses = TestProcessTreeKiller.createJavaExecutableJob("test", false);
        SchedulerTHelper.submitJob(jobForkingProcesses);
        TestProcessTreeKiller.waitUntilForkedProcessesAreRunning(TestProcessTreeKiller.detachedProcNumber);
    }

    private static TestNode startSchedulerAndRMWithOneNode() throws Exception {
        SchedulerTHelper.startSchedulerWithEmptyResourceManager();
        ResourceManager resourceManager = RMTHelper.getDefaultInstance(SchedulerTHelper.PNP_PORT).getResourceManager();
        TestNode tNode = RMTHelper.createRMNodeStarterNode("test1");
        resourceManager.addNode(tNode.getNode().getNodeInformation().getURL());
        return tNode;
    }
}