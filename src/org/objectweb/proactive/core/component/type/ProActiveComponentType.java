package org.objectweb.proactive.core.component.type;

import java.io.Serializable;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;

/**
 * Implementation of ComponentType (@see org.objectweb.fractal.api.type.ComponentType)
 * 
 * @author Matthieu Morel
 *
 */
public class ProActiveComponentType implements ComponentType, Serializable {
	protected static Logger logger = Logger.getLogger(ProActiveComponentType.class.getName());

	/**
	* The types of the interfaces of components of this type.
	*/
	private final InterfaceType[] interfaceTypes;

	/**
	 * Constructor for ProActiveComponentType.
	 */
	public ProActiveComponentType(final InterfaceType[] interfaceTypes) {
		this.interfaceTypes = interfaceTypes; //rem : julia uses a clone method		
	}
	
	/**
	 * copy constructor
	 */
	public ProActiveComponentType(final ComponentType componentType) {
		InterfaceType[] tempItfTypes = componentType.getFcInterfaceTypes();
		this.interfaceTypes = new InterfaceType[tempItfTypes.length];
		for (int i=0; i<interfaceTypes.length; i++) {
			// deep copy
			interfaceTypes[i] = new ProActiveInterfaceType(tempItfTypes[i]);
		}
	}

	/**
	 * @see org.objectweb.fractal.api.type.ComponentType#getFcInterfaceTypes()
	 */
	public InterfaceType[] getFcInterfaceTypes() {
		return interfaceTypes;
	}

	/**
	 * @see org.objectweb.fractal.api.type.ComponentType#getFcInterfaceType(String)
	 */
	public InterfaceType getFcInterfaceType(String name) throws NoSuchInterfaceException{
		for (int i = 0; i < interfaceTypes.length; i++) {
			InterfaceType type = interfaceTypes[i];
			if (type.getFcItfName().equals(name)) {
				return type;
			}
		}
		throw new NoSuchInterfaceException(name);
	}

	/**
	 * @see org.objectweb.fractal.api.Type#isFcSubTypeOf(Type)
	 */
	public boolean isFcSubTypeOf(Type type) {
		throw new RuntimeException("not yet implemented");
	}
}