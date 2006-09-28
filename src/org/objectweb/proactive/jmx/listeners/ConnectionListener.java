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
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.objectweb.proactive.ProActive;

/**
 * 
 * @author ProActive Team
 *
 */
public class ConnectionListener implements NotificationListener, Serializable {
     
	private static final long serialVersionUID = 651518694903154592L;	
	private MBeanServerConnection connection;
    private Vector<JMXEventListener> listeners = new Vector<JMXEventListener>();

    
    public ConnectionListener() {
    }
    
    public ConnectionListener(MBeanServerConnection connection) {
        this.connection = connection;
    } 

    /**
     *  Listen to MBean corresponding to the object name
     * @param name the object name of the MBean one want to listen to
     * @throws IOException
     */
    public void listenTo(ObjectName name) throws IOException {
        try {
            this.connection.addNotificationListener(name,
                (ConnectionListener) ProActive.getStubOnThis(), null, null);
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Stop listening to the MBean corresponding to the objectName
     * @param name the objectname of the MBean one want to stop listening to
     * @throws IOException
     */
    public void stopListening(ObjectName name) throws IOException {
        try {
            this.connection.removeNotificationListener(name,
                (ConnectionListener) ProActive.getStubOnThis());
        } catch (InstanceNotFoundException e) {
        	throw new IOException(e.getMessage());
        } catch (ListenerNotFoundException e) {
        	throw new IOException(e.getMessage());
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    
  
    /**
     * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
     */
    public void handleNotification(Notification notification, Object handback) {
        Enumeration e = this.listeners.elements();
        while (e.hasMoreElements()) {
            try {
                ((JMXEventListener) e.nextElement()).handleNotification(notification);
            } catch (Exception ex) {
            }
        }
    }

    /**
     *  Adds a listener 
     * @param listener
     */
    public void addListener(JMXEventListener listener) {
        this.listeners.addElement(listener);
    }

    /**
     *  //TODO A CHANGER
     * @param listener
     */
    public void removeListener(JMXEventListener listener) {
        Enumeration e = listeners.elements();
        while (e.hasMoreElements()) {
            JMXEventListener l = (JMXEventListener) e.nextElement();
            if (l.toString().equals(listener.toString())) {
                this.listeners.removeElement(l);
                break;
            }
        }
    }
}
