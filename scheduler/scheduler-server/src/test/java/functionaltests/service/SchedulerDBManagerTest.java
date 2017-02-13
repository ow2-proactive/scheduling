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
package functionaltests.service;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.Page;
import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
import org.ow2.proactive.scheduler.common.TaskFilterCriteria;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.job.ChangedTasksInfo;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * Functional tests to verify tasks pagination and criteria searches
 *
 */
public class SchedulerDBManagerTest extends BaseServiceTest {

    private static final int nbJobs = 10;

    private static final int nbTasksPerJob = 20;

    private static final int totalNbTasks = nbJobs * nbTasksPerJob;

    private TaskFilterCriteria crit = null;

    private Page<TaskInfo> actualPageInfo = null;

    private Page<TaskState> actualPageState = null;

    private List<InternalJob> actualInternalJobs = null;

    private Page<JobInfo> actualJobPage = null;

    private List<Task> lAllTasks = null;

    private List<SortSpecifierContainer> sortParameters = null;

    @Test
    public void testGetTasks() throws Throwable {

        initExpectedResults("testGetTasks-Job", "TEST-TAG");

        // no given statuses, no pagination, nothing should come up
        crit = TaskFilterCriteria.TFCBuilder.newInstance().running(false).pending(false).finished(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskInfoPage(actualPageInfo, crit);

        // all statuses, no pagination, everything should come up
        crit = TaskFilterCriteria.TFCBuilder.newInstance().criterias();
        actualPageInfo = getTasks(crit);
        assertTaskInfoPage(actualPageInfo, crit);

        // all statuses, pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance().limit(5).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskInfoPage(actualPageInfo, crit);

        // pending tasks only, no pagination
        crit = TaskFilterCriteria.TFCBuilder.newInstance().running(false).finished(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskInfoPage(actualPageInfo, crit);

        // running tasks only, no pagination
        startJob(0);
        crit = TaskFilterCriteria.TFCBuilder.newInstance().pending(false).finished(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskInfoPage(actualPageInfo, crit);

        // running tasks only, pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance().limit(5).pending(false).finished(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskInfoPage(actualPageInfo, crit);

        // finished tasks only, no pagination
        terminateJob(1, 100L);
        crit = TaskFilterCriteria.TFCBuilder.newInstance().pending(false).running(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskInfoPage(actualPageInfo, crit);

        // finished tasks only, pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance().limit(5).pending(false).running(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskInfoPage(actualPageInfo, crit);

        // finished tasks only, dates [0,100], no pagination
        crit = TaskFilterCriteria.TFCBuilder.newInstance().from(0).to(100L).pending(false).running(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskInfoPage(actualPageInfo, crit);

        // finished tasks only, dates [0,100], pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance()
                                            .from(0)
                                            .to(100L)
                                            .offset(0)
                                            .limit(5)
                                            .pending(false)
                                            .running(false)
                                            .criterias();
        actualPageInfo = getTasks(crit);
        assertTaskInfoPage(actualPageInfo, crit);

        // default parameters, with tag "TEST-TAG"
        crit = TaskFilterCriteria.TFCBuilder.newInstance().tag("TEST-TAG").criterias();
        actualPageInfo = getTasks(crit);
        assertTaskInfoPage(actualPageInfo, crit);

        // default parameters, with tag "NON-EXISTENT-TAG"
        crit = TaskFilterCriteria.TFCBuilder.newInstance().tag("NON-EXISTENT-TAG").criterias();
        actualPageInfo = getTasks(crit);
        assertTaskInfoPage(actualPageInfo, crit);

    }

    @Test
    public void testGetTaskStates() throws Throwable {

        initExpectedResults("testGetTaskStates-Job", "TEST-TAG");

        // no given statuses, no pagination, nothing should come up
        crit = TaskFilterCriteria.TFCBuilder.newInstance().running(false).pending(false).finished(false).criterias();
        actualPageState = getTaskStates(crit);
        assertTaskStatePage(actualPageState, crit);

        // all statuses, no pagination, everything should come up
        crit = TaskFilterCriteria.TFCBuilder.newInstance().criterias();
        actualPageState = getTaskStates(crit);
        assertTaskStatePage(actualPageState, crit);

        // all statuses, pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance().limit(5).criterias();
        actualPageState = getTaskStates(crit);
        assertTaskStatePage(actualPageState, crit);

        // pending tasks only, no pagination
        crit = TaskFilterCriteria.TFCBuilder.newInstance().running(false).finished(false).criterias();
        actualPageState = getTaskStates(crit);
        assertTaskStatePage(actualPageState, crit);

        // running tasks only, no pagination
        startJob(0);
        crit = TaskFilterCriteria.TFCBuilder.newInstance().pending(false).finished(false).criterias();
        actualPageState = getTaskStates(crit);
        assertTaskStatePage(actualPageState, crit);

        // running tasks only, pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance().limit(5).pending(false).finished(false).criterias();
        actualPageState = getTaskStates(crit);
        assertTaskStatePage(actualPageState, crit);

        // finished tasks only, no pagination
        terminateJob(1, 100L);
        crit = TaskFilterCriteria.TFCBuilder.newInstance().pending(false).running(false).criterias();
        actualPageState = getTaskStates(crit);
        assertTaskStatePage(actualPageState, crit);

        // finished tasks only, pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance().limit(5).pending(false).running(false).criterias();
        actualPageState = getTaskStates(crit);
        assertTaskStatePage(actualPageState, crit);

        // finished tasks only, dates [0,100], no pagination
        crit = TaskFilterCriteria.TFCBuilder.newInstance().from(0).to(100L).pending(false).running(false).criterias();
        actualPageState = getTaskStates(crit);
        assertTaskStatePage(actualPageState, crit);

        // finished tasks only, dates [0,100], pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance()
                                            .from(0)
                                            .to(100L)
                                            .offset(0)
                                            .limit(5)
                                            .pending(false)
                                            .running(false)
                                            .criterias();
        actualPageState = getTaskStates(crit);
        assertTaskStatePage(actualPageState, crit);

        // default parameters, with tag "TEST-TAG"
        crit = TaskFilterCriteria.TFCBuilder.newInstance().tag("TEST-TAG").criterias();
        actualPageState = getTaskStates(crit);
        assertTaskStatePage(actualPageState, crit);

        // default parameters, with tag "NON-EXISTENT-TAG"
        crit = TaskFilterCriteria.TFCBuilder.newInstance().tag("NON-EXISTENT-TAG").criterias();
        actualPageState = getTaskStates(crit);
        assertTaskStatePage(actualPageState, crit);

        // all statuses, no pagination, everything should come up sorted by taskId
        sortParameters = new ArrayList<>();
        sortParameters.add(new SortSpecifierContainer("id.taskId,descending"));
        crit = TaskFilterCriteria.TFCBuilder.newInstance().sortParameters(sortParameters).criterias();
        actualPageState = getTaskStates(crit);
        assertTaskStatePage(actualPageState, crit);
    }

    @Test
    public void testGetTotalJobsCount() throws Throwable {
        initExpectedResults("testGetTotalJobsCount-Job", "TEST-TAG");

        // default parameters
        actualJobPage = dbManager.getJobs(0, 0, null, true, true, true, null);
        assertEquals("Incorrect jobs total number", nbJobs, actualJobPage.getSize());

        // no pagination, no user, no pending, no running, no finished
        actualJobPage = dbManager.getJobs(0, 0, null, false, false, false, null);
        assertEquals("Incorrect jobs total number", 0, actualJobPage.getSize());

        // no pagination, user = "admin", pending, running, finished
        actualJobPage = dbManager.getJobs(0, 0, "admin", true, true, true, null);
        assertEquals("Incorrect jobs total number", nbJobs, actualJobPage.getSize());

        // no pagination, user = "invalid_user", pending, no running, finished
        actualJobPage = dbManager.getJobs(0, 0, "invalid_user", true, true, true, null);
        assertEquals("Incorrect jobs total number", 0, actualJobPage.getSize());

        // pagination [0,5[, user = "admin", pending, running, finished
        actualJobPage = dbManager.getJobs(0, 5, "admin", true, true, true, null);
        assertEquals("Incorrect jobs total number", nbJobs, actualJobPage.getSize());
    }

    @Test
    public void testGetTotalTasksCount() throws Throwable {
        initExpectedResults("testGetTotalTasksCount-Job", "TEST-TAG");
        assertEquals("Total number of tasks is incorrect", totalNbTasks, lAllTasks.size());

        // no given statuses, no pagination, nothing should come up
        crit = TaskFilterCriteria.TFCBuilder.newInstance().running(false).pending(false).finished(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskPageSizes(actualPageInfo, 0, 0);

        // all statuses, no pagination, everything should come up
        crit = TaskFilterCriteria.TFCBuilder.newInstance().criterias();
        actualPageInfo = getTasks(crit);
        assertTaskPageSizes(actualPageInfo, totalNbTasks, totalNbTasks);

        // all statuses, pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance().limit(5).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskPageSizes(actualPageInfo, 5, totalNbTasks);

        // pending tasks only, no pagination
        crit = TaskFilterCriteria.TFCBuilder.newInstance().running(false).finished(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskPageSizes(actualPageInfo, totalNbTasks, totalNbTasks);

        // running tasks only, no pagination
        startJob(0);
        crit = TaskFilterCriteria.TFCBuilder.newInstance().pending(false).finished(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskPageSizes(actualPageInfo, nbTasksPerJob, nbTasksPerJob);

        // running tasks only, pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance().limit(5).pending(false).finished(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskPageSizes(actualPageInfo, 5, nbTasksPerJob);

        // finished tasks only, no pagination
        terminateJob(1, 100L);
        crit = TaskFilterCriteria.TFCBuilder.newInstance().pending(false).running(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskPageSizes(actualPageInfo, nbTasksPerJob, nbTasksPerJob);

        // finished tasks only, pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance().limit(5).pending(false).running(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskPageSizes(actualPageInfo, 5, nbTasksPerJob);

        // finished tasks only, dates [0,100], no pagination
        crit = TaskFilterCriteria.TFCBuilder.newInstance().from(0).to(100L).pending(false).running(false).criterias();
        actualPageInfo = getTasks(crit);
        assertTaskPageSizes(actualPageInfo, nbTasksPerJob, nbTasksPerJob);

        // finished tasks only, dates [0,100], pagination [0,5[
        crit = TaskFilterCriteria.TFCBuilder.newInstance()
                                            .from(0)
                                            .to(100L)
                                            .offset(0)
                                            .limit(5)
                                            .pending(false)
                                            .running(false)
                                            .criterias();
        actualPageInfo = getTasks(crit);
        assertTaskPageSizes(actualPageInfo, 5, nbTasksPerJob);

        // default parameters, with tag "TEST-TAG"
        crit = TaskFilterCriteria.TFCBuilder.newInstance().tag("TEST-TAG").criterias();
        actualPageInfo = getTasks(crit);
        assertTaskPageSizes(actualPageInfo, totalNbTasks, totalNbTasks);

        // default parameters, with tag "NON-EXISTENT-TAG"
        crit = TaskFilterCriteria.TFCBuilder.newInstance().tag("NON-EXISTENT-TAG").criterias();
        actualPageInfo = getTasks(crit);
        assertTaskPageSizes(actualPageInfo, 0, 0);
    }

    @Test
    public void testDatesTaskCentric() throws Throwable {

        int nbJobs = 5;
        int nbTasksPerJob = 10;
        int totalNbTasks = nbJobs * nbTasksPerJob;

        // These only should come up
        actualInternalJobs = createTestJobs("Job-hier", "TAG-HIER", nbJobs, nbTasksPerJob);
        for (InternalJob j : actualInternalJobs) {
            service.submitJob(j);
        }

        // These should NOT come up
        List<InternalJob> todayJobs = createTestJobs("Job-today", "TAG-TODAY", nbJobs, nbTasksPerJob);
        for (InternalJob j : todayJobs) {
            service.submitJob(j);
        }

        for (InternalJob j : actualInternalJobs) {
            j.start();
            j.setStartTime(100L);
            for (InternalTask t : j.getITasks()) {
                t.setStartTime(100L);
                dbManager.jobTaskStarted(j, t, true);
            }
        }

        for (int i = 0; i < todayJobs.size(); i++) {
            terminateJob(i, 500L);
        }

        crit = TaskFilterCriteria.TFCBuilder.newInstance().from(100L).criterias();
        actualPageState = getTaskStates(crit);

        for (TaskState ts : actualPageState.getList()) {
            String taskStr = "Results from criteria:" + ts.getName() + "," + ts.getTag() + "," +
                             String.valueOf(ts.getStartTime()) + "," + String.valueOf(ts.getFinishedTime()) + "," +
                             ts.getStatus();
            System.out.println(taskStr);
        }
        assertEquals("Incorrect tasks list size due to coupled dates from/to",
                     totalNbTasks,
                     actualPageState.getList().size());
    }

    @Test
    public void testUpdateJobAndTasksState() throws Exception {
        InternalJob job = createTestJob("test", "tag", 1);

        service.submitJob(job);

        job.setStatus(JobStatus.KILLED);
        job.setNumberOfFailedTasks(2);
        job.setNumberOfFaultyTasks(3);
        job.setNumberOfInErrorTasks(5);
        job.setInErrorTime(7);

        InternalTask internalTask = job.getITasks().get(0);
        internalTask.setStatus(TaskStatus.IN_ERROR);
        internalTask.setInErrorTime(11);

        dbManager.updateJobAndTasksState(job);

        Page<JobInfo> jobs = dbManager.getJobs(0, 10, null, true, true, true, null);

        assertThat(jobs.getSize()).isEqualTo(1);

        JobInfo jobInfo = jobs.getList().get(0);

        assertThat(jobInfo.getStatus()).isEqualTo(JobStatus.KILLED);
        assertThat(jobInfo.getNumberOfFailedTasks()).isEqualTo(2);
        assertThat(jobInfo.getNumberOfFaultyTasks()).isEqualTo(3);
        assertThat(jobInfo.getNumberOfInErrorTasks()).isEqualTo(5);
        assertThat(jobInfo.getInErrorTime()).isEqualTo(7);

        Page<TaskState> tasks = dbManager.getTaskStates(0,
                                                        10,
                                                        null,
                                                        0,
                                                        10,
                                                        null,
                                                        true,
                                                        true,
                                                        true,
                                                        new SortSpecifierContainer());

        assertThat(tasks.getSize()).isEqualTo(1);

        TaskState taskState = tasks.getList().get(0);

        assertThat(taskState.getStatus()).isEqualTo(TaskStatus.IN_ERROR);
        assertThat(taskState.getTaskInfo().getInErrorTime()).isEqualTo(11);
    }

    @Test
    public void testUpdateTaskState() throws Exception {
        InternalJob job = createTestJob("test", "tag", 1);

        service.submitJob(job);

        InternalTask internalTask = job.getITasks().get(0);
        internalTask.setStatus(TaskStatus.ABORTED);

        dbManager.updateTaskState(internalTask);

        Page<TaskState> tasks = dbManager.getTaskStates(0,
                                                        10,
                                                        null,
                                                        0,
                                                        10,
                                                        null,
                                                        true,
                                                        true,
                                                        true,
                                                        new SortSpecifierContainer());

        assertThat(tasks.getSize()).isEqualTo(1);

        TaskState taskState = tasks.getList().get(0);

        assertThat(taskState.getStatus()).isEqualTo(TaskStatus.ABORTED);
    }

    private void initExpectedResults(String jobName, String tag) throws Throwable {
        actualInternalJobs = createTestJobs(jobName, tag, nbJobs, nbTasksPerJob);
        lAllTasks = new ArrayList<Task>(totalNbTasks);
        for (InternalJob j : actualInternalJobs) {
            service.submitJob(j);
            lAllTasks.addAll(j.getTasks());
        }
    }

    private void startJob(int jobIndex) throws Throwable {
        InternalJob startedJob = actualInternalJobs.get(jobIndex);
        startedJob.start();
        for (InternalTask task : startedJob.getITasks()) {
            task.setStatus(TaskStatus.RUNNING);
        }
        dbManager.updateJobAndTasksState(startedJob);
    }

    private void terminateJob(int jobIndex, long finishedTime) throws Throwable {
        InternalJob finishedJob = actualInternalJobs.get(jobIndex);
        finishedJob.start();
        finishedJob.terminate();
        for (InternalTask task : finishedJob.getITasks()) {
            TaskResultImpl result = new TaskResultImpl(task.getId(), "ok", null, 0);
            FlowAction action = new FlowAction(FlowActionType.CONTINUE);
            ChangedTasksInfo changesInfo = finishedJob.terminateTask(false, task.getId(), null, action, result);
            task.setFinishedTime(finishedTime);
            dbManager.updateAfterWorkflowTaskFinished(finishedJob, changesInfo, result);
        }
    }

    private List<InternalJob> createTestJobs(String jobName, String taskTag, int nbJobs, int nbTasks) throws Exception {

        List<InternalJob> lJobs = new ArrayList<InternalJob>(nbJobs);

        for (int i = 1; i <= nbJobs; i++) {
            lJobs.add(createTestJob(jobName + "-" + i, taskTag, nbTasks));
        }

        return lJobs;
    }

    private InternalJob createTestJob(String jobName, String taskTag, int nbTasks) throws Exception {

        TaskFlowJob job = new TaskFlowJob();
        job.setName(jobName);

        for (int i = 1; i <= nbTasks; i++) {
            JavaTask task = new JavaTask();
            task.setName(jobName + "-TASK-" + i + "/" + nbTasks);
            task.setExecutableClassName("class");
            task.setTag(taskTag);
            job.addTask(task);
        }

        return createJob(job);
    }

    private Page<TaskInfo> getTasks(TaskFilterCriteria criterias) {
        return dbManager.getTasks(criterias.getFrom(),
                                  criterias.getTo(),
                                  criterias.getTag(),
                                  criterias.getOffset(),
                                  criterias.getLimit(),
                                  criterias.getUser(),
                                  criterias.isPending(),
                                  criterias.isRunning(),
                                  criterias.isFinished());
    }

    private Page<TaskState> getTaskStates(TaskFilterCriteria criterias) {
        return dbManager.getTaskStates(criterias.getFrom(),
                                       criterias.getTo(),
                                       criterias.getTag(),
                                       criterias.getOffset(),
                                       criterias.getLimit(),
                                       criterias.getUser(),
                                       criterias.isPending(),
                                       criterias.isRunning(),
                                       criterias.isFinished(),
                                       SortSpecifierContainer.EMPTY_CONTAINER);
    }

    private void assertTaskPageSizes(Page<?> page, int nbTasksInPage, int totalNbTasks) {
        assertEquals("Returned number of tasks is incorrect", nbTasksInPage, page.getList().size());
        assertEquals("Total number of tasks is incorrect", totalNbTasks, page.getSize());
    }

    private void assertTaskInfoPage(Page<TaskInfo> page, TaskFilterCriteria criterias) {
        List<TaskInfo> taskInfos = page.getList();
        for (TaskInfo taskInfo : taskInfos) {

            String taskStr = taskInfo.getName() + "," + taskInfo.getTaskId().getTag() + "," +
                             String.valueOf(taskInfo.getFinishedTime()) + "," + taskInfo.getStatus();
            System.out.println(taskStr);

            String tag = criterias.getTag();

            // tag
            if (tag != null && "".compareTo(tag) != 0)
                assertEquals("Tag is incorrect for task " + taskStr, tag, taskInfo.getTaskId().getTag());

            // from
            long from = criterias.getFrom();
            if (from != 0)
                assertEquals("startTime is incorrect", from, taskInfo.getStartTime());

            // to
            long to = criterias.getTo();
            if (to != 0)
                assertEquals("finishedTime is incorrect", to, taskInfo.getFinishedTime());

            // pagination
            int pageSize = criterias.getLimit() - criterias.getOffset();
            if (pageSize > 0) {
                assertTrue("Page size is incorrect", pageSize >= taskInfos.size());
            }

            // user
            String user = criterias.getUser();
            if (user != null && "".compareTo(user) != 0) {
                assertEquals("user is incorrect", user, taskInfo.getJobInfo().getJobOwner());
            }

            // status
            // If the returned status of this task is one of those
            // the corresponding criteria should be true
            switch (taskInfo.getStatus()) {
                case SUBMITTED:
                case PENDING:
                case NOT_STARTED:
                    assertTrue("Task status is incorrect", criterias.isPending());
                    break;
                case PAUSED:
                case RUNNING:
                case WAITING_ON_ERROR:
                case WAITING_ON_FAILURE:
                    assertTrue("Task status is incorrect", criterias.isRunning());
                    break;
                case FAILED:
                case NOT_RESTARTED:
                case ABORTED:
                case FAULTY:
                case FINISHED:
                case SKIPPED:
                    assertTrue("Task status is incorrect", criterias.isFinished());
                    break;
                default:
                    fail("Incoherent task status");
            }
        }
    }

    private void assertTaskStatePage(Page<TaskState> page, TaskFilterCriteria criterias) {
        int nbTaskStates = page.getList().size();
        for (TaskState taskState : page.getList()) {

            String tag = criterias.getTag();
            if (tag != null)
                assertEquals("Tag is incorrect", tag, taskState.getTag());

            // from
            long from = criterias.getFrom();
            if (from != 0)
                assertEquals("startTime is incorrect", from, taskState.getStartTime());

            // to
            long to = criterias.getTo();
            if (to != 0)
                assertEquals("finishedTime is incorrect", to, taskState.getFinishedTime());

            // pagination
            int pageSize = criterias.getLimit() - criterias.getOffset();
            if (pageSize > 0) {
                assertTrue("Page size is incorrect", pageSize >= nbTaskStates);
            }

            // user
            String expectedUser = criterias.getUser();
            String actualUser = taskState.getTaskInfo().getJobInfo().getJobOwner();
            if (expectedUser != null && "".compareTo(expectedUser) != 0) {
                assertEquals("user is incorrect", expectedUser, actualUser);
            }

            // status
            // If the returned status of this task is one of those
            // the corresponding criteria should be true
            switch (taskState.getStatus()) {
                case SUBMITTED:
                case PENDING:
                case NOT_STARTED:
                    assertTrue("Task status is incorrect", criterias.isPending());
                    break;
                case PAUSED:
                case RUNNING:
                case WAITING_ON_ERROR:
                case WAITING_ON_FAILURE:
                    assertTrue("Task status is incorrect", criterias.isRunning());
                    break;
                case FAILED:
                case NOT_RESTARTED:
                case ABORTED:
                case FAULTY:
                case FINISHED:
                case SKIPPED:
                    assertTrue("Task status is incorrect", criterias.isFinished());
                    break;
                default:
                    fail("Incoherent task status");
            }
        }
    }

}
