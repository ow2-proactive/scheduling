/*
 * Created on Apr 20, 2004
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.adl.implementations;

import java.util.Map;

import org.objectweb.fractal.adl.implementations.ImplementationBuilder;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.adl.RegistryManager;

/**
 * @author Matthieu Morel
 */
public class ProActiveImplementationBuilder implements ImplementationBuilder, BindingController {
	
	public final static String REGISTRY_BINDING = "registry";
  
  public RegistryManager registry;
  
  // --------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // --------------------------------------------------------------------------
  
  public String[] listFc() {
	return new String[] { REGISTRY_BINDING };
  }

  public Object lookupFc (final String itf) {
	if (itf.equals(REGISTRY_BINDING)) {
	  return registry;
	}
	return null;
  }

  public void bindFc (final String itf, final Object value) {
	if (itf.equals(REGISTRY_BINDING)) {
	  registry = (RegistryManager)value;
	}
  }

  public void unbindFc (final String itf) {
	if (itf.equals(REGISTRY_BINDING)) {
	  registry = null;
	}
  }
  
  public Object createComponent (
	final Object type, 
	final String name,
	final String definition,
	final Object controllerDesc, 
	final Object contentDesc, 
	final Object context) throws Exception
  {
	Component bootstrap = null;
	if (context != null) {
	  bootstrap = (Component)((Map)context).get("bootstrap");
	}
	if (bootstrap == null) {
	  bootstrap = Fractal.getBootstrapComponent();
	}
	Component result = Fractal.getGenericFactory(bootstrap).newFcInstance(
	  (ComponentType)type, controllerDesc, contentDesc);
	try {
	  //Fractal.getNameController(result).setFcName(name);
		NameController nc = (NameController)result.getFcInterface("name-controller");
		nc.setFcName(name);
	  registry.addComponent(result);
	} catch (NoSuchInterfaceException ignored) {
	}
	return result;
  }


}
