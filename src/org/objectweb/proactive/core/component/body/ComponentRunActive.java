package org.objectweb.proactive.core.component.body;

import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;

/**
 * @author Matthieu Morel
 *
 * encapsulates activity at the object level
 */
public interface ComponentRunActive extends ComponentActive, RunActive {
    
    public void setFunctionalInitActive(InitActive initActive);
    
    public InitActive getFunctionalInitActive();
    
    public void setFunctionalRunActive(RunActive runActive);
    
    public RunActive getFunctionalRunActive();
    
    public void setFunctionalEndActive(EndActive endActive);
    
    public EndActive getFunctionalEndActive();
    

}
