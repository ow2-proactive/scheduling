package org.objectweb.proactive.core.component;

import java.io.Serializable;


import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Interface;


/** Stores a binding between a client interface and a server interface.
 * @author Matthieu Morel
 */
public class Binding implements Serializable {
    protected static Logger logger = Logger.getLogger(Binding.class.getName());
    private final Interface clientInterface;
    private final Interface serverInterface;

    /**
     * @param clientInterface a reference on a client interface
     * @param serverInterface a reference on a server interface
     */    
    public Binding(final Interface clientInterface, final Interface serverInterface) {
        this.clientInterface = clientInterface;
        this.serverInterface = serverInterface;
    }

    /**
     * @return the client interface
     */    
    public Interface getClientInterface() {
        return clientInterface;
    }
    

    /**
     * @return the server interface
     */    
    public Interface getServerInterface() {
        if (logger.isDebugEnabled()) {
            logger.debug("returning " + serverInterface.getClass().getName());
        }
        return serverInterface;
    }
}