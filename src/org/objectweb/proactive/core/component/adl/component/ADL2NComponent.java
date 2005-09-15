package org.objectweb.proactive.core.component.adl.component;

import java.util.Vector;

import org.objectweb.fractal.api.Component;

/**
 * Represents a component with the list of interfaces
 * and a lotos file used for behavioural specifications.
 * @author Daedar
 *
 */
public interface ADL2NComponent extends Component{
	/**
	 * All the interfaces binded on this component
	 * @return Array of Interfaces binded on this component
	 */
	public Vector getInterfaces();
	
	/**
	 * Set the list of interfaces available on this component
	 * @param interfaces List of interfaces
	 */
	public void setInterface(Vector interfaces);
	
	/**
	 * Gets an interface of name name associated to this component
	 * @param name Name of the interface
	 * @return Interface of name name
	 */
	public ADL2NInterface getInterfaceByName(String name);
	
	/**
	 * Add an interface to this component  (virtual)
	 * @param i Interface to add
	 */
	public void addInterface(ADL2NInterface i);
	
	/**
	 * Remove an interface to this component  (virtual)
	 * @param i Interface to add
	 */
	public void removeInterface(ADL2NInterface i);
	
	/**
	 * Gets all sub components of this component
	 * @return Array of sub components
	 */
	public Vector getComponents();

	/**
	 * Adds a sub component to this component
	 * @param component Sub components to add
	 */
	public void addComponent(ADL2NComponent component);

	/**
	 * Removes a sub component of this component
	 * @param component Component to remove
	 */
	public void removeComponent(ADL2NComponent component);
	
	/**
	 * Lotos file associated with this component (for behavioural specifications)
	 * @return Lotos file on the current file system
	 */
	public String getLotosFile();
	
	/**
	 * Sets the lotos file associated with this component (for behavioural specifications)
	 * @param lotosFile Lotos file on the current file system
	 */
	public void setLotosFile(String lotosFile);

	/**
	 * Gets the name of this component
	 * @return Name of the component
	 */
	public String getName();
	
	/**
	 * Gets the definition of this component
	 * @return Definition of the component
	 */
	public String getDefinition();
	
	/**
	 * Gets the type of the component. The integer
	 * returned is unique for each type of component.
	 * @return Type of the component
	 */
	public int getType();
	
	/**
	 * Sets the type of the component.
	 * @param type Type of the component(same for
	 * differents instances of the same component)
	 */
	public void setType(int type);
	
	/**
	 * Tests if the component is primitive or not
	 * @return True if this component is a primitive
	 */
	public boolean isPrimitive();
	
	/**
	 * Tests if the component is composite or not
	 * @return True if this component is a composite
	 */
	public boolean isComposite();
	
	/**
	 * Return the number of the method method, usefull for FC2 generator
	 * @param method Method to seek
	 * @return Number of the method
	 */
	public int getMethodNumber(String method);

	/**
	 * Search a component by his name
	 * @param name name of the component
	 * @return Component found or null if no component found
	 */
	public ADL2NComponent searchForComponent(String name);

	/** 
	 * Brief description of the component
	 * @return String representation of the component
	 */
	public String toShortString();
	
	/**
	 * Returns true if this component can have multiple instances
	 * @return true if this component can have multiple instances
	 */
	public boolean isMultiple();
	
	/**
	 * Sets this component as a multiple or single component
	 * @param mult True if this component can have multiple instances
	 */
	public void setMultiple(boolean mult);

	/**
	 * Sets the Id of the instance of the component
	 *
	 */
	public void setInstances(int IID);
	
	/**
	 * Gets the Id of the instance of the component
	 *
	 */
	public int getInstances();
	
	/**
	 * Used for LotosDecompilation to know if a component
	 * is already in the tree or not
	 * @return Component's already in the tree
	 */
	public boolean isMarked();
	
	/**
	 * Used for LotosDecompilation to know if a component
	 * is already in the tree or not
	 */
	public void setMarked();
}
