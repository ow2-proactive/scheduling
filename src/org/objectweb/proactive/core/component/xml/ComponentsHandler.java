/*
 * Created on Oct 24, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Morel
 */
public class ComponentsHandler extends AbstractUnmarshallerDecorator {

	public static Logger logger = Logger.getLogger(ComponentsHandler.class.getName());

	private ProActiveDescriptor deploymentDescriptor;
	private ComponentsCache componentsCache;
	private HashMap componentTypes;
	private List subComponents;

	public ComponentsHandler(ProActiveDescriptor deploymentDescriptor, ComponentsCache componentsCache, HashMap componentTypes) {
		this.deploymentDescriptor = deploymentDescriptor;
		this.componentsCache = componentsCache;
		this.componentTypes = componentTypes;
		// handlers for the different types of components are added when components are effectively encountered
		// this avoids infinite handlers due to the recursive structure of components 
		addHandler(ComponentsDescriptorConstants.BINDINGS_TAG, new BindingsHandler(componentsCache));
		subComponents = new ArrayList();
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
	 */
	protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws SAXException {
		if (name.equals(ComponentsDescriptorConstants.PRIMITIVE_COMPONENT_TAG)
			|| name.equals(ComponentsDescriptorConstants.COMPOSITE_COMPONENT_TAG)
			|| name.equals(ComponentsDescriptorConstants.PARALLEL_COMPOSITE_COMPONENT_TAG)) {
			// add the name of this sub component to the list
			logger.debug("adding component's name : " + (String)activeHandler.getResultObject());
			subComponents.add((String)activeHandler.getResultObject());
		}
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
	 */
	public Object getResultObject() throws SAXException {
		return subComponents;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
	 */
	public void startContextElement(String name, Attributes attributes) throws SAXException {
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
	public void startElement(String name, Attributes attributes) throws SAXException {
		if (name.equals(ComponentsDescriptorConstants.COMPOSITE_COMPONENT_TAG)) {
			addHandler(
				ComponentsDescriptorConstants.COMPOSITE_COMPONENT_TAG,
				new CompositeComponentHandler(deploymentDescriptor, componentsCache, componentTypes));
		}
		if (name.equals(ComponentsDescriptorConstants.PRIMITIVE_COMPONENT_TAG)) {
			addHandler(
				ComponentsDescriptorConstants.PRIMITIVE_COMPONENT_TAG,
				new PrimitiveComponentHandler(deploymentDescriptor, componentsCache, componentTypes));
		}
		if (name.equals(ComponentsDescriptorConstants.PARALLEL_COMPOSITE_COMPONENT_TAG)) {
			addHandler(
				ComponentsDescriptorConstants.PARALLEL_COMPOSITE_COMPONENT_TAG,
				new PrimitiveComponentHandler(deploymentDescriptor, componentsCache, componentTypes));
		}
		super.startElement(name, attributes);
	}

}
