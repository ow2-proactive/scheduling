package org.objectweb.proactive.core.component.adl.behaviour;

/**
 * An AST node interface to define a behaviour implementation.
 */
public interface Behaviour {
	/**
	 * Gets the filename of the behaviour file associated with the component
	 * @return Filename of the behaviour file
	 */
	String getLotos ();
	
	/**
	 * Changes the filename of the behaviour file associated with the component
	 * @param lotos New filename
	 */
	void setLotos (String lotos);
}
