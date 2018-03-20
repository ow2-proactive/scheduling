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
package functionaltests.recover;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import functionaltests.nodesrecovery.RecoverInfrastructureTestHelper;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;
import functionaltests.utils.TestNode;
import functionaltests.utils.TestScheduler;


/**
 * This test verifies that when tasks cannot be recovered because the node is
 * down, then we avoid waiting for the ping timeout to continue the scheduler
 * state recovery.
 */
public class TaskReconnectionToDownNodeTest extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static final URL SCHEDULER_CONFIGURATION_START = TaskReconnectionWithForkedTaskExecutorTest.class.getResource("/functionaltests/config/functionalTSchedulerProperties.ini");

    private static final URL SCHEDULER_CONFIGURATION_RESTART = TaskReconnectionWithForkedTaskExecutorTest.class.getResource("/functionaltests/config/functionalTSchedulerProperties-updateDB.ini");

    private static final URL RM_CONFIGURATION_START = TaskReconnectionToDownNodeTest.class.getResource("/functionaltests/config/functionalTRMProperties-clean-db.ini");

    private static final URL RM_CONFIGURATION_RESTART = TaskReconnectionToDownNodeTest.class.getResource("/functionaltests/config/functionalTRMProperties-keep-db.ini");

    private static final int NUMBER_OF_NODES = 10;

    private static final int NUMBER_OF_TASKS = NUMBER_OF_NODES;

    private static final int RESTART_SCHEDULER_INTER_TIME_MILLIS = 1000;

    private static final String TASK_BASE_NAME = "TASK-" + TaskReconnectionToDownNodeTest.class.getSimpleName();

    private List<TestNode> nodes;

    @Before
    public void startDedicatedScheduler() throws Exception {
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                                               new File(SCHEDULER_CONFIGURATION_START.toURI()).getAbsolutePath(),
                                               new File(RM_CONFIGURATION_START.toURI()).getAbsolutePath(),
                                               null);

        // timeout of 30 seconds for the running task and the scheduler to reconnect
        PASchedulerProperties.SCHEDULER_NODE_PING_ATTEMPTS.updateProperty("3");
        PASchedulerProperties.SCHEDULER_NODE_PING_FREQUENCY.updateProperty("10");
    }

    @Test
    public void action() throws Throwable {

        this.createNodes();

        JobId jobid = this.submitJob();

        this.waitForAllTasksToRun(jobid);

        this.killSchedulerAndNodes();

        long timeForSchedulerToBeUp = this.recordSchedulerRestartTime();

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        JobState jobState = scheduler.getJobState(jobid);

        this.checkTasks(jobState);

        this.waitForJobToFinish(jobid);

        this.checkJobResult(jobid, scheduler);

        this.checkSchedulerStateRecoveryDoesNotWaitTaskPingAttemptTimesFrequency(timeForSchedulerToBeUp);
    }

    private long recordSchedulerRestartTime() throws Exception {
        long schedulerStartTime = System.currentTimeMillis();
        this.restartScheduler();
        long schedulerUpTime = System.currentTimeMillis();
        return schedulerUpTime - schedulerStartTime;
    }

    private void checkSchedulerStateRecoveryDoesNotWaitTaskPingAttemptTimesFrequency(long timeForSchedulerToBeUp) {
        int attempts = PASchedulerProperties.SCHEDULER_NODE_PING_ATTEMPTS.getValueAsInt();
        int frequency = PASchedulerProperties.SCHEDULER_NODE_PING_FREQUENCY.getValueAsInt();
        int minimumTotalRecoveryDurationIfRetryMechanismIsExecuted = attempts * frequency * 1000 * NUMBER_OF_TASKS;

        assertThat((int) timeForSchedulerToBeUp).isLessThan(minimumTotalRecoveryDurationIfRetryMechanismIsExecuted);
    }

    private void checkJobResult(JobId jobid, Scheduler scheduler)
            throws NotConnectedException, PermissionException, UnknownJobException {
        JobResult jobResult = scheduler.getJobResult(jobid);
        Assert.assertFalse(jobResult.hadException());
    }

    private void waitForJobToFinish(JobId jobid) throws Exception {
        schedulerHelper.waitForEventJobFinished(jobid);
    }

    private void checkTasks(JobState jobState) {
        int maximumNumberOfPendingTasks = jobState.getNumberOfPendingTasks();

        for (int i = 0; i < maximumNumberOfPendingTasks; i++) {

            TaskState taskState = jobState.getTasks().get(i);
            TaskStatus taskStatus = taskState.getTaskInfo().getStatus();

            assertThat(taskStatus.equals(TaskStatus.PENDING) || taskStatus.equals(TaskStatus.RUNNING)).isTrue();
        }
    }

    private void restartScheduler() throws Exception {
        schedulerHelper = new SchedulerTHelper(false,
                                               new File(SCHEDULER_CONFIGURATION_RESTART.toURI()).getAbsolutePath(),
                                               new File(RM_CONFIGURATION_RESTART.toURI()).getAbsolutePath(),
                                               null,
                                               false);
        this.createNodes();
    }

    private void killSchedulerAndNodes() throws Exception {
        Thread.sleep(RESTART_SCHEDULER_INTER_TIME_MILLIS);

        TestScheduler.kill();
        RecoverInfrastructureTestHelper.killNodesWithStrongSigKill();

        Thread.sleep(RESTART_SCHEDULER_INTER_TIME_MILLIS);
    }

    private void waitForAllTasksToRun(JobId jobid) throws Exception {
        schedulerHelper.waitForEventTaskRunning(jobid, TASK_BASE_NAME + (NUMBER_OF_TASKS - 1));
    }

    private JobId submitJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        job.setName("JOB-" + TaskReconnectionToDownNodeTest.class.getSimpleName());

        for (int i = 0; i < NUMBER_OF_TASKS; i++) {

            ScriptTask st1 = new ScriptTask();
            st1.setName(TASK_BASE_NAME + i);
            st1.setScript(new TaskScript(new SimpleScript("Thread.sleep(60000)", "groovy")));
            job.addTask(st1);
        }

        JobId jobid = schedulerHelper.submitJob(job);
        schedulerHelper.waitForEventJobRunning(jobid);
        return jobid;
    }

    private void createNodes() throws Exception {
        this.nodes = schedulerHelper.createRMNodeStarterNodes(TaskReconnectionWithForkedTaskExecutorTest.class.getSimpleName(),
                                                              NUMBER_OF_NODES);
    }

    @After
    public void after() throws Exception {
        if (this.nodes != null) {
            for (TestNode node : this.nodes) {
                try {
                    node.kill();
                } catch (Exception e) {
                    // keep exceptions there silent
                }
            }
        }
    }

}
