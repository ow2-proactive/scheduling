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
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

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
     * Find all the jar files in the path
     * @param path
     * @return
     */
    public static URL[] findJarsInPath(String path) {
        File[] jarArray = new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        Set<URL> jarNames = new HashSet<>();
        for (File jarFile : jarArray) {
            try {
                jarNames.add(jarFile.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Malformed URL: " + jarFile);
            }
        }
        return jarNames.toArray(new URL[0]);
    }
}
