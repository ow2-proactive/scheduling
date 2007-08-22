package org.objectweb.proactive.extra.gcmdeployment.core;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.runtime.LocalNode;


/**
 * Careful synchronization is required since Virtual Node can be returned
 * to the user before nodes registration.
 *
 * @author cmathieu
 *
 */
@PublicAPI
public interface VirtualNode {

    /**
     * A magic number to indicate that a Virtual Node is asking
     * for every available nodes
     */
    static final public long MAX_CAPACITY = -2;

    /**
     * Name of the default Virtual Node.
     *
     * A Node belongs to the default Virtual Node until
     * setVirtualNodeName is called on it.
     *
     * @See {@link LocalNode}
     */
    static final public String DEFAULT_VN = "DEFAULT_VN";

    /**
     * Returns the name of this Virtual Node
     * @return name of the Virtual Node as declared inside the GCM Application Descriptor
     */
    public String getName();

    /**
     * Returns the capacity asked by this Virtual Node
     *
     * @return the capacity asked by this Virtual Node. If max is specified
     * in the GCM Application Descriptor then MAX_CAPACITY is returned.
     */
    public long getRequiredCapacity();
}
