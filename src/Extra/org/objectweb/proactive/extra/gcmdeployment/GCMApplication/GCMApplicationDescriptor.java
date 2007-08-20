package org.objectweb.proactive.extra.gcmdeployment.GCMApplication;

import java.util.Map;
import java.util.NoSuchElementException;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNode;


/**
 * GCM Application Descriptor public interface
 *
 * This interface is exported to ProActive user and allow them to
 * control and manage a GCM Application Descriptor. For example, this
 * interface must be used to retrieve the Virtual Nodes.
 *
 * @author cmathieu
 *
 */
@PublicAPI
public interface GCMApplicationDescriptor {

    /**
     * Returns the Virtual Node associated to this name
     *
     * @param vnName a Virtual Node name declared inside the GCM Application Descriptor
     * @return the VirtualNode associated to vnName
     * @throws NoSuchElementException if vnName is not declared inside the GCM Application Descriptor
     */
    public VirtualNode getVirtualNode(String vnName)
        throws IllegalArgumentException;

    /**
     * Returns all the Virtual Nodes declared inside this GCM Application Descriptor
     *
     * Keys are the Virtual Node names. Values are the Virtual Nodes.
     *
     * @return All the Virtual Nodes declared inside the GCM Application Descriptor.
     */
    public Map<String, ?extends VirtualNode> getVirtualNodes();

    /**
     * Kills all Nodes and JVMs(local or remote) created when activating the GCM Application Descriptor
     * @param softly if false, all JVMs created when activating the descriptor are killed abruptely
     * if true a JVM that originates the creation of  a rmi registry waits until registry is empty before
     * dying. To be more precise a thread is created to ask periodically the registry if objects are still
     * registered.
     * @throws ProActiveException if a problem occurs when terminating all jvms
     */
    public void kill();

    /**
     *
     * @return true is returned if all processes started by GCM Deployment have
     * exited. false is returned otherwise
     */
    public boolean allProcessExited();

    /**
     * Wait for all process
     */
    public void awaitTermination();
}
