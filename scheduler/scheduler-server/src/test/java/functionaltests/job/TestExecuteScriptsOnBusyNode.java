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

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.utils.TargetType;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.util.FileLock;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;
import functionaltests.utils.TestNode;


/**
 * Test checks that when executing "node scripts" on busy nodes, the script execution does not impact the task occupying it
 *
 * It checks as well that a task starting on a node currently executing a "node script" does not impact its execution
 * 
 * @author ProActive team
 *
 */
public class TestExecuteScriptsOnBusyNode extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static final long TIMEOUT = 60000;

    private static int startedNodesCounter;

    ExecutorService executorService;

    public static class TestJavaTaskWithLock extends JavaExecutable {

        private String fileLockPath;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            getOut().println("OK");
            FileLock.waitUntilUnlocked(fileLockPath);
            return "OK";
        }

    }

    public static class TestJavaTask extends JavaExecutable {

        private String fileLockPath;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            getOut().println("OK");
            return "OK";
        }

    }

    /**
     * Starts an scheduler in non-fork mode with an empty rm
     *
     * @throws Exception
     */
    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        schedulerHelper = new SchedulerTHelper(false,
                                               new File(SchedulerTHelper.class.getResource("/functionaltests/config/scheduler-nonforkedscripttasks.ini")
                                                                              .toURI()).getAbsolutePath(),
                                               null,
                                               null);
    }

    @Before
    public void deployNode() throws Exception {
        startNode();
        executorService = Executors.newSingleThreadExecutor();
    }

    @Test
    public void testExecuteScriptOnBusyNode() throws Exception {
        FileLock fileLock = new FileLock();
        Path fileLockPath = fileLock.lock();

        log("Submit job");
        final JobId jobId = schedulerHelper.submitJob(createJobWithLock(fileLockPath.toString()));

        log("Wait when node becomes busy");
        RMNodeEvent event;
        do {
            event = schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, TIMEOUT);
        } while (!event.getNodeState().equals(NodeState.BUSY));

        log("Wait when task starts");
        schedulerHelper.waitForEventTaskRunning(jobId, "Test task");

        log("Wait some time until task launcher is started");
        Thread.sleep(5000);

        List<ScriptResult<Object>> results = schedulerHelper.getResourceManager()
                                                            .executeScript("println \"Executing Script\";",
                                                                           "groovy",
                                                                           TargetType.NODE_URL.name(),
                                                                           Sets.newSet(testNode.getNodeURL()));
        Assert.assertEquals(1, results.size());
        ScriptResult result = results.get(0);
        if (result.errorOccured()) {
            result.getException().printStackTrace();
            Assert.fail("Script execution failed");
        }

        log("Let task finish");
        fileLock.unlock();

        log("Wait when job finish");
        schedulerHelper.waitForEventJobFinished(jobId, TIMEOUT);

        schedulerHelper.waitUntilState(testNode.getNodeURL(), NodeState.FREE, TIMEOUT);

        log("Check job result");
        checkJobResult(schedulerHelper.getSchedulerInterface(), jobId);
        checkActiveObjects();
    }

    private void checkActiveObjects() throws NodeException, ActiveObjectCreationException, InterruptedException {
        try {
            // This is to avoid slight asynchronous delays in active objects termination
            Thread.sleep(1000);
            Object[] activeObjects = testNode.getNode().getActiveObjects(ScriptHandler.class.getName());
            // a task launcher might still be alive a tiny bit after the task terminates, but there should not be any script handler
            Assert.fail("After the node is freed, no script handler active object should remain, received : " +
                        Arrays.toString(activeObjects));
        } catch (NodeException expected) {
            // when there is no active object of the type, a node exception is thrown
        }

    }

    @Test
    public void testRunningTaskWithSelectionScriptDoesNotImpactScriptExecution() throws Exception {
        testRunningTaskDoesNotImpactScriptExecution(true);
    }

    @Test
    public void testRunningTaskWithoutSelectionScriptDoesNotImpactScriptExecution() throws Exception {
        testRunningTaskDoesNotImpactScriptExecution(false);
    }

    public void testRunningTaskDoesNotImpactScriptExecution(boolean withSelectionScript) throws Exception {
        FileLock fileLock = new FileLock();
        Path fileLockPath = fileLock.lock();

        String scriptCode = "org.ow2.proactive.scheduler.util.FileLock.waitUntilUnlocked(\"" +
                            fileLockPath.toString().replace("\\", "\\\\") + "\")";

        log("Submit script (handled asynchronously by the resource manager)");
        List<ScriptResult<Object>> resultsFuture = schedulerHelper.getResourceManager()
                                                                  .executeScript(scriptCode,
                                                                                 "groovy",
                                                                                 TargetType.NODE_URL.name(),
                                                                                 Sets.newSet(testNode.getNodeURL()));

        log("Submit job");
        final JobId jobId = schedulerHelper.submitJob(createNonBlockingJob(withSelectionScript));

        // We wait a bit to be sure the job went through the scheduling loop
        // if a selection script is set, the task start will be delayed as much as the SelectionManager is busy executing this script
        // (and thus cannot execute the selection script provided by the task)
        Thread.sleep(3000);

        // unlock the script.
        log("Unlock the script");
        fileLock.unlock();

        log("Wait when job finish");
        schedulerHelper.waitForEventJobFinished(jobId, TIMEOUT);

        Assert.assertEquals(1, resultsFuture.size());
        ScriptResult result = resultsFuture.get(0);
        if (result.errorOccured()) {
            result.getException().printStackTrace();
            Assert.fail("Script execution failed");
        }

        schedulerHelper.waitUntilState(testNode.getNodeURL(), NodeState.FREE, TIMEOUT);

        log("Check job result");
        checkJobResult(schedulerHelper.getSchedulerInterface(), jobId);

        checkActiveObjects();
    }

    @After
    public void removeNode() throws Exception {
        schedulerHelper.getResourceManager().removeNode(testNode.getNodeURL(), true);
        executorService.shutdownNow();
    }

    private TestNode startNode() throws Exception {
        int nodeNumber = startedNodesCounter++;

        log("Start new node: node-" + nodeNumber);
        testNode = schedulerHelper.createNode("node" + nodeNumber);
        String nodeUrl = testNode.getNode().getNodeInformation().getURL();
        schedulerHelper.getResourceManager().addNode(nodeUrl);
        schedulerHelper.waitForNodeEvent(RMEventType.NODE_ADDED, nodeUrl, TIMEOUT);
        RMNodeEvent event = schedulerHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, nodeUrl, TIMEOUT);
        assertEquals(NodeState.FREE, event.getNodeState());
        return testNode;
    }

    private TaskFlowJob createJobWithLock(String communicationObjectUrl) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_JobWithLock");
        job.setOnTaskError(OnTaskError.CANCEL_JOB);
        job.setMaxNumberOfExecution(1);

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTaskWithLock.class.getName());
        javaTask.setMaxNumberOfExecution(1);
        javaTask.setOnTaskError(OnTaskError.CANCEL_JOB);
        javaTask.setName("Test task");
        javaTask.addArgument("fileLockPath", communicationObjectUrl);
        job.addTask(javaTask);

        return job;
    }

    private TaskFlowJob createNonBlockingJob(boolean withSelectionScript) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_SimpleJob");
        job.setOnTaskError(OnTaskError.CANCEL_JOB);
        job.setMaxNumberOfExecution(1);

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.setMaxNumberOfExecution(1);
        javaTask.setOnTaskError(OnTaskError.CANCEL_JOB);
        javaTask.setName("Test task");
        if (withSelectionScript) {
            // we want to trigger a selection script execution on the unique node, as this will call node.clean()
            javaTask.addSelectionScript(new SelectionScript("selected = true", "groovy", true));
        }
        job.addTask(javaTask);

        return job;
    }

    private void checkJobResult(Scheduler scheduler, JobId jobId) throws Exception {
        JobResult jobResult = scheduler.getJobResult(jobId);
        assertEquals("Unexpected number of task results", 1, jobResult.getAllResults().size());
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            log("Task " + taskResult.getTaskId());
            assertNull("Unexpected task result exception", taskResult.getException());
            String output = taskResult.getOutput().getAllLogs(false);
            log("Task output:");
            log(output);
            assertTrue("Unxepected output", output.contains("OK"));
        }
    }

}
