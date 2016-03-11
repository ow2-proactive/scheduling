package org.ow2.proactive_grid_cloud_portal.scheduler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scheduler.common.Page;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatesPage;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.RestPage;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;


/**
 * Set of utilitary functions for testing purpose
 *
 */
public class RestTestUtils {

    /**
     * ################################################################################
     * 
     *    Mocked entities
     * 
     * ################################################################################
     */

    protected static Page<JobInfo> newMockedJobInfoPage(String jobStr, String taskTag, int nbJobs,
            int nbTasks, int nbTotalJobs) {

        List<JobInfo> lJobInfo = new ArrayList<JobInfo>(nbJobs);

        for (int jobIter = 0; jobIter < nbJobs; jobIter++) {
            lJobInfo.add(RestTestUtils.newMockedJob(String.valueOf(jobIter), taskTag, nbTasks).getJobInfo());
        }

        return new Page<JobInfo>(lJobInfo, nbTotalJobs);
    }

    protected static JobState newMockedJob(final String jobIdStr, final String tag, final int nbTasks) {
        JobState mockedJob = mock(JobState.class);
        JobId mockedJobId = mock(JobId.class);
        JobInfo mockedJobInfo = mock(JobInfo.class);

        List<TaskState> dumbList = new ArrayList<TaskState>(nbTasks);
        for (int i = 0; i < nbTasks; i++) {
            dumbList.add(newTaskState(jobIdStr, null, i, nbTasks));
        }

        when(mockedJobId.value()).thenReturn(jobIdStr);
        when(mockedJobInfo.getJobId()).thenReturn(mockedJobId);
        when(mockedJobInfo.getStatus()).thenReturn(JobStatus.PENDING);
        when(mockedJob.getId()).thenReturn(mockedJobId);
        when(mockedJob.getTasksPaginated(0, 50)).thenReturn(new TaskStatesPage(dumbList, nbTasks));
        when(mockedJob.getTaskByTagPaginated("", 0, 50)).thenReturn(new TaskStatesPage(dumbList, nbTasks));
        when(mockedJob.getJobInfo()).thenReturn(mockedJobInfo);
        return mockedJob;
    }

    protected static Page<TaskId> newMockedTaskIdPage(String jobIdStr, int nbTasks, String tag) {
        List<TaskId> lTaskId = new ArrayList<TaskId>(nbTasks);
        for (int i = 0; i < nbTasks; i++) {
            TaskId mockedTaskId = mock(TaskId.class);
            when(mockedTaskId.getReadableName()).thenReturn(generateReadableName(jobIdStr, i, nbTasks));
            when(mockedTaskId.getTag()).thenReturn(tag);
            lTaskId.add(mockedTaskId);
        }
        return new Page<TaskId>(lTaskId, nbTasks);
    }

    protected static Page<TaskState> newMockedTaskStatePage(String jobIdStr, String tag, int nbTasks,
            int totalNumberOfTasks) {
        List<TaskState> lTasks = new ArrayList<TaskState>(nbTasks);
        for (int i = 0; i < nbTasks; i++) {
            lTasks.add(newTaskState(jobIdStr, tag, i, nbTasks));
        }
        return new Page<TaskState>(lTasks, totalNumberOfTasks);
    }

    protected static Page<TaskState> newMockedTaskStatePage(ArrayList<String> jobIds, String tag, int nbTasks,
            int totalNumberOfTasks) {
        List<TaskState> lTasks = new ArrayList<TaskState>(nbTasks);
        for (int i = 0; i < nbTasks; i++) {
            lTasks.add(newTaskState(jobIds.get(i), tag, i, nbTasks));
        }
        return new Page<TaskState>(lTasks, totalNumberOfTasks);
    }

