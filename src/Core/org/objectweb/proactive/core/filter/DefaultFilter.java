package org.objectweb.proactive.core.filter;

import org.objectweb.proactive.Body;


/**
 * A default implementation of a filter.
 * This filter doesn't do anything, and accepts all the bodies
 * @author ProActive Team
 */
public class DefaultFilter implements Filter {
    public boolean filter(Body body) {
        return true;
    }
}
