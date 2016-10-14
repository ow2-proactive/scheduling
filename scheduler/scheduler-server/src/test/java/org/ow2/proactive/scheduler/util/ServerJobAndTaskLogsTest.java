package org.ow2.proactive.scheduler.util;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;


public class ServerJobAndTaskLogsTest {

    @Rule
    public TemporaryFolder fakeSchedulerHome = new TemporaryFolder();

    private JobLogger jobLogger;
    private TaskLogger taskLogger;

    private JobId jobId;
    private TaskId taskId;

    @Before
    public void setUp() {
        PASchedulerProperties.SCHEDULER_JOB_LOGS_LOCATION
                .updateProperty(fakeSchedulerHome.getRoot().getAbsolutePath() + File.separator + "logs");
        // set a very small limit so that only 1 line would fit
        PASchedulerProperties.SCHEDULER_JOB_LOGS_MAX_SIZE.updateProperty("10");

        ServerJobAndTaskLogs.configure();

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

        checkContains(jobId, taskId, "first");

        jobLogger.info(jobId, "second job log");
        taskLogger.info(taskId, "second task log");

        checkContains(jobId, taskId, "second");
        checkDoesNotContain(jobId, taskId, "first");
    }

    @Test
    public void jobLogDeletion() throws Exception {
        jobLogger.info(jobId, "first job log");
        taskLogger.info(taskId, "first task log");
        jobLogger.info(jobId, "second job log");
        taskLogger.info(taskId, "second task log");

        assertTrue(new File(ServerJobAndTaskLogs.getLogsLocation(), JobLogger.getJobLogFilename(jobId))
                .exists());
        assertTrue(new File(ServerJobAndTaskLogs.getLogsLocation(), JobLogger.getJobLogFilename(jobId) + ".1")
                .exists());
        assertTrue(new File(ServerJobAndTaskLogs.getLogsLocation(), TaskLogger.getTaskLogFilename(taskId))
                .exists());
        assertTrue(
                new File(ServerJobAndTaskLogs.getLogsLocation(), TaskLogger.getTaskLogFilename(taskId) + ".1")
                        .exists());

        assertEquals(4, new File(ServerJobAndTaskLogs.getLogsLocation()).list().length);

        ServerJobAndTaskLogs.remove(jobId);

        assertEquals(0, new File(ServerJobAndTaskLogs.getLogsLocation()).list().length);
    }

    @Test
    public void cleanUp() throws Exception {
        jobLogger.info(jobId, "first job log");
        taskLogger.info(taskId, "first task log");
        jobLogger.info(jobId, "second job log");
        taskLogger.info(taskId, "second task log");

        assertEquals(1, fakeSchedulerHome.getRoot().list().length);

        ServerJobAndTaskLogs.removeLogsDirectory();

        assertEquals(0, fakeSchedulerHome.getRoot().list().length);
    }

    private void checkContains(JobId jobId, TaskId taskId, String word) {
        assertThat(ServerJobAndTaskLogs.getJobLog(jobId, Collections.singleton(taskId)),
                containsString(word + " job log"));
        assertThat(ServerJobAndTaskLogs.getJobLog(jobId, Collections.singleton(taskId)),
                containsString(word + " task log"));
        assertThat(ServerJobAndTaskLogs.getTaskLog(taskId), containsString(word + " task log"));
    }

    private void checkDoesNotContain(JobId jobId, TaskId taskId, String word) {
        assertThat(ServerJobAndTaskLogs.getJobLog(jobId, Collections.singleton(taskId)),
                not(containsString(word + " job log")));
        assertThat(ServerJobAndTaskLogs.getJobLog(jobId, Collections.singleton(taskId)),
                not(containsString(word + " task log")));
        assertThat(ServerJobAndTaskLogs.getTaskLog(taskId), not(containsString(word + " task log")));
    }

}