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
package org.objectweb.proactive.jmx.listeners;
import java.io.IOException;

import javax.management.NotificationBroadcasterSupport;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;


/**
 * @author ProActive Team
 *
 */
public class ProActiveConnectionNotificationEmitter
    extends NotificationBroadcasterSupport {
    private static long sequenceNumber;
    private JMXConnector connector;

    public ProActiveConnectionNotificationEmitter(JMXConnector connector) {
        this.connector = connector;
    }

    private long getNextNotificationNumber() {
        synchronized (ProActiveConnectionNotificationEmitter.class) {
            return sequenceNumber++;
        }
    }

    private String getConnectionId() {
        try {
            return connector.getConnectionId();
        } catch (IOException x) {
            return null;
        }
    }

    public void sendConnectionNotificationOpened() {
        JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.OPENED,
                connector, getConnectionId(), getNextNotificationNumber(),
                "Connection opened", null);
        sendNotification(notification);
    }

    public void sendConnectionNotificationClosed() {
        JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.CLOSED,
                connector, getConnectionId(), getNextNotificationNumber(),
                "Connection closed", null);
        sendNotification(notification);
    }

    public void sendConnectionNotificationFailed() {
        JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.FAILED,
                connector, getConnectionId(), getNextNotificationNumber(),
                "Connection failed", null);
        sendNotification(notification);
    }

    public void sendConnectionNotificationLost(long howMany) {
        JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.NOTIFS_LOST,
                connector, getConnectionId(), getNextNotificationNumber(),
                "Some notification (" + howMany + ") was lost", null);
        sendNotification(notification);
    }
}
