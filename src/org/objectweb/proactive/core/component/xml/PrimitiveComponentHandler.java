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
import org.objectweb.fractal.api.factory.InstantiationException;

import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

import org.xml.sax.SAXException;

import java.util.HashMap;


/**
 * @author Matthieu Morel
 */
public class PrimitiveComponentHandler extends ComponentHandler {
    public static Logger logger = Logger.getLogger(PrimitiveComponentHandler.class.getName());
    private String[] names; // when deployed on a VN

    /**
     * @param deploymentDescriptor
     */
    public PrimitiveComponentHandler(ProActiveDescriptor deploymentDescriptor,
        ComponentsCache componentsCache, HashMap componentTypes) {
        super(deploymentDescriptor, componentsCache, componentTypes);
        controllerDescription.setHierarchicalType(Constants.PRIMITIVE);
    }

    /**
     * handles the creation of primitive component on a virtual node
     * If the virtual node attribute is set to "null", then the component is created in the current vm
     * If the virtual node is cyclic, several instances of the primitive are created, with suffixed names
     */
    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
        names = null;
        if (logger.isDebugEnabled()) {
            logger.debug("startContextElement : " + name + "of : " +
                controllerDescription.getName());
        }

        //		for (int i = 0; i < attributes.getLength(); i++) {
        //			System.out.println("ATTRIBUTES [" + i + "] : " + attributes.getValue(i));
        //		}
        super.startContextElement(name, attributes);

        String implementation = attributes.getValue(ComponentsDescriptorConstants.PRIMITIVE_COMPONENT_IMPLEMENTATION_TAG);
        if (!checkNonEmpty(implementation)) {
            throw new SAXException("Component's implementation unspecified");
        }

        // check whether the given parameter is actually a class. A possible error is to put an interface
        try {
            if (Class.forName(implementation).isInterface()) {
                throw new SAXException(implementation +
                    " is an interface. You cannot specify an interface as the implementation of a component.");
            }
        } catch (ClassNotFoundException e) {
            throw new SAXException("Specified class for implementation of the component is not in the classpath ",
                e);
        }

        // instantiate the component and add a stub on it to the cache
        // if several nodes are mapped onto this virtual node : 
        // instantiate 1 component on each of the nodes of the cycle - if cyclic
        try {
            // get corresponding virtual node
            VirtualNode vn = null;
            try {
                if (virtualNode.equals(ComponentsDescriptorConstants.NULL)) {
                    componentsCache.addComponent(controllerDescription.getName(),
                        cf.newFcInstance(componentType, controllerDescription,
                            new ContentDescription(implementation,
                                new Object[] {  })));
                    return;
                }
                vn = deploymentDescriptor.getVirtualNode(virtualNode);
            } catch (NullPointerException npe) {
                logger.fatal(
                    "Could not find virtual node. Maybe virtual node names do not match between components descriptor and deployment descriptor");
                return;
            }
            Component components = cf.newFcInstance(componentType,
                    controllerDescription,
                    new ContentDescription(implementation, new Object[] {  }, vn));

            if (!ProActiveGroup.isGroup(components)) {
                // 1. virtual node was corresponding to 1 node ==> only 1 component has been created
                componentsCache.addComponent(controllerDescription.getName(),
                    components);
            } else {
                // 2. virtual node is cyclic; components are created on multiple nodes
                Component[] components_table = (Component[]) (ProActiveGroup.getGroup(components)
                                                                            .toArray(new Component[ProActiveGroup.getGroup(
                            components).size()]));

                // keep the names of all components for binding them later
                names = new String[components_table.length];
                // ordering in iteration is guaranteed as underlying class containing the elements of the group is a List
                for (int i = 0; i < components_table.length; i++) {
                    names[i] = controllerDescription.getName() +
                        Constants.CYCLIC_NODE_SUFFIX + i;
                    componentsCache.addComponent(names[i], components_table[i]);
                }
            }
        } catch (InstantiationException e) {
            logger.error("cannot create active component " + e.getMessage());
            throw new SAXException(e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("created primitive component : " +
                controllerDescription.getName());
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
     */
    public Object getResultObject() throws SAXException {
        if (names != null) {
            // there are several primitives components based on the same name
            return new ComponentResultObject(names);
        } else {
            return new ComponentResultObject(controllerDescription.getName());
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
     */
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
    }
}
