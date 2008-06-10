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
package org.objectweb.proactive.core.component.collectiveitfs;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.mop.Utils;


/**
 * Utility class for gathercast interfaces.
 * @author The ProActive Team
 *
 */
public class GatherBindingChecker implements Serializable {

    /**
     * client method <code>B foo (A) throws E;<code><br>
     * must be matched by <br>
     *  server method <code><pre>List<B> foo(List&lst;A&gt;) throws E;</pre></code>
     * @param clientSideMethod method of the client interface
     * @param serverSideMethods method of the gathercast server interface
     * @param clientItfIsMulticast flags a multicast client interface
     * @return the method found if any. Throws an exception, otherwise.
     * @throws ParameterDispatchException in case of a problem in the dispatch of parameters
     * @throws NoSuchMethodException if no corresponding method was found
     */
    public static Method searchMatchingMethod(Method clientSideMethod, Method[] serverSideMethods,
            boolean clientItfIsMulticast) throws ParameterDispatchException, NoSuchMethodException {
        Method result = null;
        Type clientSideReturnType = clientSideMethod.getGenericReturnType();
        Type[] clientSideParametersTypes = clientSideMethod.getGenericParameterTypes();
        Class<?>[] clientSideExceptionTypes = clientSideMethod.getExceptionTypes();

        if (!Utils.checkMethod(clientSideMethod)) {
            throw new NoSuchMethodException(
                "gather interfaces only accept client interfaces with reifiable methods, which is not the case for " +
                    clientSideMethod.toGenericString());
        }

        serverSideMethodsLoop: for (Method serverSideMethod : serverSideMethods) {
            // 1. check names
            if (!serverSideMethod.getName().equals(clientSideMethod.getName())) {
                continue serverSideMethodsLoop;
            }

            // 2. check return types
            if (!(clientSideReturnType == Void.TYPE)) {
                Type returnType = serverSideMethod.getGenericReturnType();
                if (!(returnType instanceof ParameterizedType)) {
                    throw new NoSuchMethodException(
                        "gather interfaces only accept client interfaces with parameterized return types, which is not the case for " +
                            clientSideMethod.toGenericString());
                }
                Class<?> serverSideReturnTypeArgument = (Class<?>) ((ParameterizedType) returnType)
                        .getActualTypeArguments()[0];
                try {
                    MOP.checkClassIsReifiable(((Class<?>) clientSideReturnType));
                } catch (ClassNotReifiableException e) {
                    throw new NoSuchMethodException(
                        "gather interfaces only accept client interfaces with reifiable return types, which is not the case for " +
                            clientSideMethod.toGenericString());
                }

                if (!(serverSideReturnTypeArgument.isAssignableFrom((Class<?>) clientSideReturnType))) {
                    continue serverSideMethodsLoop;
                }
            } else {
                if (!(serverSideMethod.getReturnType() == Void.TYPE)) {
                    continue serverSideMethodsLoop;
                }
            }

            // 3. check parameters types
            Type[] serverSideParametersTypes = serverSideMethod.getGenericParameterTypes();

            for (int i = 0; i < serverSideParametersTypes.length; i++) {
                ParameterizedType pServerParameterType = null;
                if (!(serverSideParametersTypes[i] instanceof ParameterizedType)) {
                    // need parameterized types for server itf parameters
                    // but: could be a stub, generated by javassist, which cannot add generics information
                    if (StubObject.class.isAssignableFrom(serverSideMethod.getDeclaringClass())) {
                        // => check in implemented interfaces
                        Class<?>[] implementedInterfaces = serverSideMethod.getDeclaringClass()
                                .getInterfaces();
                        for (int j = 0; j < implementedInterfaces.length; j++) {
                            try {
                                Method serverSideMethodFromImplementedItf = implementedInterfaces[j]
                                        .getMethod(serverSideMethod.getName(), serverSideMethod
                                                .getParameterTypes());
                                Type[] serverSideMethodFromImplementedItfParametersTypes = serverSideMethodFromImplementedItf
                                        .getGenericParameterTypes();
                                if (!(serverSideMethodFromImplementedItfParametersTypes[i] instanceof ParameterizedType)) {
                                    continue serverSideMethodsLoop;
                                } else {
                                    pServerParameterType = (ParameterizedType) serverSideMethodFromImplementedItfParametersTypes[i];
                                    // parameter types match 
                                    break;
                                }
                            } catch (NoSuchMethodException e) {
                                // not in this interface
                                continue;
                            }
                        }
                    } else {
                        continue serverSideMethodsLoop;
                    }
                } else {
                    pServerParameterType = (ParameterizedType) serverSideParametersTypes[i];
                }
                if (!List.class.isAssignableFrom((Class<?>) pServerParameterType.getRawType())) {
                    continue serverSideMethodsLoop;
                }
                if (pServerParameterType.getActualTypeArguments().length != 1) {
                    continue serverSideMethodsLoop;
                }

                if (clientItfIsMulticast) {
                    //                	if (! (((Class<?>)clientSideParametersTypes[i]).isAssignableFrom((Class<?>)pServerParameterType.getRawType()))) {
                    if (clientSideParametersTypes[i] instanceof ParameterizedType &&
                        ((Class<?>) ((ParameterizedType) clientSideParametersTypes[i]).getRawType())
                                .isAssignableFrom(List.class)) {
                        // we have a list<T> in the client multicast itf, that's fine
                        if (!((Class<?>) (((ParameterizedType) clientSideParametersTypes[i])
                                .getActualTypeArguments()[0]))
                                .isAssignableFrom((Class<?>) pServerParameterType.getActualTypeArguments()[0])) {
                            continue serverSideMethodsLoop;
                        }
                    } else {
                        continue serverSideMethodsLoop;
                    }

                    //                	}
                } else {
                    if (!(((Class<?>) clientSideParametersTypes[i])
                            .isAssignableFrom((Class<?>) pServerParameterType.getActualTypeArguments()[0]))) {
                        continue serverSideMethodsLoop;
                    }
                }
            }

            // 4. check exception types
            Class<?>[] serverSideExceptionTypes = serverSideMethod.getExceptionTypes();
            for (Class<?> clientExceptionType : clientSideExceptionTypes) {
                boolean match = false;
                for (Class<?> serverExceptionType : serverSideExceptionTypes) {
                    if (clientExceptionType.isAssignableFrom(serverExceptionType)) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    throw new NoSuchMethodException("found a matching method in server interface for " +
                        clientSideMethod.toGenericString() +
                        " but the types of thrown exceptions do not match");
                }
            }

            if (result != null) {
                throw new NoSuchMethodException("cannot find matching method for " +
                    clientSideMethod.toGenericString() +
                    " because there are several matches in the server interface ");
            } else {
                result = serverSideMethod;
            }
        }

        if (result == null) {
            throw new NoSuchMethodException("cannot find matching method for " +
                clientSideMethod.toGenericString());
        }

        return result;
    }
}
