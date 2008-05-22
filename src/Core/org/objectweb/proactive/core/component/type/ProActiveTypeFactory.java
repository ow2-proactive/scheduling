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

import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * The ProActiveTypeFactory extends the
 * <code>org.objectweb.fractal.api.type.TypeFactory</code> to support the
 * cardinality parameter.
 *
 * @see org.objectweb.fractal.api.type.TypeFactory
 */
@PublicAPI
public interface ProActiveTypeFactory extends TypeFactory {
    public static final String SINGLETON_CARDINALITY = "singleton";
    public static final String COLLECTION_CARDINALITY = "collection";
    public static final String MULTICAST_CARDINALITY = "multicast";
    public static final String GATHER_CARDINALITY = "gathercast";
    public static final boolean INTERNAL = true;
    public static final boolean EXTERNAL = false;

    /**
     * Creates an interface type with a particular cardinality.
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
    public InterfaceType createFcItfType(String name, String signature, boolean isClient, boolean isOptional,
            String cardinality) throws InstantiationException;

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
     * @param isInternal boolean value, indicating whether the interface is internal
     * @return an interface type initialized with the given values.
     * @throws InstantiationException if the interface type cannot be created.
     */
    public InterfaceType createFcItfType(String name, String signature, boolean isClient, boolean isOptional,
            String cardinality, boolean isInternal) throws InstantiationException;

}
