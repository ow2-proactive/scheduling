package org.objectweb.proactive.core.component;

import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.type.ProActiveComponentType;

/** Contains the configuration of a component :
 * - type
 * - interfaces (server and client)
 * - name
 * - hierarchical type (primitive or composite)
 */
public class ComponentParameters implements Serializable {
	protected static Logger logger = Logger.getLogger(ComponentParameters.class.getName());
	//private String name;
	private Object stubOnReifiedObject;
	private ComponentType componentType;
	private ControllerDescription controllerDesc;
	
	/** Constructor for ComponentParameters.
	 * @param name the name of the component
	 * @param hierarchicalType the hierarchical type, either PRIMITIVE or COMPOSITE or PARALLEL
	 * @param componentType
	 */
//	public ComponentParameters(String name, String hierarchicalType, ComponentType componentType) {
//		this.name = name;
//		this.hierarchicalType = hierarchicalType;
//		this.componentType = componentType;
//		//interfaceTypes = componentType.getFcInterfaceTypes();
//	}

	public ComponentParameters (String name, String hierarchicalType, ComponentType componentType) {
		this(componentType, new ControllerDescription(name, hierarchicalType));
	}
	
	public ComponentParameters(ComponentType componentType, ControllerDescription controllerDesc) {
		this.componentType = componentType;
		this.controllerDesc = controllerDesc;
	}

	public ComponentParameters() {
		controllerDesc = new ControllerDescription();
	}
	
	/**
	 * copy constructor
	 * @param componentParameters
	 */
	public ComponentParameters(final ComponentParameters componentParameters) {
		this.componentType = new ProActiveComponentType(componentParameters.getComponentType());
		this.controllerDesc = new ControllerDescription(componentParameters.getControllerDescription());
	}
	

	/**
	 * setter for the name
	 * @param name name of the component
	 */
	public void setName(String name) {
		controllerDesc.setName(name);
	}

	/**
	 * Returns the componentType.
	 * @return ComponentType
	 */
	public ComponentType getComponentType() {
		return componentType;
	}
	
	public ControllerDescription getControllerDescription() {
		return controllerDesc;
	}
	
	public void setComponentType(ComponentType componentType) {
		this.componentType = componentType;
	}

	/**
	 * @param string
	 */
	public void setHierarchicalType(String string) {
		controllerDesc.setHierarchicalType(string);
	}
	
	public String getName() {
		return controllerDesc.getName();
	}


/**
 * Returns the hierarchicalType.
 * @return String
 */
public String getHierarchicalType() {
	return controllerDesc.getHierarchicalType();
}

	/**
	 * @return the types of server interfaces
	 */
	public InterfaceType[] getServerInterfaceTypes() {
		Vector server_interfaces = new Vector();
		InterfaceType[] interfaceTypes = componentType.getFcInterfaceTypes();
		for (int i = 0; i < interfaceTypes.length; i++) {
			if (!interfaceTypes[i].isFcClientItf()) {
				server_interfaces.add(interfaceTypes[i]);
			}
		}
		return (InterfaceType[]) server_interfaces.toArray(new InterfaceType[server_interfaces.size()]);
	}

	/**
	 * @return the types of client interfacess
	 */
	public InterfaceType[] getClientInterfaceTypes() {
		Vector client_interfaces = new Vector();
		InterfaceType[] interfaceTypes = componentType.getFcInterfaceTypes();
		for (int i = 0; i < interfaceTypes.length; i++) {
			if (interfaceTypes[i].isFcClientItf()) {
				client_interfaces.add(interfaceTypes[i]);
			}
		}
		return (InterfaceType[]) client_interfaces.toArray(new InterfaceType[client_interfaces.size()]);
	}


	/**
	 * accessor on the standard ProActive stub
	 * @return standard ProActive stub on the reified object
	 */
	public Object getStubOnReifiedObject() {
		return stubOnReifiedObject;
	}

	/**
	 * keeps a reference on the standard ProActive stub
	 * @param ref on an instance of a standard ProActive stub on the reified object
	 */
	public void setStubOnReifiedObject(Object object) {
		stubOnReifiedObject = object;
	}

	/**
	 * @return
	 */
	public InterfaceType[] getInterfaceTypes() {
		return componentType.getFcInterfaceTypes();
	}

	/**
	 * @param types
	 */
	public void setInterfaceTypes(InterfaceType[] types) {
		throw new ProActiveRuntimeException("setInterfaceTypes should not be called anymore. Use ComponentType instead"); 
		//interfaceTypes = types;
	}

	public void addInterfaceType(InterfaceType type) {
		InterfaceType[] initial_array = getInterfaceTypes();
		InterfaceType[] interfaceTypes = componentType.getFcInterfaceTypes();
		if (initial_array == null) {
			interfaceTypes = new InterfaceType[] { type };
		} else {
			InterfaceType[] new_array = new InterfaceType[initial_array.length + 1];
			System.arraycopy(initial_array, 0, new_array, 0, initial_array.length);
			new_array[new_array.length-1] = type;
			interfaceTypes = new_array;
		}
	}
	
	


}