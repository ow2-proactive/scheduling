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
package org.ow2.proactive.scheduler.task.utils;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;
import org.ow2.proactive.scheduler.task.utils.task.termination.CleanupTimeoutGetter;
import org.ow2.proactive.scheduler.task.utils.task.termination.TaskKiller;


public class TaskKillerTest {

    private String taskKillerCleanupTimePropertyName = RMNodeStarter.SECONDS_TASK_CLEANUP_TIMEOUT_PROP_NAME;

    @Test
    public void testThatTaskKillerInterruptsThreadThenWaitsUntilInterruptingAgain() {
        KilledThread testThreadToBeInterrupted = new KilledThread();
        testThreadToBeInterrupted.start();

        CleanupTimeoutGetter cleanupTimeoutGetterMock = mock(CleanupTimeoutGetter.class);
        doReturn(5L).when(cleanupTimeoutGetterMock).getCleanupTimeSeconds();

        TaskKiller taskKiller = new TaskKiller(testThreadToBeInterrupted, cleanupTimeoutGetterMock);

        assertThat("Task Killer must not interrupt thread before kill() is called",
                   testThreadToBeInterrupted.isInterruptedOnce,
                   is(false));
        assertThat("Task Killer must not interrupt thread before kill() is called",
                   testThreadToBeInterrupted.isInterruptedMoreThanOnce,
                   is(false));

        startKilling(taskKiller);
        // Wait a second for killing thread to start
        waitOrFailTest(1000);

        assertThat("Task Killer must interrupt once if kill() is called and then wait for the timeout which is set to 10 seconds.",
                   testThreadToBeInterrupted.isInterruptedOnce,
                   is(true));
        assertThat("Task Killer must only interrupt once (not twice) after kill() is called and then wait for the timeout which is set to 10 seconds.",
                   testThreadToBeInterrupted.isInterruptedMoreThanOnce,
                   is(false));

        // Wait 5 seconds for killing timeout to be exceeded
        waitOrFailTest(5000);

        assertThat("Task Killer must have interrupted at least twice after timeout has passed",
                   testThreadToBeInterrupted.isInterruptedMoreThanOnce,
                   is(true));

        // Cleanup - remove system property
        System.clearProperty(this.taskKillerCleanupTimePropertyName);
    }

    @Test
    public void testThatTaskKillerInterruptsThreadThenWaitsUntilInterruptingAgainWithoutSystemPropertySet() {
        KilledThread testThreadToBeInterrupted = new KilledThread();
        testThreadToBeInterrupted.start();

        CleanupTimeoutGetter cleanupTimeoutGetterMock = mock(CleanupTimeoutGetter.class);
        doReturn(10L).when(cleanupTimeoutGetterMock).getCleanupTimeSeconds();

        TaskKiller taskKiller = new TaskKiller(testThreadToBeInterrupted, cleanupTimeoutGetterMock);

        assertThat("Task Killer must not interrupt thread before kill() is called",
                   testThreadToBeInterrupted.isInterruptedOnce,
                   is(false));
        assertThat("Task Killer must not interrupt thread before kill() is called",
                   testThreadToBeInterrupted.isInterruptedMoreThanOnce,
                   is(false));

        startKilling(taskKiller);
        // Wait a second for killing thread to start
        waitOrFailTest(1000);

        assertThat("Task Killer must  interrupt once if kill() is called and then wait for the timeout which is set to 10 seconds.",
                   testThreadToBeInterrupted.isInterruptedOnce,
                   is(true));
        assertThat("Task Killer must only interrupt once (not twice) after kill() is called and then wait for the timeout which is set to 10 seconds.",
                   testThreadToBeInterrupted.isInterruptedMoreThanOnce,
                   is(false));

        // Wait 10 seconds for killing timeout to be exceeded
        waitOrFailTest(10000);

        assertThat("Task Killer must have interrupted at least twice after timeout has passed",
                   testThreadToBeInterrupted.isInterruptedMoreThanOnce,
                   is(true));
    }

