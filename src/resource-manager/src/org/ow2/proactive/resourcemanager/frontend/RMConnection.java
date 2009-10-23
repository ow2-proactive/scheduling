/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.frontend;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.Connection;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


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
        return ProActiveLogger.getLogger(RMLoggers.CONNECTION);
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
            throw new RMException(e.getMessage());
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
            throw new RMException(e.getMessage());
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
        url = Connection.normalize(url);
        String NAME_ACTIVE_OBJECT_RMAUTHENTICATION = RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION;
        if (!url.endsWith(NAME_ACTIVE_OBJECT_RMAUTHENTICATION)) {
            url += NAME_ACTIVE_OBJECT_RMAUTHENTICATION;
        }
        return url;
    }
}
