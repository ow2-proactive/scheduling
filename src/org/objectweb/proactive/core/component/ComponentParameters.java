package org.objectweb.proactive.core.component;

import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.ProActiveRuntimeException;

/** Contains the configuration of a component :
 * - type
 * - interfaces (server and client)
 * - name
 * - hierarchical type (primitive or composite)
 */
public class ComponentParameters implements Serializable {
	protected static Logger logger = Logger.getLogger(ComponentParameters.class.getName());
	public final static String COMPOSITE = "composite";
	public final static String PRIMITIVE = "primitive";
	public final static String PARALLEL = "parallel-composite";
	private String name;
	private Object stubOnReifiedObject;
	private ComponentType componentType;
	//private InterfaceType[] interfaceTypes = null;
	private String hierarchicalType = null;

	/** Constructor for ComponentParameters.
	 * @param name the name of the component
	 * @param hierarchicalType the hierarchical type, either PRIMITIVE or COMPOSITE or PARALLEL
	 * @param componentType
	 */
	public ComponentParameters(String name, String hierarchicalType, ComponentType componentType) {
		this.name = name;
		this.hierarchicalType = hierarchicalType;
		this.componentType = componentType;
		//interfaceTypes = componentType.getFcInterfaceTypes();
	}

	public ComponentParameters() {
	}
	/**
	 * Returns the generatedClassName.
	 * @return String
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

	/**
	 * Returns the componentType.
	 * @return ComponentType
	 */
	public ComponentType getComponentType() {
		return componentType;
	}
	
	public void setComponentType(ComponentType componentType) {
		this.componentType = componentType;
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
	 * @return the types of client interfaces
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
	 * Returns the hierarchicalType.
	 * @return String
	 */
	public String getHierarchicalType() {
		return hierarchicalType;
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
	 * @param string
	 */
	public void setHierarchicalType(String string) {
		hierarchicalType = string;
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

	public void addInterfaceTypes(InterfaceType[] types) {
		throw new ProActiveRuntimeException("addInterfaceTypes should not be called anymore. Use ComponentType instead");
//		InterfaceType[] initial_array = getInterfaceTypes();
//		if (initial_array == null) {
//			interfaceTypes = types;
//		} else {
//			InterfaceType[] old_array = interfaceTypes;
//			InterfaceType[] new_array = types;
//			InterfaceType[] resulting_array = new InterfaceType[interfaceTypes.length + types.length];
//			System.arraycopy(old_array, 0, new_array, 0, old_array.length);
//			System.arraycopy(new_array, 0, resulting_array, old_array.length + 1, new_array.length);
//			interfaceTypes = resulting_array;
//		}
	}
}