/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component.type;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of InterfaceType (@see org.objectweb.fractal.api.type.InterfaceType)
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveInterfaceType implements InterfaceType, Serializable {
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
    private boolean isCollection;

    /**
     * Constructor for ProActiveInterfaceType.
     */
    public ProActiveInterfaceType() {
        super();
    }

    /**
     * copy constructor
     * @param itfType
     */
    public ProActiveInterfaceType(final InterfaceType itfType) {
        this.name = itfType.getFcItfName();
        this.signature = new String(itfType.getFcItfSignature());
        this.isClient = itfType.isFcClientItf();
        this.isOptional = itfType.isFcOptionalItf();
        this.isCollection = itfType.isFcCollectionItf();
    }

    /**
     * Constructor for ProActiveInterfaceType.
     */
    public ProActiveInterfaceType(String name, String signature,
        boolean isClient, boolean isOptional, boolean isCollection) {
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
