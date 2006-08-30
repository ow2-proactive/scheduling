package org.objectweb.proactive.core.component.controller;

import java.util.List;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.proactive.core.component.exceptions.ContentControllerExceptionListException;


/**
 * This controller interface extends {@link org.objectweb.fractal.api.control.ContentController}
 * by proposing methods for managing lists of components. The methods may be implemented using
 * multithreading, which can help scaling.
 * 
 * 
 * @author Matthieu Morel
 *
 */
public interface ProActiveContentController extends ContentController {
	
	/**
	   * Adds a list of sub components, possibly in parallel. This method delegates the addition
	   * of individual components to the
	   * {@link ContentController#addFcSubComponent(org.objectweb.fractal.api.Component)}
	   * method, and implementations of this method can parallelize
	   * the addition of the members of the list to the content of this component.
	   *
	   * @param subComponents the components to be added inside this component.
	   * @throws ContentControllerExceptionListException if the addition of one or several components
	   * 	failed. This exception lists the components that were not added and the 
	   * 	exception that occured.
	   */
	public void addFcSubComponent (List<Component> subComponents)
	    throws ContentControllerExceptionListException;
	
	  /**
	   * Removes a list of sub-components from this component, possibly in parallel. 
	   * This method delegates the removal of individual components to the 
	   * {@link ContentController#removeFcSubComponent(org.objectweb.fractal.api.Component)}
	   * method, and implementations of this method can parallelize the 
	   * removal of the members of the list from the content of this component.
	   *
	   * @param subComponents the list of components to be removed from this component.
	   * @throws ContentControllerExceptionListException if the addition of one or several components
	   * 	failed. This exception lists the components that were not added and the 
	   * 	exception that occured.
	   */

	  void removeFcSubComponent (List<Component> subComponents)
	    throws ContentControllerExceptionListException;

}
