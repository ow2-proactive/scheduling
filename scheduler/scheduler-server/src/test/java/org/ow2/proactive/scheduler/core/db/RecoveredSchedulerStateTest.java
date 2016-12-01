package org.ow2.proactive.scheduler.core.db;

import com.google.common.collect.ImmutableList;
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

import java.util.Vector;

import static com.google.common.truth.Truth.assertThat;

public class RecoveredSchedulerStateTest {

    @Test
    public void testConstructor() throws Exception {
        int nbJobs = 2;

        Vector<InternalJob> pendingJobs = createJobs(JobStatus.PENDING, nbJobs);
        Vector<InternalJob> runningJobs = createJobs(JobStatus.RUNNING, nbJobs);
        Vector<InternalJob> finishedJobs = createJobs(JobStatus.FINISHED, nbJobs);

        RecoveredSchedulerState recoveredState =
                new RecoveredSchedulerState(
                        pendingJobs, runningJobs, finishedJobs);

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
        InternalTaskFlowJob job =
                new InternalTaskFlowJob("MyJob", JobPriority.HIGH, OnTaskError.CANCEL_JOB, "Description");

        InternalScriptTask internalScriptTask = new InternalScriptTask(job);

        job.addTasks(ImmutableList.<InternalTask>of(internalScriptTask));

        JobInfoImpl jobInfo = (JobInfoImpl) job.getJobInfo();
        jobInfo.setStatus(jobStatus);

        return job;
    }

}