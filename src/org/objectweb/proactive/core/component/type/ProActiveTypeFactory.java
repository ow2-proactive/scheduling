package org.objectweb.proactive.core.component.type;


import org.apache.log4j.Logger;

import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;


/**
 * Implementation of TypeFactory (@see org.objectweb.fractal.api.type.TypeFactory)
 * 
 * Implements the Singleton pattern.
 * 
 * @author Matthieu Morel
 *
 */
public class ProActiveTypeFactory implements TypeFactory {
    protected static Logger logger = Logger.getLogger(ProActiveTypeFactory.class.getName());

    // SINGLETON implementation
    static private ProActiveTypeFactory instance = null;

    /**
     * Constructor for ProActiveTypeFactory.
     */
    private ProActiveTypeFactory() {
        super();
    }

    static public ProActiveTypeFactory instance() {
        if (instance == null) {
            instance = new ProActiveTypeFactory();
        }
        return instance;
    }

    /**
     * @see org.objectweb.fractal.api.type.TypeFactory#createFcItfType(String, String, boolean, boolean, boolean)
     */
    public InterfaceType createFcItfType(String name, String signature, boolean isClient, boolean isOptional, 
                                         boolean isCollection) throws InstantiationException {
        return new ProActiveInterfaceType(name, signature, isClient, isOptional, isCollection);
    }

    /**
     * @see org.objectweb.fractal.api.type.TypeFactory#createFcType(InterfaceType[])
     */
    public ComponentType createFcType(InterfaceType[] interfaceTypes) throws InstantiationException {
        return new ProActiveComponentType(interfaceTypes);
    }
}