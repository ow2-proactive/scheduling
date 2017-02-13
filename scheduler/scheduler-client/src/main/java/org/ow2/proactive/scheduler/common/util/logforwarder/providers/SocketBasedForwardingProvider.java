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
package org.ow2.proactive.scheduler.common.util.logforwarder.providers;

import java.net.URI;

import org.apache.log4j.Appender;
import org.apache.log4j.net.SocketAppender;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.scheduler.common.util.logforwarder.*;
import org.ow2.proactive.scheduler.common.util.logforwarder.util.SimpleLoggerServer;


/**
 * Socket based log forwarding service.
 */
public class SocketBasedForwardingProvider implements LogForwardingProvider {

    /**
     * Prefix for direct socket (i.e. "no protocol") connexion.
     */
    public static final String RAW_PROTOCOL_PREFIX = "raw";

    // remote server
    private SimpleLoggerServer sls;

    @Override
    public URI createServer(LoggingEventProcessor eventProcessor) throws LogForwardingException {
        try {
            this.sls = SimpleLoggerServer.createLoggerServer(eventProcessor);
            return new URI(RAW_PROTOCOL_PREFIX,
                           "//" + ProActiveInet.getInstance().getInetAddress().getHostName() + ":" + sls.getPort(),
                           "");
        } catch (Exception e) {
            throw new LogForwardingException("Cannot create log server.", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see LogForwardingProvider#destroyServer()
     */
    public void terminateServer() {
        this.sls.stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see LogForwardingProvider#createAppenderProvider(java.net.URI)
     */
    public AppenderProvider createAppenderProvider(URI serverURI) {
        return new SocketAppenderProvider(serverURI.getHost(), serverURI.getPort());
    }

    /**
     * A simple container for a log4j SocketAppender.
     */
    public static class SocketAppenderProvider implements AppenderProvider {

        private String hostname;

        private int port;

        /**
         * Create a provider for SocketAppender.
         * @param hostname the target host.
         * @param port the port open on the target host.
         */
        SocketAppenderProvider(String hostname, int port) {
            this.hostname = hostname;
            this.port = port;
        }

        /*
         * (non-Javadoc)
         * 
         * @see AppenderProvider#getAppender()
         */
        public Appender getAppender() {
            return new SocketAppender(this.hostname, this.port);
        }

    }

}
