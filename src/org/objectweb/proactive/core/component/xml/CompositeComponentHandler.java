/*
 * Created on Oct 14, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.xml;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
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
        componentParameters.setHierarchicalType(ComponentParameters.COMPOSITE);
        addHandler(ComponentsDescriptorConstants.BINDINGS_TAG,
            new BindingsHandler(componentsCache));
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
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
                        //					componentsCache.addComponent(componentParameters.getName(),
                        //					//PrimitiveComponentB.class.getName(),
                        composite = ProActive.newActiveComponent(Composite.class.getName(),
                                new Object[] {  }, null, null, null,
                                componentParameters);
                        componentsCache.addComponent(componentParameters.getName(),
                            composite);
                        //PrimitiveComponentB.class.getName(),
                    } else {
                        VirtualNode vn = deploymentDescriptor.getVirtualNode(virtualNode);
                        if (vn.getNodeCount() != 1) {
                            throw new NodeException(
                                "can only create a composite on a single node (currently)");
                        }

                        // get corresponding node
                        Node targeted_node = vn.getNode();
                        composite = ProActive.newActiveComponent(Composite.class.getName(),
                                new Object[] {  }, targeted_node, null, null,
                                componentParameters);
                        componentsCache.addComponent(componentParameters.getName(),
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
                } catch (NodeException ne) {
                    logger.error(
                        "cannot create active component: node exception");
                    ne.printStackTrace();
                } catch (ActiveObjectCreationException aoce) {
                    logger.error(
                        "cannot create active component : active object creation exception");
                    aoce.printStackTrace();
                } catch (NoSuchInterfaceException nsie) {
                    logger.error(
                        "cannot create active component : interface not found");
                    nsie.printStackTrace();
                } catch (IllegalLifeCycleException ilce) {
                    logger.error(
                        "cannot create active component : illegal life cycle operation");
                    ilce.printStackTrace();
                } catch (IllegalContentException ice) {
                    logger.error(
                        "cannot create active component : illegal content operation");
                    ice.printStackTrace();
                }
                logger.debug("created composite component : " +
                    componentParameters.getName());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
     */
    public Object getResultObject() throws SAXException {
        return new ComponentResultObject(componentParameters.getName());
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
     */
    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
        if (isEnabled()) {
            super.startContextElement(name, attributes);
        }
    }
}
