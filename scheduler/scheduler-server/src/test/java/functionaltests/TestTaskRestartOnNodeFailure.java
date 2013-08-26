/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.io.Serializable;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.tests.FunctionalTest;
import junit.framework.Assert;
import org.junit.Test;


/**
 * Test checks that scheduler restarts task if Node executing
 * task was killed during task execution.
 * 
 * @author ProActive team
 *
 */
public class TestTaskRestartOnNodeFailure extends FunctionalTest {

    private static final long TIMEOUT = 60000;

    public static class CommunicationObject {

        private boolean canFinish;

        public CommunicationObject() {
        }

        public void setCanFinish(boolean value) {
            canFinish = value;
        }

        public boolean canFinish() {
            return canFinish;
        }

    }

    public static class TestJavaTask extends JavaExecutable {

        private String communicationObjectUrl;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            System.out.println("OK");
            CommunicationObject communicationObject = PAActiveObject.lookupActive(CommunicationObject.class,
                    communicationObjectUrl);

            while (true) {
                if (!communicationObject.canFinish()) {
                    Thread.sleep(5000);
                } else {
                    break;
                }
            }
            return "OK";
        }

    }

    private RMTHelper rmHelper = RMTHelper.getDefaultInstance();

    @Test
    public void testRestart() throws Exception {
        System.out.println("Start RM");
        rmHelper.getResourceManager();

        TNode node1 = startNode(rmHelper);

        System.out.println("Start scheduler");
        String rmUrl = "rmi://localhost:" + CentralPAPropertyRepository.PA_RMI_PORT.getValue() + "/";
        SchedulerTHelper.startScheduler(false, null, null, rmUrl);

        CommunicationObject communicationObject = PAActiveObject.newActive(CommunicationObject.class,
                new Object[] {});

        node1 = testTaskKillNode(communicationObject, node1, false);
        node1 = testTaskKillNode(communicationObject, node1, true);
        node1 = testTaskKillNode(communicationObject, node1, false);
        node1 = testTaskKillNode(communicationObject, node1, true);
    }

    private TNode testTaskKillNode(CommunicationObject communicationObject, TNode node1,
            boolean waitBeforeKill) throws Exception {
        communicationObject.setCanFinish(false);
        TNode node2 = startNode(rmHelper);

        System.out.println("Submit job");
        final JobId jobId = SchedulerTHelper.submitJob(createJob(PAActiveObject.getUrl(communicationObject)));

        System.out.println("Wait when node becomes busy");
        RMNodeEvent event;
        do {
            event = rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, TIMEOUT);
        } while (!event.getNodeState().equals(NodeState.BUSY));

        final String taskNodeUrl = event.getNodeUrl();

        TNode aliveNode;
        TNode nodeToKill;

        if (taskNodeUrl.equals(node1.getNode().getNodeInformation().getURL())) {
            nodeToKill = node1;
            aliveNode = node2;
        } else if (taskNodeUrl.equals(node2.getNode().getNodeInformation().getURL())) {
            nodeToKill = node2;
            aliveNode = node1;
        } else {
            throw new Exception("Can't detect node by URL: " + event.getNodeUrl());
        }

        System.out.println("Wait when task starts");
        SchedulerTHelper.waitForEventTaskRunning(jobId, "Test task");

        /*
         * Want to test two cases (existed at the time of this writing):
         * - if wait some time before killing node then node failure is detected by the pinger thread
         * - if kill node immediately then node failure is detected by the thread calling TaskLauncher.doTask 
         */
        if (waitBeforeKill) {
            System.out.println("Wait some time");
            Thread.sleep(5000);
        }

        System.out.println("Stop task node process (node " +
            nodeToKill.getNode().getNodeInformation().getURL() + ")");
        nodeToKill.getNodeProcess().stopProcess();

        System.out.println("Let task finish");
        communicationObject.setCanFinish(true);

        System.out.println("Wait when job finish");
        SchedulerTHelper.waitForEventJobFinished(jobId, TIMEOUT);

        event = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, aliveNode.getNode()
                .getNodeInformation().getURL(), TIMEOUT);
        Assert.assertEquals(NodeState.BUSY, event.getNodeState());
        event = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, aliveNode.getNode()
                .getNodeInformation().getURL(), TIMEOUT);
        Assert.assertEquals(NodeState.FREE, event.getNodeState());

        System.out.println("Check job result");
        checkJobResult(SchedulerTHelper.getSchedulerInterface(), jobId);

        return aliveNode;
    }

    private static int startedNodesCounter;

    private TNode startNode(RMTHelper rmHelper) throws Exception {
        int nodeNumber = startedNodesCounter++;

        System.out.println("Start new node: node-" + nodeNumber);
        TNode node = rmHelper.createNode("node" + nodeNumber);
        String nodeUrl = node.getNode().getNodeInformation().getURL();
        rmHelper.getResourceManager().addNode(nodeUrl);
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, nodeUrl, TIMEOUT);
        RMNodeEvent event = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, nodeUrl, TIMEOUT);
        Assert.assertEquals(NodeState.FREE, event.getNodeState());
        return node;
    }

    private TaskFlowJob createJob(String communicationObjectUrl) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        job.setCancelJobOnError(true);
        job.setMaxNumberOfExecution(1);

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.setMaxNumberOfExecution(1);
        javaTask.setCancelJobOnError(true);
        javaTask.setName("Test task");
        javaTask.addArgument("communicationObjectUrl", communicationObjectUrl);
        job.addTask(javaTask);

        return job;
    }

    private void checkJobResult(Scheduler scheduler, JobId jobId) throws Exception {
        JobResult jobResult = scheduler.getJobResult(jobId);
        Assert.assertEquals("Unexpected number of task results", 1, jobResult.getAllResults().size());
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            System.out.println("Task " + taskResult.getTaskId());
            Assert.assertNull("Unexpected task result exception", taskResult.getException());
            String output = taskResult.getOutput().getAllLogs(false);
            System.out.println("Task output:");
            System.out.println(output);
            Assert.assertTrue("Unxepected output", output.contains("OK"));
        }
    }

}