    @Test
    public void testThatTaskKillerInterruptsThreadImmediatelyWhenSetToZero() {
        KilledThread testThreadToBeInterrupted = new KilledThread();
        testThreadToBeInterrupted.start();

        CleanupTimeoutGetter cleanupTimeoutGetterMock = mock(CleanupTimeoutGetter.class);
        doReturn(0L).when(cleanupTimeoutGetterMock).getCleanupTimeSeconds();

        TaskKiller taskKiller = new TaskKiller(testThreadToBeInterrupted, cleanupTimeoutGetterMock);

        assertThat("Task Killer must not interrupt thread before kill() is called",
                   testThreadToBeInterrupted.isInterruptedOnce,
                   is(false));
        assertThat("Task Killer must not interrupt thread before kill() is called",
                   testThreadToBeInterrupted.isInterruptedMoreThanOnce,
                   is(false));

        startKilling(taskKiller);
        // Wait 100 milliseconds for killing thread to start
        waitOrFailTest(100);

        assertThat("Task Killer must interrupt once if kill() is called and then wait for the timeout which is set to 10 seconds.",
                   testThreadToBeInterrupted.isInterruptedOnce,
                   is(true));
        assertThat("Task Killer must only interrupt once (not twice) after kill() is called and then wait for the timeout which is set to 10 seconds.",
                   testThreadToBeInterrupted.isInterruptedMoreThanOnce,
                   is(true));

        // Cleanup - remove system property
        System.clearProperty(this.taskKillerCleanupTimePropertyName);
    }

    @Test
    public void testThatTaskKillerInterruptsThreadImmediatelyWhenSetToNegativeNumber() {
        KilledThread testThreadToBeInterrupted = new KilledThread();
        testThreadToBeInterrupted.start();

        CleanupTimeoutGetter cleanupTimeoutGetterMock = mock(CleanupTimeoutGetter.class);
        doReturn(-20L).when(cleanupTimeoutGetterMock).getCleanupTimeSeconds();

        TaskKiller taskKiller = new TaskKiller(testThreadToBeInterrupted, cleanupTimeoutGetterMock);

        assertThat("Task Killer must not interrupt thread before kill() is called",
                   testThreadToBeInterrupted.isInterruptedOnce,
                   is(false));
        assertThat("Task Killer must not interrupt thread before kill() is called",
                   testThreadToBeInterrupted.isInterruptedMoreThanOnce,
                   is(false));

        startKilling(taskKiller);
        // Wait 100 milliseconds for killing thread to start
        waitOrFailTest(100);

        assertThat("Task Killer must interrupt once if kill() is called and then wait for the timeout which is set to 10 seconds.",
                   testThreadToBeInterrupted.isInterruptedOnce,
                   is(true));
        assertThat("Task Killer must only interrupt once (not twice) after kill() is called and then wait for the timeout which is set to 10 seconds.",
                   testThreadToBeInterrupted.isInterruptedMoreThanOnce,
                   is(true));

        // Cleanup - remove system property
        System.clearProperty(this.taskKillerCleanupTimePropertyName);
    }

    private void waitOrFailTest(long millisecondsToWait) {
        try {
            sleep(millisecondsToWait);
        } catch (InterruptedException e) {
            assertThat("Test case must no be interrupted. It tests timing", false, is(true));
        }
    }

    private void startKilling(final TaskKiller taskKiller) {
        new Thread() {
            @Override
            public void run() {
                taskKiller.kill(null);
            }
        }.start();
    }

    class KilledThread extends Thread {

        private long sleepForeverMS = 5000000;

        public boolean isInterruptedOnce = false, isInterruptedMoreThanOnce = false;

        @Override
        public void run() {
            try {
                sleep(sleepForeverMS);
            } catch (InterruptedException e1) {
                isInterruptedOnce = true;
                try {
                    sleep(sleepForeverMS);
                } catch (InterruptedException e2) {
                    isInterruptedMoreThanOnce = true;
                }
            }
        }
    }

}
