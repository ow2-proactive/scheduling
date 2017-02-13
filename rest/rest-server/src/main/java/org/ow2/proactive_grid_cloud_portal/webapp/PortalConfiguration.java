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

    public static String scheduler_url = "scheduler.url";

    public static String scheduler_cache_login = "scheduler.cache.login";

    public static String scheduler_cache_password = "scheduler.cache.password";

    public static String scheduler_cache_credential = "scheduler.cache.credential";

    public static String scheduler_logforwardingservice_provider = "scheduler.logforwardingservice.provider";

    public static String rm_url = "rm.url";

    public static String rm_cache_login = "rm.cache.login";

    public static String rm_cache_password = "rm.cache.password";

    public static String rm_cache_credential = "rm.cache.credential";

    public static String rm_cache_refreshrate = "rm.cache.refreshrate";

    public static String novnc_enabled = "novnc.enabled";

    public static String novnc_port = "novnc.port";

    public static String novnc_secured = "novnc.secured";

    public static String novnc_keystore = "novnc.keystore";

    public static String novnc_password = "novnc.password";

    public static String novnc_keypassword = "novnc.keypassword";

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
