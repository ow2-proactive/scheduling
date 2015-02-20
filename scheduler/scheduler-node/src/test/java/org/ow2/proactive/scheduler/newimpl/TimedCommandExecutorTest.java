package org.ow2.proactive.scheduler.newimpl;

import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TimedCommandExecutorTest {

    public class TimedCommandExecutorImpl extends TimedCommandExecutor {

        public int factor = 2;

        /**
         * Executes a command.
         *
         * @param outputSink Standard output.
         * @param errorSink  Error output.
         * @param command    Command represented as a String array.
         * @return The exit code of program.
         */
        @Override
        public int executeCommand(PrintStream outputSink, PrintStream errorSink, String... command)
                throws FailedExecutionException, InterruptedException {
            // Just sleep longer
            Thread.sleep(super.getCommandMaximumTime() * factor);

            return 0;
        }
    }

    @Test(expected = InterruptedException.class)
    public void timeoutCommandTest() throws FailedExecutionException, InterruptedException {
        TimedCommandExecutorImpl timedCommand = new TimedCommandExecutorImpl();
        timedCommand.factor = 2; //Wait two times longer than timeout

        timedCommand.executeTimedCommand(null, null, (String[]) null);
    }

    @Test
    public void noTimeoutCommandTest() throws FailedExecutionException, InterruptedException {

        TimedCommandExecutorImpl timedCommand = new TimedCommandExecutorImpl();
        timedCommand.factor = 0;

        // Just return 0
        assertEquals(0, timedCommand.executeCommand(null, null, (String[]) null));
    }

    @Test
    public void restoreInterruptTrue() throws FailedExecutionException, InterruptedException {
        TimedCommandExecutorImpl timedCommand = new TimedCommandExecutorImpl();
        timedCommand.factor = 0;

        // Interrupt
        Thread.currentThread().interrupt();

        // Just return 0
        assertEquals(0, timedCommand.executeTimedWhileInterrupted(null, null, (String[]) null));

        assertTrue("Interrupt status must be kept after executing while interrupted.", Thread.currentThread()
                .isInterrupted());
    }

    @Test
    public void restoreInterruptFalse() throws FailedExecutionException, InterruptedException {
        TimedCommandExecutorImpl timedCommand = new TimedCommandExecutorImpl();
        timedCommand.factor = 0;

        // Just return 0
        assertEquals(0, timedCommand.executeTimedWhileInterrupted(null, null, (String[]) null));

        assertFalse("Interrupt status must be kept after executing while interrupted.", Thread
                .currentThread().isInterrupted());
    }

    @Test(expected = InterruptedException.class)
    public void restoreInterruptTimeoutFalse() throws FailedExecutionException, InterruptedException {
        TimedCommandExecutorImpl timedCommand = new TimedCommandExecutorImpl();
        timedCommand.factor = 2;

        // Just return 0
        assertEquals(0, timedCommand.executeTimedWhileInterrupted(null, null, (String[]) null));

    }

    @Test(expected = InterruptedException.class)
    public void restoreInterruptTimeoutTrue() throws FailedExecutionException, InterruptedException {
        TimedCommandExecutorImpl timedCommand = new TimedCommandExecutorImpl();
        timedCommand.factor = 2;

        // Interrupt
        Thread.currentThread().interrupt();

        // Just return 0
        assertEquals(0, timedCommand.executeTimedWhileInterrupted(null, null, (String[]) null));

    }
}
