/*
 *  *
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core.db;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.Mockito;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

import java.security.KeyException;
import java.util.*;

import static com.google.common.truth.Truth.assertThat;

/**
 * Functional tests related to {@link SchedulerStateRecoverHelper}.
 * <p>
 * Tests are mainly checking job and task statuses once {@link SchedulerStateRecoverHelper#recover(long)} is called.
 */
public class SchedulerStateRecoverHelperTest {

    private static final int DEFAULT_NUMBER_OF_JOBS = 5;

    private static final String DEFAULT_WORKFLOW_DESCRIPTOR_NAME = "recovery.xml";

    private static final JobFactory JOB_FACTORY = JobFactory.getFactory().getFactory();

    @Test
    public void testRecoverWithNoJobToLoad() throws Exception {
        new Scenario(ImmutableList.<InternalJob>of()).execute();
    }

    @Test
    public void testRecoverWithCanceledJobOnly() throws Exception {
        InternalJob job = createJob(JobStatus.CANCELED);
        changeTasksState(job, TaskStatus.PENDING);

        ImmutableMap<String, TaskStatus> tasksStatus =
                ImmutableMap.of(
                        "Ta", TaskStatus.FINISHED,
                        "Tb", TaskStatus.FAILED,
                        "Tc", TaskStatus.NOT_STARTED);
        changeTasksState(job, tasksStatus);

        RecoveredSchedulerState recoveredState = new Scenario(job).execute();
        job = recoveredState.getFinishedJobs().get(0);

        assertThat(job.getStatus()).isEqualTo(JobStatus.CANCELED);
        assertTasksStatus(job, tasksStatus);
    }

    @Test
    public void testRecoverWithFailedJobOnly() throws Exception {
        InternalJob job = createJob(JobStatus.FAILED);
        changeTasksState(job, TaskStatus.FAILED);

        RecoveredSchedulerState recoveredState = new Scenario(job).execute();

        assertThat(recoveredState.getFinishedJobs().get(0).getStatus()).isEqualTo(JobStatus.FAILED);
        assertTasksStatus(job, TaskStatus.FAILED);
    }

    @Test
    public void testRecoverWithFinishedJobsOnly() throws Exception {
        List<InternalJob> jobs = new ArrayList<>(DEFAULT_NUMBER_OF_JOBS);

        for (int i = 0; i < DEFAULT_NUMBER_OF_JOBS; i++) {
            InternalJob job = createJob(JobStatus.FINISHED);
            changeTasksState(job, TaskStatus.FINISHED);
            jobs.add(job);
        }

        RecoveredSchedulerState recoveredState = new Scenario(jobs).execute();

        assertTasksStatus(recoveredState.getFinishedJobs(), TaskStatus.FINISHED);
    }

    @Test
    public void testRecoverWithFinishedJobsOnlyFilteredByPeriod() throws Exception {
        List<InternalJob> jobs = new ArrayList<>(DEFAULT_NUMBER_OF_JOBS);

        for (int i = 0; i < DEFAULT_NUMBER_OF_JOBS; i++) {
            InternalJob job = createJob(JobStatus.FINISHED);
            changeTasksState(job, TaskStatus.FINISHED);
            job.setSubmittedTime(i);
            jobs.add(job);
        }

        RecoveredSchedulerState recoveredState = new Scenario(jobs, 3).execute();

        assertThat(recoveredState.getFinishedJobs()).hasSize(DEFAULT_NUMBER_OF_JOBS - 3);
        assertTasksStatus(recoveredState.getFinishedJobs(), TaskStatus.FINISHED);
    }

    @Test
    public void testRecoverWithKilledJobOnly() throws Exception {
        InternalJob job = createJob(JobStatus.KILLED);
        changeTasksState(job, TaskStatus.FINISHED);

        ImmutableMap<String, TaskStatus> tasksStatus =
                ImmutableMap.of(
                        "Ta", TaskStatus.FINISHED,
                        "Tb", TaskStatus.ABORTED,
                        "Tc", TaskStatus.PENDING);
        changeTasksState(job, tasksStatus);

        RecoveredSchedulerState recoveredState = new Scenario(job).execute();

        assertThat(recoveredState.getFinishedJobs().get(0).getStatus()).isEqualTo(JobStatus.KILLED);
        assertTasksStatus(recoveredState.getFinishedJobs(), tasksStatus);
    }

