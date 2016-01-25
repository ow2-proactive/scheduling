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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Page;
import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStoreTestUtils;
import org.ow2.proactive_grid_cloud_portal.common.SortSpecifierRestContainer;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.RestPage;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskInfoData;
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

        when(mockOfScheduler.getTaskIds(tag, 0, 0, false, true, true, true, 0, nbTasks))
                .thenReturn(expectedPage);

        RestPage<String> page = restInterface.getTaskIdsByTag(sessionId, tag, 0, 0, false, true, true, true,
                0, nbTasks);

        RestTestUtils.assertTasks(nbTasks, jobIdStr, page);
    }

    @Test
    public void testGetTaskStates() throws Throwable {
        int nbTasksInPage = 50;
        int nbTotalTasks = 100;
        String jobIdStr = "1";
        String tag = null;
        Page<TaskState> expectedPage = RestTestUtils.newMockedTaskStatePage(jobIdStr, tag, nbTasksInPage,
                nbTotalTasks);

        when(mockOfScheduler.getTaskStates(anyString(), anyLong(), anyLong(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt(),
                anyInt(), any(SortSpecifierContainer.class)))
                .thenReturn(expectedPage);

        RestPage<TaskStateData> page = restInterface.getTaskStates(sessionId, 0, 0, false, true, true, true,
                0, nbTasksInPage, null);

        RestTestUtils.assertTaskStates(expectedPage, page);
    }

    @Test
    public void testGetTaskStatesByTagNoSorting() throws Throwable {
        int nbTasksInPage = 50;
        int nbTotalTasks = 100;
        String jobIdStr = "1";
        String tag = "TAG-TEST";
        Page<TaskState> expectedPage = RestTestUtils.newMockedTaskStatePage(jobIdStr, tag, nbTasksInPage,
                nbTotalTasks);

        when(mockOfScheduler.getTaskStates(anyString(), anyLong(), anyLong(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt(),
                anyInt(), any(SortSpecifierContainer.class)))
                .thenReturn(expectedPage);

        RestPage<TaskStateData> page = restInterface.getTaskStatesByTag(sessionId, tag, 0, 0, false, true,
                true, true, 0, nbTasksInPage, new SortSpecifierRestContainer());

        RestTestUtils.assertTaskStates(expectedPage, page);
    }

    @Test
    public void testGetTaskStatesByTagSortByIdDesc() throws Throwable {
        int nbTasksInPage = 50;
        int nbTotalTasks = 100;
        String jobIdStr = "1";
        String tag = "TAG-TEST";
        Page<TaskState> expectedPage = RestTestUtils.newMockedTaskStatePage(jobIdStr, tag, nbTasksInPage,
                nbTotalTasks);

        when(mockOfScheduler.getTaskStates(anyString(), anyLong(), anyLong(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt(),
                anyInt(), any(SortSpecifierContainer.class)))
                .thenReturn(expectedPage);

        RestPage<TaskStateData> page = restInterface.getTaskStatesByTag(sessionId, tag, 0, 0, false, true,
                true, true, 0, nbTasksInPage, new SortSpecifierRestContainer(".id.taskId,descending"));

        RestTestUtils.assertTaskStates(expectedPage, page);

        // let's check only the first two as the string comparison is not valid
        // after that case : "JOB-1-TASK-1/50".compareTo("JOB-1-TASK-10/50")
        List<TaskStateData> tasks = page.getList();
        TaskStateData previousTask = tasks.get(0);
        TaskStateData currentTask = tasks.get(1);
        assertTrue(previousTask.getName().compareTo(currentTask.getName()) < 0);
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
        when(mockOfScheduler.getJobState(jobIdStr)).thenReturn(job);

        List<TaskStateData> res = restInterface.getJobTaskStatesPaginated(sessionId, jobIdStr, 0, nbTasks)
                .getList();

        assertEquals("Number of tasks is incorrect", nbTasks, res.size());

    }

}
