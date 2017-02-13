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
package org.ow2.proactive.process;

import java.io.IOException;
import java.util.List;


public class ProcessExecutor {

    private final String commandName;

    private final List<String> command;

    private final boolean printOutput;

    private final boolean saveOutput;

    private Process process;

    private ProcessStreamThread outReader;

    private ProcessStreamThread errReader;

    private ProcessWaiterThread waiterThread;

    public ProcessExecutor(String commandName, List<String> command, boolean printOutput, boolean saveOutput) {
        this.commandName = commandName;
        this.command = command;
        this.printOutput = printOutput;
        this.saveOutput = saveOutput;
    }

    public void start() throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        process = builder.start();

        waiterThread = new ProcessWaiterThread(process);
        waiterThread.start();

        outReader = new ProcessStreamThread(process.getInputStream(),
                                            String.format("[%s OUT] ", commandName),
                                            printOutput,
                                            saveOutput);
        outReader.start();
        errReader = new ProcessStreamThread(process.getErrorStream(),
                                            String.format("[%s ERR] ", commandName),
                                            printOutput,
                                            saveOutput);
        errReader.start();
    }

    public List<String> getOutput() {
        if (!saveOutput) {
            throw new IllegalStateException("This ProcessExecutor doesn't save output");
        }
        return outReader.getOutput();
    }

    public List<String> getErrorOutput() {
        if (!saveOutput) {
            throw new IllegalStateException("This ProcessExecutor doesn't save output");
        }
        return errReader.getOutput();
    }

    public void killProcess() {
        process.destroy();
    }

    public boolean executeAndWaitCompletion(long timeout, boolean logOnError) throws InterruptedException {
        try {
            start();
        } catch (IOException e) {
            System.out.println("Failed to start process: " + e);
            return false;
        }
        waiterThread.join(timeout);
        boolean result;
        if (!waiterThread.isProcessFinished()) {
            System.out.println("Process didn't finish in " + timeout + "ms, killing it");
            process.destroy();
            result = false;
        } else {
            int exitCode = waiterThread.getProcessExitCode();
            if (exitCode != 0 && logOnError) {
                System.out.println(String.format("Process %s finished with code %d", commandName, exitCode));
            }
            result = waiterThread.getProcessExitCode() == 0;
        }
        if (!result && logOnError) {
            System.out.println("Process execution failed (command: " + command + ")");
            System.out.println("Process output:" + getOutput());
            System.out.println("Process error output:" + getErrorOutput());
        }
        return result;
    }

    public boolean isProcessFinished() {
        return waiterThread.isProcessFinished();
    }

    public boolean isRunningRemotely() {
        return false;
    }

    public int getExitCode() {
        return waiterThread.getProcessExitCode();
    }

}
