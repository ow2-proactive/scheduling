
package org.objectweb.proactive.core.component.request;

import org.objectweb.proactive.core.UniqueID;



/**
 * Factory for a component requests queue.
 * 
 * @author Matthieu Morel
 *
 */
public interface ComponentRequestQueueFactory {
    /**
     * factory for ComponentRequestQueue
     */
    public ComponentRequestQueue newComponentRequestQueue(UniqueID ownerID);
}