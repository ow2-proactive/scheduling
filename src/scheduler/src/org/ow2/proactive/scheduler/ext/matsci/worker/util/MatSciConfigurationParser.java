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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matsci.worker.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;


/**
 * MatSciConfigurationParser
 *
 * @author The ProActive Team
 */
public class MatSciConfigurationParser {

    protected static File findSchedulerHome() throws IOException, URISyntaxException {
        File home = null;
        Class cz = MatSciConfigurationParser.class;
        URL res = cz.getResource("/org/ow2/proactive/scheduler");
        if (res == null)
            throw new IllegalStateException("Can't find resource /org/ow2/proactive/scheduler");
        URLConnection conn = res.openConnection();
        if (conn instanceof JarURLConnection) {
            URL jarFileURL = ((JarURLConnection) conn).getJarFileURL();
            File jarFile = new File(jarFileURL.toURI());
            home = jarFile.getParentFile().getParentFile().getParentFile();
        } else {
            File ext = new File(res.toURI());
            home = ext.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile()
                    .getParentFile();
        }
        return home;
    }

    protected static void checkDir(File dir, File conf) {
        if (!dir.exists()) {
            System.out.println("In " + conf + ", " + dir + " doesn't exist");
        }
        if (!dir.isDirectory()) {
            System.out.println("In " + conf + ", " + dir + " is not a directory");
        }
        if (!dir.canRead()) {
            // When using RunAsMe, we cannot be sure the current user has the right permissions
            System.out.println("In " + conf + ", " + dir + " is not readable");
        }
    }

    protected static void checkFile(File file, File conf, boolean executable) throws Exception {
        if (!file.exists()) {
            System.out.println("In " + conf + ", " + file + " doesn't exist");
        }
        if (!file.isFile()) {
            System.out.println("In " + conf + ", " + file + " is not a file");
        }
        if (!file.canRead()) {
            // When using RunAsMe, we cannot be sure the current user has the right permissions
            System.out.println("In " + conf + ", " + file + " is not readable");
        }
    }
}
