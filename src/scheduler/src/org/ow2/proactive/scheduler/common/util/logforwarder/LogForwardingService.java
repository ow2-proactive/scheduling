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
 *  Initial developer(s): ActiveEon Team - www.activeeon.com
 *
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.util.logforwarder;

import java.net.URI;


/**
 * This class provides server and appenders for log forwarding.
 * Forwarding method/protocol (direct socket, ssh-tunneled sockets or ProActive) is specified by the LogForwarding provider.
 * @see LogForwardingProvider
 */
public final class LogForwardingService {

    // connection to the server created by initialize()
    private URI serverConnection = null;
    // can be initialized only once
    private boolean initialized = false;

    // the protocol specific provider
    private String providerClassname;
    private LogForwardingProvider provider;

    /**
     * Create a new LogForwardingService, based on the provider providerClassname.
     * @param providerClassname the classname of the provider that will be used (see {@link LogForwardingProvider}).
     */
    public LogForwardingService(String providerClassname) {
        this.providerClassname = providerClassname;
    }

    /**
     * Instantiate the LogForwardingProvider specified by providerClassname value,
     * and create and start the log server.
     * AppenderProvider to this server are available after the call to initialize().
     * @throws LogForwardingException if the LogForwardingProvider cannot be instantiated, or if the log server cannot be created.
     * @throws IllegalStateException if the LogForwardingService is already initialized.
     */
    public final synchronized void initialize() throws LogForwardingException {
        try {
            if (!initialized) {
                // load the provider
                Class<? extends LogForwardingProvider> providerClass = (Class<? extends LogForwardingProvider>) Class
                        .forName(providerClassname);
                this.provider = providerClass.newInstance();
                this.serverConnection = provider.createServer();
                this.initialized = true;
            } else {
                throw new IllegalStateException("The service has already been initialized.");
            }
        } catch (ClassNotFoundException e) {
            throw new LogForwardingException("LogForwardingProvider class cannot be found.", e);
        } catch (InstantiationException e) {
            throw new LogForwardingException("LogForwardingProvider cannot be instanciated.", e);
        } catch (IllegalAccessException e) {
            throw new LogForwardingException("LogForwardingProvider cannot be instanciated.", e);
        } catch (ClassCastException e) {
            throw new LogForwardingException(
                "Class defined as LogForwardingProvider is not a LogForwardingProvider.", e);
        }
    }

    /**
     * Terminate this logging service. The log server started by the LogForwardingProvider is terminated.
     * Appenders created by this service cannot be used anymore.
     * @throws LogForwardingException if the log server started by the LogForwardingProvider cannot be terminated.
     */
    public final synchronized void terminate() throws LogForwardingException {
        this.serverConnection = null;
        this.initialized = false;
        this.provider.terminateServer();
        this.provider = null;
        this.providerClassname = null;
    }

    /**
     * Create an appender provider that contains an appender to the server created by initialize() method.
     * @throws LogForwardingException if the appender provider cannot be created.
     * @throws IllegalStateException if the LogForwardingService is not initialized.
     * @return an appender provider that contains an appender to the server created by initialize() method.
     */
    public final synchronized AppenderProvider getAppenderProvider() throws LogForwardingException {
        if (initialized) {
            return provider.createAppenderProvider(this.serverConnection);
        } else {
            throw new IllegalStateException(
                "The service has not been initialized. Cannot create appender provider.");
        }
    }

    /**
     * Return the URI on which the log server is bound.
     * @return the URI on which the log server is bound.
     * @throws IllegalStateException if the LogForwardingService is not initialized.
     */
    public final synchronized URI getServerURI() {
        if (initialized) {
            return this.serverConnection;
        } else {
            throw new IllegalStateException("The service has not been initialized.");
        }
    }

}
