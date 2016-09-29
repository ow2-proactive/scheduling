package org.ow2.proactive.scheduler.core;

import static org.mockito.Mockito.times;

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

        Mockito.verify(frontendState, times(1)).checkPermissions("getJobResult", ij,
                "You do not have permission to get the result of this job !");

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

        Mockito.verify(frontendState, times(1)).checkPermissions("getTaskResultFromIncarnation", ij,
                "You do not have permission to get the task result of this job !");

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

        Mockito.verify(frontendState, times(1)).checkPermissions("killTask", ij,
                "You do not have permission to kill this task !");

        try {
            schedulerFrontend.killTask("jobId", "taskname");
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1)).checkPermissions("killTask", ij,
                "You do not have permission to kill this task !");

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

        Mockito.verify(frontendState, times(1)).checkPermissions("restartTask", ij,
                "You do not have permission to restart this task !");

        try {
            schedulerFrontend.restartTask("jobId", "taskname", 1);
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1)).checkPermissions("restartTask", ij,
                "You do not have permission to restart this task !");

    }

    @Test
    public void testRestartInErrorTask() throws KeyException, AlreadyConnectedException,
            NotConnectedException, PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.restartInErrorTask("123", "taskname");
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1)).checkPermissions("restartTaskOnError", ij,
                "You do not have permission to restart this task !");

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

        Mockito.verify(frontendState, times(2)).checkPermissions("preemptTask", ij,
                "You do not have permission to preempt this task !");

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

        Mockito.verify(frontendState, times(2)).checkPermissions("removeJob", ij,
                "You do not have permission to remove this job !");

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

        Mockito.verify(frontendState, times(2)).checkPermissions("listenJobLogs", ij,
                "You do not have permission to listen the log of this job !");

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

        Mockito.verify(frontendState, times(1)).checkPermissions("pauseJob", ij,
                "You do not have permission to pause this job !");

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

        Mockito.verify(frontendState, times(1)).checkPermissions("resumeJob", ij,
                "You do not have permission to resume this job !");

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

        Mockito.verify(frontendState, times(1)).checkPermissions("killJob", ij,
                "You do not have permission to kill this job !");

    }

    @Test
    public void testRestartAllInErrorTasks() throws KeyException, AlreadyConnectedException,
            NotConnectedException, PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.restartAllInErrorTasks("123");
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1)).checkPermissions("restartAllInErrorTasks", ij,
                "You do not have permission to restart in error tasks in this job !");

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

        Mockito.verify(frontendState, times(1)).checkPermissions("getJobServerLogs", ij,
                "You do not have permissions to get the logs of this job !");

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

        Mockito.verify(frontendState, times(1)).checkPermissions("getTaskServerLogs", ij,
                "You do not have permission to get the task logs of this job !");
    }

    @Test
    public void testGetTaskServerLogsByTag() throws KeyException, AlreadyConnectedException,
            NotConnectedException, PermissionException, UnknownJobException {

        Mockito.when(frontendState.getIdentifiedJob(jobId)).thenReturn(ij);
        Mockito.when(ij.isFinished()).thenReturn(false);
        Mockito.when(frontendState.getJobState(jobId)).thenReturn(jobstate);

        try {
            schedulerFrontend.getTaskServerLogsByTag("123", "taskname");
        } catch (Exception e) {
        }

        Mockito.verify(frontendState, times(1)).checkPermissions("getTaskServerLogsByTag", ij,
                "You do not have permission to get the task logs of this job !");
    }
}