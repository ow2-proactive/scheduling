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
package org.ow2.proactive.scheduler.core.db;

import static com.google.common.truth.Truth.assertThat;

import java.util.Vector;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.core.SchedulerStateImpl;
import org.ow2.proactive.scheduler.job.ClientJobState;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

import com.google.common.collect.ImmutableList;


public class RecoveredSchedulerStateTest {

    @Test
    public void testConstructor() throws Exception {
        int nbJobs = 2;

        Vector<InternalJob> pendingJobs = createJobs(JobStatus.PENDING, nbJobs);
        Vector<InternalJob> runningJobs = createJobs(JobStatus.RUNNING, nbJobs);
        Vector<InternalJob> finishedJobs = createJobs(JobStatus.FINISHED, nbJobs);

        RecoveredSchedulerState recoveredState = new RecoveredSchedulerState(pendingJobs, runningJobs, finishedJobs);

        assertThat(recoveredState.getPendingJobs()).containsExactlyElementsIn(pendingJobs);
        assertThat(recoveredState.getRunningJobs()).containsExactlyElementsIn(runningJobs);
        assertThat(recoveredState.getFinishedJobs()).containsExactlyElementsIn(finishedJobs);

        SchedulerStateImpl schedulerState = recoveredState.getSchedulerState();
        assertThat(schedulerState).isNotNull();

        assertThat(schedulerState.getPendingJobs()).hasSize(nbJobs);
        assertThat(schedulerState.getRunningJobs()).hasSize(nbJobs);
        assertThat(schedulerState.getFinishedJobs()).hasSize(nbJobs);

        assertThat(schedulerState.getPendingJobs().get(0)).isInstanceOf(ClientJobState.class);
        assertThat(schedulerState.getRunningJobs().get(0)).isInstanceOf(ClientJobState.class);
        assertThat(schedulerState.getFinishedJobs().get(0)).isInstanceOf(ClientJobState.class);
    }

    public Vector<InternalJob> createJobs(JobStatus jobStatus, int count) {
        Vector<InternalJob> result = new Vector<>(count);

        for (int i = 0; i < count; i++) {
            result.add(createJob(jobStatus));
        }

        return result;
    }

    public InternalJob createJob(JobStatus jobStatus) {
        InternalTaskFlowJob job = new InternalTaskFlowJob("MyJob",
                                                          JobPriority.HIGH,
                                                          OnTaskError.CANCEL_JOB,
                                                          "Description");

        InternalScriptTask internalScriptTask = new InternalScriptTask(job);

        job.addTasks(ImmutableList.<InternalTask> of(internalScriptTask));

        JobInfoImpl jobInfo = (JobInfoImpl) job.getJobInfo();
        jobInfo.setStatus(jobStatus);

        return job;
    }

}
