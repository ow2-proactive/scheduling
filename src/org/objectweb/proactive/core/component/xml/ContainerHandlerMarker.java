package org.objectweb.proactive.core.component.xml;

/**
 * defines handlers dealing with container components (parallel, composite)
 * 
 * @author Matthieu Morel
 */
public interface ContainerHandlerMarker {
	
	public ContainerElementHierarchy getContainerElementHierarchy();
	
	public void disable();
	
	public void enable();
	
	public boolean isEnabled();

}
