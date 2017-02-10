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
package org.ow2.proactive.jmx.provider.ro;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXConnectorServerProvider;
import javax.management.remote.JMXServiceURL;

import org.ow2.proactive.jmx.provider.JMXProviderUtils;


/**
 *  <p>A provider for creating JMX API connector servers using a given
 * protocol.  Instances of this interface are created by {@link
 * JMXConnectorServerFactory} as part of its {@link
 * JMXConnectorServerFactory#newJMXConnectorServer(JMXServiceURL,Map,MBeanServer)
 * newJMXConnectorServer} method.</p>
 *
 * @author The ProActive Team
 */
public final class ServerProvider implements JMXConnectorServerProvider {

    /**
     * @see javax.management.remote.JMXConnectorServerProvider#newJMXConnectorServer(JMXServiceURL, Map, MBeanServer)
     */
    public JMXConnectorServer newJMXConnectorServer(final JMXServiceURL serviceURL, final Map<String, ?> env,
            final MBeanServer mbs) throws IOException {
        final String protocol = serviceURL.getProtocol();
        if (!JMXProviderUtils.RO_PROTOCOL.equals(protocol)) {
            throw new MalformedURLException("Wrong protocol " + protocol + " for provider " + this);
        }
        return new ROConnectorServer(serviceURL, env, mbs);
    }
}
