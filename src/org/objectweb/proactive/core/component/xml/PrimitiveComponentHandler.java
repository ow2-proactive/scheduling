/*
 * Created on Oct 13, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.xml;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Fractal;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
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
        componentParameters.setHierarchicalType(ComponentParameters.PRIMITIVE);
    }

    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
        names = null;
        if (logger.isDebugEnabled()) {
            logger.debug("startContextElement : " + name + "of : " +
                this.componentParameters.getName());
        }

        //		for (int i = 0; i < attributes.getLength(); i++) {
        //			System.out.println("ATTRIBUTES [" + i + "] : " + attributes.getValue(i));
        //		}
        super.startContextElement(name, attributes);

        String implementation = attributes.getValue(ComponentsDescriptorConstants.PRIMITIVE_COMPONENT_IMPLEMENTATION_TAG);
        if (!checkNonEmpty(implementation)) {
            throw new SAXException("component's implementation unspecified");
        }

        // instantiate the component and add a stub on it to the cache
        // if several nodes are mapped onto this virtual node : 
        // instantiate 1 component on each of the nodes of the cycle - if cyclic
        try {
            // get corresponding virtual node
            VirtualNode vn = null;
            try {
                if (virtualNode.equals(ComponentsDescriptorConstants.NULL)) {
                    //					componentsCache.addComponent(componentParameters.getName(),
                    //					//PrimitiveComponentB.class.getName(),
                    componentsCache.addComponent(componentParameters.getName(),
                        //PrimitiveComponentB.class.getName(),
                    ProActive.newActiveComponent(implementation,
                            new Object[] {  }, null, null, null,
                            componentParameters));
                    return;
                }
                vn = deploymentDescriptor.getVirtualNode(virtualNode);
            } catch (NullPointerException npe) {
                logger.fatal(
                    "Could not find virtual node. Maybe virtual node names do not match between components descriptor and deployment descriptor");
                return;
            }
            Node[] nodes = vn.getNodes();
            if (nodes.length == 0) {
                throw new NodeException("No node defined for virtual node " +
                    vn.getName());
            }
            if ((nodes.length == 1)) {
                //					componentsCache.addComponent(componentParameters.getName(),
                //					//PrimitiveComponentB.class.getName(),
                componentsCache.addComponent(componentParameters.getName(),
                    //PrimitiveComponentB.class.getName(),
                ProActive.newActiveComponent(implementation, new Object[] {  },
                        vn.getNode(), null, null, componentParameters));
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "**************************************************************\n" +
                        "Deployment of a primitive component on a cyclic virtual node will result in the" +
                        "creation of one instance of the primitive on each node, with an extended name of type : " +
                        "primitiveName-cyclicInstanceNumber-theNumber" +
                        "**************************************************************\n");
                }
                Component[] primitives = null;
                primitives = ProActive.newActiveComponent(implementation,
                        new Object[] {  }, vn, componentParameters);
                if (primitives != null) {
                    names = new String[primitives.length];
                    for (int i = 0; i < primitives.length; i++) {
                        // consider all generated names
                        names[i] = Fractal.getComponentParametersController(primitives[i])
                                          .getComponentParameters().getName();
                        componentsCache.addComponent(names[i], primitives[i]);
                    }
                }
            }
        } catch (NodeException ne) {
            logger.error("cannot create active component: node exception : " +
                ne.getMessage());
            ne.printStackTrace();
        } catch (ActiveObjectCreationException aoce) {
            logger.error(
                "cannot create active component : active object creation exception");
            aoce.printStackTrace();
        } catch (NoSuchInterfaceException e) {
            logger.error(
                "cannot create active component : interface not found " +
                e.getMessage());
            e.printStackTrace();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("created primitive component : " +
                componentParameters.getName());
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
            return new ComponentResultObject(componentParameters.getName());
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
     */
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
    }
}
