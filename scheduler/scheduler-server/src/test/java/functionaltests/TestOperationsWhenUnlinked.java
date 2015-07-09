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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
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
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.tests.FunctionalTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Test checks that it is possible to submit, pause and kill jobs when
 * scheduler isn't linked to RM.
 * 
 * @author ProActive team
 *
 */
public class TestOperationsWhenUnlinked extends FunctionalTest {

    static final String TASK_NAME = "Test task";

    static final long EVENT_TIMEOUT = 30000;
    private static final int RM_PNP_PORT = 1199;

    public static class TestJavaTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            return "OK";
        }

    }

    private RMTHelper helper = RMTHelper.getDefaultInstance();

    private File config;

    @Before
    public void createConfig() throws Exception {
        // set property SCHEDULER_RMCONNECTION_AUTO_CONNECT to false so that RM failure is detected more fast
        File configurationFile = new File(SchedulerTHelper.functionalTestSchedulerProperties.toURI());

        Properties properties = new Properties();
        properties.load(new FileInputStream(configurationFile));

        config = new File(System.getProperty("java.io.tmpdir") + File.separator + "scheduler_config.ini");
        properties.put(PASchedulerProperties.SCHEDULER_RMCONNECTION_AUTO_CONNECT.getKey(), "false");
        properties.store(new FileOutputStream(config), null);
    }

    @After
    public void deleteConfig() {
        if (config != null) {
            config.delete();
        }
    }

    @Test
    public void test() throws Throwable {

        helper.startRM(null, RM_PNP_PORT);

        String rmUrl = helper.getLocalUrl(RM_PNP_PORT);

        SchedulerTHelper.startScheduler(false, config.getAbsolutePath(), null, rmUrl);

        testSubmitAndPause(rmUrl);

        testKillJob();
    }

    private void testKillJob() throws Throwable {
        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        System.out.println("Submitting job1");
        JobId jobId1 = scheduler.submit(createJobWithPendingTask(true));

        System.out.println("Submitting job2");
        JobId jobId2 = scheduler.submit(createJobWithPendingTask(false));

        System.out.println("Waiting when one task finishes");
        SchedulerTHelper.waitForEventTaskFinished(jobId1, TASK_NAME);

        System.out.println("Killing RM");
        helper.killRM();

        System.out.println("Waiting RM_DOWN event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.RM_DOWN, EVENT_TIMEOUT);

        System.out.println("Killing job1");
        if (!scheduler.killJob(jobId1)) {
            fail("Failed to kill job " + jobId1);
        }
        System.out.println("Killing job2");
        if (!scheduler.killJob(jobId2)) {
            fail("Failed to kill job " + jobId2);
        }

        SchedulerTHelper.waitForEventJobFinished(jobId1, EVENT_TIMEOUT);
        SchedulerTHelper.waitForEventPendingJobFinished(jobId2, EVENT_TIMEOUT);

        checkJobResult(scheduler, jobId1, 1);
        checkJobResult(scheduler, jobId2, 0);
    }

    private void testSubmitAndPause(String rmUrl) throws Throwable {
        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        System.out.println("Submitting job");
        JobId jobId1 = scheduler.submit(createJob());

        System.out.println("Killing RM");
        helper.killRM();

        System.out.println("Waiting RM_DOWN event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.RM_DOWN, EVENT_TIMEOUT);

        System.out.println("Submitting new job");
        JobId jobId2 = scheduler.submit(createJob());

        if (!scheduler.pauseJob(jobId2)) {
            fail("Failed to pause job " + jobId2);
        }
        if (!scheduler.resumeJob(jobId2)) {
            fail("Failed to resume job " + jobId2);
        }

        System.out.println("Creating new RM");

        helper.startRM(null, RM_PNP_PORT);
        ResourceManager rm = helper.getResourceManager();
        String nodeUrl = helper.createNode("test-node").getNode().getNodeInformation().getURL();
        rm.addNode(nodeUrl);
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        System.out.println("Linking new RM");
        if (!scheduler.linkResourceManager(rmUrl)) {
            fail("Failed to link another RM");
        }

        System.out.println("Waiting when jobs finish");
        SchedulerTHelper.waitForEventJobFinished(jobId1, EVENT_TIMEOUT);
        SchedulerTHelper.waitForEventJobFinished(jobId2, EVENT_TIMEOUT);

        checkJobResult(scheduler, jobId1, 1);
        checkJobResult(scheduler, jobId2, 1);
    }

    private void checkJobResult(Scheduler scheduler, JobId jobId, int expectedTasksNumber) throws Throwable {
        JobResult jobResult = scheduler.getJobResult(jobId);
        assertEquals("Unexpected number of task results", expectedTasksNumber, jobResult.getAllResults()
                .size());
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            System.out.println("Task " + taskResult.getTaskId());
            if (taskResult.getException() != null) {
                taskResult.getException().printStackTrace();
                assertNull("Unexpected task result exception", taskResult.getException());
            }

            assertEquals(taskResult.value(), "OK");
        }
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
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
            javaTask.setExecutableClassName(TestJavaTask.class.getName());
            javaTask.setName(TASK_NAME);
            job.addTask(javaTask);
        }

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.setName("Test pending task");
        javaTask.setSelectionScript(new SelectionScript("selected = false;", "JavaScript", false));
        job.addTask(javaTask);

        return job;
    }

}
