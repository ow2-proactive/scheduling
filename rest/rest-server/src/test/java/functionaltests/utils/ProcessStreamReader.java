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
package functionaltests.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.log4j.Logger;


public class ProcessStreamReader extends Thread {

    private static Logger logger = Logger.getLogger(ProcessStreamReader.class);

    private String outputPrefix;

    private InputStream in;

    public ProcessStreamReader(String type, InputStream is) {
        this.outputPrefix = type;
        this.in = is;
    }

    @Override
    public void run() {

        BufferedReader buffered = new BufferedReader(new InputStreamReader(in));
        try {
            String line;
            while ((line = buffered.readLine()) != null) {
                logger.info(outputPrefix + line);
            }
        } catch (IOException ignored) {
            // end Of Stream When Process Killed
        }
    }

}
