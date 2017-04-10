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
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class PortalConfiguration {

    public static String SCHEDULER_URL = "scheduler.url";

    public static String SCHEDULER_CACHE_LOGIN = "scheduler.cache.login";

    public static String SCHEDULER_CACHE_PASSWORD = "scheduler.cache.password";

    public static String SCHEDULER_CACHE_CREDENTIAL = "scheduler.cache.credential";

    public static String SCHEDULER_LOGFORWARDINGSERVICE_PROVIDER = "scheduler.logforwardingservice.provider";

    public static String RM_URL = "rm.url";

    public static String RM_CACHE_LOGIN = "rm.cache.login";

    public static String RM_CACHE_PASSWORD = "rm.cache.password";

    public static String RM_CACHE_CREDENTIAL = "rm.cache.credential";

    public static String RM_CACHE_REFRESHRATE = "rm.cache.refreshrate";

    public static String NOVNC_ENABLED = "novnc.enabled";

    public static String NOVNC_PORT = "novnc.port";

    public static String NOVNC_SECURED = "novnc.secured";

    public static String NOVNC_KEYSTORE = "novnc.keystore";

    public static String NOVNC_PASSWORD = "novnc.password";

    public static String NOVNC_KEYPASSWORD = "novnc.keypassword";

    public static String JOBPLANNER_URL = "jp.url";

    private static Properties properties;

    public static void load(InputStream f) throws IOException {
        properties = new Properties();
        properties.load(f);
        properties.putAll(System.getProperties());
    }

    public static Properties getProperties() {
        return properties;
    }

    /**
     * convert a job id to the location where the archive has been stored
     * @param jobId the job id
     * @return a string representing the path to the archive file
     */
    public static String jobIdToPath(String jobId) {
        return System.getProperty("java.io.tmpdir") + File.separator + "job_" + jobId + ".zip";
    }

}
