/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package functionaltests.rm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.examples.EmptyTask;
import org.ow2.proactive.scripting.SelectionScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import functionaltests.utils.RMTHelper;
import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.SchedulerTestConfiguration;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;


/**
 * Test checks that it is possible to submit, pause and kill jobs when
 * scheduler isn't linked to RM.
 * 
 * @author ProActive team
 *
 */
public class TestOperationsWhenUnlinked extends SchedulerFunctionalTest {

    static final String TASK_NAME = "Test task";

    static final long EVENT_TIMEOUT = 5*60000;
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    private File config;
    private RMTHelper rmHelper;

    @Before
    public void createConfig() throws Exception {
        // set property SCHEDULER_RMCONNECTION_AUTO_CONNECT to false so that RM failure is detected more fast
        File configurationFile = new File(SchedulerTestConfiguration.SCHEDULER_DEFAULT_CONFIGURATION.toURI());

        Properties properties = new Properties();
        properties.load(new FileInputStream(configurationFile));

        config = tmpFolder.newFile("scheduler_config.ini");
        properties.put(PASchedulerProperties.SCHEDULER_RMCONNECTION_AUTO_CONNECT.getKey(), "false");
        properties.store(new FileOutputStream(config), null);
    }

    @After
    public void killRMAndScheduler() throws Exception {
        rmHelper.killRM();
        schedulerHelper.killScheduler();
    }

    @Test
    public void testKillJob() throws Throwable {
        schedulerHelper.killScheduler();

        rmHelper = new RMTHelper();
        String rmUrl = rmHelper.startRM(null, 1299);

        schedulerHelper.startScheduler(false, config.getAbsolutePath(), null, rmUrl);

        String nodeUrl = RMTHelper.createNode("test-node").getNode().getNodeInformation().getURL();
        schedulerHelper.getResourceManager().addNode(nodeUrl);
        schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        log("Submitting job1");
        JobId jobId1 = scheduler.submit(createJobWithPendingTask(true));

        log("Submitting job2");
        JobId jobId2 = scheduler.submit(createJobWithPendingTask(false));

        log("Waiting when one task finishes");
        schedulerHelper.waitForEventTaskFinished(jobId1, TASK_NAME);

        log("Killing RM");
        rmHelper.killRM();

        log("Waiting RM_DOWN event");
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.RM_DOWN, EVENT_TIMEOUT);

        log("Killing job1");
        if (!scheduler.killJob(jobId1)) {
            fail("Failed to kill job " + jobId1);
        }
        log("Killing job2");
        if (!scheduler.killJob(jobId2)) {
            fail("Failed to kill job " + jobId2);
        }

        schedulerHelper.waitForEventJobFinished(jobId1, EVENT_TIMEOUT);
        schedulerHelper.waitForEventPendingJobFinished(jobId2, EVENT_TIMEOUT);

        checkJobResult(scheduler, jobId1, 1);
        checkJobResult(scheduler, jobId2, 0);
    }

    @Test
    public void testSubmitAndPause() throws Throwable {
        schedulerHelper.killScheduler();

        rmHelper = new RMTHelper();
        String rmUrl = rmHelper.startRM(null, 1299);

        schedulerHelper.startScheduler(false, config.getAbsolutePath(), null, rmUrl);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        log("Submitting job");
        JobId jobId1 = scheduler.submit(createJob());

        log("Killing RM");
        rmHelper.killRM();

        log("Waiting RM_DOWN event");
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.RM_DOWN, EVENT_TIMEOUT);

        log("Submitting new job");
        JobId jobId2 = scheduler.submit(createJob());

        if (!scheduler.pauseJob(jobId2)) {
            fail("Failed to pause job " + jobId2);
        }
        if (!scheduler.resumeJob(jobId2)) {
            fail("Failed to resume job " + jobId2);
        }

        log("Creating new RM");

        rmHelper.startRM(null, 1299);
        ResourceManager rm = rmHelper.getResourceManager();
        String nodeUrl = RMTHelper.createNode("test-node").getNode().getNodeInformation().getURL();
        rm.addNode(nodeUrl);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        log("Linking new RM");
        if (!scheduler.linkResourceManager(rmUrl)) {
            fail("Failed to link another RM");
        }

        log("Waiting when jobs finish");
        schedulerHelper.waitForEventJobFinished(jobId1, EVENT_TIMEOUT);
        schedulerHelper.waitForEventJobFinished(jobId2, EVENT_TIMEOUT);

        checkJobResult(scheduler, jobId1, 1);
        checkJobResult(scheduler, jobId2, 1);
    }

    private void checkJobResult(Scheduler scheduler, JobId jobId, int expectedTasksNumber) throws Throwable {
        JobResult jobResult = scheduler.getJobResult(jobId);
        assertEquals("Unexpected number of task results", expectedTasksNumber, jobResult.getAllResults()
                .size());
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            log("Task " + taskResult.getTaskId());
            if (taskResult.getException() != null) {
                fail("Unexpected task result exception:" + taskResult.getException());
            }

            assertEquals(taskResult.value(), "Nothing");
        }
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(EmptyTask.class.getName());
        javaTask.setName(TASK_NAME);
        job.addTask(javaTask);

        return job;
    }

    private TaskFlowJob createJobWithPendingTask(boolean addNormalTask) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_pending");
        job.setCancelJobOnError(false);

        if (addNormalTask) {
            JavaTask javaTask = new JavaTask();
            javaTask.setExecutableClassName(EmptyTask.class.getName());
            javaTask.setName(TASK_NAME);
            job.addTask(javaTask);
        }

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(EmptyTask.class.getName());
        javaTask.setName("Test pending task");
        javaTask.setSelectionScript(new SelectionScript("selected = false;", "JavaScript", false));
        job.addTask(javaTask);

        return job;
    }

}
