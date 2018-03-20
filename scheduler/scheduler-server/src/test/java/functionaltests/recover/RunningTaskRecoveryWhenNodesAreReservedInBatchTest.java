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

import org.junit.*;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskState;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;
import functionaltests.utils.TestNode;
import functionaltests.utils.TestScheduler;


/**
 * This test checks the specific case where multiple tasks are going to be
 * started: the nodes are reserved (put in busy state) in a batch and then,
 * each task is started one by one. If the scheduler crashes right when tasks
 * are started one by one, we must make sure that the nodes that were reserved
 * in advance (but with no task started on them yet) are freed, and that all
 * the tasks still manage to be continued/started/restarted depending on their
 * state when the scheduler crashed.
 */
public class RunningTaskRecoveryWhenNodesAreReservedInBatchTest
        extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static final URL SCHEDULER_CONFIGURATION_START = RunningTaskRecoveryWhenNodesAreReservedInBatchTest.class.getResource("/functionaltests/config/functionalTSchedulerProperties.ini");

    private static final URL SCHEDULER_CONFIGURATION_RESTART = RunningTaskRecoveryWhenNodesAreReservedInBatchTest.class.getResource("/functionaltests/config/functionalTSchedulerProperties-updateDB.ini");

    private static final URL JOB_DESCRIPTOR = RunningTaskRecoveryWhenNodesAreReservedInBatchTest.class.getResource("/functionaltests/descriptors/Job_TaskReconnectionOnRestart_25_Parallel_Tasks.xml");

    private static final URL RM_CONFIGURATION_START = RunningTaskRecoveryWhenNodesAreReservedInBatchTest.class.getResource("/functionaltests/config/functionalTRMProperties-clean-db.ini");

    private static final URL RM_CONFIGURATION_RESTART = RunningTaskRecoveryWhenNodesAreReservedInBatchTest.class.getResource("/functionaltests/config/functionalTRMProperties-keep-db.ini");

    private static final int NB_NODES = 26;

    private List<TestNode> nodes;

    @Before
    public void startDedicatedScheduler() throws Exception {
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                                               new File(SCHEDULER_CONFIGURATION_START.toURI()).getAbsolutePath(),
                                               new File(RM_CONFIGURATION_START.toURI()).getAbsolutePath(),
                                               null);
    }

    @Test
    public void action() throws Throwable {
        nodes = schedulerHelper.createRMNodeStarterNodes(RunningTaskRecoveryWhenNodesAreReservedInBatchTest.class.getSimpleName(),
                                                         NB_NODES);
        for (int i = 0; i < NB_NODES; i++) {
            schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        JobId jobid = schedulerHelper.submitJob(new File(JOB_DESCRIPTOR.toURI()).getAbsolutePath());
        schedulerHelper.waitForEventJobRunning(jobid);
        schedulerHelper.waitForEventTaskFinished(jobid, "Groovy_Task");

        JobState jobState = schedulerHelper.getSchedulerInterface().getJobState(jobid);
        SchedulerTHelper.log("Total number of tasks: " + jobState.getTotalNumberOfTasks());
        List<TaskState> tasks = jobState.getTasks();
        TaskState fifthTask = tasks.get(5);
        SchedulerTHelper.log("Wait for the fifth task running");
        // we wait for the fifth task to be running so that when we kill the
        // scheduler we have a mix of pending/submitted/running tasks
        schedulerHelper.waitForEventTaskRunning(fifthTask.getJobId(), fifthTask.getName());
        SchedulerTHelper.log("Fifth task is running");

        // restart scheduler
        printRmStateAndReturnNotFreeNodeNumber();
        printJobStateAndReturnNumberOfRunningTasks(jobid);
        TestScheduler.kill();
        schedulerHelper = new SchedulerTHelper(false,
                                               new File(SCHEDULER_CONFIGURATION_RESTART.toURI()).getAbsolutePath(),
                                               new File(RM_CONFIGURATION_RESTART.toURI()).getAbsolutePath(),
                                               null,
                                               false);

        SchedulerTHelper.log("Wait for job to finish");
        JobInfo jobInfo = schedulerHelper.waitForEventJobFinished(jobid);

        assertThat(jobInfo.getNumberOfFailedTasks()).isEqualTo(0);
        assertThat(jobInfo.getNumberOfInErrorTasks()).isEqualTo(0);

        // wait for all nodes released
        while (printRmStateAndReturnNotFreeNodeNumber() != 0) {
            schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }

        // all nodes should be free now
        int notFreeNodeNumber = printRmStateAndReturnNotFreeNodeNumber();
        assertThat(notFreeNodeNumber).isEqualTo(0);
    }

    private int printRmStateAndReturnNotFreeNodeNumber() throws Exception {
        ResourceManager rm = schedulerHelper.getResourceManager();
        RMState rmState = rm.getState();
        int totalNodesNumber = rmState.getTotalNodesNumber();
        int freeNodesNumber = rmState.getFreeNodesNumber();
        // just to give *an idea* of how many node are free/busy  just before
        // killing the scheduler
        SchedulerTHelper.log("Total node number " + totalNodesNumber);
        SchedulerTHelper.log("Free node number " + freeNodesNumber);

        return totalNodesNumber - freeNodesNumber;
    }

    private void printJobStateAndReturnNumberOfRunningTasks(JobId jobid) throws Exception {
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        JobState jobState;
        int numberOfPendingTasks;
        int numberOfRunningTasks;
        jobState = scheduler.getJobState(jobid);
        numberOfPendingTasks = jobState.getNumberOfPendingTasks();
        numberOfRunningTasks = jobState.getNumberOfRunningTasks();
        int numberOfFinishedTasks = jobState.getNumberOfFinishedTasks();
        int numberOfInErrorTasks = jobState.getNumberOfInErrorTasks();

        SchedulerTHelper.log("Number of pending tasks " + numberOfPendingTasks);
        SchedulerTHelper.log("Number of running tasks " + numberOfRunningTasks);
        SchedulerTHelper.log("Number of finished tasks " + numberOfFinishedTasks);
        SchedulerTHelper.log("Number of in error tasks " + numberOfInErrorTasks);
    }

    @After
    public void after() throws Exception {
        if (nodes != null) {
            for (TestNode node : nodes) {
                try {
                    node.kill();
                } catch (Exception e) {
                    // keep exceptions there silent
                }
            }
        }
    }

}
