package org.objectweb.proactive.core.component.body;

import org.objectweb.proactive.Body;

/**
 * @author Matthieu Morel
 *
 * encapsulates activity at the object level
 */
public interface ComponentRunActive extends ComponentActive {
    
    public void runComponentActivity(Body body);

}
