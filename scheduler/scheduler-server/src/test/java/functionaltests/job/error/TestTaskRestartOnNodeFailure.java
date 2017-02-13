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
package functionaltests.job.error;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.util.FileLock;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;
import functionaltests.utils.TestNode;


/**
 * Test checks that scheduler restarts task if Node executing
 * task was killed during task execution.
 * 
 * @author ProActive team
 *
 */
public class TestTaskRestartOnNodeFailure extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static final long TIMEOUT = 60000;

    public static class TestJavaTask extends JavaExecutable {

        private String fileLockPath;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            getOut().println("OK");
            FileLock.waitUntilUnlocked(fileLockPath);
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

    @Test
    public void testRestart() throws Exception {
        FileLock fileLock = new FileLock();
        testTaskKillNode(fileLock, false);
        testTaskKillNode(fileLock, true);
    }

    private void testTaskKillNode(FileLock fileLock, boolean waitBeforeKill) throws Exception {
        Path fileLockPath = fileLock.lock();
        TestNode nodeToKill = startNode();

        log("Submit job");
        final JobId jobId = schedulerHelper.submitJob(createJob(fileLockPath.toString()));

        log("Wait when node becomes busy");
        RMNodeEvent event;
        do {
            event = schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, TIMEOUT);
        } while (!event.getNodeState().equals(NodeState.BUSY));

        log("Wait when task starts");
        schedulerHelper.waitForEventTaskRunning(jobId, "Test task");

        /*
         * Want to test two cases (existed at the time of this writing): - if wait some time before
         * killing node then node failure is detected by the pinger thread - if kill node
         * immediately then node failure is detected by the thread calling TaskLauncher.doTask
         */
        if (waitBeforeKill) {
            log("Wait some time");
            Thread.sleep(5000);
        }

        log("Stop task node process (node " + nodeToKill.getNode().getNodeInformation().getURL() + ")");
        nodeToKill.kill();

        TestNode newNode = startNode();

        log("Let task finish");
        fileLock.unlock();

        log("Wait when job finish");
        schedulerHelper.waitForEventJobFinished(jobId, TIMEOUT);

        event = schedulerHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                                 newNode.getNode().getNodeInformation().getURL(),
                                                 TIMEOUT);
        assertEquals(NodeState.BUSY, event.getNodeState());
        event = schedulerHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                                 newNode.getNode().getNodeInformation().getURL(),
                                                 TIMEOUT);
        assertEquals(NodeState.FREE, event.getNodeState());

        log("Check job result");
        checkJobResult(schedulerHelper.getSchedulerInterface(), jobId);

        schedulerHelper.getResourceManager().removeNode(newNode.getNodeURL(), true);

        newNode.kill();
    }

    private static int startedNodesCounter;

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

    private TaskFlowJob createJob(String communicationObjectUrl) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        job.setOnTaskError(OnTaskError.CANCEL_JOB);
        job.setMaxNumberOfExecution(1);

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.setMaxNumberOfExecution(1);
        javaTask.setOnTaskError(OnTaskError.CANCEL_JOB);
        javaTask.setName("Test task");
        javaTask.addArgument("fileLockPath", communicationObjectUrl);
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
