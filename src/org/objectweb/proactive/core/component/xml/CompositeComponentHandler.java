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
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.InstantiationException;

import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * @author Matthieu Morel
 */
public class CompositeComponentHandler extends AbstractContainerComponentHandler {
    public static Logger logger = Logger.getLogger(CompositeComponentHandler.class.getName());
    private List subComponents;

    /**
     * @param deploymentDescriptor
     * @param componentsCache
     */
    public CompositeComponentHandler(ProActiveDescriptor deploymentDescriptor,
        ComponentsCache componentsCache, HashMap componentTypes,
        ComponentsHandler fatherHandler) {
        super(deploymentDescriptor, componentsCache, componentTypes,
            fatherHandler);
        controllerDescription.setHierarchicalType(Constants.COMPOSITE);
        addHandler(ComponentsDescriptorConstants.BINDINGS_TAG,
            new BindingsHandler(componentsCache));
    }

    /**
     * see {@link org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)}
     */
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
        if (getContainerElementHierarchy().containsChild(activeHandler)) {
            enable();
        }
        if (isEnabled()) {
            Component composite;
            if (name.equals(ComponentsDescriptorConstants.COMPONENTS_TAG)) {
                // then instantiate the component and add a stub on it to the cache
                try {
                    if (virtualNode.equals(ComponentsDescriptorConstants.NULL)) {
                        composite = cf.newFcInstance(componentType,
                                new ControllerDescription(controllerDescription.getName(),
                                    controllerDescription.getHierarchicalType()),
                                new ContentDescription(Composite.class.getName(),
                                    new Object[] {  }));
                        componentsCache.addComponent(controllerDescription.getName(),
                            composite);
                    } else {
                        VirtualNode vn = deploymentDescriptor.getVirtualNode(virtualNode);
                        vn.activate();
                        if (vn.getNodeCount() == 0) {
                            throw new NodeException(
                                "no node defined for the virtual node " +
                                vn.getName());
                        }
                        if (logger.isDebugEnabled()) {
                            if (vn.getNodeCount() > 1) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                        "creating a composite component on a virtual node mapped onto several nodes will actually create the component on the first retreived node");
                                }
                            }
                        }

                        // get corresponding node (1st node retreived if the vn is multiple)
                        Node targeted_node = vn.getNode();
                        composite = cf.newFcInstance(componentType,
                                new ControllerDescription(controllerDescription.getName(),
                                    controllerDescription.getHierarchicalType()),
                                new ContentDescription(Composite.class.getName(),
                                    new Object[] {  }, targeted_node));
                        componentsCache.addComponent(controllerDescription.getName(),
                            composite);
                    }

                    // add sub components
                    List sub_components = (List) getHandler(name)
                                                     .getResultObject();
                    Iterator iterator = sub_components.iterator();
                    while (iterator.hasNext()) {
                        String sub_component_name = (String) iterator.next();
                        if (logger.isDebugEnabled()) {
                            logger.debug("adding sub component : " +
                                sub_component_name);
                        }
                        ((ContentController) composite.getFcInterface(Constants.CONTENT_CONTROLLER)).addFcSubComponent(componentsCache.getComponent(
                                sub_component_name));
                    }
                } catch (InstantiationException e) {
                    logger.error("cannot instantiate component");
                    throw new SAXException(e);
                } catch (NodeException ne) {
                    logger.error(
                        "cannot create active component: node exception");
                    throw new SAXException(ne);
                } catch (NoSuchInterfaceException nsie) {
                    logger.error(
                        "cannot create active component : interface not found");
                    throw new SAXException(nsie);
                } catch (IllegalLifeCycleException ilce) {
                    logger.error(
                        "cannot create active component : illegal life cycle operation");
                    throw new SAXException(ilce);
                } catch (IllegalContentException ice) {
                    logger.error(
                        "cannot create active component : illegal content operation");
                    throw new SAXException(ice);
                }
                logger.debug("created composite component : " +
                    controllerDescription.getName());
            }
        }
    }

    /**
     * see {@link org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()}
     */
    public Object getResultObject() throws SAXException {
        return new ComponentResultObject(controllerDescription.getName());
    }

    /**
     * see {@link org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)}
     */
    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
        if (isEnabled()) {
            super.startContextElement(name, attributes);
        }
    }
}
