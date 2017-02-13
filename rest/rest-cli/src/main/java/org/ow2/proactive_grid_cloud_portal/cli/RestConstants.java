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
package org.ow2.proactive_grid_cloud_portal.cli;

import java.io.File;


public class RestConstants {

    public static final String DFLT_REST_SCHEDULER_URL = "http://localhost:8080/rest";

    public static final String SCHEDULER_RESOURCE_TYPE = "scheduler";

    public static final String RM_RESOURCE_TYPE = "rm";

    public static final String DFLT_SESSION_DIR = System.getProperty("user.home") + File.separator + ".proactive";

    public static final String DEFAULT_CREDENTIALS_PATH = "config/authentication/admin_user.cred";

    public static final String DFLT_SESSION_FILE_EXT = "-session-id";

    public static final int DFLT_PAGINATION_SIZE = 50;

    private RestConstants() {
    }
}
