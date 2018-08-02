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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ow2.proactive_grid_cloud_portal.scheduler.RestTestUtils.newTaskState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Page;
import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
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
 * because their control is done in the <code>SchedulerDBManager</code> class.
 * We are only testing the REST interface methods providing the task-centric feature.
 *
 */
public class SchedulerStateRestTaskCentricTest extends RestTestServer {

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
    public void testGetTaskIds() throws Throwable {
        int nbTasks = 100;
        String jobIdStr = "1";
        Page<TaskId> expectedPage = RestTestUtils.newMockedTaskIdPage(jobIdStr, nbTasks, null);

        when(mockOfScheduler.getTaskIds(null, 0, 0, false, true, true, true, 0, 50)).thenReturn(expectedPage);

        RestPage<String> page = restInterface.getTaskIds(sessionId, 0, 0, false, true, true, true, 0, 50);

        RestTestUtils.assertTasks(nbTasks, jobIdStr, page);
    }

    @Test
    public void testGetTaskIdsByTag() throws Throwable {
        int nbTasks = 100;
        String jobIdStr = "500";
        String tag = "TAG-TEST";
        Page<TaskId> expectedPage = RestTestUtils.newMockedTaskIdPage(jobIdStr, nbTasks, tag);

        when(mockOfScheduler.getTaskIds(tag, 0, 0, false, true, true, true, 0, nbTasks)).thenReturn(expectedPage);

        RestPage<String> page = restInterface.getTaskIdsByTag(sessionId,
                                                              tag,
                                                              0,
                                                              0,
                                                              false,
                                                              true,
                                                              true,
                                                              true,
                                                              0,
                                                              nbTasks);

        RestTestUtils.assertTasks(nbTasks, jobIdStr, page);
    }

    @Test
    public void testGetTaskStates() throws Throwable {
        int nbTasksInPage = 50;
        int nbTotalTasks = 100;
        String jobIdStr = "1";
        String tag = null;
        Page<TaskState> expectedPage = RestTestUtils.newMockedTaskStatePage(jobIdStr, tag, nbTasksInPage, nbTotalTasks);

        when(mockOfScheduler.getTaskStates(anyString(),
                                           anyLong(),
                                           anyLong(),
                                           anyBoolean(),
                                           anyBoolean(),
                                           anyBoolean(),
                                           anyBoolean(),
                                           anyInt(),
                                           anyInt(),
                                           any(SortSpecifierContainer.class))).thenReturn(expectedPage);

        RestPage<TaskStateData> page = restInterface.getTaskStates(sessionId,
                                                                   0,
                                                                   0,
                                                                   false,
                                                                   true,
                                                                   true,
                                                                   true,
                                                                   0,
                                                                   nbTasksInPage,
                                                                   null);

        RestTestUtils.assertTaskStates(expectedPage, page);
    }

    @Test
    public void testGetTaskStatesByTagNoSorting() throws Throwable {
        int nbTasksInPage = 50;
        int nbTotalTasks = 100;
        String jobIdStr = "1";
        String tag = "TAG-TEST";
        Page<TaskState> expectedPage = RestTestUtils.newMockedTaskStatePage(jobIdStr, tag, nbTasksInPage, nbTotalTasks);

        when(mockOfScheduler.getTaskStates(anyString(),
                                           anyLong(),
                                           anyLong(),
                                           anyBoolean(),
                                           anyBoolean(),
                                           anyBoolean(),
                                           anyBoolean(),
                                           anyInt(),
                                           anyInt(),
                                           any(SortSpecifierContainer.class))).thenReturn(expectedPage);

        RestPage<TaskStateData> page = restInterface.getTaskStatesByTag(sessionId,
                                                                        tag,
                                                                        0,
                                                                        0,
                                                                        false,
                                                                        true,
                                                                        true,
                                                                        true,
                                                                        0,
                                                                        nbTasksInPage,
                                                                        new SortSpecifierContainer());

        RestTestUtils.assertTaskStates(expectedPage, page);
    }

