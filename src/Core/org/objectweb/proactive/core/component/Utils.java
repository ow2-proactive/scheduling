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
package org.objectweb.proactive.core.component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;


/**
 * Utility methods
 *
 * @author Matthieu Morel
 *
 */
public class Utils {

    /**
     * @return null if clientItfName does not begin with the name of a collection interface,
     *         the name of the collection interface otherwise
     */
    public static String pertainsToACollectionInterface(String clientItfName,
        Component owner) {
        InterfaceType[] itfTypes = (((ComponentType) owner.getFcType()).getFcInterfaceTypes());
        for (int i = 0; i < itfTypes.length; i++) {
            if (itfTypes[i].isFcCollectionItf()) {
                if (clientItfName.startsWith(itfTypes[i].getFcItfName())) {
                    return itfTypes[i].getFcItfName();
                }
            }
        }
        return null;
    }

    public static InterfaceType getItfType(String itfName, Component owner)
        throws NoSuchInterfaceException {
        InterfaceType[] itfTypes = (((ComponentType) owner.getFcType()).getFcInterfaceTypes());
        for (int i = 0; i < itfTypes.length; i++) {
            if (itfTypes[i].isFcCollectionItf()) {
                if (itfName.startsWith(itfTypes[i].getFcItfName()) &&
                        !itfName.equals(itfTypes[i].getFcItfName())) {
                    return itfTypes[i];
                }
            } else {
                if (itfName.equals(itfTypes[i].getFcItfName())) {
                    return itfTypes[i];
                }
            }
        }
        return null;
    }

    public static boolean hasSingleCardinality(String itfName, Component owner) {
        Iterator<Interface> it = Arrays.<Interface>asList((Interface[]) owner.getFcInterfaces())
                                       .iterator();
        while (it.hasNext()) {
            ProActiveInterfaceType itfType = (ProActiveInterfaceType) it.next()
                                                                        .getFcItfType();
            if (itfType.getFcItfName().equals(itfName) &&
                    itfType.isFcSingletonItf()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMulticastItf(String itfName, Component owner) {
        try {
            return ProActiveTypeFactory.MULTICAST_CARDINALITY.equals(getCardinality(
                    itfName, owner));
        } catch (NoSuchInterfaceException e) {
            return false;
        }
    }

    public static boolean isGathercastItf(Interface itf) {
        if (!(itf instanceof ProActiveInterface)) {
            return false;
        }
        return ((ProActiveInterfaceType) itf.getFcItfType()).isFcGathercastItf();
    }

    public static boolean isSingletonItf(String itfName, Component owner) {
        try {
            return ProActiveTypeFactory.SINGLETON_CARDINALITY.equals(getCardinality(
                    itfName, owner));
        } catch (NoSuchInterfaceException e) {
            return false;
        }
    }

    public static String getCardinality(String itfName, Component owner)
        throws NoSuchInterfaceException {
        InterfaceType[] itfTypes = ((ComponentType) owner.getFcType()).getFcInterfaceTypes();

        for (InterfaceType type : itfTypes) {
            if (type.getFcItfName().equals(itfName)) {
                return ((ProActiveInterfaceType) type).getFcCardinality();
            }
        }
        throw new NoSuchInterfaceException(itfName);
    }

    public static String getMethodSignatureWithoutReturnTypeAndModifiers(
        Method m) {
        String result = m.toString();
        result = result.substring(result.indexOf(m.getName()));
        return result;
    }

    public static boolean isControllerInterfaceName(String itfName) {
        // according to Fractal spec v2.0 , section 4.1
        return ((itfName != null) &&
        (itfName.endsWith("-controller") || itfName.equals("component")));
    }
}
