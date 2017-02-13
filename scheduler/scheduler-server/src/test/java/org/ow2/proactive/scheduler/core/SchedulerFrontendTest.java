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
package org.ow2.proactive.scheduler.core;

import static org.mockito.Mockito.times;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSIONS_TO_GET_THE_LOGS_OF_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_RESULT_OF_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_LOGS_OF_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_RESULT_OF_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THIS_TASK;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_LISTEN_THE_LOG_OF_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_PAUSE_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_PREEMPT_THIS_TASK;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_REMOVE_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_IN_ERROR_TASKS_IN_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_THIS_TASK;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_RESUME_THIS_JOB;

import java.security.KeyException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.objectweb.proactive.core.UniqueID;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.job.IdentifiedJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;


public class SchedulerFrontendTest {

    private SchedulerFrontend schedulerFrontend;

    @Mock
    private SchedulerFrontendState frontendState;

    @Mock
    private SchedulerSpacesSupport spacesSupport;

    @Mock
    private IdentifiedJob ij;

    @Mock
    private JobState jobstate;

    private JobId jobId;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        this.schedulerFrontend = new SchedulerFrontend(frontendState, spacesSupport);

        this.jobId = new JobIdImpl(123L, "readableName");

    }

    /**
     * Related to issue #1849.
     * <p>
     * https://github.com/ow2-proactive/scheduling/issues/1849
     */
    @Test
    public void testConnection() throws KeyException, AlreadyConnectedException {
        schedulerFrontend.connect(new UniqueID(), new UserIdentificationImpl("admin"), null);

        Mockito.verify(spacesSupport, times(1)).registerUserSpace("admin");
    }

    @Test
    public void testGetJobResult() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.getJobResult(jobId);
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1))
               .checkPermissions("getJobResult", ij, YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_RESULT_OF_THIS_JOB);

    }

    @Test
    public void testGetTaskResultFromIncarnation() throws KeyException, AlreadyConnectedException,
            NotConnectedException, PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.getTaskResultFromIncarnation(jobId, "taskname", 1);
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1))
               .checkPermissions("getTaskResultFromIncarnation",
                                 ij,
                                 YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_RESULT_OF_THIS_JOB);

    }

    @Test
    public void testKillTask() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.killTask(jobId, "taskname");
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1)).checkPermissions("killTask",
                                                                 ij,
                                                                 YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THIS_TASK);

        try {
            schedulerFrontend.killTask("jobId", "taskname");
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1)).checkPermissions("killTask",
                                                                 ij,
                                                                 YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THIS_TASK);

    }

    @Test
    public void testRestartTask() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.restartTask(jobId, "taskname", 1);
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1)).checkPermissions("restartTask",
                                                                 ij,
                                                                 YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_THIS_TASK);

        try {
            schedulerFrontend.restartTask("jobId", "taskname", 1);
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1)).checkPermissions("restartTask",
                                                                 ij,
                                                                 YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_THIS_TASK);

    }

    @Test
    public void testRestartInErrorTask() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.restartInErrorTask("123", "taskname");
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1)).checkPermissions("restartTaskOnError",
                                                                 ij,
                                                                 YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_THIS_TASK);

    }

    @Test
    public void testPreemptTask() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.preemptTask("123", "taskname", 6);
        } catch (Exception e) {
        }

        try {
            schedulerFrontend.preemptTask(jobId, "taskname", 6);
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(2)).checkPermissions("preemptTask",
                                                                 ij,
                                                                 YOU_DO_NOT_HAVE_PERMISSION_TO_PREEMPT_THIS_TASK);

    }

    @Test
    public void testRemoveJob() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.removeJob("123");
        } catch (Exception e) {
        }

        try {
            schedulerFrontend.removeJob(jobId);
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(2)).checkPermissions("removeJob",
                                                                 ij,
                                                                 YOU_DO_NOT_HAVE_PERMISSION_TO_REMOVE_THIS_JOB);

    }

    @Test
    public void testListenJobLogs() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.listenJobLogs("123", null);
        } catch (Exception e) {
        }

        try {
            schedulerFrontend.listenJobLogs(jobId, null);
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(2))
               .checkPermissions("listenJobLogs", ij, YOU_DO_NOT_HAVE_PERMISSION_TO_LISTEN_THE_LOG_OF_THIS_JOB);

    }

    @Test
    public void testPauseJob() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.pauseJob(jobId);
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1)).checkPermissions("pauseJob",
                                                                 ij,
                                                                 YOU_DO_NOT_HAVE_PERMISSION_TO_PAUSE_THIS_JOB);

    }

    @Test
    public void testResumeJob() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.resumeJob(jobId);
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1)).checkPermissions("resumeJob",
                                                                 ij,
                                                                 YOU_DO_NOT_HAVE_PERMISSION_TO_RESUME_THIS_JOB);

    }

    @Test
    public void testKillJob() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.killJob(jobId);
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1)).checkPermissions("killJob",
                                                                 ij,
                                                                 YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THIS_JOB);

    }

    @Test
    public void testRestartAllInErrorTasks() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.restartAllInErrorTasks("123");
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1))
               .checkPermissions("restartAllInErrorTasks",
                                 ij,
                                 YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_IN_ERROR_TASKS_IN_THIS_JOB);

    }

    @Test
    public void testGetJobServerLogs() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.getJobServerLogs("123");
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1))
               .checkPermissions("getJobServerLogs", ij, YOU_DO_NOT_HAVE_PERMISSIONS_TO_GET_THE_LOGS_OF_THIS_JOB);

    }

    @Test
    public void testGetTaskServerLogs() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.getTaskServerLogs("123", "taskname");
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1))
               .checkPermissions("getTaskServerLogs", ij, YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_LOGS_OF_THIS_JOB);
    }

    @Test
    public void testGetTaskServerLogsByTag() throws KeyException, AlreadyConnectedException, NotConnectedException,
            PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.getTaskServerLogsByTag("123", "taskname");
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1))
               .checkPermissions("getTaskServerLogsByTag",
                                 ij,
                                 YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_LOGS_OF_THIS_JOB);
    }
}
