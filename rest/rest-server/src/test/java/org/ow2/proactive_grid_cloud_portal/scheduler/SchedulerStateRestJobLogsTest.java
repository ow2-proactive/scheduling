/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobResultImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStoreTestUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SchedulerStateRestJobLogsTest {

    private SchedulerProxyUserInterface mockScheduler;
    private SchedulerStateRest restScheduler;
    private String validSessionId;

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

    private JobResultImpl createJobResult(String taskOutput, String taskErrput) {
        JobResultImpl jobResult = new JobResultImpl();
        jobResult.addTaskResult("OneTask",
          new TaskResultImpl(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("123"), "OneTask", 1, false),
            "result", new SimpleTaskLogs(taskOutput, taskErrput), 100), false);
        return jobResult;
    }
}
