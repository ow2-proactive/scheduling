/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.objectweb.proactive.utils.OperatingSystem;


/**
 * Kill processes according to their pid.
 *
 * Java does not provides an API to kill processes. Even worse, PID is a
 * non existant concept in Java.
 *
 * This class allows to kill process in Java.
 *
 * @author ProActive team
 * @since  ProActive 5.2.0
 */
public abstract class ProcessKiller {

    /**
     * Get a ProcessKiller suited for the local operating system.
     *
     * @return
     *    A new ProcessKiller object
     * @throws IllegalStateException
     *    If the local operating system is not supported
     */
    static public ProcessKiller get() throws IllegalStateException {
        switch (OperatingSystem.getOperatingSystem()) {
            case unix:
                return new LinuxProcessKiller();
            case windows:
                return new WindowsProcessKiller();
            default:
                throw new IllegalStateException("Unsupported operating system");
        }
    }

    private ProcessKiller() {
    }

    /**
     * Kill a process according its PID.
     *
     * @param pid
     *    The pid
     * @throws IOException
     *    If the process cannot be killed
     * @throws InterruptedException
     *    If interrupted while killing the process
     */
    abstract public void kill(int pid) throws IOException, InterruptedException;

    /**
     * A processKiller for Linux.
     *
     * Should works on all UNIX since kill is defined by POSIX.
     *
     * @author ProActive team
     * @since  ProActive 5.2.0
     */
    private static class LinuxProcessKiller extends ProcessKiller {

        @Override
        public void kill(int pid) throws IOException, InterruptedException {
            ProcessBuilder pb = new ProcessBuilder("kill", "-9", Integer.toString(pid));
            Process p = pb.start();
            p.waitFor();
        }
    }

    /**
     * A processKiller for Windows.
     *
     * @author ProActive team
     * @since  ProActive 5.2.0
     */
    private static class WindowsProcessKiller extends ProcessKiller {

        @Override
        public void kill(int pid) throws IOException, InterruptedException {
            ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/PID", Integer.toString(pid));
            pb.redirectErrorStream(true);
            Process p = pb.start();

            // Read output to avoid deadlock
            Reader r = new InputStreamReader(p.getInputStream());
            BufferedReader br = new BufferedReader(r);
            for (String line = br.readLine(); line != null; line = br.readLine()) {
            }

            p.waitFor();
        }
    }
}
