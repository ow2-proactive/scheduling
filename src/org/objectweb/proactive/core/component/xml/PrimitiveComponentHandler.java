/*
 * Created on Oct 13, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.xml;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Morel
 */
public class PrimitiveComponentHandler extends ComponentHandler {
	public static Logger logger = Logger.getLogger(PrimitiveComponentHandler.class.getName());

	/**
	 * @param deploymentDescriptor
	 */
	public PrimitiveComponentHandler(
		ProActiveDescriptor deploymentDescriptor,
		ComponentsCache componentsCache,
		HashMap componentTypes) {
		super(deploymentDescriptor, componentsCache, componentTypes);
		componentParameters.setHierarchicalType(ComponentParameters.PRIMITIVE);
	}

	public void startContextElement(String name, Attributes attributes) throws SAXException {
		logger.debug("startContextElement : " + name);

		super.startContextElement(name, attributes);

		String implementation =
			attributes.getValue(ComponentsDescriptorConstants.PRIMITIVE_COMPONENT_IMPLEMENTATION_TAG);
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
				vn = deploymentDescriptor.getVirtualNode(virtualNode);
			} catch (NullPointerException npe) {
				logger.fatal(
					"Could not find virtual node. Maybe virtual node names do not match between components descriptor and deployment descriptor");
				return;
			}
			Node[] nodes = vn.getNodes();
			if (nodes.length == 0) {
				throw new NodeException("No node defined for virtual node " + vn.getName());
			}
			if (nodes.length == 1) {
				//					componentsCache.addComponent(componentParameters.getName(),
				//					//PrimitiveComponentB.class.getName(),
				componentsCache.addComponent(componentParameters.getName(),
				//PrimitiveComponentB.class.getName(),
				ProActive.newActiveComponent(implementation, new Object[] {
				},
				//nodes[0], 
				null, null, null, componentParameters));
			} else {

				logger.debug(
					"**************************************************************\n"
						+ "Deployment of a primitive component on a cyclic virtual node will result in the"
						+ "creation of one instance of the primitive on each node, with an extended name of type : "
						+ "primitiveName-cyclicInstanceNumber-theNumber"
						+ "**************************************************************\n");
				// loop on nodes
				String original_component_name = componentParameters.getName();
				for (int i = 0; i < nodes.length; i++) {
					// add an index on the name of the component
					//					componentParameters.setName(original_component_name + "-cyclicInstanceNumber-" + i);
					//					componentsCache
					//						.addComponent(
					//							componentParameters.getName(),
					//							ProActive.newActiveComponent(implementation, new Object[] {
					//					}, nodes[i], null, null, componentParameters));
					//					FIXME local deployment for debugging 
					componentParameters.setName(original_component_name + "-cyclicInstanceNumber-" + i);
					componentsCache
						.addComponent(
							componentParameters.getName(),
							ProActive.newActiveComponent(implementation, new Object[] {
					}, null, null, null, componentParameters));

				}
			}
		} catch (NodeException ne) {
			logger.error("cannot create active component: node exception : " + ne.getMessage());
			ne.printStackTrace();
		} catch (ActiveObjectCreationException aoce) {
			logger.error("cannot create active component : active object creation exception");
			aoce.printStackTrace();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("created primitive component : " + componentParameters.getName());
		}
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
	 */
	public Object getResultObject() throws SAXException {
		return componentParameters.getName();
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
	 */
	protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws SAXException {
	}
}
