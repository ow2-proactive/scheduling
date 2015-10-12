package functionaltests.job.taskkill;

import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.TestNode;
import org.junit.Test;


public class TestChildProcessOfNodeKilled extends SchedulerFunctionalTest {

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
        TestProcessTreeKiller.waitUntilForkedProcessesAreRunning(TestProcessTreeKiller.detachedProcNumber);
    }

    private TestNode startSchedulerAndRMWithOneNode() throws Exception {
        schedulerHelper.startSchedulerWithEmptyResourceManager();
        ResourceManager resourceManager = schedulerHelper.getResourceManager();
        TestNode tNode = schedulerHelper.createRMNodeStarterNode("test1");
        resourceManager.addNode(tNode.getNode().getNodeInformation().getURL());
        return tNode;
    }

}