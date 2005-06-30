package org.objectweb.proactive.core.component.body;

import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.component.request.ComponentRequest;

/**
 * @author Matthieu Morel
 *
 */
public interface ComponentRequestFilter extends RequestFilter {
    
    public boolean acceptRequest(ComponentRequest componentRequest);

}
