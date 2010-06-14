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

import java.io.IOException;
import java.net.URI;

import org.apache.log4j.Appender;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.appenders.SocketAppenderWithSSHTunneling;


/**
 * SSH-tunneled socket based log forwarding service.
 * @see SocketBasedForwardingProvider
 * @see SocketAppenderWithSSHTunneling
 */
public class SocketWithSSHTunnelBasedForwardingProvider extends SocketBasedForwardingProvider {

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingProvider#createAppenderProvider(java.net.URI)
     */
    @Override
    public AppenderProvider createAppenderProvider(URI serverURI) {
        // use ProActive defined ssh port if any
        if (CentralPAPropertyRepository.PA_RMISSH_REMOTE_PORT.isSet()) {
            return new SocketSSHAppenderProvider(serverURI.getHost(), serverURI.getPort(),
                CentralPAPropertyRepository.PA_RMISSH_REMOTE_PORT.getValue());
        } else {
            return new SocketSSHAppenderProvider(serverURI.getHost(), serverURI.getPort(), 22);
        }
    }

    /**
     * A simple container for a log4j SocketAppenderWithSSHTunneling.
     */
    public static class SocketSSHAppenderProvider implements AppenderProvider {

        /**  */
		private static final long serialVersionUID = 21L;
		private String hostname;
        private int port;
        private int remoteSSHPort;

        /**
         * Create a provider for SocketAppenderWithSSHTunneling.
         */
        SocketSSHAppenderProvider(String hostname, int port, int remoteSSHPort) {
            this.hostname = hostname;
            this.port = port;
            this.remoteSSHPort = remoteSSHPort;
        }

        /* (non-Javadoc)
         * @see org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider#getAppender()
         */
        public Appender getAppender() throws LogForwardingException {
            // resolve username locally: use ProActive defined username, or current username if any
            String sshUserName = CentralPAPropertyRepository.PA_RMISSH_REMOTE_USERNAME.getValue();
            if ((sshUserName == null) || sshUserName.equals("")) {
                sshUserName = System.getProperty("user.name");
            }
            try {
                return new SocketAppenderWithSSHTunneling(sshUserName, this.hostname, this.port,
                    this.remoteSSHPort);
            } catch (IOException e) {
                throw new LogForwardingException("Cannot create a SSH-tunneled appender to " + this.hostname +
                    ":" + this.port, e);
            }
        }

    }

}
