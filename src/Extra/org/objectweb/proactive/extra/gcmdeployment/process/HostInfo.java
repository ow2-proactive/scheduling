package org.objectweb.proactive.extra.gcmdeployment.process;

import java.io.Serializable;
import java.util.Set;

import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.extra.gcmdeployment.process.hostinfo.Tool;


/**
 *
 * @author cmathieu
 *
 */
public interface HostInfo extends Serializable {

    /**
     * Returns the Id of this of set
     * @return the Id as declared by the host's id attribute
     */
    public String getId();

    public void setId(String id);

    /**
     * Returns the username associated to this set of hosts
     * @return the username it is present inside the GCM Deployment Descriptor. If
     * not null is returned
     */
    public String getUsername();

    /**
     * Returns the homeDirectory associated to this set of hosts
     *
     * @return the home directory as an platform dependent absolute path.
     */
    public String getHomeDirectory();

    /**
     * Returns the Operating System associated to this set of hosts
     *
     * @return the Operating System
     */
    public OperatingSystem getOS();

    /**
     * Returns the set of available tools on this set of hosts
     *
     * @return A set of available tools as declared inside the GCM Deployment Descriptor
     */
    public Set<Tool> getTools();

    /**
     * Returns the Tool identified by this id
     *
     * @param id the identifier of this tool
     * @return If declared the Tool is returned. Otherwise null is returned
     */
    public Tool getTool(final String id);

    /**
     * Returns the capacity of this host
     *
     * @return the capacity of this host
     */
    public int getHostCapacity();

    /**
     * Returns the VM capacity of this host
     *
     * @return the VM capacity of this host
     */
    public int getVmCapacity();

    public boolean isCapacitiyValid();

    /**
     * Check that this bridge is in a consistent state and is ready to be
     * used.
     *
     * @throws IllegalStateException thrown if anything is wrong
     */
    public void check() throws IllegalStateException;

    /**
     * Return the deployment id of this HostInfo.
     *
     * A Deployment ID is an uniq ID associated to an HostInfo at runtime. This
     * ID can be used to discover topology and manage the application. This ID is a link
     * between GCM descriptors and middlewares/runtime.
     *
     * @return the deployment ID
     */
    public long getDeploymentId();

    /**
     * Set the deployment id of this HostInfo
     */
    public void setDeploymentId(long deploymentId);
}
