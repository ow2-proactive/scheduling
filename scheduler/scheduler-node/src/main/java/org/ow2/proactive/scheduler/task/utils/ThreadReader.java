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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;


/** Pipe between two streams */
public class ThreadReader implements Runnable {

    private BufferedReader in;

    private PrintStream out;

    public ThreadReader(BufferedReader in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public void run() {
        Thread readerThread = new Thread() {
            @Override
            public void run() {
                String str;

                try {
                    while ((str = in.readLine()) != null) {
                        out.println(str);
                    }
                } catch (IOException e) {
                    //nothing to do, socket is dead
                }
            }
        };
        readerThread.setDaemon(true);
        readerThread.start();

        while (readerThread.isAlive()) {
            try {
                readerThread.join(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

}
