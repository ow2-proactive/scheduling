/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.jmx.provider.proactive;

import java.io.IOException;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXConnectorServerProvider;
import javax.management.remote.JMXServiceURL;

import org.objectweb.proactive.core.jmx.server.ProActiveConnectorServer;


/**
 *  <p>A provider for creating JMX API connector servers using a given
 * protocol.  Instances of this interface are created by {@link
 * JMXConnectorServerFactory} as part of its {@link
 * JMXConnectorServerFactory#newJMXConnectorServer(JMXServiceURL,Map,MBeanServer)
 * newJMXConnectorServer} method.</p>
 *
 * @author vlegrand
 */
public class ServerProvider implements JMXConnectorServerProvider {

    /**
     * <p>Creates a new connector server at the given address.  Each
     * successful call to this method produces a different
     * <code>JMXConnectorServer</code> object.</p>
     *
     * @param serviceURL the address of the new connector server.  The
     * actual address of the new connector server, as returned by its
     * {@link JMXConnectorServer#getAddress() getAddress} method, will
     * not necessarily be exactly the same.  For example, it might
     * include a port number if the original address did not.
     *
     * @param environment a read-only Map containing named attributes
     * to control the new connector server's behaviour.  Keys in this
     * map must be Strings.  The appropriate type of each associated
     * value depends on the attribute.
     *
     * @param mbeanServer the MBean server that this connector server
     * is attached to.  Null if this connector server will be attached
     * to an MBean server by being registered in it.
     *
     * @return a <code>JMXConnectorServer</code> representing the new
     * connector server.  Each successful call to this method produces
     * a different object.
     *
     * @exception NullPointerException if <code>serviceURL</code> or
     * <code>environment</code> is null.
     *
     * @exception IOException if the connector server cannot be
     * created.
     */
    public JMXConnectorServer newJMXConnectorServer(JMXServiceURL url, Map<String, ?> env, MBeanServer mbs)
            throws IOException {
        return new ProActiveConnectorServer(url, env, mbs);
    }
}
