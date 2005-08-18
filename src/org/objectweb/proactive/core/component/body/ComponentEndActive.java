package org.objectweb.proactive.core.component.body;

import org.objectweb.proactive.Body;

/**
 * @author Matthieu Morel
 *
 */
public interface ComponentEndActive extends ComponentActive {
    
    public void endComponentActivity(Body body);

}