    @Test
    public void testRecoverWithPausedJobOnly() throws Exception {
        InternalJob job = createJob(JobStatus.PAUSED);
        changeTasksState(job, TaskStatus.PAUSED);

        ImmutableMap<String, TaskStatus> tasksStatus =
                ImmutableMap.of(
                        "Ta", TaskStatus.FINISHED,
                        "Tb", TaskStatus.PAUSED,
                        "Tc", TaskStatus.PENDING);
        changeTasksState(job, tasksStatus);

        RecoveredSchedulerState recoveredState = new Scenario(job).execute();
        job = recoveredState.getRunningJobs().get(0);

        assertThat(job.getStatus()).isEqualTo(JobStatus.PAUSED);
        assertTasksStatus(job,
                ImmutableMap.of(
                        "Ta", TaskStatus.FINISHED,
                        "Tb", TaskStatus.PAUSED,
                        "Tc", TaskStatus.PAUSED));
    }

    @Test
    public void testRecoverWithPausedJobOnlyAllTasksSameStatus() throws Exception {
        InternalJob job = createJob(JobStatus.PAUSED);
        changeTasksState(job, TaskStatus.PAUSED);

        RecoveredSchedulerState recoveredState = new Scenario(job).execute(false);
        job = recoveredState.getPendingJobs().get(0);

        assertThat(job.getStatus()).isEqualTo(JobStatus.PAUSED);
        assertTasksStatus(job, TaskStatus.PAUSED);
    }

    @Test
    public void testRecoverWithRunningJobOnly() throws Exception {
        InternalJob job = createJob(JobStatus.RUNNING);
        changeTasksState(job, TaskStatus.RUNNING);

        ImmutableMap<String, TaskStatus> tasksStatus =
                ImmutableMap.of(
                        "Ta", TaskStatus.ABORTED,
                        "Tb", TaskStatus.FAULTY,
                        "Tc", TaskStatus.PENDING);
        changeTasksState(job, tasksStatus);

        RecoveredSchedulerState recoveredState = new Scenario(job).execute();
        job = recoveredState.getRunningJobs().get(0);

        assertThat(job.getStatus()).isEqualTo(JobStatus.STALLED);
        assertTasksStatus(job, tasksStatus);
    }

    @Test
    public void testRecoverWithRunningJobOnlyAllTasksSameStatus() throws Exception {
        InternalJob job = createJob(JobStatus.RUNNING);
        changeTasksState(job, TaskStatus.RUNNING);

        RecoveredSchedulerState recoveredState = new Scenario(job).execute();
        job = recoveredState.getRunningJobs().get(0);

        assertThat(job.getStatus()).isEqualTo(JobStatus.STALLED);
        assertTasksStatus(job, TaskStatus.PENDING);
    }

    @Test
    public void testRecoverWithPendingJobOnly() throws Exception {
        InternalJob job = createJob(JobStatus.PENDING);
        changeTasksState(job, TaskStatus.PENDING);

        RecoveredSchedulerState recoveredState = new Scenario(job).execute();
        assertTasksStatus(recoveredState.getPendingJobs(), TaskStatus.PENDING);
    }

    @Test
    public void testRecoverWithStalledJobOnly() throws Exception {
        InternalJob job = createJob(JobStatus.STALLED);
        changeTasksState(job, TaskStatus.PENDING);

        ImmutableMap<String, TaskStatus> taskStatus =
                ImmutableMap.of(
                        "Ta", TaskStatus.FINISHED,
                        "Tb", TaskStatus.SKIPPED,
                        "Tc", TaskStatus.PENDING);
        changeTasksState(job, taskStatus);

        RecoveredSchedulerState recoveredState = new Scenario(job).execute();
        job = recoveredState.getRunningJobs().get(0);

        assertThat(job.getStatus()).isEqualTo(JobStatus.STALLED);
        assertTasksStatus(job, taskStatus);
    }

    @Test
    public void testRecoverWithMixedJobs() throws Exception {
        ImmutableList<JobStatus> jobStatuses =
                ImmutableList.of(
                        JobStatus.CANCELED,
                        JobStatus.FAILED,
                        JobStatus.FINISHED,
                        JobStatus.KILLED,
                        JobStatus.PAUSED,
                        JobStatus.PENDING,
                        JobStatus.RUNNING,
                        JobStatus.STALLED);

        ImmutableList<TaskStatus> taskStatuses =
                ImmutableList.of(
                        TaskStatus.ABORTED,
                        TaskStatus.FAILED,
                        TaskStatus.FINISHED,
                        TaskStatus.ABORTED,
                        TaskStatus.PAUSED,
                        TaskStatus.PENDING,
                        TaskStatus.RUNNING,
                        TaskStatus.SUBMITTED);

        List<InternalJob> jobs = new ArrayList<>(jobStatuses.size());

        for (int i = 0; i < jobStatuses.size(); i++) {
            InternalJob job = createJob(jobStatuses.get(i));
            jobs.add(job);
            changeTasksState(job, taskStatuses.get(i));
        }

        RecoveredSchedulerState recoveredState = new Scenario(jobs).execute();

        assertThat(recoveredState.getFinishedJobs()).hasSize(4);
        assertThat(recoveredState.getPendingJobs()).hasSize(2);
        assertThat(recoveredState.getRunningJobs()).hasSize(2);
    }

