/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
package org.ow2.proactive.utils;

import java.io.File;
import java.net.URI;


public class ClasspathUtils {

    /**
     * Used for instance when looking for SCHEDULER_HOME using jars location.
     * We know that the JAR is in dist/lib so we can find SCHEDULER_HOME by using 'dist' as childDirectoryName
     *
     * @param childDirectoryName the name of a directory, we want to the find parent directory of it
     * @return the path to the directory containing childDirectoryName, itself a child of the current classpath
     * or current folder if cannot be found
     */
    public static String findBaseDirectoryFromJarLocation(String childDirectoryName) {
        try {
            URI jarPath = ClasspathUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File parentFile = new File(jarPath);
            while (parentFile.getParentFile() != null) {
                if (parentFile.getParentFile().getName().equals(childDirectoryName)) {
                    return parentFile.getParentFile().getParent();
                }
                parentFile = parentFile.getParentFile();
            }
            return new File(".").getAbsolutePath();
        } catch (Exception e) {
            return new File(".").getAbsolutePath();
        }
    }
}
