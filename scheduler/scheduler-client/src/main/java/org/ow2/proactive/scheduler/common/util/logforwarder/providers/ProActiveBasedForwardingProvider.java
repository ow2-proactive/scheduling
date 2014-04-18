/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.remote.ProActiveAppender;
import org.objectweb.proactive.core.util.log.remote.ProActiveLogCollector;
import org.objectweb.proactive.core.util.log.remote.ProActiveLogCollectorDeployer;
import org.objectweb.proactive.core.util.log.remote.ThrottlingProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingProvider;
import org.apache.log4j.Appender;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;


/**
 * ProActive communication based log forwarding service.
 * @see ProActiveAppender
 * @see ProActiveLogCollector
 */
public class ProActiveBasedForwardingProvider implements LogForwardingProvider {

    // log collector deployer
    private ProActiveLogCollectorDeployer collectorDeployer;

    // used for unique collector name
    private static final AtomicInteger collectorCounter = new AtomicInteger(0);

    // bind name
    public static final String COLLECTOR_BIND_NAME = "proactive_lfs_collector";

    /* (non-Javadoc)
     * @see LogForwardingProvider#createAppenderProvider(java.net.URI)
     */
    public AppenderProvider createAppenderProvider(URI serverURI) throws LogForwardingException {
        try {
            return new ProActiveAppenderProvider(new URI(this.collectorDeployer.getCollectorURL()));
        } catch (Exception e) {
            throw new LogForwardingException("Cannot create ProActive appender provider.", e);
        }
    }

    @Override
    public URI createServer(LogForwardingService.LoggingEventProcessor eventProcessor) throws LogForwardingException {
        return null;
    }

    /* (non-Javadoc)
         * @see LogForwardingProvider#createServer()
         */
    public URI createServer() throws LogForwardingException {
        try {
            collectorDeployer = new ProActiveLogCollectorDeployer(ProActiveRuntimeImpl.getProActiveRuntime()
                    .getVMInformation().getName() +
                COLLECTOR_BIND_NAME + collectorCounter.addAndGet(1));
            return new URI(collectorDeployer.getCollectorURL());
        } catch (URISyntaxException e) {
            throw new LogForwardingException("Cannot create ProActive log collector.", e);
        } catch (ProActiveException e) {
            throw new LogForwardingException("Cannot create ProActive log collector.", e);
        }
    }

    /* (non-Javadoc)
     * @see LogForwardingProvider#destroyServer()
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
                return new ProActiveAppender(new ThrottlingProvider(FLUSH_PERIOD, 50, 10000, true),
                    remoteCollector);
            } catch (ProActiveException e) {
                throw new LogForwardingException("Cannot lookup remote log collector at " +
                    this.remoteCollectorURI, e);
            }
        }

    }

}
