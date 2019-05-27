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
package org.ow2.proactive.scheduler.util;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.*;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.tests.ProActiveTestClean;


public class ServerJobAndTaskLogsTest extends ProActiveTestClean {

    private static File fakeSchedulerHome;

    private JobLogger jobLogger;

    private TaskLogger taskLogger;

    private JobId jobId;

    private TaskId taskId;

    @BeforeClass
    public static void setUpClass() throws IOException {
        fakeSchedulerHome = Files.createTempDirectory(ServerJobAndTaskLogs.class.getSimpleName()).toFile();
        PASchedulerProperties.SCHEDULER_JOB_LOGS_LOCATION.updateProperty(fakeSchedulerHome.getAbsolutePath() +
                                                                         File.separator + "logs");
        // set a very small limit so that only 1 line would fit
        PASchedulerProperties.SCHEDULER_JOB_LOGS_MAX_SIZE.updateProperty("10");

        ServerJobAndTaskLogs.getInstance().configure();
    }

    @AfterClass
    public static void wrapup() throws IOException {
        PASchedulerProperties.SCHEDULER_JOB_LOGS_LOCATION.updateProperty("logs/jobs/");
        PASchedulerProperties.SCHEDULER_JOB_LOGS_MAX_SIZE.updateProperty("10000");
    }

    @Before
    public void setUp() {
        jobLogger = JobLogger.getInstance();
        Logger.getLogger(JobLogger.class).setLevel(Level.INFO);
        taskLogger = TaskLogger.getInstance();
        Logger.getLogger(TaskLogger.class).setLevel(Level.INFO);

        jobId = JobIdImpl.makeJobId("1");
        taskId = TaskIdImpl.createTaskId(jobId, "task1", 10001);
    }

    @Test
    public void sizeLimiting() throws Exception {
        jobLogger.info(jobId, "first job log");
        taskLogger.info(taskId, "first task log");

        jobLogger.close(jobId);
        taskLogger.close(taskId);

        checkContains(jobId, taskId, "first");

        jobLogger.info(jobId, "second job log");
        taskLogger.info(taskId, "second task log");

        jobLogger.close(jobId);
        taskLogger.close(taskId);

        checkContains(jobId, taskId, "second");
        checkDoesNotContain(jobId, taskId, "first");
    }

    @After
    public void after() {
        jobLogger.close(jobId);
        taskLogger.close(taskId);
    }

    @Test
    public void jobLogDeletion() throws Exception {
        jobLogger.info(jobId, "first job log");
        taskLogger.info(taskId, "first task log");
        jobLogger.info(jobId, "second job log");
        taskLogger.info(taskId, "second task log");

        jobLogger.close(jobId);
        taskLogger.close(taskId);

        assertTrue(new File(ServerJobAndTaskLogs.getInstance().getLogsLocation(),
                            JobLogger.getJobLogRelativePath(jobId)).exists());
        assertTrue(new File(ServerJobAndTaskLogs.getInstance().getLogsLocation(),
                            JobLogger.getJobLogRelativePath(jobId) + ".1").exists());
        assertTrue(new File(ServerJobAndTaskLogs.getInstance().getLogsLocation(),
                            TaskLogger.getTaskLogRelativePath(taskId)).exists());
        assertTrue(new File(ServerJobAndTaskLogs.getInstance().getLogsLocation(),
                            TaskLogger.getTaskLogRelativePath(taskId) + ".1").exists());

        assertEquals(4, new File(ServerJobAndTaskLogs.getInstance().getLogsLocation() + "/" + jobId).list().length);

        ServerJobAndTaskLogs.getInstance().remove(jobId, "test");

        assertEquals(0, new File(ServerJobAndTaskLogs.getInstance().getLogsLocation()).list().length);
    }

    @Test
    public void cleanUp() throws Exception {
        jobLogger.info(jobId, "first job log");
        taskLogger.info(taskId, "first task log");
        jobLogger.info(jobId, "second job log");
        taskLogger.info(taskId, "second task log");

        //Thread.sleep(2000);
        System.out.println(fakeSchedulerHome);
        assertEquals(1, fakeSchedulerHome.list().length);

        // avoid keeping a handle on the files
        jobLogger.close(jobId);
        taskLogger.close(taskId);

        ServerJobAndTaskLogs.getInstance().removeLogsDirectory();

        assertEquals(0, fakeSchedulerHome.list().length);
    }

    private void checkContains(JobId jobId, TaskId taskId, String word) throws InterruptedException {
        Thread.sleep(200);
        assertThat(ServerJobAndTaskLogs.getInstance().getJobLog(jobId, Collections.singleton(taskId)),
                   containsString(word + " job log"));
        assertThat(ServerJobAndTaskLogs.getInstance().getJobLog(jobId, Collections.singleton(taskId)),
                   containsString(word + " task log"));
        assertThat(ServerJobAndTaskLogs.getInstance().getTaskLog(taskId), containsString(word + " task log"));
    }

    private void checkDoesNotContain(JobId jobId, TaskId taskId, String word) throws InterruptedException {
        Thread.sleep(200);
        assertThat(ServerJobAndTaskLogs.getInstance().getJobLog(jobId, Collections.singleton(taskId)),
                   not(containsString(word + " job log")));
        assertThat(ServerJobAndTaskLogs.getInstance().getJobLog(jobId, Collections.singleton(taskId)),
                   not(containsString(word + " task log")));
        assertThat(ServerJobAndTaskLogs.getInstance().getTaskLog(taskId), not(containsString(word + " task log")));
    }

}
