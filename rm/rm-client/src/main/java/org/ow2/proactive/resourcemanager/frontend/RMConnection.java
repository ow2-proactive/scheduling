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
package org.ow2.proactive.resourcemanager.frontend;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.URIBuilder;
import org.ow2.proactive.authentication.Connection;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.exception.RMException;


/**
 * This class provides means to connect to an existing RM.
 * As a result of connection returns {@link RMAuthentication} for further authentication.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
@PublicAPI
public class RMConnection extends Connection<RMAuthentication> {

    private static RMConnection instance;

    private RMConnection() {
        super(RMAuthentication.class);
    }

    public Logger getLogger() {
        return Logger.getLogger(RMConnection.class);
    }

    public static synchronized RMConnection getInstance() {
        if (instance == null) {
            instance = new RMConnection();
        }
        return instance;
    }

    /**
     * Returns the {@link RMAuthentication} from the specified
     * URL. If resource manager is not available or initializing throws an exception.
     * 
     * @param url the URL of the resource manager to join.
     * @return the resource manager authentication at the specified URL.
     * @throws RMException
     *             thrown if the connection to the resource manager cannot be
     *             established.
     */
    public static RMAuthentication join(String url) throws RMException {
        try {
            return getInstance().connect(normalizeRM(url));
        } catch (Exception e) {
            throw new RMException("Cannot join the Resource Manager at " + url + " due to " + e.getMessage(), e);
        }
    }

    /**
     * Connects to the resource manager using given URL. The current thread will be block until
     * connection established or an error occurs.
     */
    public static RMAuthentication waitAndJoin(String url) throws RMException {
        return waitAndJoin(url, 0);
    }

    /**
     * Connects to the resource manager with a specified timeout value. A timeout of
     * zero is interpreted as an infinite timeout. The connection will then
     * block until established or an error occurs.
     */
    public static RMAuthentication waitAndJoin(String url, long timeout) throws RMException {
        try {
            return getInstance().waitAndConnect(normalizeRM(url), timeout);
        } catch (Exception e) {
            throw new RMException("Cannot join the Resource Manager at " + url + " due to " + e.getMessage(), e);
        }
    }

    /**
     * Normalize the URL of the RESOURCE MANAGER.<br>
     *
     * @param url, the URL to normalize.
     * @return 	//localhost/RM_NAME if the given url is null.<br>
     * 			the given URL if it terminates by the RM_NAME<br>
     * 			the given URL with /RM_NAME appended if URL does not end with /<br>
     * 			the given URL with RM_NAME appended if URL does end with /<br>
     * 			the given URL with RM_NAME appended if URL does not end with RM_NAME
     */
    private static String normalizeRM(String url) {
        return URIBuilder.buildURI(Connection.normalize(url), RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION)
                         .toString();
    }
}