    @Test
    public void testRecoverWithCopyAndSortThrowingRuntimeException() throws KeyException, JobCreationException {
        RecoveredSchedulerState recoveredState =
                new Scenario(createJob(JobStatus.RUNNING)).execute(new SchedulerStateRecoverHelperSupplier() {
                    @Override
                    public SchedulerStateRecoverHelper get(SchedulerDBManager dbManager) {
                        return new SchedulerStateRecoverHelper(dbManager) {
                            @Override
                            protected List<InternalTask> copyAndSort(List<InternalTask> tasks) {
                                throw new RuntimeException("bouh!");
                            }
                        };
                    }
                }, false);

        assertThat(recoveredState.getFinishedJobs()).hasSize(1);
        assertThat(recoveredState.getPendingJobs()).hasSize(0);
        assertThat(recoveredState.getRunningJobs()).hasSize(0);

        assertThat(recoveredState.getFinishedJobs().get(0).getStatus()).isEqualTo(JobStatus.CANCELED);
    }

    @Test(expected = IllegalStateException.class)
    public void testRecoverWithCanceledStatusForLoadedNotFinishedJobs() {
        testRecoverWithIncorrectStatusForLoadedNotFinishedJobs(JobStatus.CANCELED);
    }

    @Test(expected = IllegalStateException.class)
    public void testRecoverWithFailedStatusForLoadedNotFinishedJobs() {
        testRecoverWithIncorrectStatusForLoadedNotFinishedJobs(JobStatus.FAILED);
    }

    @Test(expected = IllegalStateException.class)
    public void testRecoverWithFinishedStatusForLoadedNotFinishedJobs() {
        testRecoverWithIncorrectStatusForLoadedNotFinishedJobs(JobStatus.FINISHED);
    }

    @Test(expected = IllegalStateException.class)
    public void testRecoverWithKilledStatusForLoadedNotFinishedJobs() {
        testRecoverWithIncorrectStatusForLoadedNotFinishedJobs(JobStatus.KILLED);
    }

    public void testRecoverWithIncorrectStatusForLoadedNotFinishedJobs(JobStatus jobStatus) {
        InternalJob job = new InternalTaskFlowJob();
        job.setStatus(jobStatus);

        SchedulerDBManager dbManager = Mockito.mock(SchedulerDBManager.class);
        SchedulerStateRecoverHelper stateRecoverHelper = new SchedulerStateRecoverHelper(dbManager);
        Mockito.when(dbManager.loadNotFinishedJobs(true)).thenReturn(ImmutableList.of(job));

        stateRecoverHelper.recover(-1);
    }

