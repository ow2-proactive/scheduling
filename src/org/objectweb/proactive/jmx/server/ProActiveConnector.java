/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.jmx.server;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.jmx.ProActiveConnection;
import org.objectweb.proactive.jmx.listeners.ProActiveConnectionNotificationEmitter;

import com.sun.jmx.remote.util.EnvHelp;


public class ProActiveConnector implements JMXConnector, Serializable {

    /**
         *
         */
    private static final long serialVersionUID = -4295401093312884914L;
    private static final int CLOSED = 0;
    private static final int OPEN = 1;
    private ProActiveConnection connection;
    private ProActiveServerImpl paServer;
    private JMXServiceURL jmxServiceURL;
    private transient ProActiveConnectionNotificationEmitter emitter;
    private Map env;
    private int state = CLOSED;

    public ProActiveConnector() {
    }

    private ProActiveConnector(ProActiveServerImpl paServer,
        JMXServiceURL address, Map environment) {
        if ((paServer == null) && (address == null)) {
            throw new IllegalArgumentException(
                "proactive server jmxServiceURL both null");
        }
        //        this.emitter = new ProActiveConnectionNotificationEmitter(this);
        this.paServer = paServer;
        this.jmxServiceURL = address;
        if (environment == null) {
            this.env = Collections.EMPTY_MAP;
        } else {
            EnvHelp.checkAttributes(environment);
            this.env = Collections.unmodifiableMap(environment);
        }
    }

    public ProActiveConnector(JMXServiceURL url, Map environment) {
        this(null, url, environment);
    }

    public ProActiveConnector(ProActiveServerImpl paServer, Map environment) {
        this(paServer, null, environment);
    }

    public void connect() throws IOException {
        connect(null);
    }

    public void connect(Map arg0) throws IOException {
        try {
            String hostname = jmxServiceURL.getHost();
            int port = jmxServiceURL.getPort();
            String lookupUrl = "http://" + hostname + ":" + port +
                "/PAJMXServer";
            ProActiveServerImpl paServer = (ProActiveServerImpl) ProActive.lookupActive(ProActiveServerImpl.class.getName(),
                    lookupUrl);

            this.connection = paServer.newClient(null);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
        this.state = OPEN;
        //        emitter.sendConnectionNotificationOpened();
    }

    public synchronized MBeanServerConnection getMBeanServerConnection()
        throws IOException {
        return getMBeanServerConnection(null);
    }

    public synchronized MBeanServerConnection getMBeanServerConnection(
        Subject delegationSubject) throws IOException {
        return connection;
    }

    public void close() throws IOException {
        this.state = CLOSED;
        emitter.sendConnectionNotificationClosed();
    }

    public void addConnectionNotificationListener(
        NotificationListener listener, NotificationFilter filter,
        Object handback) {
        this.emitter.addNotificationListener(listener, filter, handback);
    }

    public void removeConnectionNotificationListener(
        NotificationListener listener) throws ListenerNotFoundException {
        this.emitter.removeNotificationListener(listener);
    }

    public void removeConnectionNotificationListener(
        NotificationListener listener, NotificationFilter filter,
        Object handback) throws ListenerNotFoundException {
        this.emitter.removeNotificationListener(listener, filter, handback);
    }

    public String getConnectionId() throws IOException {
        return "" + this.hashCode();
    }
}
