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
package org.ow2.proactive_grid_cloud_portal.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobResultImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStoreTestUtils;


public class SchedulerStateRestJobLogsTest {

    private SchedulerProxyUserInterface mockScheduler;

    private SchedulerStateRest restScheduler;

    private String validSessionId;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        restScheduler = new SchedulerStateRest();
        mockScheduler = mock(SchedulerProxyUserInterface.class);
        validSessionId = SharedSessionStoreTestUtils.createValidSession(mockScheduler);
    }

    @Test
    public void jobLogs_not_finished() throws Exception {
        when(mockScheduler.getJobResult("123")).thenReturn(null);

        String jobLogs = restScheduler.jobLogs(validSessionId, "123");

        assertEquals("", jobLogs);
    }

    @Test
    public void jobLogs_finished() throws Exception {
        JobResultImpl jobResult = createJobResult("Hello", "");
        when(mockScheduler.getJobResult("123")).thenReturn(jobResult);

        String jobLogs = restScheduler.jobLogs(validSessionId, "123");

        assertEquals("Hello", jobLogs);
    }

    @Test
    public void jobLogs_finished_with_errput() throws Exception {
        JobResultImpl jobResult = createJobResult("Hello", "World");
        when(mockScheduler.getJobResult("123")).thenReturn(jobResult);

        String jobLogs = restScheduler.jobLogs(validSessionId, "123");

        assertEquals("HelloWorld", jobLogs);
    }

    @Test
    public void job_full_logs_not_finished() throws Exception {
        InternalTaskFlowJob jobState = new InternalTaskFlowJob();
        jobState.addTask(new InternalScriptTask(jobState));
        when(mockScheduler.getJobState("123")).thenReturn(jobState);

        InputStream fullLogs = restScheduler.jobFullLogs(validSessionId, "123", validSessionId);

        assertNull(fullLogs);
    }

    @Test
    public void job_full_logs_finished() throws Exception {
        InternalTaskFlowJob jobState = new InternalTaskFlowJob();
        InternalScriptTask task = new InternalScriptTask(jobState);
        task.setPreciousLogs(true);
        jobState.addTask(task);

        File logFolder = tempFolder.newFolder("0");
        File logFile = new File(logFolder, "TaskLogs-0-0.log");
        FileUtils.write(logFile, "logs", Charset.defaultCharset());

        when(mockScheduler.getJobState("0")).thenReturn(jobState);
        when(mockScheduler.getUserSpaceURIs()).thenReturn(Collections.singletonList(logFolder.getParent()));
        when(mockScheduler.getGlobalSpaceURIs()).thenReturn(Collections.singletonList(logFolder.getParent()));

        InputStream fullLogs = restScheduler.jobFullLogs(validSessionId, "0", validSessionId);

        assertEquals("logs", IOUtils.toString(fullLogs, Charset.defaultCharset()));
    }

    @Test
    public void job_full_logs_sorted() throws Exception {
        InternalTaskFlowJob jobState = new InternalTaskFlowJob();
        addTask(jobState, 1, 10);
        addTask(jobState, 10, 2);
        addTask(jobState, 3, 3);
        File logFolder = tempFolder.newFolder("123");

        File logFile = new File(logFolder, "TaskLogs-123-10.log");
        FileUtils.write(logFile, "10");
        logFile = new File(logFolder, "TaskLogs-123-2.log");
        FileUtils.write(logFile, "2");
        logFile = new File(logFolder, "TaskLogs-123-3.log");
        FileUtils.write(logFile, "3");

        when(mockScheduler.getJobState("123")).thenReturn(jobState);
        when(mockScheduler.getUserSpaceURIs()).thenReturn(Collections.singletonList(logFolder.getParent()));
        when(mockScheduler.getGlobalSpaceURIs()).thenReturn(Collections.singletonList(logFolder.getParent()));

        InputStream fullLogs = restScheduler.jobFullLogs(validSessionId, "123", validSessionId);

        assertEquals("1032", IOUtils.toString(fullLogs));
    }

    private static void addTask(InternalTaskFlowJob jobState, long finishedTime, long id) {
        InternalScriptTask task = new InternalScriptTask(jobState);
        task.setPreciousLogs(true);
        task.setFinishedTime(finishedTime);
        jobState.addTask(task);
        task.setId(TaskIdImpl.createTaskId(new JobIdImpl(123, "job"), "task", id));
    }

    private JobResultImpl createJobResult(String taskOutput, String taskErrput) {
        JobResultImpl jobResult = new JobResultImpl();
        jobResult.addTaskResult("OneTask",
                                new TaskResultImpl(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("123"), "OneTask", 1),
                                                   "result",
                                                   new SimpleTaskLogs(taskOutput, taskErrput),
                                                   100),
                                false);
        return jobResult;
    }
}
