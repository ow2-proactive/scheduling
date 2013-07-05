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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class ProcessStreamThread extends Thread {

    private final InputStream stream;

    private final String outputPrefix;

    private final boolean printOutput;

    private final boolean saveOutput;

    private List<String> output;

    public ProcessStreamThread(InputStream stream, String outputPrefix, boolean printOutput,
            boolean saveOutput) {
        setDaemon(true);
        this.stream = stream;
        this.outputPrefix = outputPrefix;
        this.printOutput = printOutput;
        this.saveOutput = saveOutput;
        if (saveOutput) {
            output = new ArrayList<String>();
        }
    }

    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String outputLine = outputPrefix + line;
                if (printOutput) {
                    System.out.println(outputLine);
                }
                if (saveOutput) {
                    synchronized (output) {
                        output.add(line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error during stream reading: " + e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                System.out.println("Error during stream closing: " + e);
            }
        }
    }

    public List<String> getOutput() {
        synchronized (output) {
            return new ArrayList<String>(output);
        }
    }
}
