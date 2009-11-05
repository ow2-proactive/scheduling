/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.util.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import org.ow2.proactive.scheduler.common.task.executable.Executable;


/** Pipe between two streams */
public class ThreadReader implements Runnable {
    private BufferedReader in;
    private PrintStream out;
    private Executable executable;

    /**
     * Create a new instance of ThreadReader.
     *
     * @param in input stream.
     * @param out output stream
     * @param executable Executable that is concerned by the read.
     */
    public ThreadReader(BufferedReader in, PrintStream out, Executable executable) {
        this.in = in;
        this.out = out;
        this.executable = executable;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        final boolean[] stop = new boolean[] { false };

        new Thread() {
            @Override
            public void run() {
                String str = null;
                try {
                    while ((str = in.readLine()) != null) {
                        out.println(str);
                    }
                    stop[0] = true;

                } catch (IOException e) {
                    //nothing to do, socket is dead
                }
            }
        }.start();

        while (!stop[0] && !executable.isKilled()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }

        //        String str = null;
        //
        //        try {
        //            while ((str = in.readLine()) != null && !executable.isKilled()) {
        //                out.println(str);
        //            }
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        }
    }
}