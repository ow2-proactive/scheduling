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
package org.ow2.proactive.scheduler.common.util.logforwarder;

import java.net.URI;


/**
 * This interface should be implemented by all log forwarding methods. It provide protocol-specific log server and appenders.
 * Appenders are wrapped in an AppenderProvider to be sent over the network and created/activated on the remote side.
 * @see LogForwardingService
 */
public interface LogForwardingProvider {

    /**
     * Create an appender provider (i.e. a container for appender). This provider is serializable and can be remotely sent.
     * Note that a specific AppenderProvider implementation must be provided with a specific LogForwardingProvider implementation.
     * @param serverURI the URI of the server that receives logging events appended in the contained appender.
     * @return a container for an appender.
     * @throws LogForwardingException if the provider cannot be created.
     */
    AppenderProvider createAppenderProvider(URI serverURI) throws LogForwardingException;

    /**
     * Create locally a server that is able to receive logging events from the appender created by createAppenderProvider.
     * @return the URI to reach this server.
     * @throws LogForwardingException if the server cannot be created.
     */
    URI createServer(LoggingEventProcessor eventProcessor) throws LogForwardingException;

    /**
     * Terminate the server created by createServer().
     * @throws LogForwardingException if the server cannot be terminated.
     */
    void terminateServer() throws LogForwardingException;

}
