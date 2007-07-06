package org.objectweb.proactive.core.jmx.mbean;

import java.util.ArrayList;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;


public class ProActiveRuntimeWrapper extends NotificationBroadcasterSupport
    implements ProActiveRuntimeWrapperMBean {
    private ProActiveRuntime runtime;
    private transient ObjectName objectName;
    private long counter = 0;
    private String url;

    public ProActiveRuntimeWrapper() {

        /* Empty Constructor required by JMX */
    }

    public ProActiveRuntimeWrapper(ProActiveRuntime runtime) {
        this.runtime = runtime;
        this.url = this.runtime.getURL();
        this.objectName = FactoryName.createRuntimeObjectName(url);
    }

    public String getURL() {
        return /*this.runtime.getURL();*/ url;
    }

    public ObjectName getObjectName() {
        return this.objectName;
    }

    public List<ObjectName> getNodes()
        throws ProActiveException, MalformedObjectNameException,
            NullPointerException, InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException {
        String[] nodeNames = null;
        nodeNames = this.runtime.getLocalNodeNames();

        //String runtimeUrl = this.runtime.getURL();
        List<ObjectName> onames = new ArrayList<ObjectName>();
        for (int i = 0; i < nodeNames.length; i++) {
            String nodeName = nodeNames[i];

            /* String host = UrlBuilder.getHostNameFromUrl(runtimeUrl);
            String protocol = UrlBuilder.getProtocol(runtimeUrl);
            int port = UrlBuilder.getPortFromUrl(runtimeUrl);
            String nodeUrl = UrlBuilder.buildUrl(host, nodeName, protocol, port);
            */
            ObjectName oname = FactoryName.createNodeObjectName(getURL(),
                    nodeName);

            onames.add(oname);
        }
        return onames;
    }

    public void addProActiveEventListener() {
        System.out.println("[Runtime : addProActiveEventListener]");
        // BodyEventListener
        // LocalBodyStore.getInstance().addBodyEventListener(this);
        // FutureEventListener
        // FutureProxy.getFutureEventProducer().addFutureEventListener(this);
        // NodeCreationEventListener
        //NodeCreationEventProducer node = new NodeCreationEventProducerImpl();
        //node.addNodeCreationEventListener(this);   	
    }

    public void sendNotification(String type) {
        this.sendNotification(type, null);
    }

    public void sendNotification(String type, Object userData) {
        System.out.print("[" + type + "] ");
        ObjectName source = getObjectName();
        //NotificationSource source = new NotificationSource(objectName, url);
        System.out.println("[ProActiveRuntimeWrapper.sendNotification] source=" +
            source);
        Notification notification = new Notification(type, source, counter++);
        notification.setUserData(userData);
        this.sendNotification(notification);
        //notifications.add(notification);
    }
}
