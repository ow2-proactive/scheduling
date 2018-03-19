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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

import functionaltests.nodesrecovery.RecoverInfrastructureTestHelper;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;
import functionaltests.utils.TestNode;
import functionaltests.utils.TestScheduler;


/**
 * This test verifies that when a running task cannot be recovered because the
 * node is down, then we avoid waiting for the ping timeout to continue the
 * scheduler state recovery.
 */
public class TaskReconnectionToDownNodeTest extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static final URL JOB_DESCRIPTOR = TaskReconnectionToDownNodeTest.class.getResource("/functionaltests/descriptors/Job_TaskReconnectionOnRestart_25_Parallel_Tasks.xml");

    private static final URL RM_CONFIGURATION_START = TaskReconnectionToDownNodeTest.class.getResource("/functionaltests/config/functionalTRMProperties-clean-db.ini");

    private static final URL RM_CONFIGURATION_RESTART = TaskReconnectionToDownNodeTest.class.getResource("/functionaltests/config/functionalTRMProperties-keep-db.ini");

    private static final int NUMBER_OF_NODES = 25;

    private static final int NUMBER_OF_REPLICATE_TASKS = 25;

    private static final int RESTART_SCHEDULER_INTER_TIME_IN_MILLISECONDS = 1000;

    private static final String LAST_REPLICATE_TASK_NAME = "Groovy_Task26";

    private List<TestNode> nodes;

    private Map<Long, String> taskExecutionHostnamePerTaskId;

    private static final URL SCHEDULER_CONFIGURATION_START = TaskReconnectionWithForkedTaskExecutorTest.class.getResource("/functionaltests/config/functionalTSchedulerProperties.ini");

    private static final URL SCHEDULER_CONFIGURATION_RESTART = TaskReconnectionWithForkedTaskExecutorTest.class.getResource("/functionaltests/config/functionalTSchedulerProperties-updateDB.ini");

    protected URL getSchedulerStartConfigurationURL() {
        return SCHEDULER_CONFIGURATION_START;
    }

    protected URL getSchedulerReStartConfigurationURL() {
        return SCHEDULER_CONFIGURATION_RESTART;
    }

    @Before
    public void startDedicatedScheduler() throws Exception {
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                                               new File(getSchedulerStartConfigurationURL().toURI()).getAbsolutePath(),
                                               new File(RM_CONFIGURATION_START.toURI()).getAbsolutePath(),
                                               null);
        this.taskExecutionHostnamePerTaskId = new HashMap<>();
    }

    @Test
    public void action() throws Throwable {

        this.createNodes();

        JobId jobid = this.submitJob();

        this.waitForAllTasksToRun(jobid);

        this.recordTaskExecutionHostInfo(jobid);

        this.killSchedulerAndNodes();

        long schedulerStartTime = System.currentTimeMillis();
        this.restartScheduler();
        long schedulerUpTime = System.currentTimeMillis();

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        JobState jobState = scheduler.getJobState(jobid);

        this.checkFirstAndLastTask(jobState);

        this.checkReplicateTasks(jobState);

        this.waitForJobToFinish(jobid);

        this.checkJobResult(jobid, scheduler);

        this.checkSchedulerStateRecoveryDoesNotWaitTaskPingAttemptTimesFrequency(schedulerStartTime, schedulerUpTime);
    }

    private void checkSchedulerStateRecoveryDoesNotWaitTaskPingAttemptTimesFrequency(long schedulerStartTime,
            long schedulerUpTime) {
        int attempts = PASchedulerProperties.SCHEDULER_NODE_PING_ATTEMPTS.getValueAsInt();
        int frequency = PASchedulerProperties.SCHEDULER_NODE_PING_FREQUENCY.getValueAsInt();
        int minimumTotalRecoveryDurationIfRetryMechanismIsExecuted = attempts * frequency * 1000 *
                                                                     NUMBER_OF_REPLICATE_TASKS;

        int schedulerTimeToBeUpAndRunning = (int) (schedulerUpTime - schedulerStartTime);
        assertThat(schedulerTimeToBeUpAndRunning).isLessThan(minimumTotalRecoveryDurationIfRetryMechanismIsExecuted);
    }

    private void checkJobResult(JobId jobid, Scheduler scheduler)
            throws NotConnectedException, PermissionException, UnknownJobException {
        JobResult jobResult = scheduler.getJobResult(jobid);
        Assert.assertFalse(jobResult.hadException());
    }

    private void waitForJobToFinish(JobId jobid) throws Exception {
        schedulerHelper.waitForEventJobFinished(jobid);
    }

    private void checkReplicateTasks(JobState jobState) {
        int maximumNumberOfPendingReplicateTasks = jobState.getNumberOfPendingTasks() - 1;

        for (int i = 1; i <= maximumNumberOfPendingReplicateTasks; i++) {

            TaskState taskState = jobState.getTasks().get(i);
            TaskStatus taskStatus = taskState.getTaskInfo().getStatus();

            assertThat(taskStatus.equals(TaskStatus.PENDING) || taskStatus.equals(TaskStatus.RUNNING)).isTrue();

            if (taskStatus.equals(TaskStatus.RUNNING)) {
                this.checkThatExecutionHostnameIsDifferent(taskState);
            }
        }
    }

    private void checkThatExecutionHostnameIsDifferent(TaskState taskState) {
        String formerExecutionHost = this.taskExecutionHostnamePerTaskId.get(taskState.getTaskInfo()
                                                                                      .getTaskId()
                                                                                      .longValue());
        String currentExecutionHost = taskState.getTaskInfo().getExecutionHostName();

        assertThat(formerExecutionHost).isNotEqualTo(currentExecutionHost);
    }

    private void checkFirstAndLastTask(JobState jobState) {
        // we have exactly one task that is finished (the first one)
        assertThat(jobState.getNumberOfFinishedTasks()).isEqualTo(1);
        // at least we have the last task that is pending (the merge task)
        assertThat(jobState.getNumberOfPendingTasks()).isAtLeast(1);
    }

    private void recordTaskExecutionHostInfo(JobId jobid) throws Exception {
        for (int i = 0; i < NUMBER_OF_REPLICATE_TASKS; i++) {
            TaskState taskState = schedulerHelper.getSchedulerInterface().getJobState(jobid).getTasks().get(i);
            TaskInfo taskInfo = taskState.getTaskInfo();
            this.taskExecutionHostnamePerTaskId.put(taskInfo.getTaskId().longValue(), taskInfo.getExecutionHostName());
        }
    }

    private void restartScheduler() throws Exception {
        schedulerHelper = new SchedulerTHelper(false,
                                               new File(this.getSchedulerReStartConfigurationURL()
                                                            .toURI()).getAbsolutePath(),
                                               new File(RM_CONFIGURATION_RESTART.toURI()).getAbsolutePath(),
                                               null,
                                               false);
        this.createNodes();
    }

    private void killSchedulerAndNodes() throws Exception {
        Thread.sleep(RESTART_SCHEDULER_INTER_TIME_IN_MILLISECONDS);

        TestScheduler.kill();
        RecoverInfrastructureTestHelper.killNodesWithStrongSigKill();

        Thread.sleep(RESTART_SCHEDULER_INTER_TIME_IN_MILLISECONDS);
    }

    private void waitForAllTasksToRun(JobId jobid) throws Exception {
        schedulerHelper.waitForEventTaskRunning(jobid, LAST_REPLICATE_TASK_NAME);
    }

    private JobId submitJob() throws Exception {
        JobId jobid = schedulerHelper.submitJob(new File(JOB_DESCRIPTOR.toURI()).getAbsolutePath());
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
