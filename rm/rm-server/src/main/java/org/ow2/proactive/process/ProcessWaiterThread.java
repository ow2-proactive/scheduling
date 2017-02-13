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

public class ProcessWaiterThread extends Thread {

    private final Process process;

    private volatile boolean finished;

    private volatile int exitCode;

    private volatile boolean interrupted;

    public ProcessWaiterThread(Process process) {
        setDaemon(true);
        this.process = process;
    }

    public void run() {
        try {
            exitCode = process.waitFor();
            finished = true;
        } catch (InterruptedException e) {
            interrupted = true;
        }
    }

    public boolean isProcessFinished() {
        if (interrupted) {
            throw new IllegalStateException("Waiter thread was interrupted");
        }
        return finished;
    }

    public int getProcessExitCode() {
        if (!finished) {
            throw new IllegalStateException("Process didn't finsh yet");
        }
        if (interrupted) {
            throw new IllegalStateException("Waiter thread was interrupted");
        }
        return exitCode;
    }

}
