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

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of ComponentType (@see org.objectweb.fractal.api.type.ComponentType)
 *
 * @author The ProActive Team
 *
 */
public class ProActiveComponentTypeImpl implements ComponentType, ProActiveInterfaceType, Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);

    /**
     * The types of the interfaces of components of this type.
     */
    private final InterfaceType[] interfaceTypes;

    /**
     * Constructor for ProActiveComponentTypeImpl.
     */
    public ProActiveComponentTypeImpl(final InterfaceType[] interfaceTypes) throws InstantiationException {
        this.interfaceTypes = clone(interfaceTypes);
        // verifications
        for (int i = 0; i < interfaceTypes.length; ++i) {
            String p = interfaceTypes[i].getFcItfName();
            boolean collection = interfaceTypes[i].isFcCollectionItf();
            for (int j = i + 1; j < interfaceTypes.length; ++j) {
                String q = interfaceTypes[j].getFcItfName();
                if (p.equals(q)) {
                    throw new InstantiationException("Two interfaces have the same name '" + q + "'");
                }
                if (collection && q.startsWith(p)) {
                    throw new InstantiationException("The name of the interface '" + q + "' starts with '" +
                        p + "', which is the name of a collection interface");
                }
                if (interfaceTypes[j].isFcCollectionItf() && p.startsWith(q)) {
                    throw new InstantiationException("The name of the interface '" + p + "' starts with '" +
                        q + "', which is the name of a collection interface");
                }
            }
        }
    }

    /**
     * Seems doesn't be used
     * copy constructor
     */
    //    public ProActiveComponentTypeImpl(final ComponentType componentType) {
    //        InterfaceType[] tempItfTypes = componentType.getFcInterfaceTypes();
    //        this.interfaceTypes = new InterfaceType[tempItfTypes.length];
    //        for (int i = 0; i < interfaceTypes.length; i++) {
    //            // deep copy
    //            interfaceTypes[i] = new ProActiveInterfaceTypeImpl(tempItfTypes[i]);
    //        }
    //    }
    /**
     * @see org.objectweb.fractal.api.type.ComponentType#getFcInterfaceTypes()
     */
    public InterfaceType[] getFcInterfaceTypes() {
        return interfaceTypes;
    }

    /**
     * @see org.objectweb.fractal.api.type.ComponentType#getFcInterfaceType(String)
     */
    public InterfaceType getFcInterfaceType(String name) throws NoSuchInterfaceException {
        for (int i = 0; i < interfaceTypes.length; i++) {
            InterfaceType type = interfaceTypes[i];
            if (type.getFcItfName().equals(name)) {
                return type;
            }
        }
        throw new NoSuchInterfaceException(name);
    }

    /**
     * @see org.objectweb.fractal.api.Type#isFcSubTypeOf(Type)
     */
    public boolean isFcSubTypeOf(Type type) {
        throw new RuntimeException("not yet implemented");
    }

    /**
     * Returns a copy of the given interface type array. This method is used to
     * return a copy of the field of this class, instead of the field itself, so
     * that its content can not be modified from outside.
     *
     * @param types the array to be cloned.
     * @return a clone of the given array, or an empty array if <tt>type</tt> is
     *      <tt>null</tt>.
     */
    private static InterfaceType[] clone(final InterfaceType[] types) {
        if (types == null) {
            return new InterfaceType[0];
        } else {
            InterfaceType[] clone = new InterfaceType[types.length];
            System.arraycopy(types, 0, clone, 0, types.length);
            return clone;
        }
    }

    public String getFcItfName() {
        return "component";
    }

    public String getFcItfSignature() {
        return ProActiveComponent.class.getName();
    }

    public boolean isFcClientItf() {
        return false;
    }

    public boolean isFcCollectionItf() {
        return false;
    }

    public boolean isFcOptionalItf() {
        return false;
    }

    public boolean isFcStreamItf() {
        return false;
    }

    public String getFcCardinality() {
        return ProActiveTypeFactory.SINGLETON_CARDINALITY;
    }

    public boolean isFcGathercastItf() {
        return false;
    }

    public boolean isFcMulticastItf() {
        return false;
    }

    public boolean isFcSingletonItf() {
        return true;
    }
}
