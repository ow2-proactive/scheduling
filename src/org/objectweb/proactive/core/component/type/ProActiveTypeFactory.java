/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
    public InterfaceType createFcItfType(String name, String signature,
        boolean isClient, boolean isOptional, boolean isCollection)
        throws InstantiationException {
        return new ProActiveInterfaceType(name, signature, isClient,
            isOptional, isCollection);
    }

    /**
     * @see org.objectweb.fractal.api.type.TypeFactory#createFcType(InterfaceType[])
     */
    public ComponentType createFcType(InterfaceType[] interfaceTypes)
        throws InstantiationException {
        return new ProActiveComponentType(interfaceTypes);
    }
}
