/*
 * Created on Feb 3, 2004
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component;

import java.io.Serializable;


/**
 * Fractal implementation-specific description of  the controllers of components.
 * It is currently used to specify the hierarchical type and the name of the components. 
 * @author Matthieu Morel
 */
public class ControllerDescription implements Serializable {

	private String hierarchicalType;
	private String name;

	/**
	 * a no-arg constructor (used in the ProActive parser)
	 *
	 */
	public ControllerDescription() {
		hierarchicalType = null;
		name = null;
	}
	
	/**
	 * constructor
	 * @param name the name of the component
	 * @param hierarchicalType the hierachical type of the component. See {@link Constants}
	 */
	public ControllerDescription(String name, String hierarchicalType) {
		this.hierarchicalType = hierarchicalType;
		this.name = name;
	}
	
	/**
	 * copy constructor (clones the object)
	 * @param controllerDesc the ControllerDescription to copy.
	 */
	public ControllerDescription(ControllerDescription controllerDesc) {
		hierarchicalType = new String(controllerDesc.getHierarchicalType());
		name = new String(controllerDesc.getName());
	}
	
	/**
	 * Returns the hierarchicalType.
	 * @return String
	 */
	public String getHierarchicalType() {
		return hierarchicalType;
	}

	
	/**
	 * setter for hierarchical type
	 * @param string hierarchical type. See {@link Constants}
	 */
	public void setHierarchicalType(String string) {
		hierarchicalType = string;
	}
	
	/**
	 * getter for the name
	 * @return the name of the component
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * setter for the name
	 * @param name name of the component
	 */
	public void setName(String name) {
		this.name = name;
	}
	


}
