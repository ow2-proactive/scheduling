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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


public class PortalConfiguration {

    public static String scheduler_url = "scheduler.url";
    public static String scheduler_cache_login = "scheduler.cache.login";
    public static String scheduler_cache_password = "scheduler.cache.password";
    public static String scheduler_cache_credential = "scheduler.cache.credential";
    public static String scheduler_cache_refreshrate = "scheduler.cache.refreshrate";
    public static String scheduler_logforwardingservice_provider = "scheduler.logforwardingservice.provider";
    public static String rm_url = "rm.url";
    public static String REST_API_VERSION = "0.80-3.0.x";

    private static Properties properties;
    public static String lease_renew_rate = "lease.renew.rate";

    public static void load(File f) throws FileNotFoundException, IOException {
        properties = new Properties();
        properties.load(new FileInputStream(f));
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
