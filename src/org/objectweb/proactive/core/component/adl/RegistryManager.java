/*
 * Created on Apr 21, 2004
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.adl;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;

/**
 * @author Matthieu Morel
 */
public interface RegistryManager {

	public void addComponent(Component component) throws ADLException;
	
	public Component getComponent(String name);
}
