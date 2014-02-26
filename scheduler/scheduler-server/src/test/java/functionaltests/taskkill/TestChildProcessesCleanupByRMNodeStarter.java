package functionaltests.taskkill;


import functionaltests.RMTHelper;
import functionaltests.SchedulerTHelper;
import functionaltests.TNode;
import org.objectweb.proactive.core.process.JVMProcess;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.tests.FunctionalTest;

public class TestChildProcessesCleanupByRMNodeStarter extends FunctionalTest {

    @org.junit.Test
    public void childProcessesForkedByTaskAreCleanedUpWhenRMNodeStarterIsKilled() throws Throwable {
        JVMProcess nodeProcess = startSchedulerAndRMWithOneNode();
        startJobForkingProcesses();

        nodeProcess.stopProcess();

        TestProcessTreeKiller.waitUntilAllForkedProcessesAreKilled();
    }

    private static void startJobForkingProcesses() throws Exception {
        TaskFlowJob jobForkingProcesses = TestProcessTreeKiller.createJavaExecutableJob("test", false);
        SchedulerTHelper.submitJob(jobForkingProcesses);
        TestProcessTreeKiller.waitUntilForkedProcessesAreRunning(TestProcessTreeKiller.detachedProcNumber);
    }

    private static JVMProcess startSchedulerAndRMWithOneNode() throws Exception {
        SchedulerTHelper.startSchedulerWithEmptyResourceManager();
        ResourceManager resourceManager = RMTHelper.getDefaultInstance().getResourceManager();
        TNode tNode = RMTHelper.createRMNodeStarterNode("test1");
        resourceManager.addNode(tNode.getNode().getNodeInformation().getURL());
        return tNode.getNodeProcess();
    }
}