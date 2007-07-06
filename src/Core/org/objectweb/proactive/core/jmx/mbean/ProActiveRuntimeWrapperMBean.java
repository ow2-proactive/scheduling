package org.objectweb.proactive.core.jmx.mbean;

import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.objectweb.proactive.core.ProActiveException;


/**
 * MBean representing an ProActiveRuntime.
 * @author ProActiveRuntime
 */
public interface ProActiveRuntimeWrapperMBean {

    /**
     * Returns the url of the ProActiveRuntime associated.
     * @return The url of the ProActiveRuntime associated.
     */
    public String getURL();

    /**
     * For each node of this ProActiveRuntime a new NodeWrapperBMean is created.
     * And returns the list of ObjectName of MBeans representing the nodes of this ProActiveRuntime.
     * @return
     * @throws ProActiveException
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws NotCompliantMBeanException
     */
    public List<ObjectName> getNodes()
        throws ProActiveException, MalformedObjectNameException,
            NullPointerException, InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException;

    /**
     * Adds a BodyEventListener, a FuturEventListener,
     * and a NodeCreationEventListener.
     */
    public void addProActiveEventListener();

    public void sendNotification(String type);

    public void sendNotification(String type, Object userData);

    public ObjectName getObjectName();
}
