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
package org.objectweb.proactive.core.jmx.listeners;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 *
 * @author ProActive Team
 *
 */
public class ListenerAdapter implements NotificationListener {
    private Logger JMX_NOTIFICATION = ProActiveLogger.getLogger(Loggers.JMX_NOTIFICATION);
    private NotificationListener listener;
    private transient MBeanServer mbs;
    private ObjectName name;

    /**
     *
     * @param listener
     */
    public ListenerAdapter(NotificationListener listener, MBeanServer mbs, ObjectName name) {
        this.listener = listener;
        this.mbs = mbs;
        this.name = name;
    }

    /**
     * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
     */
    public void handleNotification(Notification notification, Object handback) {
        try {
            this.listener.handleNotification(notification, handback);
        } catch (Exception e) {
            JMX_NOTIFICATION.debug("an exception occured (" + e.getMessage() +
                ") while sending the notification -- removing the listener");
            try {
                mbs.removeNotificationListener(name, this);
            } catch (InstanceNotFoundException e1) {
                e1.printStackTrace();
            } catch (ListenerNotFoundException e1) {
                e1.printStackTrace();
            }
        }
    }
}