    @Test
    public void testGetTaskStatesByTagSortByTaskIdDesc() throws Throwable {
        int nbTasksInPage = 50;
        int nbTotalTasks = 100;
        String jobIdStr = "1";
        String tag = "TAG-TEST";
        Page<TaskState> expectedPage = RestTestUtils.newMockedTaskStatePage(jobIdStr, tag, nbTasksInPage, nbTotalTasks);

        when(mockOfScheduler.getTaskStates(anyString(),
                                           anyLong(),
                                           anyLong(),
                                           anyBoolean(),
                                           anyBoolean(),
                                           anyBoolean(),
                                           anyBoolean(),
                                           anyInt(),
                                           anyInt(),
                                           any(SortSpecifierContainer.class))).thenReturn(expectedPage);

        RestPage<TaskStateData> page = restInterface.getTaskStatesByTag(sessionId,
                                                                        tag,
                                                                        0,
                                                                        0,
                                                                        false,
                                                                        true,
                                                                        true,
                                                                        true,
                                                                        0,
                                                                        nbTasksInPage,
                                                                        new SortSpecifierContainer(".id.taskId,descending"));

        RestTestUtils.assertTaskStates(expectedPage, page);

        // let's check only the first two as the string comparison is not valid
        // after that case : "JOB-1-TASK-1/50".compareTo("JOB-1-TASK-10/50")
        List<TaskStateData> tasks = page.getList();
        TaskStateData previousTask = tasks.get(0);
        TaskStateData currentTask = tasks.get(1);
        assertTrue(previousTask.getName().compareTo(currentTask.getName()) < 0);
    }

    @Test
    public void testGetTaskStatesByTagSortByJobIdAsc() throws Throwable {
        int nbTasksInPage = 6;
        int nbTotalTasks = 6;
        ArrayList<String> jobIds = new ArrayList<String>(Arrays.asList("1", "8", "4", "4", "6", "2"));
        String tag = "TAG-TEST";
        Page<TaskState> expectedPage = RestTestUtils.newMockedTaskStatePage(jobIds, tag, nbTasksInPage, nbTotalTasks);

        when(mockOfScheduler.getTaskStates(anyString(),
                                           anyLong(),
                                           anyLong(),
                                           anyBoolean(),
                                           anyBoolean(),
                                           anyBoolean(),
                                           anyBoolean(),
                                           anyInt(),
                                           anyInt(),
                                           any(SortSpecifierContainer.class))).thenReturn(expectedPage);

        RestPage<TaskStateData> page = restInterface.getTaskStatesByTag(sessionId,
                                                                        tag,
                                                                        0,
                                                                        0,
                                                                        false,
                                                                        true,
                                                                        true,
                                                                        true,
                                                                        0,
                                                                        nbTasksInPage,
                                                                        new SortSpecifierContainer(".jobData.id,ascending"));

        RestTestUtils.assertTaskStates(expectedPage, page);
    }

    @Test
    public void testGetJobTasksIdsPaginated() throws Throwable {

        int nbTasks = 50;
        String jobIdStr = "1";
        JobState job = RestTestUtils.newMockedJob(jobIdStr, "", nbTasks);
        when(mockOfScheduler.getJobState(jobIdStr)).thenReturn(job);

        RestPage<String> page = restInterface.getTasksNamesPaginated(sessionId, jobIdStr, 0, nbTasks);

        RestTestUtils.assertTasks(nbTasks, jobIdStr, page);

    }

    @Test
    public void testGetJobTasksIdsByTagPaginated() throws Throwable {
        int nbTasks = 50;
        String jobIdStr = "1";
        JobState job = RestTestUtils.newMockedJob(jobIdStr, "", nbTasks);
        when(mockOfScheduler.getJobState(jobIdStr)).thenReturn(job);

        RestPage<String> page = restInterface.getTasksNamesPaginated(sessionId, jobIdStr, 0, nbTasks);

        RestTestUtils.assertTasks(nbTasks, jobIdStr, page);
    }

    @Test
    public void testGetJobTaskStatesPaginated() throws Throwable {
        int nbTasks = 50;
        String jobIdStr = "1";
        JobState job = RestTestUtils.newMockedJob(jobIdStr, null, nbTasks);

        List<TaskState> dumbList = new ArrayList<TaskState>(nbTasks);
        for (int i = 0; i < nbTasks; i++) {
            dumbList.add(newTaskState(jobIdStr, null, i, nbTasks));
        }

        when(mockOfScheduler.getTaskPaginated(jobIdStr, 0, nbTasks)).thenReturn(new TaskStatesPage(dumbList, nbTasks));

        List<TaskStateData> res = restInterface.getJobTaskStatesPaginated(sessionId, jobIdStr, 0, nbTasks).getList();

        assertEquals("Number of tasks is incorrect", nbTasks, res.size());

    }

}
