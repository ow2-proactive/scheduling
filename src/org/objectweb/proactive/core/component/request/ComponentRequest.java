package org.objectweb.proactive.core.component.request;

import org.objectweb.proactive.core.body.request.Request;



/**
 * Marker interface.
 * 
 * @author Matthieu Morel
 *
 */
public interface ComponentRequest extends Request {
	
    /**
     * tells whether the request is a call to a control interface
     */
    public boolean isControllerRequest();
}