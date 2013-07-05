/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
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
