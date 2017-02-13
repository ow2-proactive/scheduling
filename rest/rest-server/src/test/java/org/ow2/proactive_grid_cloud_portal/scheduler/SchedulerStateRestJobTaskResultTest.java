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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobResultImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStoreTestUtils;


public class SchedulerStateRestJobTaskResultTest extends RestTestServer {

    private SchedulerRestInterface restInterface = null;

    private SchedulerProxyUserInterface mockOfScheduler = null;

    private String sessionId = null;

    @Before
    public void setUp() throws Throwable {
        restInterface = new SchedulerStateRest();
        mockOfScheduler = mock(SchedulerProxyUserInterface.class);
        sessionId = SharedSessionStoreTestUtils.createValidSession(mockOfScheduler);
    }

    @Test
    public void testValueOfTaskResult_ExceptionNoMessage() throws Throwable {
        TaskResultImpl taskResultWithException = new TaskResultImpl(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("42"),
                                                                                            "mytask",
                                                                                            1),
                                                                    null,
                                                                    new byte[0],
                                                                    null);
        when(mockOfScheduler.getTaskResult("42", "mytask")).thenReturn(taskResultWithException);

        String exceptionStackTrace = (String) restInterface.valueOfTaskResult(sessionId, "42", "mytask");

        assertNotNull(exceptionStackTrace);
    }

    @Test
    public void testValueOfJobResult_ExceptionNoMessage() throws Throwable {
        TaskResultImpl taskResultWithException = new TaskResultImpl(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("42"),
                                                                                            "mytask",
                                                                                            1),
                                                                    null,
                                                                    new byte[0],
                                                                    null);
        JobResultImpl jobResultWithException = new JobResultImpl();
        jobResultWithException.addTaskResult("mytask", taskResultWithException, false);
        when(mockOfScheduler.getJobResult("42")).thenReturn(jobResultWithException);

        Map<String, String> jobResult = restInterface.jobResultValue(sessionId, "42");
        String exceptionStackTrace = jobResult.get("mytask");

        assertNotNull(exceptionStackTrace);
    }

}
