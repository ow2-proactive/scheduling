package org.objectweb.proactive.core.component.request;

import org.objectweb.proactive.core.body.request.BlockingRequestQueue;

/**
 * Defines the request queue for components.
 * 
 * @author Matthieu Morel
 *
 */
public interface ComponentRequestQueue extends BlockingRequestQueue {
	public void start();

	public void stop();

	public boolean isStarted();
}