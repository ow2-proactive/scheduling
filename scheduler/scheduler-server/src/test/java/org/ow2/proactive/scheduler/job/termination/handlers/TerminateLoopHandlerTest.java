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
import static org.mockito.Mockito.times;
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
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.core.SchedulerStateUpdate;
import org.ow2.proactive.scheduler.job.ChangedTasksInfo;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.policy.ISO8601DateUtil;

import com.google.common.collect.Maps;


public class TerminateLoopHandlerTest {

    private TerminateLoopHandler terminateLoopHandler;

    @Mock
    private InternalJob internalJob;

    @Mock
    private FlowAction action;

    private InternalTask initiator;

    @Mock
    private ChangedTasksInfo changesInfo;

    @Mock
    private SchedulerStateUpdate frontend;

    @Mock
    private JobInfoImpl jobInfoImpl;

    @Before
    public void init() {

        MockitoAnnotations.initMocks(this);
        when(internalJob.getJobInfo()).thenReturn(jobInfoImpl);
        Map<TaskId, InternalTask> tasks = genearteTaks();
        when(internalJob.getIHMTasks()).thenReturn(tasks);
        this.terminateLoopHandler = new TerminateLoopHandler(internalJob);
    }

    @Test
    public void testTerminateLoopTaskWithNoCronExrpression() {
        initiator = generateInternalTask();
        String initiatorName = initiator.getName();
        when(action.getTarget()).thenReturn(initiatorName);
        when(internalJob.replicateForNextLoopIteration(initiator,
                                                       initiator,
                                                       changesInfo,
                                                       frontend,
                                                       action)).thenReturn(true);
        boolean result = terminateLoopHandler.terminateLoopTask(action, initiator, changesInfo, frontend);
        assertThat(result, is(true));
        verify(changesInfo, times(0)).getNewTasks();
    }

    @Test
    public void testTerminateLoopTaskWithCronExrpression() {
        Map<TaskId, InternalTask> newTasks = genearteTaks();
        initiator = generateInternalTask();
        String initiatorName = initiator.getName();
        when(action.getTarget()).thenReturn(initiatorName);
        when(action.getCronExpr()).thenReturn("* * * * *");
        when(changesInfo.getNewTasks()).thenReturn(newTasks.keySet());
        when(internalJob.getIHMTasks()).thenReturn(newTasks);
        when(internalJob.replicateForNextLoopIteration(initiator,
                                                       initiator,
                                                       changesInfo,
                                                       frontend,
                                                       action)).thenReturn(true);
        boolean result = terminateLoopHandler.terminateLoopTask(action, initiator, changesInfo, frontend);
        assertThat(result, is(true));
        Map<String, String> genericInformation = newTasks.values().iterator().next().getRuntimeGenericInformation();
        assertThat(genericInformation.containsKey(InternalJob.GENERIC_INFO_START_AT_KEY), is(true));
        assertThat(newTasks.values().iterator().next().getScheduledTime(),
                   is(ISO8601DateUtil.toDate(genericInformation.get(InternalJob.GENERIC_INFO_START_AT_KEY)).getTime()));
    }

    private Map<TaskId, InternalTask> genearteTaks() {
        Map<TaskId, InternalTask> tempTasks = Maps.newHashMap();
        InternalTask internalTask = generateInternalTask();
        tempTasks.put(internalTask.getId(), internalTask);
        return tempTasks;
    }

    private InternalTask generateInternalTask() {
        InternalJob job = new InternalTaskFlowJob("test-name",
                                                  JobPriority.NORMAL,
                                                  OnTaskError.CANCEL_JOB,
                                                  "description");
        InternalTask internalTask = new InternalScriptTask(job);
        internalTask.setId(TaskIdImpl.createTaskId(new JobIdImpl(666L, "JobName"), "readableName", 555L));
        return internalTask;

    }

}