    private void assertTasksStatus(Iterable<? extends InternalJob> iterable, TaskStatus expectedTasksStatus) {
        Iterator<? extends InternalJob> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            assertTasksStatus(iterator.next(), expectedTasksStatus);
        }
    }

    private void assertTasksStatus(InternalJob job, TaskStatus expectedTasksStatus) {
        assertTasksStatus(job, getStringTaskStatusMap(job, expectedTasksStatus));
    }

    private void assertTasksStatus(Iterable<? extends InternalJob> iterable, Map<String, TaskStatus> expectedTasksStatus) {
        Iterator<? extends InternalJob> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            assertTasksStatus(iterator.next(), expectedTasksStatus);
        }
    }

    private void assertTasksStatus(InternalJob job, Map<String, TaskStatus> expectedTasksStatus) {
        for (InternalTask internalTask : job.getITasks()) {
            TaskStatus expectedTaskStatus = expectedTasksStatus.get(internalTask.getName());

            if (expectedTaskStatus != null) {
                assertThat(internalTask.getStatus()).isEqualTo(expectedTaskStatus);
            }
        }
    }

    private static class Scenario {

        private long loadJobPeriod;

        private List<InternalJob> jobs;

        public Scenario(InternalJob job) {
            this(ImmutableList.of(job));
        }

        public Scenario(List<InternalJob> jobs) {
            this(jobs, -1);
        }

        public Scenario(List<InternalJob> jobs, long loadJobPeriod) {
            this.loadJobPeriod = loadJobPeriod;
            this.jobs = jobs;
        }

        public RecoveredSchedulerState execute() {
            return execute(true);
        }

        public RecoveredSchedulerState execute(boolean checkBasics) {
            return execute(new SchedulerStateRecoverHelperSupplier() {
                @Override
                public SchedulerStateRecoverHelper get(SchedulerDBManager dbManager) {
                    return new SchedulerStateRecoverHelper(dbManager);
                }
            }, checkBasics);
        }

        public RecoveredSchedulerState execute(SchedulerStateRecoverHelperSupplier supplier, boolean checkBasics) {
            List<InternalJob> finishedJobs = new ArrayList<>();
            List<InternalJob> notFinishedJobs = new ArrayList<>();

            for (InternalJob job : jobs) {
                if (SchedulerDBManager.FINISHED_JOB_STATUSES.contains(job.getStatus())) {
                    addJob(finishedJobs, job);
                } else {
                    addJob(notFinishedJobs, job);
                }
            }

            SchedulerDBManager dbManager = Mockito.mock(SchedulerDBManager.class);
            SchedulerStateRecoverHelper stateRecoverHelper = supplier.get(dbManager);
            Mockito.when(dbManager.loadNotFinishedJobs(true)).thenReturn(notFinishedJobs);
            Mockito.when(dbManager.loadFinishedJobs(false, loadJobPeriod)).thenReturn(finishedJobs);

            RecoveredSchedulerState recoveredState = stateRecoverHelper.recover(loadJobPeriod);

            if (checkBasics) {
                checkBasics(finishedJobs, notFinishedJobs, recoveredState);
            }

            return recoveredState;
        }

        private void addJob(List<InternalJob> finishedJobs, InternalJob job) {
            if (loadJobPeriod == -1 || job.getSubmittedTime() >= loadJobPeriod) {
                finishedJobs.add(job);
            }
        }

        private void checkBasics(List<InternalJob> finishedJobs, List<InternalJob> notFinishedJobs, RecoveredSchedulerState recoveredState) {
            assertThat(recoveredState.getFinishedJobs()).hasSize(finishedJobs.size());
            assertThat(notFinishedJobs).hasSize(recoveredState.getPendingJobs().size() + recoveredState.getRunningJobs().size());

            for (InternalJob job : recoveredState.getFinishedJobs()) {
                assertThat(job.getStatus()).isIn(SchedulerDBManager.FINISHED_JOB_STATUSES);
            }

            for (InternalJob job : recoveredState.getRunningJobs()) {
                assertThat(job.getStatus()).isIn(SchedulerDBManager.RUNNING_JOB_STATUSES);
            }
        }

    }

    private interface SchedulerStateRecoverHelperSupplier {

        SchedulerStateRecoverHelper get(SchedulerDBManager dbManager);

    }

    private InternalJob createJob(JobStatus jobStatus) throws KeyException, JobCreationException {
        return createJob(DEFAULT_WORKFLOW_DESCRIPTOR_NAME, jobStatus);
    }

    private InternalJob createJob(String workflowDescriptor, JobStatus jobStatus) throws JobCreationException, KeyException {
        Job job = JOB_FACTORY.createJob(
                this.getClass().getResource(
                        "/workflow/descriptors/" + workflowDescriptor).getPath());

        InternalJob internalJob = InternalJobFactory.createJob(job, null);
        internalJob.setStatus(jobStatus);

        return internalJob;
    }

    private void changeTasksState(InternalJob job, TaskStatus newTaskStatus) {
        changeTasksState(job, getStringTaskStatusMap(job, newTaskStatus));
    }

    private Map<String, TaskStatus> getStringTaskStatusMap(InternalJob job, TaskStatus newTaskStatus) {
        List<InternalTask> tasks = job.getITasks();
        Map<String, TaskStatus> toUpdate = new HashMap<>(tasks.size());

        for (InternalTask internalTask : tasks) {
            toUpdate.put(internalTask.getName(), newTaskStatus);
        }
        return toUpdate;
    }

    private void changeTasksState(InternalJob job, Map<String, TaskStatus> newStatuses) {
        int nbPending = 0;
        int nbRunning = 0;
        int nbFinished = 0;

        for (InternalTask internalTask : job.getITasks()) {
            TaskStatus newStatus = newStatuses.get(internalTask.getName());

            if (newStatus != null) {
                internalTask.setStatus(newStatus);
            }

            switch (internalTask.getStatus()) {
                case PENDING:
                    nbPending++;
                    break;
                case RUNNING:
                    nbRunning++;
                    break;
                case FINISHED:
                    nbFinished++;
                    break;
            }
        }

        updateJobCounters(job, nbPending, nbRunning, nbFinished);
    }

    private void updateJobCounters(InternalJob job, int nbPending, int nbRunning, int nbFinished) {
        job.setNumberOfPendingTasks(nbPending);
        job.setNumberOfRunningTasks(nbRunning);
        job.setNumberOfFinishedTasks(nbFinished);
    }

}
