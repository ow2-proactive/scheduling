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
package org.ow2.proactive.scheduler.job.termination.handlers;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;
import org.ow2.proactive.scheduler.core.SchedulerStateUpdate;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.ChangedTasksInfo;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.python.google.common.collect.Lists;

import com.google.common.collect.Maps;


/**
 * @author ActiveEon Team
 * @since 2 Jan 2017
 */
public class TerminateReplicateTaskHandlerTest {

    /**
     * 
     */
    private static final long MERGE_TASK_ID = 777L;

    private TerminateReplicateTaskHandler terminateReplicateTaskHandler;

    @Mock
    private InternalJob internalJob;

    @Mock
    private JobDescriptorImpl jobDescriptorImpl;

    @Mock
    private ChangedTasksInfo changesInfo;

    @Mock
    private SchedulerStateUpdate frontend;

    @Mock
    private JobInfoImpl jobInfoImpl;

    private InternalTask initiator;

    private FlowAction action;

    private Map<TaskId, InternalTask> tasks = genearteTaks();

    @Before
    public void init() {
        action = new FlowAction(FlowActionType.REPLICATE);
        action.setDupNumber(0);
        MockitoAnnotations.initMocks(this);
        when(internalJob.getJobInfo()).thenReturn(jobInfoImpl);
        when(internalJob.getJobDescriptor()).thenReturn(jobDescriptorImpl);
        terminateReplicateTaskHandler = new TerminateReplicateTaskHandler(internalJob);

    }

    @Test
    public void testTerminateReplicateTaskSkipOneTask() {
        tasks = genearteTaks();
        when(internalJob.getIHMTasks()).thenReturn(tasks);
        initiator = generateInitiatorTask();
        boolean result = terminateReplicateTaskHandler.terminateReplicateTask(action,
                                                                              initiator,
                                                                              changesInfo,
                                                                              frontend,
                                                                              initiator.getId());
        assertThat(result, is(true));
        verify(jobInfoImpl).setTasksChanges(changesInfo, internalJob);
        assertThat(tasks.get(generateInternalTask(555L).getId()).getStatus(), is(TaskStatus.SKIPPED));
        assertThat(tasks.get(generateInternalTask(MERGE_TASK_ID).getId()).getStatus(), is(TaskStatus.PENDING));

    }

    @Test
    public void testTerminateReplicateTaskSkipBlockOfTasks() {
        tasks = genearteTaksWithBlock();
        when(internalJob.getIHMTasks()).thenReturn(tasks);

        initiator = generateInitiatorTask();
        boolean result = terminateReplicateTaskHandler.terminateReplicateTask(action,
                                                                              initiator,
                                                                              changesInfo,
                                                                              frontend,
                                                                              initiator.getId());
        assertThat(result, is(true));
        verify(jobInfoImpl).setTasksChanges(changesInfo, internalJob);
        assertThat(tasks.get(generateInternalTask(555L).getId()).getStatus(), is(TaskStatus.SKIPPED));
        assertThat(tasks.get(generateInternalTask(666L).getId()).getStatus(), is(TaskStatus.SKIPPED));

        assertThat(tasks.get(generateInternalTask(888L).getId()).getStatus(), is(TaskStatus.SKIPPED));
        assertThat(tasks.get(generateInternalTask(999L).getId()).getStatus(), is(TaskStatus.SKIPPED));

        assertThat(tasks.get(generateInternalTask(MERGE_TASK_ID).getId()).getStatus(), is(TaskStatus.PENDING));

    }

    private Map<TaskId, InternalTask> genearteTaks() {
        Map<TaskId, InternalTask> tempTasks = Maps.newHashMap();
        InternalTask internalTask = generateInternalTask(555L);
        tempTasks.put(internalTask.getId(), internalTask);
        InternalTask mergeTask = generateInternalTask(MERGE_TASK_ID);
        tempTasks.put(mergeTask.getId(), mergeTask);
        InternalTask initiatorTask = generateInitiatorTask();
        internalTask.addDependence(initiatorTask);
        mergeTask.addDependence(internalTask);
        tempTasks.put(initiatorTask.getId(), initiatorTask);
        return tempTasks;
    }

    private Map<TaskId, InternalTask> genearteTaksWithBlock() {
        Map<TaskId, InternalTask> tempTasks = Maps.newHashMap();
        InternalTask startTask = generateInternalTask(555L);
        startTask.setFlowBlock(FlowBlock.START);
        tempTasks.put(startTask.getId(), startTask);

        InternalTask internalTask2 = generateInternalTask(666L);
        tempTasks.put(internalTask2.getId(), internalTask2);
        internalTask2.addDependence(startTask);

        InternalTask internalTask3 = generateInternalTask(888L);
        tempTasks.put(internalTask3.getId(), internalTask3);
        internalTask3.addDependence(startTask);
        when(jobDescriptorImpl.getTaskChildren(startTask)).thenReturn(Lists.newArrayList(internalTask2, internalTask3));

        InternalTask endTask = generateInternalTask(999L);
        tempTasks.put(endTask.getId(), endTask);
        endTask.setFlowBlock(FlowBlock.END);
        when(jobDescriptorImpl.getTaskChildren(internalTask2)).thenReturn(Lists.newArrayList(endTask));
        when(jobDescriptorImpl.getTaskChildren(internalTask3)).thenReturn(Lists.newArrayList(endTask));

        endTask.addDependence(internalTask2);
        endTask.addDependence(internalTask3);

        InternalTask mergeTask = generateInternalTask(MERGE_TASK_ID);
        tempTasks.put(mergeTask.getId(), mergeTask);
        mergeTask.addDependence(endTask);

        InternalTask initiatorTask = generateInitiatorTask();
        startTask.addDependence(initiatorTask);

        tempTasks.put(initiatorTask.getId(), initiatorTask);

        return tempTasks;
    }

    private InternalTask generateInternalTask(long id) {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        InternalTask internalTask = new InternalScriptTask(job);
        internalTask.setId(TaskIdImpl.createTaskId(new JobIdImpl(666L, "JobName"), "readableName", id));
        internalTask.setStatus(TaskStatus.PENDING);
        return internalTask;

    }

    private InternalTask generateInitiatorTask() {
        InternalJob job = new InternalTaskFlowJob("initiator",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        InternalTask initiatorTask = new InternalScriptTask(job);
        initiatorTask.setId(TaskIdImpl.createTaskId(new JobIdImpl(666L, "JobName"), "readableName", 111L));
        initiatorTask.setReplicationIndex(1);
        return initiatorTask;

    }

}
