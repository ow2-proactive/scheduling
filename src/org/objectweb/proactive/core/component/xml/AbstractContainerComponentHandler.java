package org.objectweb.proactive.core.component.xml;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;

import java.util.HashMap;


/**
 * This class allows the coherency of the nesting of components (component
 * containers such as composite or parallel components) :
 * only the container handler of level n+1 is involved (whereas with the
 * standard design, the top containing handler receives all the messages)
 *
 * @author Matthieu Morel
 */
public abstract class AbstractContainerComponentHandler extends ComponentHandler
    implements ContainerHandlerMarker {
    private boolean enabled;
    private ContainerElementHierarchy containersHierarchy;

    /**
     * @param deploymentDescriptor
     * @param componentsCache
     * @param componentTypes
     */
    public AbstractContainerComponentHandler(
        ProActiveDescriptor deploymentDescriptor,
        ComponentsCache componentsCache, HashMap componentTypes,
        ComponentsHandler fatherHandler) {
        super(deploymentDescriptor, componentsCache, componentTypes);
        enable();
        containersHierarchy = new ContainerElementHierarchy();
        containersHierarchy.addFatherHandler(fatherHandler);
        containersHierarchy.disableGrandFatherHandler();
        // add handler on components element
        ComponentsHandler handler = new ComponentsHandler(deploymentDescriptor,
                componentsCache, componentTypes, this);
        addHandler(ComponentsDescriptorConstants.COMPONENTS_TAG, handler);
        getContainerElementHierarchy().addChildContainerHandler(handler);
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.component.xml.ContainerHandlerMarker#getContainerElementHierarchy()
     */
    public ContainerElementHierarchy getContainerElementHierarchy() {
        return containersHierarchy;
    }
}
