
package org.objectweb.proactive.core.component.exceptions;


/**
 * Exception thrown when a named component cannot be found
 * 
 * @author Matthieu Morel
 *
 */
public class NoSuchComponentException extends Exception {

	/**
	 * 
	 */
	public NoSuchComponentException() {
		super();
		
	}

	/**
	 * @param message
	 */
	public NoSuchComponentException(String message) {
		super(message);
		
	}


}
