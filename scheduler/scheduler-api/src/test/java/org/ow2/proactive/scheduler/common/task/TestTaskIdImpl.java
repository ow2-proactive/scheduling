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
package org.ow2.proactive.scheduler.common.task;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;


public class TestTaskIdImpl {

    @Test
    public void testGetIterationIndex() throws Exception {
        TaskId taskNoIterationIndex = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task", 1);
        TaskId taskIterationIndexSmallerThan9 = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task#1", 1);
        TaskId taskIterationIndexGreaterThan9 = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task#10", 1);
        TaskId taskReplicatedAndIterated = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task#10*10", 1);

        assertEquals(0, taskNoIterationIndex.getIterationIndex());
        assertEquals(1, taskIterationIndexSmallerThan9.getIterationIndex());
        assertEquals(10, taskIterationIndexGreaterThan9.getIterationIndex());
        assertEquals(10, taskReplicatedAndIterated.getIterationIndex());
    }

    @Test
    public void testGetReplicationIndex() throws Exception {
        TaskId taskNoReplicationIndex = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task", 1);
        TaskId taskReplicationIndexSmallerThan9 = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task*1", 1);
        TaskId taskReplicationIndexGreaterThan9 = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task*10", 1);
        TaskId taskReplicatedAndIterated = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task#10*10", 1);

        assertEquals(0, taskNoReplicationIndex.getReplicationIndex());
        assertEquals(1, taskReplicationIndexSmallerThan9.getReplicationIndex());
        assertEquals(10, taskReplicationIndexGreaterThan9.getReplicationIndex());
        assertEquals(10, taskReplicatedAndIterated.getReplicationIndex());
    }

}
