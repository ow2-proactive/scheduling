package org.objectweb.proactive.core.component.body;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.component.request.ComponentRequest;


public class RequestFilterOnComponentControllerClasses implements RequestFilter,
    java.io.Serializable {
    public RequestFilterOnComponentControllerClasses() {
    }

    public boolean acceptRequest(Request request) {
        // standard requests cannot be component controller requests
        return false;
    }

    public boolean acceptRequest(ComponentRequest componentRequest) {
        return componentRequest.isControllerRequest();
    }
}
