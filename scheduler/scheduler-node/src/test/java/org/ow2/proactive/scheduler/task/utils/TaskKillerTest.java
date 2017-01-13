package org.ow2.proactive.scheduler.task.utils;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;

public class TaskKillerTest {

    private String taskKillerCleanupTimePropertyName = RMNodeStarter.SECONDS_TASK_CLEANUP_TIMEOUT_PROP_NAME;

    @Test
    public void testThatTaskKillerInterruptsThreadThenWaitsUntilInterruptingAgain() {
        KilledThread testThreadToBeInterrupted = new KilledThread();
        testThreadToBeInterrupted.start();

        System.setProperty(this.taskKillerCleanupTimePropertyName, "5");
        TaskKiller taskKiller = new TaskKiller(testThreadToBeInterrupted);

        assertThat("Task Killer must not interrupt thread before kill() is called",
                testThreadToBeInterrupted.isInterruptedOnce, is(false));
        assertThat("Task Killer must not interrupt thread before kill() is called",
                testThreadToBeInterrupted.isInterruptedMoreThanOnce, is(false));

        startKilling(taskKiller);
        // Wait a second for killing thread to start
        waitOrFailTest(1000);

        assertThat("Task Killer must interrupt once if kill() is called and then wait for the timeout which is set to 10 seconds.",
                testThreadToBeInterrupted.isInterruptedOnce, is(true));
        assertThat("Task Killer must only interrupt once (not twice) after kill() is called and then wait for the timeout which is set to 10 seconds.",
                testThreadToBeInterrupted.isInterruptedMoreThanOnce, is(false));

        // Wait 5 seconds for killing timeout to be exceeded
        waitOrFailTest(5000);

        assertThat("Task Killer must have interrupted at least twice after timeout has passed",
                testThreadToBeInterrupted.isInterruptedMoreThanOnce, is(true));

        // Cleanup - remove system property
        System.clearProperty(this.taskKillerCleanupTimePropertyName);
    }

    @Test
    public void testThatTaskKillerInterruptsThreadWithDefaultTimeoutWhenPropertyIsSetToGarbage() {
        KilledThread testThreadToBeInterrupted = new KilledThread();
        testThreadToBeInterrupted.start();

        System.setProperty(this.taskKillerCleanupTimePropertyName, "A34Gf");
        TaskKiller taskKiller = new TaskKiller(testThreadToBeInterrupted);

        assertThat("Task Killer must not interrupt thread before kill() is called",
                testThreadToBeInterrupted.isInterruptedOnce, is(false));
        assertThat("Task Killer must not interrupt thread before kill() is called",
                testThreadToBeInterrupted.isInterruptedMoreThanOnce, is(false));

        startKilling(taskKiller);
        // Wait a second for killing thread to start
        waitOrFailTest(1000);

        assertThat("Task Killer must interrupt once if kill() is called and then wait for the timeout which is set to 10 seconds.",
                testThreadToBeInterrupted.isInterruptedOnce, is(true));
        assertThat("Task Killer must only interrupt once (not twice) after kill() is called and then wait for the timeout which is set to 10 seconds.",
                testThreadToBeInterrupted.isInterruptedMoreThanOnce, is(false));

        // Wait 10 seconds for killing timeout to be exceeded
        waitOrFailTest(10000);

        assertThat("Task Killer must have interrupted at least twice after timeout has passed",
                testThreadToBeInterrupted.isInterruptedMoreThanOnce, is(true));

        // Cleanup - remove system property
        System.clearProperty(this.taskKillerCleanupTimePropertyName);
    }

    @Test
    public void testThatTaskKillerInterruptsThreadThenWaitsUntilInterruptingAgainWithoutSystemPropertySet() {
        KilledThread testThreadToBeInterrupted = new KilledThread();
        testThreadToBeInterrupted.start();

        TaskKiller taskKiller = new TaskKiller(testThreadToBeInterrupted);

        assertThat("Task Killer must not interrupt thread before kill() is called",
                testThreadToBeInterrupted.isInterruptedOnce, is(false));
        assertThat("Task Killer must not interrupt thread before kill() is called",
                testThreadToBeInterrupted.isInterruptedMoreThanOnce, is(false));

        startKilling(taskKiller);
        // Wait a second for killing thread to start
        waitOrFailTest(1000);

        assertThat("Task Killer must  interrupt once if kill() is called and then wait for the timeout which is set to 10 seconds.",
                testThreadToBeInterrupted.isInterruptedOnce, is(true));
        assertThat("Task Killer must only interrupt once (not twice) after kill() is called and then wait for the timeout which is set to 10 seconds.",
                testThreadToBeInterrupted.isInterruptedMoreThanOnce, is(false));

        // Wait 10 seconds for killing timeout to be exceeded
        waitOrFailTest(10000);

        assertThat("Task Killer must have interrupted at least twice after timeout has passed",
                testThreadToBeInterrupted.isInterruptedMoreThanOnce, is(true));
    }

    @Test
    public void testThatTaskKillerInterruptsThreadImmediatelyWhenSetToZero() {
        KilledThread testThreadToBeInterrupted = new KilledThread();
        testThreadToBeInterrupted.start();

        System.setProperty(this.taskKillerCleanupTimePropertyName, "0");
        TaskKiller taskKiller = new TaskKiller(testThreadToBeInterrupted);

        assertThat("Task Killer must not interrupt thread before kill() is called",
                testThreadToBeInterrupted.isInterruptedOnce, is(false));
        assertThat("Task Killer must not interrupt thread before kill() is called",
                testThreadToBeInterrupted.isInterruptedMoreThanOnce, is(false));

        startKilling(taskKiller);
        // Wait 100 milliseconds for killing thread to start
        waitOrFailTest(100);

        assertThat("Task Killer must interrupt once if kill() is called and then wait for the timeout which is set to 10 seconds.",
                testThreadToBeInterrupted.isInterruptedOnce, is(true));
        assertThat("Task Killer must only interrupt once (not twice) after kill() is called and then wait for the timeout which is set to 10 seconds.",
                testThreadToBeInterrupted.isInterruptedMoreThanOnce, is(true));

        // Cleanup - remove system property
        System.clearProperty(this.taskKillerCleanupTimePropertyName);
    }

    @Test
    public void testThatTaskKillerInterruptsThreadImmediatelyWhenSetToNegativeNumber() {
        KilledThread testThreadToBeInterrupted = new KilledThread();
        testThreadToBeInterrupted.start();

        System.setProperty(this.taskKillerCleanupTimePropertyName, "-20");
        TaskKiller taskKiller = new TaskKiller(testThreadToBeInterrupted);

        assertThat("Task Killer must not interrupt thread before kill() is called",
                testThreadToBeInterrupted.isInterruptedOnce, is(false));
        assertThat("Task Killer must not interrupt thread before kill() is called",
                testThreadToBeInterrupted.isInterruptedMoreThanOnce, is(false));

        startKilling(taskKiller);
        // Wait 100 milliseconds for killing thread to start
        waitOrFailTest(100);

        assertThat("Task Killer must interrupt once if kill() is called and then wait for the timeout which is set to 10 seconds.",
                testThreadToBeInterrupted.isInterruptedOnce, is(true));
        assertThat("Task Killer must only interrupt once (not twice) after kill() is called and then wait for the timeout which is set to 10 seconds.",
                testThreadToBeInterrupted.isInterruptedMoreThanOnce, is(true));

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
        } .start();
    }

    class KilledThread extends Thread{

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