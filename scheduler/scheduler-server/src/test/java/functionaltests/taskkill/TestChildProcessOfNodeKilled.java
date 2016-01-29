package functionaltests.taskkill;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.tests.FunctionalTest;

import org.junit.Test;

import functionaltests.RMTHelper;
import functionaltests.SchedulerTHelper;
import functionaltests.TNode;


public class TestChildProcessOfNodeKilled extends FunctionalTest {

    @Test
    public void childProcessesForkedByTaskAreCleanedUpWhenRMNodeStarterIsKilled() throws Throwable {
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.windows) {
            System.out.println("Test disabled on windows because of \"The filename or extension is too long\" issue");
            return;
        }
        TNode tNode = startSchedulerAndRMWithOneNode();
        startJobForkingProcesses();

        RMTHelper.getDefaultInstance().killNode(tNode.getNode().getNodeInformation().getURL());

        TestProcessTreeKiller.waitUntilAllForkedProcessesAreKilled();
    }

    private static void startJobForkingProcesses() throws Exception {
        TaskFlowJob jobForkingProcesses = TestProcessTreeKiller.createJavaExecutableJob("test", false);
        SchedulerTHelper.submitJob(jobForkingProcesses);
        TestProcessTreeKiller.waitUntilForkedProcessesAreRunning(TestProcessTreeKiller.detachedProcNumber);
    }

    private static TNode startSchedulerAndRMWithOneNode() throws Exception {
        SchedulerTHelper.startSchedulerWithEmptyResourceManager();
        ResourceManager resourceManager = RMTHelper.getDefaultInstance().getResourceManager();
        TNode tNode = RMTHelper.createRMNodeStarterNode("test1");
        resourceManager.addNode(tNode.getNode().getNodeInformation().getURL());
        return tNode;
    }
}