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

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author Matthieu Morel
 */
public class ComponentsHandler extends AbstractUnmarshallerDecorator
    implements ContainerHandlerMarker {
    public static Logger logger = Logger.getLogger(ComponentsHandler.class.getName());
    private ProActiveDescriptor deploymentDescriptor;
    private ComponentsCache componentsCache;
    private HashMap componentTypes;
    private List subComponents;
    private boolean enabled;
    private ContainerElementHierarchy containersHierarchy;

    public ComponentsHandler(ProActiveDescriptor deploymentDescriptor,
        ComponentsCache componentsCache, HashMap componentTypes,
        AbstractContainerComponentHandler fatherHandler) {
        enable();
        containersHierarchy = new ContainerElementHierarchy();
        containersHierarchy.addFatherHandler(fatherHandler);
        this.deploymentDescriptor = deploymentDescriptor;
        this.componentsCache = componentsCache;
        this.componentTypes = componentTypes;

        // handlers for the different types of components are added when components are effectively encountered
        // this avoids infinite handlers due to the recursive structure of components
        BindingsHandler bindings_handler = new BindingsHandler(componentsCache);
        addHandler(ComponentsDescriptorConstants.BINDINGS_TAG, bindings_handler);
        subComponents = new ArrayList();
        getContainerElementHierarchy().disableGrandFatherHandler();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
     */
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
        if (isEnabled() ||
                getContainerElementHierarchy().containsChild(activeHandler)) {
            enable(); // just to make sure
            if (name.equals(
                        ComponentsDescriptorConstants.PRIMITIVE_COMPONENT_TAG) ||
                    name.equals(
                        ComponentsDescriptorConstants.COMPOSITE_COMPONENT_TAG) ||
                    name.equals(
                        ComponentsDescriptorConstants.PARALLEL_COMPOSITE_COMPONENT_TAG)) {
                // add the name of this sub component to the list
                ComponentResultObject result = (ComponentResultObject) activeHandler.getResultObject();
                if (result.componentsAreParallelized()) {
                    String[] component_names = result.getNames();
                    for (int i = 0; i < component_names.length; i++) {
                        subComponents.add(component_names[i]);
                        if (logger.isDebugEnabled()) {
                            logger.debug("adding component's name : " +
                                component_names[i]);
                        }
                    }
                } else {
                    subComponents.add(result.getName());
                    if (logger.isDebugEnabled()) {
                        logger.debug("adding component's name : " +
                            ((ComponentResultObject) activeHandler.getResultObject()).getName());
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
     */
    public Object getResultObject() throws SAXException {
        return subComponents;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.io.XMLHandler#readValue(java.lang.String)
     */
    public void readValue(String value) throws SAXException {
        super.readValue(value);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.io.XMLHandler#startElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
     */
    public void startElement(String name, Attributes attributes)
        throws SAXException {
        if (isEnabled()) {
            if (name.equals(
                        ComponentsDescriptorConstants.COMPOSITE_COMPONENT_TAG)) {
                CompositeComponentHandler handler = new CompositeComponentHandler(deploymentDescriptor,
                        componentsCache, componentTypes, this);
                getContainerElementHierarchy().addChildContainerHandler(handler);
                addHandler(ComponentsDescriptorConstants.COMPOSITE_COMPONENT_TAG,
                    handler);
            }
            if (name.equals(
                        ComponentsDescriptorConstants.PRIMITIVE_COMPONENT_TAG)) {
                addHandler(ComponentsDescriptorConstants.PRIMITIVE_COMPONENT_TAG,
                    new PrimitiveComponentHandler(deploymentDescriptor,
                        componentsCache, componentTypes));
            }
            if (name.equals(
                        ComponentsDescriptorConstants.PARALLEL_COMPOSITE_COMPONENT_TAG)) {
                ParallelCompositeComponentHandler handler = new ParallelCompositeComponentHandler(deploymentDescriptor,
                        componentsCache, componentTypes, this);
                addHandler(ComponentsDescriptorConstants.PARALLEL_COMPOSITE_COMPONENT_TAG,
                    handler);
                getContainerElementHierarchy().addChildContainerHandler(handler);
            }
        }
        super.startElement(name, attributes);
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

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
     */
    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
        // Auto-generated method stub
    }
}
