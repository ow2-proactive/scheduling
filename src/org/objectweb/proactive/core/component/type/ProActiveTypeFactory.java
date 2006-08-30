package org.objectweb.proactive.core.component.type;

import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;


public interface ProActiveTypeFactory extends TypeFactory {
    
    public static final String SINGLETON_CARDINALITY="singleton";
    public static final String COLLECTION_CARDINALITY="collection";
    public static final String MULTICAST_CARDINALITY = "multicast";
    public static final String GATHER_CARDINALITY = "gathercast";

    /**
     * Creates an interface type.
     *
     * @param name the name of interfaces of this type (see {@link
     *       InterfaceType#getFcItfName getFcItfName}).
     * @param signature signatures of the methods of interfaces of this type. In
     *       Java this "signature" is the fully qualified name of a Java interface
     *       corresponding to these method signatures.
     * @param isClient <tt>true</tt> if component interfaces of this type are
     *      client interfaces.
     * @param isOptional <tt>true</tt> if component interfaces of this type are
     *      optional interfaces.
     * @param cardinality see { @link ProActiveInterfaceType#getFcCardinality() } 
     * for a description of cardinalities 
     * @return an interface type initialized with the given values.
     * @throws InstantiationException if the interface type cannot be created.
     */
    public InterfaceType createFcItfType(String name, String signature,
        boolean isClient, boolean isOptional, String cardinality)
        throws InstantiationException;
}
