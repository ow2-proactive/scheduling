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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;


public class TaskStatusTest {

    @Test
    public void testSubmitted() {
        Set<TaskStatus> expected = new HashSet<>(Collections.singletonList(TaskStatus.SUBMITTED));
        Set<TaskStatus> actual = TaskStatus.expandAggregatedStatusesToRealStatuses(Collections.singletonList(TaskStatus.SUBMITTED.name()));

        assertEquals(expected, actual);
    }

    @Test
    public void testPending() {
        Set<TaskStatus> expected = new HashSet<>(Collections.singletonList(TaskStatus.PENDING));
        Set<TaskStatus> actual = TaskStatus.expandAggregatedStatusesToRealStatuses(Collections.singletonList(TaskStatus.PENDING.name()));

        assertEquals(expected, actual);
    }

    @Test
    public void testRunning() {
        Set<TaskStatus> expected = TaskStatus.RUNNING_TASKS;
        Set<TaskStatus> actual = TaskStatus.expandAggregatedStatusesToRealStatuses(Collections.singletonList(TaskStatus.RUNNING.name()));

        assertEquals(expected, actual);
    }

    @Test
    public void testFinished() {
        Set<TaskStatus> expected = TaskStatus.FINISHED_TASKS;
        Set<TaskStatus> actual = TaskStatus.expandAggregatedStatusesToRealStatuses(Collections.singletonList(TaskStatus.FINISHED.name()));

        assertEquals(expected, actual);
    }

    @Test
    public void testError() {
        Set<TaskStatus> expected = TaskStatus.ERROR_TASKS;
        Set<TaskStatus> actual = TaskStatus.expandAggregatedStatusesToRealStatuses(Collections.singletonList("Error"));

        assertEquals(expected, actual);
    }

    @Test
    public void testEmpty() {
        Set<TaskStatus> expected = Collections.emptySet();
        Set<TaskStatus> actual = TaskStatus.expandAggregatedStatusesToRealStatuses(Collections.singletonList(""));

        assertEquals(expected, actual);
    }

    @Test
    public void testWrong() {
        Set<TaskStatus> expected = Collections.emptySet();
        Set<TaskStatus> actual = TaskStatus.expandAggregatedStatusesToRealStatuses(Arrays.asList("wrong", "wrong2"));

        assertEquals(expected, actual);
    }

    @Test
    public void testWrongAndFinished() {
        Set<TaskStatus> expected = TaskStatus.FINISHED_TASKS;
        Set<TaskStatus> actual = TaskStatus.expandAggregatedStatusesToRealStatuses(Arrays.asList("wrong",
                                                                                                 TaskStatus.FINISHED.name(),
                                                                                                 "wrong2"));

        assertEquals(expected, actual);
    }

    @Test
    public void testRunningAndFinished() {
        Set<TaskStatus> expected = new HashSet<>();
        expected.addAll(TaskStatus.RUNNING_TASKS);
        expected.addAll(TaskStatus.FINISHED_TASKS);
        Set<TaskStatus> actual = TaskStatus.expandAggregatedStatusesToRealStatuses(Arrays.asList(TaskStatus.RUNNING.name(),
                                                                                                 TaskStatus.FINISHED.name()));

        assertEquals(expected, actual);
    }

    @Test
    public void testRunningAndFinishedAndRunning() {
        Set<TaskStatus> expected = new HashSet<>();
        expected.addAll(TaskStatus.RUNNING_TASKS);
        expected.addAll(TaskStatus.FINISHED_TASKS);
        Set<TaskStatus> actual = TaskStatus.expandAggregatedStatusesToRealStatuses(Arrays.asList(TaskStatus.RUNNING.name(),
                                                                                                 TaskStatus.FINISHED.name(),
                                                                                                 TaskStatus.RUNNING.name()));

        assertEquals(expected, actual);
    }

    @Test
    public void testEverything() {
        Set<TaskStatus> expected = new HashSet<>();
        expected.addAll(TaskStatus.RUNNING_TASKS);
        expected.addAll(TaskStatus.FINISHED_TASKS);
        expected.add(TaskStatus.PENDING);
        Set<TaskStatus> actual = TaskStatus.expandAggregatedStatusesToRealStatuses(Arrays.asList(TaskStatus.PENDING.name(),
                                                                                                 "nonsense",
                                                                                                 TaskStatus.FINISHED.name(),
                                                                                                 "morenonsense",
                                                                                                 TaskStatus.RUNNING.name(),
                                                                                                 TaskStatus.PENDING.name(),
                                                                                                 "nonsense",
                                                                                                 TaskStatus.FINISHED.name(),
                                                                                                 "morenonsense",
                                                                                                 TaskStatus.RUNNING.name()));

        assertEquals(expected, actual);
    }

}
