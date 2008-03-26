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
package org.objectweb.proactive.ic2d.jmxmonitoring.data.listener;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.notification.RequestNotificationData;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.EnableDisableMonitoringAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NamesFactory;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.IC2DThreadPool;


/**
 *
 * Listener for an Active Object. Listens for notifications concerning an ActiveObject and updates the
 * IC2D model object representation of it.
 * For each IC2D representation of an ActiveObject, an ActiveObjectListener will be created and subscribed to
 * the </code>org.objectweb.proactive.core.jmx.util.JMXNotificationManager</code> (singleton).
 * Each time an event occur related to the ActiveObject this listener listens for, a notification
 * will be sent to this listener.  This listener will update the ActiveObject model representation
 * which will send notification for its own listener(s) (the edit part(s))
 * @author The ProActive Team
 *
 */
public class ActiveObjectListener implements NotificationListener {
    private transient Logger logger = ProActiveLogger.getLogger(Loggers.JMX_NOTIFICATION);

    private enum Type {
        SENDER, RECEIVER;
    }

    ////////   Begin -- Task for handling notifications ///////
    private class Task implements Runnable {
        private ConcurrentLinkedQueue<Notification> notifications;

        public Task(ConcurrentLinkedQueue<Notification> notifications) {
            this.notifications = notifications;
        }

        public void run() {
            for (Notification notification : notifications) {
                String type = notification.getType();

                if (type.equals(NotificationType.requestReceived)) {
                    logger.debug(".................................Request Received : " + ao.getName());
                    RequestNotificationData request = (RequestNotificationData) notification.getUserData();
                    addRequest(request, ao, Type.RECEIVER);
                } else if (type.equals(NotificationType.waitForRequest)) {
                    logger.debug("...............................Wait for request");
                    ao.setState(org.objectweb.proactive.ic2d.jmxmonitoring.util.State.WAITING_FOR_REQUEST);
                    ao.setRequestQueueLength(0);
                }
                // --- MessageEvent ---------------------
                else if (type.equals(NotificationType.replyReceived)) {
                    logger.debug("...............................Reply received : " + ao.getName());
                } else if (type.equals(NotificationType.replySent)) {
                    logger.debug("...............................Reply sent : " + ao.getName());
                    ao.setState(org.objectweb.proactive.ic2d.jmxmonitoring.util.State.ACTIVE);
                    Integer requestQueueLength = (Integer) notification.getUserData();
                    ao.setRequestQueueLength(requestQueueLength);
                } else if (type.equals(NotificationType.requestSent)) {
                    logger.debug("...............................Request sent : " + ao.getName());
                    RequestNotificationData request = (RequestNotificationData) notification.getUserData();
                    addRequest(request, ao, Type.SENDER);
                } else if (type.equals(NotificationType.servingStarted)) {
                    logger.debug("...............................Serving started : " + ao.getName());
                    ao.setState(org.objectweb.proactive.ic2d.jmxmonitoring.util.State.SERVING_REQUEST);
                    Integer requestQueueLength = (Integer) notification.getUserData();
                    ao.setRequestQueueLength(requestQueueLength);
                } else if (type.equals(NotificationType.voidRequestServed)) {
                    logger.debug("...............................Void request served : " + ao.getName());
                    ao.setState(org.objectweb.proactive.ic2d.jmxmonitoring.util.State.ACTIVE);
                    Integer requestQueueLength = (Integer) notification.getUserData();
                    ao.setRequestQueueLength(requestQueueLength);
                }
                // --- MigrationEvent -------------------
                else if (type.equals(NotificationType.migratedBodyRestarted)) {
                    logger.debug("...............................Migration body restarted : " + ao.getName());
                } else if (type.equals(NotificationType.migrationAboutToStart)) {
                    logger.debug("...............................Migration about to start " + ao + ", node=" +
                        ao.getParent());
                    ao.setState(org.objectweb.proactive.ic2d.jmxmonitoring.util.State.MIGRATING);
                } else if (type.equals(NotificationType.migrationExceptionThrown)) {
                    logger.debug("...............................Migration Exception thrown : " +
                        ao.getName());
                    ao.migrationFailed((MigrationException) notification.getUserData());
                } else if (type.equals(NotificationType.migrationFinished)) {
                    logger.debug("...............................Migration finished : " + ao.getName());
                }
                // --- FuturEvent -------------------
                else if (type.equals(NotificationType.waitByNecessity)) {
                    logger.debug("...............................Wait By Necessity : " + ao.getName());
                    ao.setState(org.objectweb.proactive.ic2d.jmxmonitoring.util.State.WAITING_BY_NECESSITY);
                } else if (type.equals(NotificationType.receivedFutureResult)) {
                    logger.debug("...............................Received Future Result : " + ao.getName());
                    ao.setState(org.objectweb.proactive.ic2d.jmxmonitoring.util.State.RECEIVED_FUTURE_RESULT);
                } else {
                    System.out.println(ao.getName() + " => " + type);
                }
            }
        }
    }

