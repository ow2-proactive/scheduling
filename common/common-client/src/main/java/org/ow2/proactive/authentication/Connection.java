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
package org.ow2.proactive.authentication;

import java.io.IOException;
import java.io.Serializable;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.exceptions.NotBoundException;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * Class implements connection to the service with authentication. It attempts lookup authentication 
 * active object and checks that system is up and running (in another words authentication object is activated).
 * Provides an ability to connect in blocking and non blocking manner.
 *
 * @param <T> the real type of authentication
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public abstract class Connection<T extends Authentication> implements Loggable, Serializable {

    /** Error msg */
    private static final String ERROR_CANNOT_LOOKUP_AUTH = "Cannot lookup authentication active object.";

    /** Error msg */
    private static final String ERROR_NOT_ACTIVATED = "System is initializing. Try to connect later.";

    /** Error msg */
    private static final String ERROR_CONNECTION_INTERRUPTED = "Connection is interrupted.";

    /** Time to wait inside connecting loop retries */
    private static final int PERIOD_IN_MS = 100;

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
        logger.trace("Looking up authentication interface '" + url + "'");
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
        if (authentication.isActivated().getBooleanValue()) {
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
                } catch (NotBoundException e) {
                    // expected NotBoundException is not printed in the log
                    leftTime = sleepOrThrow(startTime, leftTime, e);
                } catch (IOException e) {
                    leftTime = sleepOrThrow(startTime, leftTime, e);
                } catch (Exception e) {
                    // unexpected Exception
                    logger.error("", e);
                    leftTime = sleepOrThrow(startTime, leftTime, e);
                }
            }

            // waiting until scheduling is initialized
            while (leftTime > 0) {
                long startTime = System.currentTimeMillis();
                BooleanWrapper future = authentication.isActivated();

                try {
                    PAFuture.waitFor(future, leftTime);
                } catch (ProActiveTimeoutException e) {
                    logger.error(e);
                    throw e;
                }

                if (authentication.isActivated().getBooleanValue()) {
                    // success
                    break;
                } else {
                    leftTime = sleepOrThrow(startTime, leftTime, new Exception(ERROR_NOT_ACTIVATED));
                }
            }

        } catch (InterruptedException e) {
            throw new Exception(ERROR_CONNECTION_INTERRUPTED);
        }

        // TODO two cycles has the same pattern => the code can be unified
        return authentication;
    }

    /**
     * Waits for the configured period or throw an exception if not time is left
     * @param startTime original starting time
     * @param leftTime time still available
     * @param toThrow exception to throw if time is consumed
     * @return new available time
     * @throws Exception
     */
    private long sleepOrThrow(final long startTime, long leftTime, final Exception toThrow) throws Exception {
        Thread.sleep(Math.min(PERIOD_IN_MS, leftTime));
        leftTime -= (System.currentTimeMillis() - startTime);
        if (leftTime <= 0) {
            logger.error(toThrow);
            throw toThrow;
        }
        return leftTime;
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
    public static URI normalize(String url) {
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

        return URI.create(url);
    }

    /**
     * Selects a network interface for proactive runtime based on provided url.
     * 
     * @throws IOException if an I/O error occurs when creating the socket. 
     * @throws UnknownHostException if the IP address of the host could not be determined.
     * @throws URISyntaxException if the given string violates RFC&nbsp;2396,
     */
    public static String getNetworkInterfaceFor(String url) throws IOException, URISyntaxException {

        if (url != null && CentralPAPropertyRepository.PA_NET_INTERFACE.getValue() == null &&
            CentralPAPropertyRepository.PA_NET_NETMASK.getValue() == null) {
            URI parsedUrl = new URI(url);
            int port = parsedUrl.getPort();
            if (port == -1) {
                // use default port for rmi
                port = 1099;
            }

            Socket socket = null;
            try {
                socket = new Socket(parsedUrl.getHost(), port);
                NetworkInterface ni = NetworkInterface.getByInetAddress(socket.getLocalAddress());
                return ni.getName();
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }

        throw new IllegalStateException("The network interface is already selected");
    }
}
