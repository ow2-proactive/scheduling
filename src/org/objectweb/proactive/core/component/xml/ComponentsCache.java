
package org.objectweb.proactive.core.component.xml;

import java.util.Hashtable;
import java.util.Map;

import org.objectweb.fractal.api.Component;


/**
 * A cache for component representatives.
 * 
 * @author Matthieu Morel
 *
 */
public class ComponentsCache {
	
	private Map componentsTable;
	
	public ComponentsCache(){
		componentsTable = new Hashtable();
	}
	
	public void addComponent(String name, Component component) {
		
		componentsTable.put(name, component);
	}
	
	public Component getComponent(String name) {
		return (Component)componentsTable.get(name);
	}
	
	public boolean containsComponentNamed(String componentName) {
		return componentsTable.containsKey(componentName);
	}
	

}
