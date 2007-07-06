package org.objectweb.proactive.core.filter;

import org.objectweb.proactive.Body;


/**
 * A filter indicates if we must keep or not the given body.
 * @author ProActive Team
 */
public interface Filter {

    /**
     * Filters a body.
     * @param body the body to filter.
     * @return true if the body passes through the filter, false otherwise
     */
    public boolean filter(Body body);
}
