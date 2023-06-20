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
package functionaltests.db.schedulerdb;

import static org.ow2.proactive.scheduler.common.SchedulerConstants.SUBMISSION_MODE_REST_API;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.db.SortOrder;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.JobSortParameter;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestLoadJobsPagination extends BaseSchedulerDBTest {

    private TaskFlowJob createJob() throws Exception {
        return createJob(null, null, null, null, null, null);
    }

    private TaskFlowJob createJob(String name, JobPriority priority, String projectName, String bucketName,
            String label, Long parentId) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        if (name != null) {
            job.setName(name);
        } else {
            job.setName(this.getClass().getSimpleName());
        }
        if (projectName != null) {
            job.setProjectName(projectName);
        } else {
            job.setProjectName(this.getClass().getSimpleName() + " project");
        }
        job.setBucketName(bucketName);
        job.setLabel(label);
        if (priority != null) {
            job.setPriority(priority);
        } else {
            job.setPriority(JobPriority.NORMAL);
        }

        job.setParentId(parentId);
        job.setDescription("TestLoadJobsPagination desc");
        JavaTask task = new JavaTask();
        task.setExecutableClassName("className");
        job.addTask(task);
        return job;
    }

    private TaskFlowJob createJob(String name, JobPriority priority) throws Exception {
        return createJob(name, priority, null, null, null, null);
    }

    @Test
    public void testSorting() throws Exception {
        InternalJob job1 = defaultSubmitJob(createJob("A", JobPriority.IDLE), "user_a"); // 1
        defaultSubmitJob(createJob("B", JobPriority.LOWEST), "user_b"); // 2
        InternalJob job3 = defaultSubmitJob(createJob("C", JobPriority.LOW), "user_c"); // 3
        defaultSubmitJob(createJob("A", JobPriority.NORMAL), "user_d"); // 4
        InternalJob job5 = defaultSubmitJob(createJob("B", JobPriority.HIGH), "user_e"); // 5
        defaultSubmitJob(createJob("C", JobPriority.HIGHEST), "user_f"); // 6

        // change status for some jobs
        job1.failed(null, JobStatus.KILLED);
        dbManager.updateAfterJobKilled(job1, Collections.<TaskId> emptySet());

        job3.setPaused();
        dbManager.updateJobAndTasksState(job3);

        job5.start();
        InternalTask taskJob5 = startTask(job5, job5.getITasks().get(0));
        dbManager.jobTaskStarted(job5, taskJob5, true, null);

        List<JobInfo> jobs;

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters(new SortParameter<>(JobSortParameter.ID, SortOrder.ASC)))
                        .getList();
        checkJobs(jobs, 1, 2, 3, 4, 5, 6);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters(new SortParameter<>(JobSortParameter.ID, SortOrder.DESC)))
                        .getList();
        checkJobs(jobs, 6, 5, 4, 3, 2, 1);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 false,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters(new SortParameter<>(JobSortParameter.NAME, SortOrder.ASC),
                                                new SortParameter<>(JobSortParameter.ID, SortOrder.ASC)))
                        .getList();
        checkJobs(jobs, 1, 4, 2, 5, 3, 6);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters(new SortParameter<>(JobSortParameter.NAME, SortOrder.ASC),
                                                new SortParameter<>(JobSortParameter.ID, SortOrder.DESC)))
                        .getList();
        checkJobs(jobs, 4, 1, 5, 2, 6, 3);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters(new SortParameter<>(JobSortParameter.OWNER, SortOrder.ASC)))
                        .getList();
        checkJobs(jobs, 1, 2, 3, 4, 5, 6);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters(new SortParameter<>(JobSortParameter.OWNER, SortOrder.DESC)))
                        .getList();
        checkJobs(jobs, 6, 5, 4, 3, 2, 1);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters(new SortParameter<>(JobSortParameter.PRIORITY, SortOrder.ASC)))
                        .getList();
        checkJobs(jobs, 1, 2, 3, 4, 5, 6);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters(new SortParameter<>(JobSortParameter.PRIORITY, SortOrder.DESC)))
                        .getList();
        checkJobs(jobs, 6, 5, 4, 3, 2, 1);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters(new SortParameter<>(JobSortParameter.STATE, SortOrder.DESC),
                                                new SortParameter<>(JobSortParameter.ID, SortOrder.ASC)))
                        .getList();
        checkJobs(jobs, 2, 4, 6, 3, 5, 1);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters(new SortParameter<>(JobSortParameter.STATE, SortOrder.ASC),
                                                new SortParameter<>(JobSortParameter.ID, SortOrder.ASC)))
                        .getList();
        checkJobs(jobs, 1, 3, 5, 2, 4, 6);
    }

    @Test
    public void testPagingAndFiltering() throws Exception {
        InternalJob job;
        InternalTask task;

        String projectName = "project1";
        String bucketName = "bucket1";
        String jobName = "job1";
        String label = "label";

        // pending job with projectName - 1
        defaultSubmitJob(createJob(null, null, projectName, bucketName, label, null));

        // job for user1 with jobName - 2
        defaultSubmitJob(createJob(jobName, null, null, null, null, 1L), "user1");

        // running job - 3
        job = defaultSubmitJob(createJob(null, null, projectName, bucketName, label, 1L));
        job.start();
        task = startTask(job, job.getITasks().get(0));
        dbManager.jobTaskStarted(job, task, true, null);

        // killed job - 4
        job = defaultSubmitJob(createJob());
        job.failed(null, JobStatus.KILLED);
        dbManager.updateAfterJobKilled(job, Collections.<TaskId> emptySet());

        // job for user2 - 5
        defaultSubmitJob(createJob(), "user2");

        // finished job - 6
        job = defaultSubmitJob(createJob());
        job.start();
        task = startTask(job, job.getITasks().get(0));
        dbManager.jobTaskStarted(job, task, true, null);
        TaskResultImpl result = new TaskResultImpl(null, new TestResult(0, "result"), null, 0);
        job.terminateTask(InternalJob.FinishTaskStatus.NORMAL, task.getId(), null, null, result);
        job.terminate();
        dbManager.updateAfterTaskFinished(job, task, new TaskResultImpl(null, new TestResult(0, "result"), null, 0));

        // canceled job - 7
        job = defaultSubmitJob(createJob());
        job.failed(job.getITasks().get(0).getId(), JobStatus.CANCELED);
        dbManager.updateAfterJobKilled(job, Collections.<TaskId> emptySet());

        // job marked as removed, method 'getJobs' shouldn't return it
        job = defaultSubmitJob(createJob());
        dbManager.removeJob(job.getId(), System.currentTimeMillis(), true);

        List<JobInfo> jobs;

        List<SortParameter<JobSortParameter>> sortParameters = new ArrayList<>();
        sortParameters.add(new SortParameter<>(JobSortParameter.ID, SortOrder.ASC));

        jobs = dbManager.getJobs(5,
                                 1,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        JobInfo jobInfo = jobs.get(0);
        Assert.assertEquals("6", jobInfo.getJobId().value());
        Assert.assertEquals(JobStatus.FINISHED, jobInfo.getStatus());
        Assert.assertEquals("TestLoadJobsPagination", jobInfo.getJobId().getReadableName());
        Assert.assertEquals(1, jobInfo.getTotalNumberOfTasks());
        Assert.assertEquals(1, jobInfo.getNumberOfFinishedTasks());
        Assert.assertEquals(0, jobInfo.getNumberOfRunningTasks());
        Assert.assertEquals(0, jobInfo.getNumberOfPendingTasks());
        Assert.assertEquals(JobPriority.NORMAL, jobInfo.getPriority());
        Assert.assertEquals(DEFAULT_USER_NAME, jobInfo.getJobOwner());
        Assert.assertEquals(SUBMISSION_MODE_REST_API, jobInfo.getSubmissionMode());

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 1, 2, 3, 4, 5, 6, 7);
        Assert.assertEquals(2, jobs.get(0).getChildrenCount());

        jobs = dbManager.getJobs(-1,
                                 -1,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 1, 2, 3, 4, 5, 6, 7);

        jobs = dbManager.getJobs(-1,
                                 5,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 1, 2, 3, 4, 5);

        jobs = dbManager.getJobs(2,
                                 -1,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 3, 4, 5, 6, 7);

        jobs = dbManager.getJobs(0,
                                 0,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 1, 2, 3, 4, 5, 6, 7);

        jobs = dbManager.getJobs(0,
                                 1,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 1);

        jobs = dbManager.getJobs(0,
                                 3,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 1, 2, 3);

        jobs = dbManager.getJobs(1,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 2, 3, 4, 5, 6, 7);

        jobs = dbManager.getJobs(5,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 6, 7);

        jobs = dbManager.getJobs(6,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 7);

        jobs = dbManager.getJobs(7,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs);

        jobs = dbManager.getJobs(0,
                                 10,
                                 DEFAULT_USER_NAME,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 1, 3, 4, 6, 7);

        jobs = dbManager.getJobs(0,
                                 10,
                                 "user1",
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 2);

        jobs = dbManager.getJobs(0,
                                 10,
                                 DEFAULT_USER_NAME,
                                 null,
                                 false,
                                 true,
                                 false,
                                 false,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 1);

        jobs = dbManager.getJobs(0,
                                 10,
                                 DEFAULT_USER_NAME,
                                 null,
                                 false,
                                 false,
                                 true,
                                 false,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 3);

        jobs = dbManager.getJobs(0,
                                 10,
                                 DEFAULT_USER_NAME,
                                 null,
                                 false,
                                 false,
                                 false,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 4, 6, 7);

        jobs = dbManager.getJobs(0,
                                 10,
                                 DEFAULT_USER_NAME,
                                 null,
                                 false,
                                 false,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 3, 4, 6, 7);

        jobs = dbManager.getJobs(0,
                                 10,
                                 DEFAULT_USER_NAME,
                                 null,
                                 false,
                                 true,
                                 false,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 1, 4, 6, 7);

        jobs = dbManager.getJobs(0,
                                 10,
                                 DEFAULT_USER_NAME,
                                 null,
                                 false,
                                 true,
                                 true,
                                 false,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 1, 3);

        jobs = dbManager.getJobs(0,
                                 10,
                                 DEFAULT_USER_NAME,
                                 null,
                                 false,
                                 false,
                                 false,
                                 false,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 jobName,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 2);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 projectName,
                                 bucketName,
                                 null,
                                 null,
                                 label,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 1, 3);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 null,
                                 null,
                                 1L,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 2, 3);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 jobName,
                                 null,
                                 null,
                                 1L,
                                 null,
                                 null,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 2);

        jobs = dbManager.getJobs(0,
                                 10,
                                 null,
                                 null,
                                 false,
                                 true,
                                 true,
                                 true,
                                 false,
                                 true,
                                 null,
                                 projectName,
                                 bucketName,
                                 1L,
                                 SUBMISSION_MODE_REST_API,
                                 label,
                                 sortParameters)
                        .getList();
        checkJobs(jobs, 3);
    }

    private List<SortParameter<JobSortParameter>> sortParameters(SortParameter<JobSortParameter>... params) {
        return Arrays.asList(params);
    }

    private void checkJobs(List<JobInfo> jobs, Integer... expectedIds) {
        List<Integer> ids = new ArrayList<>();
        for (JobInfo job : jobs) {
            ids.add(Integer.valueOf(job.getJobId().value()));
        }
        Assert.assertEquals(Arrays.asList(expectedIds), ids);
    }
}
