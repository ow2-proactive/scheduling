/*
 * Created on Oct 8, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.xml;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Morel
 */
public abstract class ComponentHandler extends AbstractUnmarshallerDecorator {
	public static Logger logger = Logger.getLogger(ComponentHandler.class.getName());

	protected ComponentParameters componentParameters;
	protected String virtualNode;
	protected ProActiveDescriptor deploymentDescriptor;
	protected ComponentsCache componentsCache;
	protected HashMap componentTypes;
	TypeFactory typeFactory = ProActiveTypeFactory.instance();

	/**
	 * 
	 */
	public ComponentHandler(
		ProActiveDescriptor deploymentDescriptor,
		ComponentsCache componentsCache,
		HashMap componentTypes) {
		componentParameters = new ComponentParameters();
		this.deploymentDescriptor = deploymentDescriptor;
		this.componentsCache = componentsCache;
		this.componentTypes = componentTypes;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
	 */
	protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
	 */
	public Object getResultObject() throws SAXException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
	 */
	public void startContextElement(String name, Attributes attributes) throws SAXException {
		logger.debug("start context element");

		String component_name = attributes.getValue(ComponentsDescriptorConstants.COMPONENT_NAME_TAG);
		if (!checkNonEmpty(component_name)) {
			throw new SAXException("component's name unspecified");
		}
		String virtual_node = attributes.getValue(ComponentsDescriptorConstants.COMPONENT_VIRTUAL_NODE_TAG);
		if (!checkNonEmpty(virtual_node)) {
			throw new SAXException("component's virtual node unspecified");
		}
		String type_name = attributes.getValue(ComponentsDescriptorConstants.COMPONENT_TYPE_ATTRIBUTE_TAG);
		if (!checkNonEmpty(type_name)) {
			throw new SAXException("name of component type unspecified");
		}
		componentParameters.setName(component_name);
		virtualNode = virtual_node;
		componentParameters.setComponentType((ComponentType)componentTypes.get(type_name));
	}

}
