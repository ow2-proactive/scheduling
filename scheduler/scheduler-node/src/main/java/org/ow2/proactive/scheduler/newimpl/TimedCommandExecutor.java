/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * Tobias Wiens
 */
package org.ow2.proactive.scheduler.newimpl;

import java.io.PrintStream;



public abstract class TimedCommandExecutor implements CommandExecutor {

    // Maximum milliseconds for a timed command to exit
    private int commandMaximumTime = 5000;

    public int getCommandMaximumTime() {
        return commandMaximumTime;
    }

    public void setCommandMaximumTime(int commandMaximumTime) {
        this.commandMaximumTime = commandMaximumTime;
    }

    /**
     * Executes a command.
     * @param outputSink Standard output.
     * @param errorSink Error output.
     * @param command Command represented as a String array.
     * @return The exit code of program.
     */
    public abstract int executeCommand(PrintStream outputSink, PrintStream errorSink, String... command)
            throws FailedExecutionException, InterruptedException;

    /**
     * Run command which will be killed after a time limit. When either outputSink or errorSink is null, neither of
     * the streams will be served with input.
     *
     * @param outputSink outputSink Standard output.
     * @param errorSink  Error output.
     * @param command    Command which will be executed.
     * @return Exit code of command.
     * @throws InterruptedException         Thrown when thread is interrupted during command execution or
     *                                      when execution takes too long and timeout occurs.
     */
    public int executeTimedCommand(PrintStream outputSink, PrintStream errorSink, String... command)
            throws InterruptedException, FailedExecutionException {

        // Create walltimer
         org.ow2.proactive.scheduler.newimpl.WallTimer walltimer = new org.ow2.proactive.scheduler.newimpl.WallTimer(commandMaximumTime, Thread.currentThread());
        // Execute command
        int returnCode = this.executeCommand(outputSink, errorSink, command);
        // Stop timer
        walltimer.stop();

        return returnCode;

    }

    /**
     * Run timed command while thread is already interrupted. This resets the current interrupted flag
     * during command execution and restores the flag afterwards.
     * When either outputSink or errorSink is null, neither of
     * the streams will be served with input.
     *
     * @param outputSink outputSink Standard output.
     * @param errorSink  Error output.
     * @param command    Command which will be executed.
     * @return Exit code of command
     * @throws InterruptedException
     * @throws FailedExecutionException

     */
    public int executeTimedWhileInterrupted(PrintStream outputSink, PrintStream errorSink, String... command)
            throws FailedExecutionException, InterruptedException {

        // The thread might have been interrupted, but it has to wait for the next command to finish.
        // Save the current interrupted state and reset interrupt flag
        boolean isInterrupted = Thread.interrupted();

        // Execute command
        int returnCode = this.executeTimedCommand(outputSink, errorSink, command);

        // Restore interrupt state of thread
        if (isInterrupted)
            Thread.currentThread().interrupt();

        return returnCode;
    }
}
