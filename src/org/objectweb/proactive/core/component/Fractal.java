/*
 * Created on Dec 4, 2003
 * author : Matthieu Morel
  */

package org.objectweb.proactive.core.component;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.AttributeController;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.control.SuperController;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;

/**
 * Provides static methods to access standard interfaces of Fractal components.
 */

public class Fractal {

  /**
   * Private constructor (uninstantiable class).
   */

  private Fractal () {
  }

 /**
   * Returns the {@link BindingController} interface of the given component.
   *
   * @param component a component.
   * @return the {@link BindingController} interface of the given component.
   * @throws NoSuchInterfaceException if there is no such interface.
   */

  public static BindingController getBindingController (
	final Component component) throws NoSuchInterfaceException
  {
	return (BindingController)component.getFcInterface("binding-controller");
  }

  /**
   * Returns the {@link ContentController} interface of the given component.
   *
   * @param component a component.
   * @return the {@link ContentController} interface of the given component.
   * @throws NoSuchInterfaceException if there is no such interface.
   */

  public static ContentController getContentController (
	final Component component) throws NoSuchInterfaceException
  {
	return (ContentController)component.getFcInterface("content-controller");
  }

  /**
   * Returns the {@link SuperController} interface of the given component.
   *
   * @param component a component.
   * @return the {@link SuperController} interface of the given component.
   * @throws NoSuchInterfaceException if there is no such interface.
   */

  public static SuperController getSuperController (final Component component)
	throws NoSuchInterfaceException
  {
	return (SuperController)component.getFcInterface("super-controller");
  }

  /**
   * Returns the {@link NameController} interface of the given component.
   *
   * @param component a component.
   * @return the {@link NameController} interface of the given component.
   * @throws NoSuchInterfaceException if there is no such interface.
   */

  public static NameController getNameController (final Component component)
	throws NoSuchInterfaceException
  {
	return (NameController)component.getFcInterface("name-controller");
  }

  /**
   * Returns the {@link LifeCycleController} interface of the given component.
   *
   * @param component a component.
   * @return the {@link LifeCycleController} interface of the given component.
   * @throws NoSuchInterfaceException if there is no such interface.
   */

  public static LifeCycleController getLifeCycleController (
	final Component component) throws NoSuchInterfaceException
  {
	return (LifeCycleController)component.getFcInterface("lifecycle-controller");
  }
  
  
  /**
	* Returns the {@link ContentController} interface of the given component.
	*
	* @param component a component.
	* @return the {@link ContentController} interface of the given component.
	* @throws NoSuchInterfaceException if there is no such interface.
	*/

   public static ComponentParametersController getComponentParametersController (
	 final Component component) throws NoSuchInterfaceException
   {
	 return (ComponentParametersController)component.getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER);
   }
   
   
   /**
	 * Returns the {@link AttributeController} interface of the given component (if defined).
	 *
	 * @param component a component.
	 * @return the {@link AttributeController} interface of the given component (if defined)
	 * @throws NoSuchInterfaceException if there is no such interface.
	 */

	public static AttributeController getAttributeController (
	  final Component component) throws NoSuchInterfaceException
	{
	  return (AttributeController)component.getFcInterface(Constants.ATTRIBUTE_CONTROLLER);
	}
   
   
   /**
	 * Returns the {@link TypeFactory} interface of the system.
	 *
	 * @return the {@link TypeFactory} interface of the system
	 * @throws NoSuchInterfaceException if there is no such interface.
	 */

	public static TypeFactory getTypeFactory ()
	  throws NoSuchInterfaceException
	{
	  return (TypeFactory)ProActiveTypeFactory.instance();
	}
	
	/**
	  * Returns the {@link GenericFactory} interface of the system.
	  *
	  * @return the {@link GenericFactory} interface of the system
	  * @throws NoSuchInterfaceException if there is no such interface.
	  */	
	public static GenericFactory getGenericFactory() {
		return GenericFactoryImpl.instance();
	}
	
  }


