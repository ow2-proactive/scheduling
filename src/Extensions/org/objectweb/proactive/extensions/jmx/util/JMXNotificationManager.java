package org.objectweb.proactive.extensions.jmx.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.jmx.ProActiveConnection;
import org.objectweb.proactive.extensions.jmx.client.ClientConnector;


/**
 * This class is an utility class.
 * It gives you the possibility to register/unregister a listener to the notifications of a remote JMX MBean.
 * When an active object migrates, the notificationManager subscribes to the new JMX MBean server, and send you the notifications.
 *
 * @author ProActive Team
 * @version 07/28/2007
 * @see org.objectweb.proactive.core.jmx.ProActiveConnection
 * @see org.objectweb.proactive.core.jmx.client.ClientConnector
 * @see org.objectweb.proactive.core.jmx.server.ServerConnector
 */
public class JMXNotificationManager implements NotificationListener {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.JMX);

    /**
     * To find the connection for a given server
     */
    private Map<ServerListened, ProActiveConnection> connections;

    /**
     * To find the server for a given ObjectName
     */
    private Map<ObjectName, ServerListened> servers;

    /**
     * To find the listeners for a given ObjectName
     */
    private Map<ObjectName, ConcurrentLinkedQueue<NotificationListener>> listeners;

    // Singleton
    private static JMXNotificationManager instance;

    /**
     * The active object listener of all notifications.
     */
    private JMXNotificationListener notificationlitener;

    private JMXNotificationManager() {
        connections = new ConcurrentHashMap<ServerListened, ProActiveConnection>();
        servers = new ConcurrentHashMap<ObjectName, ServerListened>();

        listeners = new ConcurrentHashMap<ObjectName, ConcurrentLinkedQueue<NotificationListener>>();

        try {
            this.notificationlitener = (JMXNotificationListener) ProActive.newActive(JMXNotificationListener.class.getName(),
                    new Object[] {  });
        } catch (ActiveObjectCreationException e) {
            logger.error("Can't create the JMX notifications listener active object",
                e);
        } catch (NodeException e) {
            logger.error("Can't create the JMX notifications listener active object",
                e);
        }
    }

    /**
     * Returns the unique instance of the JMXNotificationManager
     * @return Returns the unique instance of the JMXNotificationManager
     */
    public static JMXNotificationManager getInstance() {
        if (instance == null) {
            instance = new JMXNotificationManager();
        }
        return instance;
    }

    /**
     * Subscribes a notification listener to a remote JMX MBean.
     * @param objectName The object name of the MBean.
     * @param listener The notification listener.
     * @param hostUrl The url of the remote host.
     * @param serverName The name of the MBean server.
     */
    public void subscribe(ObjectName objectName, NotificationListener listener,
        String hostUrl, String serverName) {
        // We want the complete url 'protocol://host:port/path'
        String completeUrl = getCompleteUrl(hostUrl);

        ServerListened server = new ServerListened(hostUrl, serverName);
        ProActiveConnection connection = connections.get(server);

        // We have to create a new connection
        if (connection == null) {
            // Creation of the new connection
            ClientConnector cc = new ClientConnector(completeUrl, serverName);
            cc.connect();
            connection = cc.getConnection();
            // Updates our maps
            connections.put(server, connection);
            servers.put(objectName, server);
        }

        ConcurrentLinkedQueue<NotificationListener> notificationListeners = listeners.get(objectName);

        // This objectName is already listened
        if (notificationListeners != null) {
            // We add this listener to the listeners of this object.
            notificationListeners.add(listener);

            // Is it useful?
            listeners.put(objectName, notificationListeners);
        }
        // This objectName is not yet listened
        else {
            notificationListeners = new ConcurrentLinkedQueue<NotificationListener>();
            notificationListeners.add(listener);
            listeners.put(objectName, notificationListeners);
            notificationlitener.subscribe(connection, objectName, null, null);
        }
    }

    /**
     * Unsubscribes a notification listener to a remote JMX MBean.
     * @param objectName The object name if the MBean.
     * @param listener The notification listener.
     */
    public void unsubscribe(ObjectName objectName, NotificationListener listener) {
        ConcurrentLinkedQueue<NotificationListener> notificationListeners = listeners.get(objectName);

        // No listener listen this objectName, so we display an error message.
        if (notificationListeners == null) {
            logger.warn(
                "JMXNotificationManager.unsubscribe() ObjectName not known");
            return;
        }
        // We have to remove the listener.
        else {
            boolean isRemoved = notificationListeners.remove(listener);

            // The listener didn't be listening this objectName, so we display an error message.
            if (!isRemoved) {
                logger.warn(
                    "JMXNotificationManager.unsubscribe() Listener not known");
            }

            // If there is no listeners which listen this objectName, we remove this one.
            if (notificationListeners.isEmpty()) {
                listeners.remove(objectName);
                ServerListened server = servers.get(objectName);
                if (server != null) {
                    ProActiveConnection connection = connections.get(server);
                    if (connection != null) {
                        // The connection is not yet closed
                        notificationlitener.unsubscribe(connections.get(server),
                            objectName, null, null);
                    }
                }
                // Updates our maps
                servers.remove(objectName);
            }
        }
    }

    public void handleNotification(Notification notification, Object handback) {
        String type = notification.getType();
        ObjectName oname = (ObjectName) notification.getSource();

        if (logger.isDebugEnabled()) {
            logger.debug("[" + type + "]\n[JMXNotificationManager] source=" +
                oname);
        }

        // The active object containing the MBean has migrated, so we have to connect to a new remote host.
        if (type.equals(NotificationType.migrationFinished)) {
            // The JMX MBean server url
            String runtimeUrl = (String) notification.getUserData();

            String host = UrlBuilder.getHostNameFromUrl(runtimeUrl);
            String runtimeName = UrlBuilder.getNameFromUrl(runtimeUrl);
            String protocol = UrlBuilder.getProtocol(runtimeUrl);
            int port = UrlBuilder.getPortFromUrl(runtimeUrl);

            String hostUrl = UrlBuilder.buildUrl(host, "", protocol, port);

            // The JMX MBean Server name
            // Warning: This is a convention used in the ServerConnector
            String serverName = runtimeName;

            // Search in our established connections
            ProActiveConnection connection = connections.get(new ServerListened(
                        hostUrl, serverName));

            // We have to open a new connection
            if (connection == null) {
                // Creates a new Connection
                ClientConnector cc = new ClientConnector(hostUrl, serverName);
                cc.connect();
                connection = cc.getConnection();
            }

            // Subscribes to the JMX notifications
            notificationlitener.subscribe(connection, oname, null, null);

            // Updates ours map
            ServerListened server = new ServerListened(hostUrl, serverName);
            servers.put(oname, server);
            connections.put(server, connection);

            // We send always a notification containing a set of notifications.
            Notification notif = new Notification(NotificationType.unknown,
                    notification.getSource(), notification.getSequenceNumber());
            ConcurrentLinkedQueue<Notification> userData = new ConcurrentLinkedQueue<Notification>();
            userData.add(notification);
            notif.setUserData(userData);
            notification = notif;
        }

        listeners.get(oname);
        ConcurrentLinkedQueue<NotificationListener> l = listeners.get(oname);
        if (l == null) {
            // No listener listen this objectName
            listeners.remove(oname);
            return;
        }

        // Sends to the listeners the notification
        for (NotificationListener listener : l) {
            listener.handleNotification(notification, handback);
        }
    }

    /**
     * Creates a complete url 'protocol://host:port/path'
     * @param url
     * @return A complete url
     */
    public static String getCompleteUrl(String url) {
        String host = UrlBuilder.getHostNameFromUrl(url);
        String name = UrlBuilder.getNameFromUrl(url);
        int port = UrlBuilder.getPortFromUrl(url);
        String protocol = UrlBuilder.getProtocol(url);

        return UrlBuilder.buildUrl(host, name, protocol, port);
    }

    public ProActiveConnection getConnection(String hostUrl, String serverName) {
        return connections.get(new ServerListened(hostUrl, serverName));
    }

    //
    // ------- INNER CLASS ---------
    //
    private class ServerListened {

        /**
         * The url of the remote host
         */
        private String hostUrl;

        /**
         * The JMX MBean Server name
         */
        private String serverName;

        /**
         * Creates a new ServerListened
         * @param hostUrl The url of the remote host.
         * @param serverName The JMX MBean Server name
         */
        public ServerListened(String hostUrl, String serverName) {
            this.hostUrl = hostUrl;
            this.serverName = serverName;
        }

        /**
         * Returns the url of the remote host.
         * @return the url of the remote host.
         */
        public String getHostUrl() {
            return hostUrl;
        }

        /**
         * Returns the JMX MBean Server name.
         * @return The JMX MBean Server name.
         */
        public String getServerName() {
            return serverName;
        }

        @Override
        public boolean equals(Object anObject) {
            if (!(anObject instanceof ServerListened)) {
                return false;
            }
            ServerListened otherServerListened = (ServerListened) anObject;
            return (this.hostUrl.equals(otherServerListened.getHostUrl()) &&
            this.serverName.equals(otherServerListened.getServerName()));
        }

        @Override
        public int hashCode() {
            return this.hostUrl.hashCode() + this.serverName.hashCode();
        }
    }
}
