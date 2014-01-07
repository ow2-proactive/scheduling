/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.utils;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;


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
