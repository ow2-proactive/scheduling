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
package functionaltests.utils;

import org.ow2.proactive.core.properties.PASharedProperties;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.tests.ProActiveTest;


public final class Credentials extends ProActiveTest {

    private static final String DEFAULT_USER_NAME = "admin";

    private static org.ow2.proactive.authentication.crypto.Credentials defaultCredentials;

    public static org.ow2.proactive.authentication.crypto.Credentials getDefaultCredentials() throws Exception {
        if (defaultCredentials == null) {
            defaultCredentials = org.ow2.proactive.authentication.crypto.Credentials.createCredentials(DEFAULT_USER_NAME,
                                                                                                       "admin",
                                                                                                       PASharedProperties.getAbsolutePath(PASharedProperties.AUTH_PUBKEY_PATH.getValueAsString()));
        }

        return defaultCredentials;
    }

}
