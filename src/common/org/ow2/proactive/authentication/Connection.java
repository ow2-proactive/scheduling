/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.authentication;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.util.ProActiveInet;


/**
 * Class implements connection to the service with authentication. It attempts lookup authentication 
 * active object and checks that system is up and running (in another words authentication object is activated).
 * Provides an ability to connect in blocking and non blocking manner.
 *
 * @param T the real type of authentication
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public abstract class Connection<T extends Authentication> implements Loggable, Serializable {

    /**  */
	private static final long serialVersionUID = 21L;
	/** Error msg */
    private static final String ERROR_CANNOT_LOOKUP_AUTH = "Cannot lookup authentication active object.";
    /** Error msg */
    private static final String ERROR_NOT_ACTIVATED = "System is initializing. Try to connect later.";
    /** Error msg */
    private static final String ERROR_CONNECTION_INTERRUPTED = "Connection is interrupted.";

    /** Time to wait inside connecting loop retries */
    private static final int PERIOD = 1000; // 1 sec	

    /** loggers */
    private Logger logger = getLogger();
    /** The real class type of the authentication */
    private Class<? extends Authentication> clazz = null;

    /**
     * Create a new instance of Connection
     * 
     * @param clazz the real type of the authentication 
     */
    public Connection(Class<? extends Authentication> clazz) {
        this.clazz = clazz;
    }

    /**
     * Lookup of authentication active object
     * 
     * @param url the URL of the service to join.
     * @throws Exception if something wrong append
     */
    @SuppressWarnings("unchecked")
    private T lookupAuthentication(String url) throws Exception {

        logger.debug("Looking up authentication interface '" + url + "'");

        return (T) (PAActiveObject.lookupActive(clazz.getName(), url));
    }

    /**
     * Connect to the service with given url. If it is not available or initializing throws an exception.
     *
     * @param url the URL of the service to join.
     * @return the service authentication interface at the specified URL.
     * @throws Exception if something wrong append
     */
    public T connect(String url) throws Exception {
        T authentication = lookupAuthentication(url);
        if (authentication.isActivated()) {
            return authentication;
        } else {
            throw new Exception(ERROR_NOT_ACTIVATED);
        }
    }

    /**
     * Connects to the service using given URL. The current thread will be block until
     * connection established or an error occurs.
     * 
     * @param url the url on which to connect
     * @return the authentication if connection succeed
     * @throws Exception if something wrong append
     */
    public T waitAndConnect(String url) throws Exception {
        return waitAndConnect(url, 0);
    }

    /**
     * Connects to the service with a specified timeout value. A timeout of
     * zero is interpreted as an infinite timeout. The connection will then
     * block until established or an error occurs.
     * 
     * @param url the url on which to connect
     * @param timeout the maximum amount of time to wait if it cannot connect
     * @return the authentication if connection succeed
     * @throws Exception if something wrong append
     */
    public T waitAndConnect(String url, long timeout) throws Exception {

        T authentication = null;
        long leftTime = timeout == 0 ? Long.MAX_VALUE : timeout;
        try {
            // obtaining authentication active object
            while (leftTime > 0) {
                long startTime = System.currentTimeMillis();
                try {
                    authentication = lookupAuthentication(url);
                    if (authentication == null) {
                        // strange situation : should not be here
                        // simulating an exception during lookup
                        throw new Exception(ERROR_CANNOT_LOOKUP_AUTH);
                    }
                    // success
                    break;
                } catch (Exception e) {
                    logger.debug("", e);
                    Thread.sleep(Math.min(PERIOD, leftTime));
                    leftTime -= (System.currentTimeMillis() - startTime);
                    if (leftTime <= 0) {
                        throw e;
                    }
                }
            }

            // waiting until scheduling is initialized
            while (leftTime > 0) {
                long startTime = System.currentTimeMillis();

                if (authentication.isActivated()) {
                    // success
                    break;
                } else {
                    Thread.sleep(Math.min(PERIOD, leftTime));
                    leftTime -= (System.currentTimeMillis() - startTime);
                    if (leftTime <= 0) {
                        throw new Exception(ERROR_NOT_ACTIVATED);
                    }
                }
            }

        } catch (InterruptedException e) {
            throw new Exception(ERROR_CONNECTION_INTERRUPTED);
        }

        // TODO two cycles has the same pattern => the code can be unified
        return authentication;
    }

    /**
     * Normalize the URL <code>url</code>.<br>
     *
     * @param url, the URL to normalize.
     * @return  the default base URI if the given url is null. @see {@link RemoteObjectFactory}<br>
     *          the given URL with // prepended if it does not contain any scheme identifier
     *          the given URL with / appended if URL does not end with / and does not
     *          contain any path element,<br>
     *          or the given URL, unchanged.
     */
    public static String normalize(String url) {
        if (url == null || url.trim().equals("")) {
            try {
                RemoteObjectFactory rof = AbstractRemoteObjectFactory.getDefaultRemoteObjectFactory();
                // NO_LOOP_BACK property is handled by getBaseURI()
                url = rof.getBaseURI().toString();
            } catch (Exception e) {
                url = "//" + ProActiveInet.getInstance().getHostname() + "/";
            }
        } else {
            url = url.trim();
        }
        if (!url.contains("//")) {
            url = "//" + url;
        }

        String[] elements = url.split("/", -2);
        if (elements.length == 3 && !url.endsWith("/")) {
            url += "/";
        }
        return url;
    }
}