    protected static TaskState newTaskState(String jobIdStr, String tag, long cnt, int nbTasks) {
        TaskId mockedTaskId = mock(TaskId.class);
        TaskState mockedState = mock(TaskState.class);
        when(mockedTaskId.getReadableName()).thenReturn(generateReadableName(jobIdStr, cnt, nbTasks));
        when(mockedTaskId.longValue()).thenReturn(cnt);
        when(mockedState.getTag()).thenReturn(tag);
        when(mockedState.getId()).thenReturn(mockedTaskId);
        when(mockedState.getName()).thenReturn(generateReadableName(jobIdStr, cnt, nbTasks));
        return mockedState;
    }

    protected static TaskStateData newMockedTaskStateData(String jobIdStr, String tag, long cnt, int nbTasks) {
        TaskStateData mockedTaskStateData = mock(TaskStateData.class);
        TaskInfoData mockedTaskInfoData = mock(TaskInfoData.class);
        TaskIdData mockedTaskIdData = mock(TaskIdData.class);
        when(mockedTaskIdData.getReadableName()).thenReturn(generateReadableName(jobIdStr, cnt, nbTasks));
        when(mockedTaskIdData.getId()).thenReturn(cnt);
        when(mockedTaskInfoData.getTaskId()).thenReturn(mockedTaskIdData);
        when(mockedTaskStateData.getTaskInfo()).thenReturn(mockedTaskInfoData);
        when(mockedTaskStateData.getTag()).thenReturn(tag);
        return mockedTaskStateData;
    }

    /**
     * ################################################################################
     * 
     *    Assertions
     * 
     * ################################################################################
     */

    protected static void assertTasks(final int nbTasks, String jobIdStr, RestPage<String> page) {
        assertEquals("Number of tasks is incorrect", nbTasks, page.getSize());

        for (int i = 0; i < nbTasks; i++) {
            assertEquals("Task readable name is incorrect", generateReadableName(jobIdStr, i, nbTasks),
                    page.getList().get(i));
        }
    }

    protected static void assertTaskStates(Page<TaskState> expected, RestPage<TaskStateData> actual) {
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

    protected static void assertJobInfoPage(Page<JobInfo> expected, RestPage<String> actual) {

        assertEquals("Returned number of jobs is incorrect", expected.getList().size(),
                actual.getList().size());

        assertEquals("Total number of jobs is incorrect", expected.getSize(), actual.getSize());
        for (int i = 0; i < expected.getList().size(); i++) {
            assertJobInfo(expected.getList().get(i), actual.getList().get(i));
        }
    }

    protected static void assertJobsInfoPage(Page<JobInfo> expected, RestPage<UserJobData> actual) {

        assertEquals("Returned number of jobs is incorrect", expected.getList().size(),
                actual.getList().size());

        assertEquals("Total number of jobs is incorrect", expected.getSize(), actual.getSize());
        for (int i = 0; i < expected.getList().size(); i++) {
            assertJobInfo(expected.getList().get(i), actual.getList().get(i));
        }
    }

    protected static void assertJobInfo(JobInfo expectedJobInfo, String actualIdValue) {
        assertEquals("JobId value incorrect", expectedJobInfo.getJobId().value(), actualIdValue);
    }

    protected static void assertJobInfo(JobInfo expectedJobInfo, UserJobData actualJobData) {
        assertEquals("JobId value incorrect", expectedJobInfo.getJobId().value(), actualJobData.getJobid());
    }

    protected static void assertJobInfo(JobInfo expectedJobInfo, JobInfoData actualJobInfoData) {
        assertEquals("JobId value incorrect", expectedJobInfo.getJobId().value(),
                String.valueOf(actualJobInfoData.getJobId().getId()));
    }

    /**
     * ################################################################################
     * 
     *    Misc
     * 
     * ################################################################################
     */

    protected static String generateReadableName(final String jobIdStr, final long i, final int nbTasks) {
        return "JOB-" + jobIdStr + "-TASK-" + (i + 1) + "/" + nbTasks;
    }

}
