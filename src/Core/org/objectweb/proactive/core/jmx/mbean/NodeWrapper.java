package org.objectweb.proactive.core.jmx.mbean;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.filter.ProActiveInternalObjectFilter;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.runtime.LocalNode;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of a NodeWrapper MBean.
 * @author ProActive Team
 */
public class NodeWrapper extends NotificationBroadcasterSupport
    implements Serializable, NodeWrapperMBean {

    /** JMX Logger */
    private transient Logger logger = ProActiveLogger.getLogger(Loggers.JMX_MBEAN);

    /** ObjectName of this MBean */
    private transient ObjectName objectName;

    /** The Node wrapped in this MBean */
    private LocalNode localNode;

    /** The url of the LocalNode */
    private String nodeUrl;

    /** The url of the ProActive Runtime */
    private String runtimeUrl;

    /** Used by the JMX notifications */
    private long counter = 1;

    public NodeWrapper() {

        /* Empty Constructor required by JMX */
    }

    /**
     * Creates a new NodeWrapper MBean, representing a Local Node.
     * @param objectName
     * @param localNode
     * @param runtimeUrl
     */
    public NodeWrapper(ObjectName objectName, LocalNode localNode,
        String runtimeUrl) {
        this.objectName = objectName;
        this.localNode = localNode;
        this.runtimeUrl = runtimeUrl;

        URI runtimeURI = URI.create(runtimeUrl);
        String host = runtimeURI.getHost();
        String protocol = URIBuilder.getProtocol(runtimeURI);
        int port = runtimeURI.getPort();

        this.nodeUrl = URIBuilder.buildURI(host, localNode.getName(), protocol,
                port).toString();
    }

    public String getURL() {
        return this.nodeUrl;
    }

    public List<ObjectName> getActiveObjects() {
        List<UniversalBody> activeObjects = this.localNode.getActiveObjects(new ProActiveInternalObjectFilter());

        List<ObjectName> onames = new ArrayList<ObjectName>();
        for (UniversalBody ub : activeObjects) {
            UniqueID id = ub.getID();

            System.out.println("NodeWrapper.getActiveObjects() " + id);

            ObjectName name = FactoryName.createActiveObjectName(id);
            onames.add(name);
        }
        return onames;
    }

    public ObjectName getObjectName() {
        return this.objectName;
    }

    public String getVirtualNodeName() {
        return localNode.getVirtualNodeName();
    }

    public String getJobId() {
        return localNode.getJobId();
    }

    public void sendNotification(String type) {
        sendNotification(type, null);
    }

    public void sendNotification(String type, Object userData) {
        ObjectName source = getObjectName();

        if (logger.isDebugEnabled()) {
            logger.debug("[" + type +
                "]#[NodeWrapper.sendNotification] source=" + source +
                ", userData=" + userData);
        }
        Notification notification = new Notification(type, source, counter++);
        notification.setUserData(userData);
        sendNotification(notification);
    }
}
