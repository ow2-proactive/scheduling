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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Page;
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
        Page<TaskId> expectedPage = newMockedTaskIdPage(jobIdStr, nbTasks, null);
        
        when(mockOfScheduler.getTaskIds(null, 0, 0, false, true, true, true, 0, 50))
        .thenReturn(expectedPage);

        RestPage<String> page = restInterface.getTaskIds(sessionId, 0, 0, false, true, true, true, 0, 50);
        
        assertTasks(nbTasks, jobIdStr, page);
    }
    
    @Test
    public void testGetTaskIdsByTag() throws Throwable {
        int nbTasks = 100;
        String jobIdStr = "500";
        String tag = "TAG-TEST";
        Page<TaskId> expectedPage = newMockedTaskIdPage(jobIdStr, nbTasks, tag);
        
        when(mockOfScheduler.getTaskIds(tag, 0, 0, false, true, true, true, 0, nbTasks))
        .thenReturn(expectedPage);

        RestPage<String> page = restInterface.getTaskIdsByTag(sessionId, tag, 0, 0, false, true, true, true, 0, nbTasks);
        
        assertTasks(nbTasks, jobIdStr, page);
    } 
    
    @Test
    public void testGetTaskStates() throws Throwable {
        int nbTasksInPage = 50;
        int nbTotalTasks = 100;
        String jobIdStr = "1";
        String tag = null;
        Page<TaskState> expectedPage = newMockedTaskStatePage(jobIdStr, tag, nbTasksInPage, nbTotalTasks);
        
        when(mockOfScheduler.getTaskStates(null, 0, 0, false, true, true, true, 0, nbTasksInPage))
        .thenReturn(expectedPage);

        RestPage<TaskStateData> page = restInterface.getTaskStates(sessionId, 0, 0, false, true, true, true, 0, nbTasksInPage);
        
        assertTaskStates(expectedPage, page);
    }

    @Test
    public void testGetTaskStatesByTag() throws Throwable {
        int nbTasksInPage = 50;
        int nbTotalTasks = 100;
        String jobIdStr = "1";
        String tag = "TAG-TEST";
        Page<TaskState> expectedPage = newMockedTaskStatePage(jobIdStr, tag, nbTasksInPage, nbTotalTasks);

        when(mockOfScheduler.getTaskStates(tag, 0, 0, false, true, true, true, 0, nbTasksInPage))
                .thenReturn(expectedPage);

        RestPage<TaskStateData> page = restInterface.getTaskStatesByTag(sessionId, tag, 0, 0, false, true,
                true, true, 0, nbTasksInPage);

        assertTaskStates(expectedPage, page);
    }

    @Test
    public void testGetJobTasksIdsPaginated() throws Throwable {

        int nbTasks = 50;
        String jobIdStr = "1";
        JobState job = newMockedJob(jobIdStr, "", nbTasks);
        when(mockOfScheduler.getJobState(jobIdStr)).thenReturn(job);

        RestPage<String> page = restInterface.getTasksNamesPaginated(sessionId, jobIdStr, 0, nbTasks);

        assertTasks(nbTasks, jobIdStr, page);
        
    }

    @Test
    public void testGetJobTasksIdsByTagPaginated() throws Throwable {
        int nbTasks = 50;
        String jobIdStr = "1";
        JobState job = newMockedJob(jobIdStr, "", nbTasks);
        when(mockOfScheduler.getJobState(jobIdStr)).thenReturn(job);

        RestPage<String> page = restInterface.getTasksNamesPaginated(sessionId, jobIdStr, 0, nbTasks);

        assertTasks(nbTasks, jobIdStr, page);
    }

    @Test
    public void testGetJobTaskStatesPaginated() throws Throwable {
        int nbTasks = 50;
        String jobIdStr = "1";
        JobState job = newMockedJob(jobIdStr, null, nbTasks);
        when(mockOfScheduler.getJobState(jobIdStr)).thenReturn(job);

        List<TaskStateData> res = restInterface.getJobTaskStatesPaginated(sessionId, jobIdStr, 0, nbTasks)
                .getList();

        assertEquals("Number of tasks is incorrect", nbTasks, res.size());

    }

    @Test
    public void testGetJobTaskStatesByTagPaginated() throws Throwable {
        int nbTasks = 50;
        String jobIdStr = "1";
        JobState job = newMockedJob(jobIdStr, "", nbTasks);
        when(mockOfScheduler.getJobState(jobIdStr)).thenReturn(job);

        List<TaskStateData> res = restInterface
                .getJobTaskStatesByTagPaginated(sessionId, jobIdStr, "", 0, nbTasks).getList();

        assertEquals("Number of tasks is incorrect", nbTasks, res.size());

    }

    private JobState newMockedJob(final String jobIdStr, final String tag, final int nbTasks) {
        JobState mockedJob = mock(JobState.class);
        JobId mockedJobId = mock(JobId.class);

        List<TaskState> dumbList = new ArrayList<TaskState>(nbTasks);
        for (int i = 0; i < nbTasks; i++) {
            dumbList.add(newTaskState(jobIdStr, null, i, nbTasks));
        }

        when(mockedJobId.value()).thenReturn(jobIdStr);
        when(mockedJob.getId()).thenReturn(mockedJobId);
        when(mockedJob.getTasksPaginated(0, 50)).thenReturn(new TaskStatesPage(dumbList, nbTasks));
        when(mockedJob.getTaskByTagPaginated("", 0, 50)).thenReturn(new TaskStatesPage(dumbList, nbTasks));
        return mockedJob;
    }
    
    private Page<TaskId> newMockedTaskIdPage(String jobIdStr, int nbTasks, String tag) {
        List<TaskId> lTaskId = new ArrayList<TaskId>(nbTasks);
        for (int i = 0 ; i < nbTasks ; i++) {
            TaskId mockedTaskId = mock(TaskId.class);
            when(mockedTaskId.getReadableName()).thenReturn(generateReadableName(jobIdStr, i, nbTasks));
            when(mockedTaskId.getTag()).thenReturn(tag);
            lTaskId.add(mockedTaskId);
        }
        return new Page<TaskId>(lTaskId, nbTasks);
    }
    
    private Page<TaskState> newMockedTaskStatePage(String jobIdStr, String tag, int nbTasks, int totalNumberOfTasks) {
        List<TaskState> lTasks = new ArrayList<TaskState>(nbTasks);
        for (int i = 0; i < nbTasks ; i++) {
            lTasks.add(newTaskState(jobIdStr, tag, i, nbTasks));
        }
        return new Page<TaskState>(lTasks, totalNumberOfTasks);
    }
    
    private TaskState newTaskState(String jobIdStr, String tag, int cnt, int nbTasks) {
        TaskId mockedTaskId = mock(TaskId.class);
        TaskState mockedState = mock(TaskState.class);
        when(mockedTaskId.getReadableName()).thenReturn(generateReadableName(jobIdStr, cnt, nbTasks));
        when(mockedState.getTag()).thenReturn(tag);
        when(mockedState.getId()).thenReturn(mockedTaskId);
        when(mockedState.getName()).thenReturn(generateReadableName(jobIdStr, cnt, nbTasks));
        return mockedState;
    }
    
    private TaskStateData newMockedTaskStateData(String jobIdStr, String tag, int cnt, int nbTasks) {
        TaskStateData mockedTaskStateData = mock(TaskStateData.class);
        TaskInfoData mockedTaskInfoData = mock(TaskInfoData.class);
        TaskIdData mockedTaskIdData = mock(TaskIdData.class);
        when(mockedTaskIdData.getReadableName()).thenReturn(generateReadableName(jobIdStr, cnt, nbTasks));
        when(mockedTaskInfoData.getTaskId()).thenReturn(mockedTaskIdData);
        when(mockedTaskStateData.getTaskInfo()).thenReturn(mockedTaskInfoData);
        when(mockedTaskStateData.getTag()).thenReturn(tag);
        return mockedTaskStateData;
    }


    private String generateReadableName(final String jobIdStr, final int i, final int nbTasks) {
        return "JOB-" + jobIdStr + "-TASK-" + (i + 1) + "/" + nbTasks;
    }
    
    private void assertTasks(final int nbTasks, String jobIdStr, RestPage<String> page) {
        assertEquals("Number of tasks is incorrect", nbTasks, page.getSize());

        for (int i = 0; i < nbTasks; i++) {
            assertEquals("Task readable name is incorrect", generateReadableName(jobIdStr, i, nbTasks),
                    page.getList().get(i));
        }
    }
    
    private void assertTaskStates(Page<TaskState> expected, RestPage<TaskStateData> actual) {

        assertEquals("Number of tasks in page is incorrect", expected.getList().size(),
                actual.getList().size());
        assertEquals("Total number of tasks is incorrect", expected.getSize(), actual.getSize());
        
        for (int i = 0; i < expected.getList().size(); i++) {
            TaskState tExpected = expected.getList().get(i);
            TaskStateData tActual = actual.getList().get(i);
            assertEquals("readableName incorrect", tExpected.getId().getReadableName(), tActual.getName());
            assertEquals("tag incorrect", tExpected.getTag(), tActual.getTag());
        }
        
    }
    
}
