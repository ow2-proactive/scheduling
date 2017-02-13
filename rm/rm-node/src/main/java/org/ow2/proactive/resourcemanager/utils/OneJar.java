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
package org.ow2.proactive.resourcemanager.utils;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.io.File;
import java.util.List;


public class OneJar {
    public static boolean isRunningWithOneJar() {
        return System.getProperty("one-jar.expand.dir") != null;
    }

    /**
     * When running with OneJar, jars are expanded to a temporary folder to be accessed by sub processes.
     * It is used for forked tasks for instance.
     *
     * @return A list of classpath elements to add to the classpath of sub processes.
     */
    public static List<String> getClasspath() {
        if (isRunningWithOneJar()) {
            String expandedDir = System.getProperty("one-jar.expand.dir", "");
            return asList(expandedDir + File.separatorChar + "lib" + File.separatorChar + "*",
                          expandedDir + File.separatorChar + "main" + File.separatorChar + "*");
        } else {
            return emptyList();
        }
    }
}
