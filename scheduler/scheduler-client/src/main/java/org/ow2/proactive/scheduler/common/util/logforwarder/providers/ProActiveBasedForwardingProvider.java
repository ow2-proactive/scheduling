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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.remote.ProActiveAppender;
import org.objectweb.proactive.core.util.log.remote.ProActiveLogCollector;
import org.objectweb.proactive.core.util.log.remote.ThrottlingProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.*;
import org.apache.log4j.Appender;


/**
 * ProActive communication based log forwarding service.
 * @see ProActiveAppender
 */
public class ProActiveBasedForwardingProvider implements LogForwardingProvider {

    // log collector deployer
    private LogCollectorDeployer collectorDeployer;

    // used for unique collector name
    private static final AtomicInteger collectorCounter = new AtomicInteger(0);

    // bind name
    public static final String COLLECTOR_BIND_NAME = "proactive_lfs_collector";
    private LoggingEventProcessor eventProcessor;

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
    public URI createServer(LoggingEventProcessor eventProcessor) throws LogForwardingException {
        try {
            collectorDeployer = new LogCollectorDeployer(ProActiveRuntimeImpl.getProActiveRuntime()
                    .getVMInformation().getName() +
                COLLECTOR_BIND_NAME + collectorCounter.addAndGet(1), eventProcessor);
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

    private static final long serialVersionUID = 62L;

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
                LogCollector remoteCollector = (LogCollector) PARemoteObject.lookup(remoteCollectorURI);
                return new ProActiveAppender(new ThrottlingProvider(FLUSH_PERIOD, 50, 10000, true),
                    remoteCollector);
            } catch (ProActiveException e) {
                throw new LogForwardingException("Cannot lookup remote log collector at " +
                    this.remoteCollectorURI, e);
            }
        }

    }

    public static class LogCollectorDeployer {
        final private String url;

        final private LogCollector collector;

        final RemoteObjectExposer<LogCollector> roe;

        public LogCollectorDeployer(String name, LoggingEventProcessor eventProcessor)
                throws ProActiveException {
            this.collector = new LogCollector(eventProcessor);
            this.roe = PARemoteObject.newRemoteObject(LogCollector.class.getName(), this.collector);
            this.roe.createRemoteObject(name, false);
            this.url = roe.getURL();
        }

        public String getCollectorURL() {
            return this.url;
        }

        public void terminate() throws ProActiveException {
            roe.unexportAll();
        }
    }

    public static class LogCollector extends ProActiveLogCollector {
        private LoggingEventProcessor eventProcessor;

        public LogCollector() {
        }

        public LogCollector(LoggingEventProcessor eventProcessor) {
            this.eventProcessor = eventProcessor;
        }

        public void sendEvent(List<LoggingEvent> events) {
            for (LoggingEvent event : events) {
                eventProcessor.processEvent(event);
            }
        }
    }

}
