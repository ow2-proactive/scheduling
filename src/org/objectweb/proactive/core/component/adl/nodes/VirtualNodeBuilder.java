/*
 * Created on May 18, 2004 author : Matthieu Morel
 */
package org.objectweb.proactive.core.component.adl.nodes;
/**
 * @author Matthieu Morel
 */
public interface VirtualNodeBuilder {
	/**
	 * A builder interface to set virtual nodes.
	 */
	void setVirtualNode(Object component, String virtualNode, Object context) throws Exception;
}