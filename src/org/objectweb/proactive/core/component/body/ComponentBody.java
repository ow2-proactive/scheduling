package org.objectweb.proactive.core.component.body;

import org.objectweb.proactive.core.component.identity.ProActiveComponent;

/** Defines the actions specific to a component body.
 * @author mmorel
 */
public interface ComponentBody {
    /**
     * @return a reference on the component meta-object ProActiveComponentImpl
     */    
    public ProActiveComponent getProActiveComponent();

}