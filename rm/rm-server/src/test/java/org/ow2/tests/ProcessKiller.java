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

            // SCHEDULING-1527: using tskill as fallback
            int exitValue = p.waitFor();
            if (exitValue != 0) {
                try {
                    p = Runtime.getRuntime().exec(new String[] { "tskill", Integer.toString(pid) });
                    p.waitFor();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}
