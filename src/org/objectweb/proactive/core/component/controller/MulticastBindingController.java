package org.objectweb.proactive.core.component.controller;

import org.objectweb.fractal.api.NoSuchInterfaceException;

public interface MulticastBindingController  {
    
    public Object getMulticastFcItfRef(String itfName) throws NoSuchInterfaceException;
    
    public void setMulticastFcItfRef(String itfName, Object itfRef);
    
}
