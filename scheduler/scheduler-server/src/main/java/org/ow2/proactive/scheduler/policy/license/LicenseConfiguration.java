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
package org.ow2.proactive.scheduler.policy.license;

import java.util.ArrayList;
import java.util.Properties;

import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.utils.FileUtils;


public class LicenseConfiguration extends ArrayList<Object> {

    private String path;

    private static LicenseConfiguration configuration;

    private LicenseConfiguration() {
        path = PASchedulerProperties.LICENSE_SCHEDULING_POLICY_CONFIGURATION.getValueAsString();
    }

    public static synchronized LicenseConfiguration getConfiguration() {
        if (configuration == null) {
            configuration = new LicenseConfiguration();
        }
        return configuration;
    }

    public String getPath() {
        if (path != null && path.length() > 0) {
            path = PASchedulerProperties.getAbsolutePath(path);
        }
        return path;
    }

    public Properties getProperties() {
        return FileUtils.resolvePropertiesFile(getPath());
    }
}
