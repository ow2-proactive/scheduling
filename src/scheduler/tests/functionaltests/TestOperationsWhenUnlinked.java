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

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
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


/**
 * Test checks that it is possible to submit, pause and kill jobs when
 * scheduler isn't linked to RM.
 * 
 * @author ProActive team
 *
 */
public class TestOperationsWhenUnlinked extends FunctionalTest {

    static final String TASK_NAME = "Test task";

    public static class TestJavaTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            System.out.println("OK");
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
    public void test() throws Exception {
        helper.getResourceManager();

        String rmUrl = "rmi://localhost:" + CentralPAPropertyRepository.PA_RMI_PORT.getValue() + "/";

        SchedulerTHelper.startScheduler(false, config.getAbsolutePath(), null, rmUrl);

        testSubmitAndPause(rmUrl);

        testKillJob(rmUrl);
    }

    private void testKillJob(String rmUrl) throws Exception {
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
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.RM_DOWN, 30000);

        System.out.println("Killing job1");
        if (!scheduler.killJob(jobId1)) {
            Assert.fail("Failed to kill job " + jobId1);
        }
        System.out.println("Killing job2");
        if (!scheduler.killJob(jobId2)) {
            Assert.fail("Failed to kill job " + jobId2);
        }

        SchedulerTHelper.waitForEventJobFinished(jobId1);
        SchedulerTHelper.waitForEventPendingJobFinished(jobId2, 30000);

        checkJobResult(scheduler, jobId1, 1);
        checkJobResult(scheduler, jobId2, 0);
    }

    private void testSubmitAndPause(String rmUrl) throws Exception {
        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        System.out.println("Submitting job");
        JobId jobId1 = scheduler.submit(createJob());

        System.out.println("Killing RM");
        helper.killRM();

        System.out.println("Waiting RM_DOWN event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.RM_DOWN, 30000);

        System.out.println("Submitting new job");
        JobId jobId2 = scheduler.submit(createJob());

        if (!scheduler.pauseJob(jobId2)) {
            Assert.fail("Failed to pause job " + jobId2);
        }
        if (!scheduler.resumeJob(jobId2)) {
            Assert.fail("Failed to resume job " + jobId2);
        }

        System.out.println("Creating new RM");
        ResourceManager rm = helper.getResourceManager();
        String nodeUrl = helper.createNode("test-node").getNode().getNodeInformation().getURL();
        rm.addNode(nodeUrl);
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

        System.out.println("Linking new RM");
        if (!scheduler.linkResourceManager(rmUrl)) {
            Assert.fail("Failed to link another RM");
        }

        System.out.println("Waiting when jobs finish");
        SchedulerTHelper.waitForEventJobFinished(jobId1);
        SchedulerTHelper.waitForEventJobFinished(jobId2);

        checkJobResult(scheduler, jobId1, 1);
        checkJobResult(scheduler, jobId2, 1);
    }

    private void checkJobResult(Scheduler scheduler, JobId jobId, int expectedTasksNumber) throws Exception {
        JobResult jobResult = scheduler.getJobResult(jobId);
        Assert.assertEquals("Unexpected number of task results", expectedTasksNumber, jobResult
                .getAllResults().size());
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            System.out.println("Task " + taskResult.getTaskId());
            if (taskResult.getException() != null) {
                taskResult.getException().printStackTrace();
                Assert.assertNull("Unexpected task result exception", taskResult.getException());
            }

            String output = taskResult.getOutput().getAllLogs(false);
            System.out.println("Task output:");
            System.out.println(output);
            Assert.assertTrue("Unxepected output", output.contains("OK"));
        }
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Test job");

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.setName(TASK_NAME);
        job.addTask(javaTask);

        return job;
    }

    private TaskFlowJob createJobWithPendingTask(boolean addNormalTask) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Test pending job");
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
