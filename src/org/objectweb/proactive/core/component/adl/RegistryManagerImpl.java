/*
 * Created on Apr 21, 2004
 * author : Matthieu Morel
  */
package org.objectweb.proactive.core.component.adl;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;

/**
 * TODO : change this design flaw ? (component refers to a static resource)?
 * use a shared component?
 * @author Matthieu Morel
 */
public class RegistryManagerImpl implements RegistryManager {
	
    Registry registry;

	public RegistryManagerImpl() {
		registry = Registry.instance();
	}

	/* (non-Javadoc)
	 * @see org.objectweb.fractal.adl.RegistryManager#addComponent(org.objectweb.fractal.api.Component)
	 */
	public void addComponent(Component component) throws ADLException {
		registry.addComponent(component);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.fractal.adl.RegistryManager#getComponent(java.lang.String)
	 */
	public Component getComponent(String name) {
		return registry.getComponent(name);
	}

}
