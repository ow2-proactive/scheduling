package org.objectweb.proactive.core.component.exceptions;

import java.util.List;

import org.objectweb.fractal.api.factory.InstantiationException;

/**
 * An exception that is also a container for a list of exceptions generated when instantiating components.
 * This exception may be thrown when instantiating several components simultaneously (using group communication
 * for instance).
 * 
 * @author Matthieu Morel
 *
 */
public class InstantiationExceptionListException extends InstantiationException {

	
	List<InstantiationException> exceptions;
	
	public List<InstantiationException> getExceptionList() {
		return exceptions;
	}

	public InstantiationExceptionListException(String msg) {
		super(msg);
	}
	
	public InstantiationExceptionListException(List<InstantiationException> exceptions) {
		super("The creation of some components failed");
		this.exceptions = exceptions;
	}
	
	
	
	
	

}
