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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.util.logforwarder.providers;

import java.net.URI;

import org.apache.log4j.Appender;
import org.apache.log4j.net.SocketAppender;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingProvider;
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

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingProvider#createServer()
     */
    public URI createServer() throws LogForwardingException {
        try {
            this.sls = SimpleLoggerServer.createLoggerServer();
            return new URI(RAW_PROTOCOL_PREFIX, "//" +
                ProActiveInet.getInstance().getInetAddress().getHostName() + ":" + sls.getPort(), "");
        } catch (Exception e) {
            throw new LogForwardingException("Cannot create log server.", e);
        }
    }

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingProvider#destroyServer()
     */
    public void terminateServer() {
        this.sls.stop();
    }

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingProvider#createAppenderProvider(java.net.URI)
     */
    public AppenderProvider createAppenderProvider(URI serverURI) {
        return new SocketAppenderProvider(serverURI.getHost(), serverURI.getPort());
    }

    /**
     * A simple container for a log4j SocketAppender.
     */
    public static class SocketAppenderProvider implements AppenderProvider {

        /**  */
		private static final long serialVersionUID = 21L;
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

        /* (non-Javadoc)
         * @see org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider#getAppender()
         */
        public Appender getAppender() {
            return new SocketAppender(this.hostname, this.port);
        }

    }

}
