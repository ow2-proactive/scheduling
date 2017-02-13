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
package org.ow2.proactive.scheduler.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class DefaultPolicyTest {

    private int jobId;

    @Test
    public void empty_list_of_tasks() throws Exception {
        LinkedList<EligibleTaskDescriptor> orderedTasks = new DefaultPolicy().getOrderedTasks(Collections.<JobDescriptor> emptyList());

        assertTrue(orderedTasks.isEmpty());
    }

    @Test
    public void single_job() throws Exception {
        JobDescriptorImpl job = createSingleTaskJob();
        List<JobDescriptor> jobs = submitJobs(job);

        LinkedList<EligibleTaskDescriptor> orderedTasks = new DefaultPolicy().getOrderedTasks(jobs);

        assertEquals(1, orderedTasks.size());
    }

    @Test
    public void job_with_different_priorities() throws Exception {
        JobDescriptorImpl jobHigh = createSingleTaskJob(JobPriority.HIGH);
        JobDescriptorImpl jobLow = createSingleTaskJob(JobPriority.LOW);
        JobDescriptorImpl jobNormal = createSingleTaskJob(JobPriority.NORMAL);

        List<JobDescriptor> jobs = submitJobs(jobHigh, jobLow, jobNormal);

        LinkedList<EligibleTaskDescriptor> orderedTasks = new DefaultPolicy().getOrderedTasks(jobs);

        assertEquals(jobHigh.getJobId(), orderedTasks.get(0).getJobId());
        assertEquals(jobNormal.getJobId(), orderedTasks.get(1).getJobId());
        assertEquals(jobLow.getJobId(), orderedTasks.get(2).getJobId());
    }

    @Test
    public void job_with_same_priorities() throws Exception {
        JobDescriptorImpl job1 = createSingleTaskJob();
        JobDescriptorImpl job2 = createSingleTaskJob();
        JobDescriptorImpl job3 = createSingleTaskJob();

        List<JobDescriptor> jobs = submitJobs(job1, job3, job2);

        LinkedList<EligibleTaskDescriptor> orderedTasks = new DefaultPolicy().getOrderedTasks(jobs);

        assertEquals(job1.getJobId(), orderedTasks.get(0).getJobId());
        assertEquals(job2.getJobId(), orderedTasks.get(1).getJobId());
        assertEquals(job3.getJobId(), orderedTasks.get(2).getJobId());
    }

    private JobDescriptorImpl createSingleTaskJob(JobPriority jobPriority) {
        InternalTaskFlowJob taskFlowJob = new InternalTaskFlowJob("test", jobPriority, OnTaskError.CANCEL_JOB, "");
        taskFlowJob.setId(JobIdImpl.makeJobId(Integer.toString(jobId++)));
        ArrayList<InternalTask> tasks = new ArrayList<>();
        tasks.add(new InternalScriptTask(taskFlowJob));
        taskFlowJob.addTasks(tasks);
        return new JobDescriptorImpl(taskFlowJob);
    }

    private List<JobDescriptor> submitJobs(JobDescriptorImpl... jobs) {
        List<JobDescriptor> submittedJobs = new ArrayList<>();
        Collections.addAll(submittedJobs, jobs);
        return submittedJobs;
    }

    private JobDescriptorImpl createSingleTaskJob() {
        return createSingleTaskJob(JobPriority.NORMAL);
    }
}
