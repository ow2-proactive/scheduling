/*
 * Created on Oct 7, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.xml;

/**
 * xml tags of the component descriptors.
 * 
 * @author Matthieu Morel
 */
public interface ComponentsDescriptorConstants {

	public static final String TYPES_TAG = "types";
	public static final String COMPONENT_TYPE_TAG = "component-type";
	public static final String COMPONENT_TYPE_NAME_TAG = "name";
	public static final String COMPONENTS_DESCRIPTOR_TAG = "componentsDescriptor";
	public static final String COMPONENTS_TAG = "components";
	public static final String COMPOSITE_COMPONENT_TAG = "composite-component";
	public static final String COMPONENT_NAME_TAG = "name"; // would that work?
	public static final String COMPONENT_VIRTUAL_NODE_TAG = "virtualNode";
	public static final String COMPONENT_TYPE_ATTRIBUTE_TAG = "type";
	public static final String PROVIDES_TAG = "provides";
	public static final String INTERFACE_TAG = "interface";
	public static final String INTERFACE_NAME_TAG = "name";
	public static final String INTERFACE_SIGNATURE_TAG = "signature";
	public static final String INTERFACE_CARDINALITY_TAG = "cardinality";
	public static final String INTERFACE_CARDINALITY_SINGLE_TAG = "single";
	public static final String INTERFACE_CARDINALITY_COLLECTIVE_TAG = "single";
	public static final String INTERFACE_CONTINGENCY_TAG = "contingency";
	public static final String INTERFACE_CONTINGENCY_MANDATORY_TAG = "mandatory";
	public static final String INTERFACE_CONTINGENCY_OPTIONAL_TAG = "mandatory";
	public static final String REQUIRES_TAG = "requires";
	public static final String PARALLEL_COMPOSITE_COMPONENT_TAG = "parallel-composite-component";
	public static final String PRIMITIVE_COMPONENT_TAG = "primitive-component";
	public static final String PRIMITIVE_COMPONENT_IMPLEMENTATION_TAG = "implementation";
	public static final String BINDINGS_TAG = "bindings";
	public static final String BINDING_TAG = "binding";
	public static final String BINDING_CLIENT_TAG = "client";
	public static final String BINDING_SERVER_TAG = "server";
	public static final String NULL = "null";

}
