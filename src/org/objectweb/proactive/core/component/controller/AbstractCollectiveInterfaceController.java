package org.objectweb.proactive.core.component.controller;

import java.lang.reflect.Method;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
/**
 * Abstract parent class for controllers of collective interfaces
 *  
 * @author Matthieu Morel
 *
 */
public abstract class AbstractCollectiveInterfaceController extends AbstractProActiveController {
    
    /**
     * called after creation of all controllers and interfaces
     */
    public abstract void init();


    public AbstractCollectiveInterfaceController(Component owner) {
        super(owner);
        // TODO Auto-generated constructor stub
    }
    
//    
    
    protected abstract Method searchMatchingMethod(Method clientSideMethod, Method[] serverSideMethods, boolean clientItfIsMulticast, boolean serverItfIsGathercast, ProActiveInterface serverSideItf);




}
