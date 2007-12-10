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
package org.objectweb.proactive.examples.jmx.remote.management.events;

import java.io.IOException;
import java.io.Serializable;

import javax.management.InstanceNotFoundException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.examples.jmx.remote.management.client.entities.ManageableEntity;


public class ActiveNotificationListener implements Serializable,
    NotificationListener {

    /**
     *
     */
    private static final long serialVersionUID = 8099866747677304010L;

    public ActiveNotificationListener() {
    }

    public void handleNotification(Notification notification, Object handback) {
        try {
            EntitiesEventManager.getInstance().handleNotification(notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listenTo(ManageableEntity entity) {
        ObjectName name = entity.getObjectName();
        ProActiveConnection connection = entity.getConnection();
        try {
            connection.addNotificationListener(name,
                (NotificationListener) PAActiveObject.getStubOnThis(), null,
                null);
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
