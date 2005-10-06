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
package org.objectweb.proactive.core.group;

import java.io.File;
import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.ProActiveInterfaceImpl;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.component.gen.RepresentativeInterfaceClassGenerator;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentativeImpl;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfProxyObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.InvalidProxyClassException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.StubObject;


/**
 *
 *  // TODO : change class name (interfaces only are grouped)
 *
 * A class for creating groups of interfaces
 * Indeed, the standard mechanism cannot be used here, as we are referencing components
 * through interfaces of component representatives.
 *
 *  It was moved to this package so it can see className attribute in ProxyForGroup
 *
 * @author Matthieu Morel
 */
public class ProActiveComponentGroup {
    protected static Logger logger = Logger.getLogger(ProActiveComponentGroup.class.getName());

    /**
     * creates an empty group able to contain ProActiveInterfaceImpl objects of the given type..
     * The stub in front of the group proxy is of type ProActiveInterfaceImpl.
     * @param interfaceType the type of interface we need a group of Interface objects on
     * @return a group of ProActiveInterfaceImpl elements
     * @throws ClassNotFoundException
     * @throws ClassNotReifiableException
     */
    public static ProActiveInterface newComponentInterfaceGroup(
        InterfaceType interfaceType, Component owner)
        throws ClassNotFoundException, ClassNotReifiableException {
        try {
            Object result = MOP.newInstance(ProActiveInterfaceImpl.class.getName(),
                    null, ProActiveGroup.DEFAULT_PROXYFORGROUP_CLASS_NAME, null);

            ProxyForGroup proxy = (org.objectweb.proactive.core.group.ProxyForGroup) ((StubObject) result).getProxy();
            proxy.className = ProActiveInterfaceImpl.class.getName();

            //return a reference on the generated interface reference corresponding to the interface type
            ProActiveInterface generated = RepresentativeInterfaceClassGenerator.instance()
                                                                                .generateFunctionalInterface(interfaceType.getFcItfName(),
                    owner, interfaceType);
            ((StubObject) generated).setProxy(proxy);
            return generated;
            //            return (ProActiveInterface) (ProActiveComponentRepresentativeFactory.instance()
            //                                                                                .createComponentRepresentative(component_type, null,
            //                proxy)).getFcInterface(interfaceType.getFcItfName());
        } catch (InvalidProxyClassException e) {
            logger.error("**** InvalidProxyClassException ****");
        } catch (ConstructionOfProxyObjectFailedException e) {
            logger.error("**** ConstructionOfProxyObjectFailedException ****");
        } catch (ConstructionOfReifiedObjectFailedException e) {
            logger.error("**** ConstructionOfReifiedObjectFailedException ****");
        } catch (InterfaceGenerationFailedException e) {
            logger.error("**** Interface could not be generated **** " +
                e.getMessage());
        }
        return null;
    }

    /**
     * Creates an empty  component stub+a group proxy.
     * The stub in front of the group proxy is a component stub (instance of ComponentRepresentativeImpl),
     * that offers references to the functional interfaces defined in the type of the component.
     * @param componentType the type of the component (i.e. the functional interfaces it offers and requires)
     * @return a stub/proxy
     * @throws ClassNotFoundException
     * @throws java.lang.InstantiationException
     */
    public static ProActiveComponentRepresentative newComponentRepresentativeGroup(
        ComponentParameters componentParameters)
        throws ClassNotFoundException, java.lang.InstantiationException {
        try {
            ProActiveComponentRepresentative result = null;

            // create the stub with the appropriate parameters
            Constructor constructor = ProActiveComponentRepresentativeImpl.class.getConstructor(new Class[] {
                        ComponentType.class, String.class, File.class
                    });
            result = (ProActiveComponentRepresentative) constructor.newInstance(new Object[] {
                        componentParameters.getComponentType(),
                        componentParameters.getHierarchicalType(),
                        componentParameters.getControllerDescription()
                                           .getControllersConfigFileLocation()
                    });

            // build the constructor call for the proxy object to create
            ConstructorCall reifiedCall = MOP.buildTargetObjectConstructorCall(ProActiveComponentRepresentativeImpl.class,
                    new Object[] {
                        componentParameters.getComponentType(),
                        componentParameters.getHierarchicalType(),
                        componentParameters.getControllerDescription()
                                           .getControllersConfigFileLocation()
                    });

            // Instanciates the proxy object
            ProxyForGroup proxy = (ProxyForGroup) MOP.createProxyObject(ProActiveGroup.DEFAULT_PROXYFORGROUP_CLASS_NAME,
                    MOP.EMPTY_OBJECT_ARRAY, reifiedCall);

            // connect the stub to the proxy
            result.setProxy(proxy);

            proxy.className = ProActiveComponentRepresentative.class.getName();

            return result;
        } catch (Exception e) {
            throw new java.lang.InstantiationException(
                "cannot create group of component representatives : " +
                e.getMessage());
        }
    }
}
