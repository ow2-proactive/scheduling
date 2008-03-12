/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component.type;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.StreamInterface;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of InterfaceType (@see org.objectweb.fractal.api.type.InterfaceType)
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveInterfaceTypeImpl implements ProActiveInterfaceType, Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);

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
    private boolean isStream;
    private String cardinality;

    /**
     * Constructor for ProActiveInterfaceTypeImpl.
     */
    public ProActiveInterfaceTypeImpl() {
        super();
    }

    /**
     * copy constructor
     * @param itfType
     */
    public ProActiveInterfaceTypeImpl(final InterfaceType itfType) {
        this.name = itfType.getFcItfName();
        this.signature = itfType.getFcItfSignature();
        this.isClient = itfType.isFcClientItf();
        this.isOptional = itfType.isFcOptionalItf();
        this.isStream = checkIsStream(signature);
        if (itfType.isFcCollectionItf()) {
            cardinality = ProActiveTypeFactory.COLLECTION_CARDINALITY;
        } else {
            cardinality = ProActiveTypeFactory.SINGLETON_CARDINALITY;
        }
    }

    /**
     * Constructor for ProActiveInterfaceTypeImpl.
     */
    public ProActiveInterfaceTypeImpl(String name, String signature, boolean isClient, boolean isOptional,
            String cardinality) throws InstantiationException {
        this.name = name;
        this.signature = signature;
        this.isClient = isClient;
        this.isOptional = isOptional;
        this.isStream = checkIsStream(signature);
        this.cardinality = cardinality;
        checkMethodsSignatures(signature, cardinality);
    }

    private boolean checkIsStream(String signature) {
        try {
            Class<?> c = Class.forName(signature);
            return StreamInterface.class.isAssignableFrom(c);
        } catch (ClassNotFoundException e) {
            throw new ProActiveRuntimeException(
                "cannot find interface defined in component interface signature : " + e.getMessage());
        }
    }

    private void checkMethodsSignatures(String signature, String cardinality) throws InstantiationException {
        checkMethodsStream(signature, cardinality);
        checkMethodsCardinality(signature, cardinality);
    }

    private void checkMethodsStream(String signature, String cardinality) throws InstantiationException {
        try {
            if (isStream) {
                Class<?> c = Class.forName(signature);
                Method[] methods = c.getMethods();
                for (Method m : methods) {
                    if (!(Void.TYPE == m.getReturnType())) {
                        throw new InstantiationException("methods of a stream interface must return void, " +
                            "which is not the case for method " + m.toString() + " in interface " + signature);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new ProActiveRuntimeException(
                "cannot find interface defined in component interface signature : " + e.getMessage());
        }
    }

    private void checkMethodsCardinality(String signature, String cardinality) throws InstantiationException {
        try {
            if (ProActiveTypeFactory.MULTICAST_CARDINALITY.equals(cardinality)) {
                Class<?> c = Class.forName(signature);
                Method[] methods = c.getMethods();
                for (Method m : methods) {
                    if (!(m.getGenericReturnType() instanceof ParameterizedType) &&
                        !(Void.TYPE == m.getReturnType())) {
                        throw new InstantiationException(
                            "methods of a multicast interface must return parameterized types or void, " +
                                "which is not the case for method " + m.toString() + " in interface " +
                                signature);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new ProActiveRuntimeException(
                "cannot find interface defined in component interface signature : " + e.getMessage());
        }
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
    * TODO : provide implementation for isFcSubTypeOf
    * @see org.objectweb.fractal.api.Type#isFcSubTypeOf(Type)
    */
    public boolean isFcSubTypeOf(final Type type) {
        throw new RuntimeException("Not yet implemented.");
    }

    public boolean isFcStreamItf() {
        return isStream;
    }

    public String getFcCardinality() {
        return cardinality;
    }

    public boolean isFcCollective() {
        return (ProActiveTypeFactory.GATHER_CARDINALITY.equals(cardinality) || (ProActiveTypeFactory.MULTICAST_CARDINALITY
                .equals(cardinality)));
    }

    public boolean isFcGathercastItf() {
        return ProActiveTypeFactory.GATHER_CARDINALITY.equals(cardinality);
    }

    public boolean isFcMulticastItf() {
        return ProActiveTypeFactory.MULTICAST_CARDINALITY.equals(cardinality);
    }

    public boolean isFcSingletonItf() {
        return ProActiveTypeFactory.SINGLETON_CARDINALITY.equals(cardinality);
    }

    /*
     * @see org.objectweb.fractal.api.type.InterfaceType#isFcCollectionItf()
     */
    public boolean isFcCollectionItf() {
        return ProActiveTypeFactory.COLLECTION_CARDINALITY.equals(cardinality);
    }
}
