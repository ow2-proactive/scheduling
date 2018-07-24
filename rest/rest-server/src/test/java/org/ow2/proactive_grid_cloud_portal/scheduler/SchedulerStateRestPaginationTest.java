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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatesPage;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStoreTestUtils;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.RestPage;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;


/**
 * The [offset, limit[ boundaries and tag filtering are out of the scope of these tests
 * because their control is done in the <code>JobState</code> class.
 * We are only testing the REST interface methods providing the pagination on tasks.
 * @author paraita
 *
 */
public class SchedulerStateRestPaginationTest extends RestTestServer {

    private SchedulerRestInterface restInterface = null;

    private SchedulerProxyUserInterface mockOfScheduler = null;

    private String sessionId = null;

    private int nbTasks;

    private String jobIdStr = null;

    @Before
    public void setUp() throws Throwable {
        restInterface = new SchedulerStateRest();
        mockOfScheduler = mock(SchedulerProxyUserInterface.class);
        sessionId = SharedSessionStoreTestUtils.createValidSession(mockOfScheduler);
        nbTasks = 50;
        jobIdStr = "1";
    }

    @Test
    public void testGetJobTasksIdsPaginated() throws Throwable {

        JobState job = newMockedJob(jobIdStr, nbTasks);
        when(mockOfScheduler.getJobState(jobIdStr)).thenReturn(job);

        RestPage<String> page = restInterface.getTasksNamesPaginated(sessionId, jobIdStr, 0, nbTasks);

        assertTasks(nbTasks, jobIdStr, page);

    }

    @Test
    public void testGetJobTasksIdsByTagPaginated() throws Throwable {

        JobState job = newMockedJob(jobIdStr, nbTasks);
        when(mockOfScheduler.getJobState(jobIdStr)).thenReturn(job);

        RestPage<String> page = restInterface.getTasksNamesPaginated(sessionId, jobIdStr, 0, nbTasks);

        assertTasks(nbTasks, jobIdStr, page);
    }

    @Test
    public void testGetJobTaskStatesPaginated() throws Throwable {

        JobState job = newMockedJob(jobIdStr, nbTasks);

        List<TaskState> dumbList = new ArrayList<TaskState>(nbTasks);
        for (int i = 0; i < nbTasks; i++) {
            TaskState mockedTask = mock(TaskState.class);
            TaskId mockedTaskId = mock(TaskId.class);
            when(mockedTaskId.getReadableName()).thenReturn(generateReadableName(jobIdStr, i, nbTasks));
            when(mockedTask.getId()).thenReturn(mockedTaskId);
            dumbList.add(mockedTask);
        }

        when(mockOfScheduler.getTaskPaginated(jobIdStr, 0, nbTasks)).thenReturn(new TaskStatesPage(dumbList, nbTasks));

        List<TaskStateData> res = restInterface.getJobTaskStatesPaginated(sessionId, jobIdStr, 0, nbTasks).getList();

        assertEquals("Number of tasks is incorrect", nbTasks, res.size());

    }

    @Test
    public void testGetJobTaskStatesByTagPaginated() throws Throwable {

        JobState job = newMockedJob(jobIdStr, nbTasks);
        when(mockOfScheduler.getJobState(jobIdStr)).thenReturn(job);

        List<TaskStateData> res = restInterface.getJobTaskStatesByTagPaginated(sessionId, jobIdStr, "", 0, nbTasks)
                                               .getList();

        assertEquals("Number of tasks is incorrect", nbTasks, res.size());

    }

    private JobState newMockedJob(final String jobIdStr, final int nbTasks) {
        JobState mockedJob = mock(JobState.class);
        JobId mockedJobId = mock(JobId.class);

        List<TaskState> dumbList = new ArrayList<TaskState>(nbTasks);
        for (int i = 0; i < nbTasks; i++) {
            TaskState mockedTask = mock(TaskState.class);
            TaskId mockedTaskId = mock(TaskId.class);
            when(mockedTaskId.getReadableName()).thenReturn(generateReadableName(jobIdStr, i, nbTasks));
            when(mockedTask.getId()).thenReturn(mockedTaskId);
            dumbList.add(mockedTask);
        }

        when(mockedJobId.value()).thenReturn(jobIdStr);
        when(mockedJob.getId()).thenReturn(mockedJobId);
        when(mockedJob.getTasksPaginated(0, 50)).thenReturn(new TaskStatesPage(dumbList, nbTasks));
        when(mockedJob.getTaskByTagPaginated("", 0, 50)).thenReturn(new TaskStatesPage(dumbList, nbTasks));
        return mockedJob;
    }

    private String generateReadableName(final String jobIdStr, final int i, final int nbTasks) {
        return "JOB-" + jobIdStr + "-TASK-" + (i + 1) + "/" + nbTasks;
    }

    private void assertTasks(final int nbTasks, String jobIdStr, RestPage<String> page) {
        assertEquals("Number of tasks is incorrect", nbTasks, page.getSize());

        for (int i = 0; i < nbTasks; i++) {
            assertEquals("Task readable name is incorrect",
                         generateReadableName(jobIdStr, i, nbTasks),
                         page.getList().get(i));
        }
    }

}
