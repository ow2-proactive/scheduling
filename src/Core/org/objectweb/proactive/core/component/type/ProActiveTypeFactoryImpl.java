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

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of TypeFactory (@see org.objectweb.fractal.api.type.TypeFactory)
 *
 * Implements the Singleton pattern.
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveTypeFactoryImpl implements ProActiveTypeFactory {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);

    // SINGLETON implementation
    static private ProActiveTypeFactoryImpl instance = null;

    /**
     * Constructor for ProActiveTypeFactoryImpl.
     */
    private ProActiveTypeFactoryImpl() {
        super();
    }

    static public ProActiveTypeFactoryImpl instance() {
        if (instance == null) {
            instance = new ProActiveTypeFactoryImpl();
        }

        return instance;
    }

    /*
     * @see org.objectweb.fractal.api.type.TypeFactory#createFcItfType(String, String, boolean, boolean, boolean)
     */
    public InterfaceType createFcItfType(String name, String signature,
        boolean isClient, boolean isOptional, boolean isCollection)
        throws InstantiationException {
        return new ProActiveInterfaceTypeImpl(name, signature, isClient,
            isOptional,
            (isCollection ? ProActiveTypeFactory.COLLECTION_CARDINALITY
                          : ProActiveTypeFactory.SINGLETON_CARDINALITY));
    }

    /*
     * @see org.objectweb.fractal.api.type.TypeFactory#createFcType(InterfaceType[])
     */
    public ComponentType createFcType(InterfaceType[] interfaceTypes)
        throws InstantiationException {

        /*
         * Workaround for null component types.
         * AOKell and ProActive/Fractal assumes a component type is non null, whereas Julia envisions
         * situations where this can be the case. To preserve a kind of
         * compatibility, we bypass null component types with empty arrays of
         * interface types.
         */
        if (interfaceTypes == null) {
            interfaceTypes = new InterfaceType[] {  };
        }
        return new ProActiveComponentTypeImpl(interfaceTypes);
    }

    /*
     * @see org.objectweb.proactive.core.component.type.ProActiveTypeFactory#createFcItfType(java.lang.String, java.lang.String, boolean, boolean, java.lang.String)
     */
    public InterfaceType createFcItfType(String name, String signature,
        boolean isClient, boolean isOptional, String cardinality)
        throws InstantiationException {
        return new ProActiveInterfaceTypeImpl(name, signature, isClient,
            isOptional, cardinality);
    }
}
