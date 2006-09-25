package org.objectweb.proactive.core.component.controller;

import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;

/**
 * A collective interface controller is able to check compatibility between 
 * @author Matthieu Morel
 *
 */
public interface CollectiveInterfaceController {
    
    public void ensureCompatibility(ProActiveInterfaceType itfType, ProActiveInterface itf) throws IllegalBindingException;
    


}
