/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;
public class Executor {
    final static private Executor singleton = new Executor();
    private List<Thread> threads;

    private Executor() {
        GCMD_LOGGER.trace("Executor started");
        threads = new ArrayList<Thread>();
    }

    static public synchronized Executor getExecutor() {
        return singleton;
    }

    public void submit(String command) {
        GCMD_LOGGER.trace("Command submited: " + command);
        try {
            System.out.println("executing command=" + command);

            Process p = Runtime.getRuntime()
                               .exec(new String[] { "sh", "-c", command });

            InputStreamMonitor stdoutM = new InputStreamMonitor(MonitorType.STDOUT,
                    p.getInputStream(), command);
            InputStreamMonitor stderrM = new InputStreamMonitor(MonitorType.STDERR,
                    p.getErrorStream(), command);
            stderrM.start();
            stdoutM.start();
            threads.add(stdoutM);
            threads.add(stderrM);
        } catch (IOException e) {
            GCMD_LOGGER.warn("Cannot execute: " + command, e);
        }
    }

    public void awaitTermination() throws InterruptedException {
        for (Thread t : threads) {
            t.join();
        }
    }
    private enum MonitorType {STDOUT,
        STDERR;
    }
    private class InputStreamMonitor extends Thread {
        MonitorType type;
        InputStream stream;
        String cmd;

        public InputStreamMonitor(MonitorType type, InputStream stream,
            String cmd) {
            GCMD_LOGGER.trace("Monitor started: " + type.name() + " " + cmd);
            this.type = type;
            this.stream = stream;
            this.cmd = cmd;
        }

        @Override
        public void run() {
            try {
                BufferedReader br;
                String line;

                br = new BufferedReader(new InputStreamReader(stream));
                while ((line = br.readLine()) != null) {
                    GCMD_LOGGER.info(line);
                }
                GCMD_LOGGER.trace("Monitor exited: " + type.name() + " " + cmd);
            } catch (IOException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }
}
