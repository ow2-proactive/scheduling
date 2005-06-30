package org.objectweb.proactive.core.component.body;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.request.ComponentRequest;


public class PrioritizedComponentRequestFilter extends RequestFilterOnComponentControllerClasses {
    
    public boolean acceptRequest(Request request) {
        // standard requests cannot be component controller requests
        return false;
    }
    
    public boolean acceptRequest(ComponentRequest componentRequest) {
        if (super.acceptRequest(componentRequest)) {
            short priority = componentRequest.getPriority();
            return ((ComponentRequest.IMMEDIATE_PRIORITY == priority) || (ComponentRequest.BEFORE_FUNCTIONAL_PRIORITY == priority) || (ComponentRequest.STRICT_FIFO_PRIORITY == priority));
        } else {
            return false;
        }
        
    }
}