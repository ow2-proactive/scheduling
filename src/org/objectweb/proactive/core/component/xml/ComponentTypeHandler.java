/*
 * Created on Oct 29, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.xml;

import java.util.Vector;

import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Morel
 */
public class ComponentTypeHandler extends AbstractUnmarshallerDecorator {
	Vector interfaceTypes;
	String typeName;

	public ComponentTypeHandler() {
		interfaceTypes = new Vector();
		addHandler(ComponentsDescriptorConstants.PROVIDES_TAG, new ProvidesHandler());
		addHandler(ComponentsDescriptorConstants.REQUIRES_TAG, new RequiresHandler());
	}
	
	/**
	 * resets the variables so that this handler can be reused properly for the next type
	 * occurence
	 */
	public void reset() {
		interfaceTypes.removeAllElements();
		typeName=null;
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
		try {
		// return a (typeName / ComponentType instance) couple.
		interfaceTypes.trimToSize();
		InterfaceType[] itf_types = (InterfaceType[])interfaceTypes.toArray(new InterfaceType[interfaceTypes.size()]);
		return new Object[] {typeName, ProActiveTypeFactory.instance().createFcType(itf_types)};	
		} catch (InstantiationException e) {
			throw new SAXException ("cannot create component type");
		}
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
	 */
	public void startContextElement(String name, Attributes attributes) throws SAXException {
		String type_name = attributes.getValue(ComponentsDescriptorConstants.COMPONENT_TYPE_NAME_TAG);
		if (!checkNonEmpty(type_name)) {
			throw new SAXException("the name of the component type needs to be specified");
		}
		typeName = type_name;
	}

	//	----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	// provides has no attributes, but contains several interface elements
	private class ProvidesHandler extends CollectionUnmarshaller {
		private ProvidesHandler() {
			addHandler(
				ComponentsDescriptorConstants.INTERFACE_TAG,
				new InterfaceHandler(ComponentsDescriptorConstants.PROVIDES_TAG));
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

	}

	//	----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	// requires has no attributes, but contains several interface elements
	private class RequiresHandler extends CollectionUnmarshaller {
		private RequiresHandler() {
			addHandler(
				ComponentsDescriptorConstants.INTERFACE_TAG,
				new InterfaceHandler(ComponentsDescriptorConstants.REQUIRES_TAG));
		}

		/* (non-Javadoc)
		 * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
		 */
		protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws SAXException {
		}
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	protected class InterfaceHandler extends BasicUnmarshaller {
		boolean isClient;
		boolean contingency;
		boolean cardinality;

		private InterfaceHandler(String role) {
			if (role.equals(ComponentsDescriptorConstants.REQUIRES_TAG)) {
				isClient = true;
			} else if (role.equals(ComponentsDescriptorConstants.PROVIDES_TAG)) {
				isClient = false;
			} else {
				throw new RuntimeException("none of the following tags was found : " + ComponentsDescriptorConstants.REQUIRES_TAG + ',' + ComponentsDescriptorConstants.PROVIDES_TAG);
			}
		}

		public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
			ComponentHandler.logger.debug("#####startContextElement : " + name);
			// store all data in a FunctionalInterfaceData object

			String itf_name = attributes.getValue(ComponentsDescriptorConstants.INTERFACE_NAME_TAG);
			if (!checkNonEmpty(itf_name)) {
				throw new SAXException("interface name unspecified");
			}
			String itf_signature = attributes.getValue(ComponentsDescriptorConstants.INTERFACE_SIGNATURE_TAG);
			if (!checkNonEmpty(itf_signature)) {
				throw new SAXException("interface signature unspecified");
			}
			String value = attributes.getValue(ComponentsDescriptorConstants.INTERFACE_CONTINGENCY_TAG);
			if (!checkNonEmpty(value)
				|| value.equals(ComponentsDescriptorConstants.INTERFACE_CONTINGENCY_OPTIONAL_TAG)) {
				contingency = TypeFactory.OPTIONAL;
			} else if (value.equals(ComponentsDescriptorConstants.INTERFACE_CONTINGENCY_MANDATORY_TAG)) {
				contingency = TypeFactory.MANDATORY;
			} else {
				throw new SAXException(
					"contingency values are : "
						+ ComponentsDescriptorConstants.INTERFACE_CONTINGENCY_MANDATORY_TAG
						+ ", "
						+ ComponentsDescriptorConstants.INTERFACE_CONTINGENCY_OPTIONAL_TAG
						+ ", or nothing (if schema validation is enabled) (default is "
						+ ComponentsDescriptorConstants.INTERFACE_CONTINGENCY_OPTIONAL_TAG
						+ ")");
			}

			value = attributes.getValue(ComponentsDescriptorConstants.INTERFACE_CARDINALITY_TAG);
			if (checkNonEmpty(value)) {
				if (value.equals(ComponentsDescriptorConstants.INTERFACE_CARDINALITY_SINGLE_TAG)) {
					cardinality = TypeFactory.SINGLE;
				} else if (value.equals(ComponentsDescriptorConstants.INTERFACE_CARDINALITY_COLLECTIVE_TAG)) {
				cardinality = TypeFactory.COLLECTION;
				}
			} else {
				throw new SAXException(
					"cardinality values are : "
						+ ComponentsDescriptorConstants.INTERFACE_CARDINALITY_COLLECTIVE_TAG
						+ ", "
						+ ComponentsDescriptorConstants.INTERFACE_CARDINALITY_SINGLE_TAG
						+ ", or nothing (if schema validation is enabled) (default is "
						+ ComponentsDescriptorConstants.INTERFACE_CARDINALITY_SINGLE_TAG
						+ ") ");
			}

			InterfaceType itf_type = null;
			try {
				itf_type = ProActiveTypeFactory.instance().createFcItfType(itf_name, itf_signature, isClient, contingency, cardinality);
			} catch (InstantiationException e) {
				throw new SAXException(e);
			}
			//componentParameters.addInterfaceTypes((InterfaceType[]) activeHandler.getResultObject());
			if (logger.isDebugEnabled()) {
				ComponentHandler.logger.debug("new interface type added: " + itf_type.getFcItfName());
			}
			
			interfaceTypes.add(itf_type);
			//componentParameters.addInterfaceType(itf_type);
			//setResultObject(itf_type);
		}
	}

}
