package org.objectweb.proactive.core.component.body;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.component.request.ComponentRequest;


public class NFRequestFilterImpl implements RequestFilter,
    java.io.Serializable {
    public NFRequestFilterImpl() {
    }

    // TODO requestPriority
    public boolean acceptRequest(Request request) {
                if (request instanceof ComponentRequest) {
                    // request is accepted only if it matches the rule : 
                    return ((ComponentRequest) request).isControllerRequest();
                } else {
                    // standard requests cannot be component controller requests
        return false;
                }
    }
    
}