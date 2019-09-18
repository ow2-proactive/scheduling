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
package org.ow2.proactive.scheduler.rest;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive_grid_cloud_portal.common.RMRestInterface;


/**
 * Resource manager interface available in the rest-client framework.
 * It extends the default resource manager interface with new methods
 * which can be used to transfer files or wait for jobs or tasks to finish.
 *
 * @author ActiveEon Team
 */
@PublicAPI
public interface IRMClient extends RMRestInterface {

    /**
     * Initialize this instance.
     *
     *
     * @param connectionInfo various info about the connection attempt
     * @throws Exception
     *             if an error occurs during the initialization
     */
    void init(ConnectionInfo connectionInfo) throws Exception;

    /**
     * Returns the connection info, if initialized previously
     * @return the connection info
     */
    ConnectionInfo getConnectionInfo();

    /**
     * Sets the session identifier explicitly. This might run on an uninitialized
     * client.
     *
     * @param sid session identifier
     */
    void setSession(String sid);

    /**
     * Retrieves the current session identifier, or null if the session was not initialized yet.
     *
     * @return the current session identifier if initialize, null otherwise
     */
    String getSession();

}
