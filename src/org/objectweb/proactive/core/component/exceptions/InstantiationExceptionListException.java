package org.objectweb.proactive.core.component.exceptions;

import java.util.List;

import org.objectweb.fractal.api.factory.InstantiationException;

public class InstantiationExceptionListException extends InstantiationException {

	
	List<InstantiationException> exceptions;
	
	public List<InstantiationException> getExceptionList() {
		return exceptions;
	}

	public InstantiationExceptionListException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}
	
	public InstantiationExceptionListException(List<InstantiationException> exceptions) {
		super("The creation of some components failed");
		this.exceptions = exceptions;
	}
	
	
	
	
	

}
