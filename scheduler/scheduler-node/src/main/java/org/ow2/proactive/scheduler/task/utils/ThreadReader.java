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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
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