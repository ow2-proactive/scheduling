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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.util.logforwarder.providers;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Appender;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.log.remote.ProActiveAppender;
import org.objectweb.proactive.core.util.log.remote.ProActiveLogCollector;
import org.objectweb.proactive.core.util.log.remote.ProActiveLogCollectorDeployer;
import org.objectweb.proactive.core.util.log.remote.ThrottlingProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingProvider;


/**
 * ProActive communication based log forwarding service.
 * @see ProActiveAppender
 * @see ProActiveLogCollector
 */
public class ProActiveBasedForwardingProvider implements LogForwardingProvider {

    // log collector deployer
    private ProActiveLogCollectorDeployer collectorDeployer;

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingProvider#createAppenderProvider(java.net.URI)
     */
    public AppenderProvider createAppenderProvider(URI serverURI) throws LogForwardingException {
        try {
            return new ProActiveAppenderProvider(new URI(this.collectorDeployer.getCollectorURL()));
        } catch (Exception e) {
            throw new LogForwardingException("Cannot create ProActive appender provider.", e);
        }
    }

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingProvider#createServer()
     */
    public URI createServer() throws LogForwardingException {
        try {
            collectorDeployer = new ProActiveLogCollectorDeployer("scheduler_collector");
            return new URI(collectorDeployer.getCollectorURL());
        } catch (URISyntaxException e) {
            throw new LogForwardingException("Cannot create ProActive log collector.", e);
        }
    }

    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingProvider#destroyServer()
     */
    public void terminateServer() throws LogForwardingException {
        try {
            this.collectorDeployer.terminate();
            this.collectorDeployer = null;
        } catch (ProActiveException e) {
            throw new LogForwardingException("Cannot terminate ProActive log collector.", e);
        }
    }

    public static class ProActiveAppenderProvider implements AppenderProvider {

        /**
         * Default flusing period of the ProActive appender.
         * Can be overridden by org.objectweb.proactive.core.util.log.remote.ThrottlingProvider.period ProActive Property.
         */
        public final static int FLUSH_PERIOD = 1000;//ms

        private URI remoteCollectorURI;

        ProActiveAppenderProvider(URI remoteCollectorURI) {
            this.remoteCollectorURI = remoteCollectorURI;
        }

        public Appender getAppender() throws LogForwardingException {
            try {
                ProActiveLogCollector remoteCollector = (ProActiveLogCollector) PARemoteObject
                        .lookup(remoteCollectorURI);
                return new ProActiveAppender(new ThrottlingProvider(FLUSH_PERIOD, 50, 10000), remoteCollector);
            } catch (ProActiveException e) {
                throw new LogForwardingException("Cannot lookup remote log collector at " +
                    this.remoteCollectorURI, e);
            }
        }

    }

}
