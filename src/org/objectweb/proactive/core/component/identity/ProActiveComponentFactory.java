package org.objectweb.proactive.core.component.identity;

import org.objectweb.proactive.Body;


/**
 * 
 * A factory for component meta-objects
 * 
  * @author Matthieu Morel
  */
public interface ProActiveComponentFactory {
    /** factory for ProActiveComponent
     * @param myBody a reference on the body of the active object
     * @return
     */
    public ProActiveComponent newProActiveComponent(Body myBody);
}