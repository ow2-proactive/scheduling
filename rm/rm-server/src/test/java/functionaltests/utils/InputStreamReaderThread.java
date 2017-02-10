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


/**
 * Thread reads text from the given input stream and prints it into the System.out.
 * 
 * @author ProActive team
 *
 */
public class InputStreamReaderThread extends Thread {

    private final InputStream stream;

    private final String outputPrefix;

    public InputStreamReaderThread(InputStream stream, String outputPrefix) {
        setDaemon(true);
        this.stream = stream;
        this.outputPrefix = outputPrefix;
    }

    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                System.out.println(outputPrefix + line);
            }
        } catch (IOException ignored) {
            // ignored
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                System.out.println("Error during stream closing: " + e);
            }
        }
    }

}
