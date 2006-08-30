package org.objectweb.proactive.core.component.controller;

import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.proactive.core.component.ProActiveInterface;

/**
 * A collective interface controller is able to check compatibility between 
 * @author Matthieu Morel
 *
 */
public interface CollectiveInterfaceController {
    
    public void checkCompatibility(String itfName, ProActiveInterface itf) throws IllegalBindingException;
    


}