    ////////  End -- Task for handling notifications ///////
    private ActiveObject ao;

    //private String name;
    public ActiveObjectListener(ActiveObject ao) {
        this.ao = ao;
    }

    @SuppressWarnings("unchecked")
    public void handleNotification(Notification notifications, Object handback) {
        if (!ao.getWorldObject().getEnableMonitoring()) {
            return;
        }

        ConcurrentLinkedQueue notifs = (ConcurrentLinkedQueue<Notification>) notifications.getUserData();
        IC2DThreadPool.execute(new Task(notifs));
    }

    /**
     *  Manages the fact that the active object this listener is registered for has communicated.
     *  it updates the ActiveObject (the model object) and manages automatic discovery
     * @param request
     * @param ao
     * @param type
     */
    private void addRequest(RequestNotificationData request, ActiveObject ao, Type type) {
        UniqueID sourceID = request.getSource();
        UniqueID destinationID = request.getDestination();
        UniqueID aoID;
        String sourceHost = URIBuilder.getHostNameFromUrl(request.getSourceNode());
        String destinationHost = URIBuilder.getHostNameFromUrl(request.getDestinationNode());
        String hostToDiscovered;
        String nodeUrlToDiscovered;

        // MethodName
        //String methodName = request.getMethodName();
        if (type == Type.SENDER) {
            if (!sourceID.equals(ao.getUniqueID())) {
                System.err.println("ActiveObjectListener.handleNotification() the source id != ao.id");
            }
            aoID = destinationID;
            nodeUrlToDiscovered = request.getDestinationNode();
            hostToDiscovered = destinationHost;
        } else {
            if (!destinationID.equals(ao.getUniqueID())) {
                System.err.println("ActiveObjectListener.handleNotification() the destination id != ao.id");
            }
            aoID = sourceID;
            nodeUrlToDiscovered = request.getSourceNode();
            hostToDiscovered = sourceHost;
        }

        // Try to find the name used in the display by the active object
        String name = NamesFactory.getInstance().getName(aoID);
        if (name == null) {
            if ((sourceHost == null) || (destinationHost == null)) {
                System.err.println("ActiveObjectListener.handleNotification() source=" +
                    request.getSourceNode() + ", destination=" + request.getDestinationNode());
                return;
            }

            // We need to re-explore the host, because some new runtimes have been created.
            if (sourceHost.equals(destinationHost)) {
                WorldObject wo = ao.getParent().getParent().getParent().getParent();
                wo.getMonitorThread().addObjectToExplore(ao.getParent().getParent().getParent());
            } else { // We have to monitore a new host.
                String protocol = URIBuilder.getProtocol(nodeUrlToDiscovered);
                int port = URIBuilder.getPortNumber(nodeUrlToDiscovered);
                if ((ao.getDepth() - ao.getHostRank()) > 0) {
                    ao.getWorldObject().addHost(
                            URIBuilder.buildURI(hostToDiscovered, "", protocol, port).toString(),
                            ao.getHostRank() + 1);
                    if (type == Type.SENDER) {
                        ao.addCommunicationTo(destinationID);
                        return;
                    }
                }
            }
        }
        // Update the request queue length
        ao.setRequestQueueLength(request.getRequestQueueLength());

        // Add a communication
        if (type == Type.RECEIVER) {
            ao.addCommunicationFrom(sourceID);
        }
    }
}
