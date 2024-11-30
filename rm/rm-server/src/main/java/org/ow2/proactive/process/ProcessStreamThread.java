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

import io.github.pixee.security.BoundedLineReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


public class ProcessStreamThread extends Thread {

    private static final Logger logger = Logger.getLogger(ProcessStreamThread.class);

    private final InputStream stream;

    private final String outputPrefix;

    private final boolean printOutput;

    private final boolean saveOutput;

    private final int maxLines = PAResourceManagerProperties.RM_INFRASTRUCTURE_PROCESS_OUTPUT_MAX_LINES.getValueAsInt();

    private volatile long storedLines = 0;

    private List<String> output;

    public ProcessStreamThread(InputStream stream, String outputPrefix, boolean printOutput, boolean saveOutput) {
        setDaemon(true);
        this.stream = stream;
        this.outputPrefix = outputPrefix;
        this.printOutput = printOutput;
        this.saveOutput = saveOutput;
        if (saveOutput) {
            output = new ArrayList<>(maxLines);
        }
    }

    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        try {
            while ((line = BoundedLineReader.readLine(reader, 5_000_000)) != null) {
                String outputLine = outputPrefix + line;
                if (printOutput) {
                    System.out.println(outputLine);
                }
                if (saveOutput && storedLines < maxLines) {
                    synchronized (output) {
                        output.add(line);
                        storedLines++;
                    }
                }
            }
        } catch (IOException e) {
            logger.debug("Error during stream reading: " + e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                logger.debug("Error during stream closing: " + e);
            }
        }
    }

    public List<String> getOutput() {
        synchronized (output) {
            return new ArrayList<>(output);
        }
    }
}
