package org.objectweb.proactive.core.component.exceptions;

import java.util.Hashtable;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

public class ContentControllerExceptionListException extends Exception {
	
	Map<Component, IllegalLifeCycleException> lifeCycleExceptions = null;
	Map<Component, IllegalContentException> contentExceptions = null;
	
	public ContentControllerExceptionListException() {
	}
	
	public ContentControllerExceptionListException(Map<Component, IllegalLifeCycleException> lifeCycleExceptions, Map<Component, IllegalContentException> contentExceptions) {
		this.lifeCycleExceptions = lifeCycleExceptions;
		this.contentExceptions = contentExceptions;
	}

	public Map<Component, IllegalContentException> getContentExceptions() {
		return contentExceptions;
	}

	public Map<Component, IllegalLifeCycleException> getLifeCycleExceptions() {
		return lifeCycleExceptions;
	}
	
	public boolean isEmpty() {
		return (((lifeCycleExceptions == null) || (lifeCycleExceptions.isEmpty()))
				&&
				((contentExceptions == null) || (contentExceptions.isEmpty())));
	}
	
	
	public void addIllegalLifeCycleException(Component c, IllegalLifeCycleException e) {
		if (lifeCycleExceptions == null) {
			lifeCycleExceptions = new Hashtable<Component, IllegalLifeCycleException>();
		}
		lifeCycleExceptions.put(c, e);
	}
	
	public void addIllegalContentException(Component c, IllegalContentException e) {
		if (contentExceptions == null) {
			contentExceptions =  new Hashtable<Component, IllegalContentException>();
		}
		contentExceptions.put(c, e);
	}
	
	
}
