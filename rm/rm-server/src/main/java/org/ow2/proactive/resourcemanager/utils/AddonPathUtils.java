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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


/**
 * @author ActiveEon Team
 * @since 03/12/19
 */
public class AddonPathUtils {

    public static final String ADDONS_BASE_DIRNAME = "addons";

    public static String getAddonsBasePath() {
        return getRmHome() + File.separator + ADDONS_BASE_DIRNAME;
    }

    public static String getRmHome() {
        return PAResourceManagerProperties.RM_HOME.getValueAsString();
    }

    public static String getPathInAddons(String addonName) {
        return getAddonsBasePath() + File.separator + addonName;
    }

    /**
     * Check whether exist a directory named dirName inside the addons directory
     *
     * @param dirName
     * @return
     */
    public static boolean existDirectoryInAddons(String dirName) {
        File infraAddonDir = new File(getPathInAddons(dirName));
        return infraAddonDir.exists() && infraAddonDir.isDirectory();
    }

    /**
     * Find all the jar files in the directory
     * @param jarDirPath the path of the directory where located the jar files
     * @return the URL array of jar files under the directory
     */
    public static URL[] findJarsInPath(String jarDirPath) {
        Set<URL> jarSet = new HashSet<>();

        try (DirectoryStream<Path> jars = Files.newDirectoryStream(new File(jarDirPath).toPath(), "*.jar")) {
            for (Path jar : jars) {
                jarSet.add(jar.toUri().toURL());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error during getting the jar file from the path: " + jarDirPath, e);
        }

        return jarSet.toArray(new URL[0]);
    }
}
