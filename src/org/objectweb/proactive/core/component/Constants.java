/*
 * Created on Dec 4, 2003
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component;

/**
 * Constant strings used throughout the components implementation
 * 
 * @author Matthieu Morel
 */
public interface Constants {
	
	public final static String CONTENT_CONTROLLER = "content-controller";
	public final static String LIFECYCLE_CONTROLLER = "lifecycle-controller";
	public final static String BINDING_CONTROLLER = "binding-controller";
	public final static String COMPONENT_PARAMETERS_CONTROLLER = "component-parameters-controller";
	public final static String ATTRIBUTE_CONTROLLER = "attribute-controller";
	public final static String COMPONENT = "component";
	public final static String CYCLIC_NODE_SUFFIX = "-cyclicInstanceNumber-";
	// hierarchical types of component
	public final static String COMPOSITE = "composite";
	public final static String PRIMITIVE = "primitive";
	public final static String PARALLEL = "parallel-composite";


}
