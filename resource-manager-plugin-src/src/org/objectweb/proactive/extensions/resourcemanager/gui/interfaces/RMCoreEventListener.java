package org.objectweb.proactive.extensions.resourcemanager.gui.interfaces;


/**
 * @author FRADJ Johann
 * 
 */
public interface RMCoreEventListener {

	public void imKilledEvent();

	public void imShutDownEvent();

	public void imShuttingDownEvent();

	public void imStartedEvent();
}
