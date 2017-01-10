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
package org.ow2.proactive.scheduler.task.internal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;


/**
 * @author ActiveEon Team
 * @since 2 Jan 2017
 */
public class InternalTaskParentFinderTest {

    private InternalTaskParentFinder internalTaskParentFinder;

    @Before
    public void init() {
        internalTaskParentFinder = InternalTaskParentFinder.getInstance();
    }

    @Test
    public void testGetFirstNotSkippedParentTaskIdsNotSkipped() {
        InternalTask parentTask = generateInternalTask(123L, TaskStatus.FINISHED);
        Set<TaskId> parentsIds = internalTaskParentFinder.getFirstNotSkippedParentTaskIds(parentTask);
        assertThat(parentsIds.size(), is(1));

    }

    @Test
    public void testGetFirstNotSkippedParentTaskIdsSkipped() {
        InternalTask parentTaskSkipped = generateInternalTask(123L, TaskStatus.SKIPPED);
        InternalTask parentTaskSkipped2 = generateInternalTask(124L, TaskStatus.SKIPPED);
        InternalTask parentTaskSkipped3 = generateInternalTask(125L, TaskStatus.SKIPPED);
        InternalTask parentTaskNotSkipped = generateInternalTask(127L, TaskStatus.FINISHED);
        InternalTask parentTaskNotSkipped2 = generateInternalTask(128L, TaskStatus.FINISHED);

        parentTaskSkipped.addDependence(parentTaskSkipped2);
        parentTaskSkipped2.addDependence(parentTaskSkipped3);
        parentTaskSkipped3.addDependence(parentTaskNotSkipped);
        parentTaskSkipped3.addDependence(parentTaskNotSkipped2);

        Set<TaskId> parentsIds = internalTaskParentFinder.getFirstNotSkippedParentTaskIds(parentTaskSkipped);
        assertThat(parentsIds.size(), is(2));
        assertThat(parentsIds.contains(parentTaskNotSkipped.getId()), is(true));
        assertThat(parentsIds.contains(parentTaskNotSkipped2.getId()), is(true));

    }

    private InternalTask generateInternalTask(long id, TaskStatus taskStatus) {
        InternalJob job = new InternalTaskFlowJob("test-name", JobPriority.NORMAL, OnTaskError.CANCEL_JOB,
            "description");
        InternalTask internalTask = new InternalScriptTask(job);
        internalTask.setId(TaskIdImpl.createTaskId(new JobIdImpl(666L, "JobName"), "readableName", id));
        internalTask.setStatus(taskStatus);
        return internalTask;

    }

}
