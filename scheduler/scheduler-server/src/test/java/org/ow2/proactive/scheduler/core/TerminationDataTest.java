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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxy;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxyCreationException;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.ExecuterInformation;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.utils.VariablesMap;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.utils.NodeSet;


public class TerminationDataTest {

    private TerminationData terminationData;

    @Mock
    private SchedulingService service;

    @Mock
    private TaskLauncher launcher;

    @Mock
    private SchedulingInfrastructure schedulingInfrastructure;

    @Mock
    private RMProxiesManager proxiesManager;

    @Mock
    private RMProxy rmProxy;

    @Before
    public void init() throws RMProxyCreationException {
        MockitoAnnotations.initMocks(this);
        Mockito.when(service.getInfrastructure()).thenReturn(schedulingInfrastructure);
        Mockito.when(schedulingInfrastructure.getRMProxiesManager()).thenReturn(proxiesManager);
        Mockito.when(proxiesManager.getUserRMProxy("user", null)).thenReturn(rmProxy);

        terminationData = TerminationData.newTerminationData();
    }

    @Test
    public void testAddJobToTerminate() {
        assertThat(terminationData.isEmpty(), is(true));
        JobId jobId = new JobIdImpl(666, "readableName");
        terminationData.addJobToTerminate(jobId);
        assertThat(terminationData.isEmpty(), is(false));
        assertThat(terminationData.jobTerminated(jobId), is(true));
    }

    @Test
    public void testAddTaskData() {
        assertThat(terminationData.isEmpty(), is(true));
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId jobId = new JobIdImpl(666, "readableName");
        InternalTask internalTask = new InternalScriptTask(job);
        TaskId taskId = TaskIdImpl.createTaskId(jobId, "task-name", 777L);
        internalTask.setId(taskId);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        RunningTaskData taskData = new RunningTaskData(internalTask, "user", null, null);
        terminationData.addTaskData(null, taskData, true, null);
        assertThat(terminationData.isEmpty(), is(false));
        assertThat(terminationData.taskTerminated(jobId, "task-name"), is(true));
    }

    @Test
    public void testAddRestartData() {
        assertThat(terminationData.isEmpty(), is(true));
        JobId jobId = new JobIdImpl(666, "readableName");
        TaskId taskId = TaskIdImpl.createTaskId(jobId, "task-name", 777L);
        terminationData.addRestartData(taskId, 1000L);
        assertThat(terminationData.isEmpty(), is(false));
    }

    @Test
    public void testHandleTerminationForJob() throws IOException, ClassNotFoundException {
        JobId jobId = new JobIdImpl(666, "readableName");
        terminationData.addJobToTerminate(jobId);
        terminationData.handleTermination(service);
        Mockito.verify(service, Mockito.times(1)).terminateJobHandling(jobId);
    }

    @Test
    public void testHandleTerminationForTaskNotNormalTermination() throws IOException, ClassNotFoundException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId jobId = new JobIdImpl(666, "readableName");
        InternalTask internalTask = new InternalScriptTask(job);
        TaskId taskId = TaskIdImpl.createTaskId(jobId, "task-name", 777L);
        internalTask.setId(taskId);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        RunningTaskData taskData = new RunningTaskData(internalTask, "user", null, launcher);
        terminationData.addTaskData(null, taskData, false, null);
        terminationData.handleTermination(service);
        Mockito.verify(launcher, Mockito.times(1)).kill();
    }

    @Test
    public void testHandleTerminationForTaskNormalTermination()
            throws RMProxyCreationException, IOException, ClassNotFoundException {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        JobId jobId = new JobIdImpl(666, "readableName");
        InternalTask internalTask = new InternalScriptTask(job);
        TaskId taskId = TaskIdImpl.createTaskId(jobId, "task-name", 777L);
        internalTask.setId(taskId);
        internalTask.setName("task-name");
        internalTask.setStatus(TaskStatus.RUNNING);
        internalTask.setExecuterInformation(Mockito.mock(ExecuterInformation.class));
        RunningTaskData taskData = new RunningTaskData(internalTask, "user", null, launcher);
        terminationData.addTaskData(null, taskData, true, null);
        terminationData.handleTermination(service);
        Mockito.verify(proxiesManager, Mockito.times(1)).getUserRMProxy("user", null);
        Mockito.verify(rmProxy, Mockito.times(1)).releaseNodes(org.mockito.Matchers.any(NodeSet.class),
                                                               org.mockito.Matchers.any(org.ow2.proactive.scripting.Script.class),
                                                               Mockito.any(VariablesMap.class),
                                                               Mockito.any(HashMap.class),
                                                               Mockito.any(TaskId.class));

    }

    @Test
    public void testHandleTerminationForTaskToRestart()
            throws RMProxyCreationException, IOException, ClassNotFoundException {
        JobId jobId = new JobIdImpl(666, "readableName");
        TaskId taskId = TaskIdImpl.createTaskId(jobId, "task-name", 777L);
        terminationData.addRestartData(taskId, 1000L);
        terminationData.handleTermination(service);
        Mockito.verify(schedulingInfrastructure, Mockito.times(1)).schedule(org.mockito.Matchers.any(Runnable.class),
                                                                            org.mockito.Matchers.anyLong());

    }

}
