package org.objectweb.proactive.core.component.type;

import java.io.Serializable;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.factory.InstantiationException;
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
	public ProActiveComponentType(final InterfaceType[] interfaceTypes) throws InstantiationException{
		this.interfaceTypes = clone(interfaceTypes); 
		// verifications
		for (int i = 0; i < interfaceTypes.length; ++i) {
		  String p = interfaceTypes[i].getFcItfName();
		  boolean collection = interfaceTypes[i].isFcCollectionItf();
		  for (int j = i + 1; j < interfaceTypes.length; ++j) {
			String q = interfaceTypes[j].getFcItfName();
			if (p.equals(q)) {
			  throw new InstantiationException(
				 "Two interfaces have the same name '" + q + "'");
			}
			if (collection && q.startsWith(p)) {
			  throw new InstantiationException(
				"The name of the interface '" + q + "' starts with '" +
				p + "', which is the name of a collection interface");
			}
			if (interfaceTypes[j].isFcCollectionItf() && p.startsWith(q)) {
			  throw new InstantiationException(
				"The name of the interface '" + p + "' starts with '" +
				q + "', which is the name of a collection interface");
			}
		  }
		}		
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
	
	/**
	 * Returns a copy of the given interface type array. This method is used to
	 * return a copy of the field of this class, instead of the field itself, so
	 * that its content can not be modified from outside.
	 *
	 * @param types the array to be cloned.
	 * @return a clone of the given array, or an empty array if <tt>type</tt> is
	 *      <tt>null</tt>.
	 */

	private static InterfaceType[] clone (final InterfaceType[] types) {
	  if (types == null) {
		return new InterfaceType[0];
	  } else {
		InterfaceType[] clone = new InterfaceType[types.length];
		System.arraycopy(types, 0, clone, 0, types.length);
		return clone;
	  }
	}
}