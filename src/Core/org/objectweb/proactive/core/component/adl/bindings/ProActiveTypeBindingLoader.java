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
package org.objectweb.proactive.core.component.adl.bindings;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.bindings.Binding;
import org.objectweb.fractal.adl.bindings.BindingContainer;
import org.objectweb.fractal.adl.bindings.TypeBindingLoader;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;


/**
 * @author The ProActive Team
 */
public class ProActiveTypeBindingLoader extends TypeBindingLoader {
    @Override
    protected void checkNode(final Object node, final Map context) throws ADLException {
        try {
            super.checkNode(node, context);
        } catch (ADLException e) {
            Binding[] bindings = ((BindingContainer) node).getBindings();
            Set fromItfs = new HashSet();
            for (int i = 0; i < bindings.length; i++) {
                if (fromItfs.contains(bindings[i].getFrom())) {
                    // ignore (allows multicast bindings
                }
            }
        }
    }

    @Override
    protected void checkBinding(final Binding binding, final Interface fromItf, final Interface toItf,
            final Map context) throws ADLException {
        try {
            super.checkBinding(binding, fromItf, toItf, context);
        } catch (ADLException e) {
            // check if signatures are incompatible
            TypeInterface cItf = (TypeInterface) fromItf;
            TypeInterface sItf = (TypeInterface) toItf;

            try {
                ClassLoader cl = getClassLoader(context);
                Class<?> clientSideItfClass = cl.loadClass(cItf.getSignature());
                Class<?> serverSideItfClass = cl.loadClass(sItf.getSignature());

                //            	Class<?> clientSideItfClass = Class<?>.forName(cItf.getSignature());
                //            	Class<?> serverSideItfClass = Class<?>.forName(sItf.getSignature());
                if (!clientSideItfClass.isAssignableFrom(serverSideItfClass)) {
                    // check if multicast interface
                    if (ProActiveTypeFactory.MULTICAST_CARDINALITY.equals(cItf.getCardinality())) {
                        Method[] clientSideItfMethods = clientSideItfClass.getMethods();
                        Method[] serverSideItfMethods = serverSideItfClass.getMethods();

                        if (clientSideItfMethods.length != serverSideItfMethods.length) {
                            throw new ADLException(
                                "incompatible binding between multicast client interface " +
                                    cItf.getName() +
                                    " (" +
                                    cItf.getSignature() +
                                    ")  and server interface " +
                                    sItf.getName() +
                                    " (" +
                                    sItf.getSignature() +
                                    ") : there is not the same number of methods (including those inherited) " +
                                    "in both interfaces !", (Node) binding);
                        }

                        //                        Map<Method, Method> matchingMethodsForThisItf = new HashMap<Method, Method>(clientSideItfMethods.length);
                        //
                        //                        for (Method method : clientSideItfMethods) {
                        //                            try {
                        ////                                matchingMethodsForThisItf
                        ////                                        .put(method, MulticastBindingChecker.searchMatchingMethod(method, serverSideItfMethods, ProActiveTypeInterface.GATHER_CARDINALITY.equals(sItf.getCardinality()), (ProActiveInterface)toItf));
                        //                                matchingMethodsForThisItf
                        //                                .put(method, MulticastBindingChecker.searchMatchingMethod(method, serverSideItfMethods, ProActiveTypeInterface.GATHER_CARDINALITY.equals(sItf.getCardinality())));
                        //                            } catch (ParameterDispatchException e1) {
                        //                                throw new ADLException("incompatible binding between multicast client interface " + cItf
                        //                                        .getName() + " (" + cItf.getSignature()
                        //                                        + ")  and server interface " + sItf.getName() + " (" + sItf
                        //                                        .getSignature() + ") : incompatible dispatch " +
                        //                                        e1.getMessage(), (Node) binding);
                        //                            } catch (NoSuchMethodException e1) {
                        //                                throw new ADLException("incompatible binding between multicast client interface " + cItf
                        //                                        .getName() + " (" + cItf.getSignature()
                        //                                        + ")  and server interface " + sItf.getName() + " (" + sItf
                        //                                        .getSignature() + ") : cannot find corresponding method " +
                        //                                        e1.getMessage(), (Node) binding);
                        //                            }
                        //                        }
                    }
                }
            } catch (ClassNotFoundException e1) {
                throw new ADLException("incompatible binding between multicast client interface " +
                    cItf.getName() + " (" + cItf.getSignature() + ")  and server interface " +
                    sItf.getName() + " (" + sItf.getSignature() + ") : cannot find interface " +
                    e1.getMessage(), (Node) binding);
            }
        }
    }
}
