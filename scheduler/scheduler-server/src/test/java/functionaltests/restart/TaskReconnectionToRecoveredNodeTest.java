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
package functionaltests.restart;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive.scheduler.common.util.logforwarder.providers.SocketBasedForwardingProvider;

import functionaltests.job.log.TestListenJobLogs;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;
import functionaltests.utils.TestNode;
import functionaltests.utils.TestScheduler;


/**
 * @author ActiveEon Team
 * @since 20/09/17
 */
public abstract class TaskReconnectionToRecoveredNodeTest extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static final URL JOB_DESCRIPTOR = TaskReconnectionToRecoveredNodeTest.class.getResource("/functionaltests/descriptors/Job_TaskReconnectionOnRestart.xml");

    private static final URL RM_CONFIGURATION_START = TaskReconnectionToRecoveredNodeTest.class.getResource("/functionaltests/config/functionalTRMProperties-clean-db.ini");

    private static final URL RM_CONFIGURATION_RESTART = TaskReconnectionToRecoveredNodeTest.class.getResource("/functionaltests/config/functionalTRMProperties-keep-db.ini");

    private static final int NB_NODES = 3;

    private static final String TASK_LOG_OUTPUT_STARTING_STRING = "Step ";

    private static final int MAXIMUM_STEP_IN_TASK_LOOP = 119;

    private static final long LOG_EVENT_TIMEOUT = 120000;

    private static final int RESTART_SCHEDULER_INTER_TIME_IN_MILLISECONDS = 5000;

    private static final String TASK_NAME = "TwoMinutes_Task";

    private static final String LOGGER_NAME = "taskReconnectionAppender";

    private static final String OK_TASK_RESULT_VALUE = "OK";

    private static LogForwardingService logForwardingService;

    private List<TestNode> nodes;

    protected abstract URL getSchedulerStartConfigurationURL();

    protected abstract URL getSchedulerReStartConfigurationURL();

    @Before
    public void startDedicatedScheduler() throws Exception {
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                                               new File(getSchedulerStartConfigurationURL().toURI()).getAbsolutePath(),
                                               new File(RM_CONFIGURATION_START.toURI()).getAbsolutePath(),
                                               null);
        logForwardingService = new LogForwardingService(SocketBasedForwardingProvider.class.getName());
        logForwardingService.initialize();
    }

    @Test
    public void action() throws Throwable {
        ResourceManager rm = schedulerHelper.getResourceManager();

        nodes = schedulerHelper.createRMNodeStarterNodes(TaskReconnectionWithForkedTaskExecutorTest.class.getSimpleName(),
                                                         NB_NODES);

        JobId jobid = schedulerHelper.submitJob(new File(JOB_DESCRIPTOR.toURI()).getAbsolutePath());
        schedulerHelper.waitForEventJobRunning(jobid);

        TaskState taskState = schedulerHelper.getSchedulerInterface().getJobState(jobid).getTasks().get(0);
        String firstExecutionHostInfo = taskState.getTaskInfo().getExecutionHostName();

        // wait and restart scheduler
        Thread.sleep(RESTART_SCHEDULER_INTER_TIME_IN_MILLISECONDS);
        TestScheduler.kill();
        Thread.sleep(RESTART_SCHEDULER_INTER_TIME_IN_MILLISECONDS);

        schedulerHelper = new SchedulerTHelper(false,
                                               new File(getSchedulerReStartConfigurationURL().toURI()).getAbsolutePath(),
                                               new File(RM_CONFIGURATION_RESTART.toURI()).getAbsolutePath(),
                                               null,
                                               false);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        TestListenJobLogs.TestAppender appender = new TestListenJobLogs.TestAppender(LOGGER_NAME);
        String loggerName = Log4JTaskLogs.JOB_LOGGER_PREFIX + jobid;
        logForwardingService.removeAllAppenders(LOGGER_NAME);
        logForwardingService.addAppender(loggerName, appender);
        scheduler.listenJobLogs(jobid, logForwardingService.getAppenderProvider());

        System.out.println("Number of nodes: " + schedulerHelper.getResourceManager().getState().getAllNodes().size());

        for (String freeNodeUrl : schedulerHelper.getResourceManager().getState().getFreeNodes()) {
            // previous executing node should not be free when the nodes are added back to the rm
            Assert.assertFalse(firstExecutionHostInfo.contains(freeNodeUrl));
        }

        // we should have just one running task
        JobState jobState = scheduler.getJobState(jobid);
        Assert.assertEquals(0, jobState.getNumberOfPendingTasks());
        Assert.assertEquals(1, jobState.getNumberOfRunningTasks());

        taskState = jobState.getTasks().get(0);
        Assert.assertEquals(firstExecutionHostInfo, taskState.getTaskInfo().getExecutionHostName());

        appender.waitForLoggingEvent(LOG_EVENT_TIMEOUT, TASK_LOG_OUTPUT_STARTING_STRING + MAXIMUM_STEP_IN_TASK_LOOP);
        schedulerHelper.waitForEventJobFinished(jobid);

        TaskResult taskResult = scheduler.getJobResult(jobid).getResult(TASK_NAME);
        Assert.assertFalse(taskResult.hadException());
        Assert.assertEquals(OK_TASK_RESULT_VALUE, taskResult.value());

        String logs = taskResult.getOutput().getStdoutLogs();
        for (int i = 0; i < MAXIMUM_STEP_IN_TASK_LOOP; i++) {
            Assert.assertTrue(logs.contains(TASK_LOG_OUTPUT_STARTING_STRING + i));
        }

    }

    @After
    public void after() throws Exception {
        if (logForwardingService != null) {
            logForwardingService.terminate();
        }
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
