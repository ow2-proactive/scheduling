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
package org.ow2.proactive.resourcemanager.authentication;

import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.KeycloakLoginModule;


/**
 * RM login module that performs user authentication and authorization against Keycloak.
 * It loads Keycloak configuration from the file config/keycloak.cfg.
 * It extends KeycloakLoginModule to perform user authentication and authorization.
 *
 * @author The ActiveEon Team
 * @since ProActive Scheduling 14.0
 */
public class RMKeycloakLoginModule extends KeycloakLoginModule {

    /**
     * Returns logger for authentication
     *
     * @return logger for authentication
     */
    public Logger getLogger() {
        return Logger.getLogger(RMKeycloakLoginModule.class);
    }
}
