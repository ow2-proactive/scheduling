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

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

import org.xml.sax.SAXException;

import java.util.HashMap;


/**
 * @author Matthieu Morel
 */
public abstract class ComponentHandler extends AbstractUnmarshallerDecorator {
    public static Logger logger = Logger.getLogger(ComponentHandler.class.getName());

    protected ControllerDescription controllerDescription;
    protected String virtualNode;
    protected ProActiveDescriptor deploymentDescriptor;
    protected ComponentsCache componentsCache;
    protected HashMap componentTypes;
    protected TypeFactory typeFactory;
    protected GenericFactory cf;
    protected ComponentType componentType = null;

    /**
     *
     */
    public ComponentHandler(ProActiveDescriptor deploymentDescriptor,
        ComponentsCache componentsCache, HashMap componentTypes) {
        controllerDescription = new ControllerDescription();
        this.deploymentDescriptor = deploymentDescriptor;
        this.componentsCache = componentsCache;
        this.componentTypes = componentTypes;
        try {
            Component boot = Fractal.getBootstrapComponent();
            typeFactory = Fractal.getTypeFactory(boot);
            cf = Fractal.getGenericFactory(boot);
        } catch (InstantiationException e1) {
            throw new ProActiveRuntimeException("Cannot find Fractal boot component",
                e1);
        } catch (NoSuchInterfaceException e1) {
            throw new ProActiveRuntimeException("Cannot find Fractal interface",
                e1);
        }
    }

    /**
     * @link {org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)}
     */
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
    }

    /**
     * @link {org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()}
     */
    public Object getResultObject() throws SAXException {
        return null;
    }

    /**
     * @link {org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)}
     */
    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
        String component_name = attributes.getValue(ComponentsDescriptorConstants.COMPONENT_NAME_TAG);
        if (!checkNonEmpty(component_name)) {
            throw new SAXException("component's name unspecified");
        }
        String virtual_node = attributes.getValue(ComponentsDescriptorConstants.COMPONENT_VIRTUAL_NODE_TAG);
        if (!checkNonEmpty(virtual_node)) {
            virtual_node = ComponentsDescriptorConstants.NULL;
        }
        String type_name = attributes.getValue(ComponentsDescriptorConstants.COMPONENT_TYPE_ATTRIBUTE_TAG);
        if (!checkNonEmpty(type_name)) {
            throw new SAXException("name of component type unspecified");
        }
        controllerDescription.setName(component_name);
        virtualNode = virtual_node;
        componentType = ((ComponentType) componentTypes.get(type_name));
    }
}
