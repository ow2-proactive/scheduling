/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *  
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s): 
 * 
 * ################################################################
 */ 
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

    /**
     * see {@link org.objectweb.proactive.core.component.xml.ContainerHandlerMarker#getContainerElementHierarchy()}
     */
    public ContainerElementHierarchy getContainerElementHierarchy() {
        return containersHierarchy;
    }
}
