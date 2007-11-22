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
package org.objectweb.proactive.core.component.gen;

import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class is the parent of classes for generating component interfaces. It provides utility methods that are used in subclasses.
 *
 * @author Matthieu Morel
 *
 */
public abstract class AbstractInterfaceClassGenerator {
    protected static final transient Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_GEN_ITFS);
    protected static ClassPool pool = ClassPool.getDefault();

    protected Class<?> loadClass(final String className)
        throws ClassNotFoundException {
        // try to fetch the class from the default class loader
        return Thread.currentThread().getContextClassLoader()
                     .loadClass(className);
    }

    public ProActiveInterface generateControllerInterface(
        final String controllerInterfaceName, Component owner,
        ProActiveInterfaceType interfaceType)
        throws InterfaceGenerationFailedException {
        return generateInterface(controllerInterfaceName, owner, interfaceType,
            false, false);
    }

    public ProActiveInterface generateFunctionalInterface(
        final String functionalInterfaceName, Component owner,
        ProActiveInterfaceType interfaceType)
        throws InterfaceGenerationFailedException {
        return generateInterface(functionalInterfaceName, owner, interfaceType,
            false, true);
    }

    public abstract ProActiveInterface generateInterface(
        final String interfaceName, Component owner,
        ProActiveInterfaceType interfaceType, boolean isInternal,
        boolean isFunctionalInterface)
        throws InterfaceGenerationFailedException;

    /**
     * Gets all super-interfaces from the interfaces of this list, and
     * adds them to this list.
     * @param interfaces a list of interfaces
     */
    public static void addSuperInterfaces(List<CtClass> interfaces)
        throws NotFoundException {
        for (int i = 0; i < interfaces.size(); i++) {
            CtClass[] super_itfs_table = interfaces.get(i).getInterfaces();
            List<CtClass> super_itfs = new ArrayList<CtClass>(super_itfs_table.length); // resizable list
            for (int j = 0; j < super_itfs_table.length; j++) {
                super_itfs.add(super_itfs_table[j]);
            }
            addSuperInterfaces(super_itfs);
            CtClass super_itf;
            for (int j = 0; j < super_itfs.size(); j++) {
                if (!interfaces.contains(super_itfs.get(j))) {
                    super_itf = super_itfs.get(j);
                    if (!(super_itf.equals(pool.get(
                                    ProActiveInterface.class.getName())) ||
                            super_itf.equals(pool.get(Interface.class.getName())))) {
                        interfaces.add(super_itfs.get(j));
                    }
                }
            }
        }
    }
}
