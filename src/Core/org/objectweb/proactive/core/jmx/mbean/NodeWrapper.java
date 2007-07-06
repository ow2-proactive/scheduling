package org.objectweb.proactive.core.jmx.mbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.filter.ProActiveInternalObjectFilter;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.runtime.LocalNode;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class NodeWrapper extends NotificationBroadcasterSupport
    implements Serializable, NodeWrapperMBean {
    private transient Logger logger = ProActiveLogger.getLogger(Loggers.JMX_MBEAN);

    //NEW
    private LocalNode localNode;
    private String runtimeUrl;
    private String nodeUrl;

    /** ObjectName of this MBean */
    private transient ObjectName objectName;

    /** Used by the JMX notifications */
    private long counter = 1;

    public NodeWrapper() {

        /* Empty Constructor required by JMX */
    }

    /**
     *
     * @param objectName
     * @param localNode
     * @param runtimeUrl
     */
    public NodeWrapper(ObjectName objectName, LocalNode localNode,
        String runtimeUrl) {
        this.objectName = objectName;
        this.localNode = localNode;
        this.runtimeUrl = runtimeUrl;

        String host = UrlBuilder.getHostNameFromUrl(runtimeUrl);
        String protocol = UrlBuilder.getProtocol(runtimeUrl);
        int port = UrlBuilder.getPortFromUrl(runtimeUrl);

        this.nodeUrl = UrlBuilder.buildUrl(host, localNode.getName(), protocol,
                port);
    }

    public String getURL() {
        return nodeUrl;
    }

    public List<ObjectName> getActiveObjects()
        throws ProActiveException, MalformedObjectNameException,
            NullPointerException, InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException {
        List<List<Object>> activeObjects = this.localNode.getActiveObjects(new ProActiveInternalObjectFilter());

        List<ObjectName> onames = new ArrayList<ObjectName>();
        for (List<Object> ao : activeObjects) {
            UniversalBody ub = (UniversalBody) ao.get(0);
            UniqueID id = ub.getID();

            ObjectName name = FactoryName.createActiveObjectName(id);
            onames.add(name);
        }
        return onames;
    }

    public void sendNotification(String type) {
        sendNotification(type, null);
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public void sendNotification(String type, Object userData) {
        logger.debug("[" + type + "]");
        ObjectName source = getObjectName();
        //NotificationSource source = new NotificationSource(objectName, nodeUrl);
        logger.debug("[NodeWrapper.sendNotification] source=" + source);
        Notification notification = new Notification(type, source, counter++);
        notification.setUserData(userData);
        sendNotification(notification);
    }
}
