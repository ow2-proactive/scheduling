package org.objectweb.proactive.core.component.type;

import java.io.Serializable;


import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.type.InterfaceType;


/**
 * Implementation of InterfaceType (@see org.objectweb.fractal.api.type.InterfaceType)
 * 
 * @author Matthieu Morel
 *
 */
public class ProActiveInterfaceType implements InterfaceType, Serializable {
    protected static Logger logger = Logger.getLogger(ProActiveInterfaceType.class.getName());


    /**
       * The generatedClassName of the interface described by this type.
       */
    private String name;

    /**
     * The Java type of the interface described by this type.
     */
    private String signature;

	private boolean isClient;
	private boolean isOptional;
	private boolean isCollection;
    /**
     * Constructor for ProActiveInterfaceType.
     */
    public ProActiveInterfaceType() {
        super();
    }
    
    /**
     * copy constructor
     * @param name
     * @param signature
     * @param isClient
     * @param isOptional
     * @param isCollection
     */
	public ProActiveInterfaceType(final InterfaceType itfType) {
		this.name = new String(itfType.getFcItfName());
		this.signature = new String(itfType.getFcItfSignature());
		this.isClient = itfType.isFcClientItf();
		this.isOptional = itfType.isFcOptionalItf();
		this.isCollection = itfType.isFcCollectionItf();
	}
    /**
     * Constructor for ProActiveInterfaceType.
     */
    public ProActiveInterfaceType(String name, String signature, boolean isClient, boolean isOptional, 
                                  boolean isCollection) {
		
        this.name = name;
        this.signature = signature;
        this.isClient = isClient;
        this.isOptional = isOptional;
        this.isCollection = isCollection;
    }

    // -------------------------------------------------------------------------
    // Implementation of the InterfaceType interface
    // -------------------------------------------------------------------------

    /**
     * @see org.objectweb.fractal.api.type.InterfaceType#getFcItfName()
     */
    public String getFcItfName() {
        return name;
    }

    /**
     * @see org.objectweb.fractal.api.type.InterfaceType#getFcItfSignature()
     */
    public String getFcItfSignature() {
        return signature;
    }

    /**
     * @see org.objectweb.fractal.api.type.InterfaceType#isFcClientItf()
     */
    public boolean isFcClientItf() {
        return isClient;
    }

    /**
     * @see org.objectweb.fractal.api.type.InterfaceType#isFcOptionalItf()
     */
    public boolean isFcOptionalItf() {
        return isOptional;
    }

    /**
     * @see org.objectweb.fractal.api.type.InterfaceType#isFcCollectionItf()
     */
    public boolean isFcCollectionItf() {
        return isCollection;
    }

    /**
     * TODO : provide implementation for isFcSubTypeOf 
     * @see org.objectweb.fractal.api.Type#isFcSubTypeOf(Type)
     */
    public boolean isFcSubTypeOf(final Type type) {
        throw new RuntimeException("Not yet implemented.");
    }
}