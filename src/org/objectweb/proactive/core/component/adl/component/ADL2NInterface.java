package org.objectweb.proactive.core.component.adl.component;

import java.lang.reflect.Method;
import java.util.Vector;


/**
 * Represents an interface of a parsed Component
 * @author Dalmasso Nicolas
 *
 */
public interface ADL2NInterface {
	/**
	 * Gets the name of the interface
	 * @return Name of this interface
	 */
	public String getName();
	
	/**
	 * Gets the components wich this interface belong to
	 * @return Component associated with this interface
	 */
	public ADL2NComponent getComponent();
	
	/**
	 * Sets the components wich this interface belong to
	 * @param component Component wich this interface belong to
	 */
	public void setComponent(ADL2NComponent component);
	
	/**
	 * Set the name of the interface
	 * @param name Name of this interface
	 */
	public void setName(String name);
	
	/**
	 * Get all methods on this interface
	 * @return Array of java.lang.reflect.Method on this interface
	 */
	public Method[] getMethods();
	
	/**
	 * Add a method in the vectors of methods on this interface
	 * @param method Method added in the vector of methods on this interface
	 */ 
	public void addMethod(Method method);
	
	/**
	 * Removes a method in the vectors of methods on this interface
	 * @param method Method removed in the vector of methods on this interface
	 */
	public void removeMethod(Method method);
	
	/**
	 * Is this a client or a server interface
	 * @return True if this interface is a client interface
	 */
	public boolean isClientInterface();
	
	/**
	 * Defines this interface as a client interface
	 *
	 */
	public void setInterfaceAsClient();
	
	/**
	 * Defines this interface as a server interface
	 *
	 */
	public void setInterfaceAsServer();
	
	/**
	 * Add a binding beetween this interface and the one passed by parameter
	 * @param itf Interface to bind with this one
	 */
	public void addBinding(ADL2NInterface itf,boolean isClient);

	/**
	 * Gets all interfaces binded with this component
	 * @return Array of interfaces
	 */
	public Vector getBindings();
	
	/**
	 * Sets an interface as client or server one
	 * @param b Role of this interface
	 */
	public void setRole(boolean b);
	
}
