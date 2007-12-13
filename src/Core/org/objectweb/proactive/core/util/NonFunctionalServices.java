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
package org.objectweb.proactive.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;


/**
 *  <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 *  <p>
 *  This class is a way to add non functionnal services to managed active objects.
 *
 * The methods are reifed in the active object's Stub, so that their implementation is transparent for the programmer.
 *
 */
public class NonFunctionalServices {
    static Class<?> nonFunctionalServicesClass = null;
    static Method terminateAOMethod = null;
    static Method terminateAOImmediatelyMethod = null;
    static Class<?>[] paramTypes;

    static {
        try {
            nonFunctionalServicesClass = java.lang.Class
                    .forName("org.objectweb.proactive.core.util.NonFunctionalServices");
            paramTypes = new Class<?>[1];
            paramTypes[0] = java.lang.Class.forName("org.objectweb.proactive.core.mop.Proxy");
            terminateAOMethod = nonFunctionalServicesClass.getMethod("_terminateAO", paramTypes);
            terminateAOImmediatelyMethod = nonFunctionalServicesClass.getMethod("_terminateAOImmediately",
                    paramTypes);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException es) {
            es.printStackTrace();
        } catch (NoSuchMethodException en) {
            en.printStackTrace();
        }
    }

    /**
     * Reify the "_terminateAO" method.
     * @param proxy
     * @throws Throwable
     */
    public static void terminateAO(Proxy proxy) throws Throwable {
        proxy.reify(MethodCall.getMethodCall(terminateAOMethod, paramTypes,
                (Map<TypeVariable, Class<?>>) null));
    }

    /**
     * Reify the "_terminateAOImmediately" method.
     * A call on this method is an immediateService.
     * @param proxy
     * @throws Throwable
     */
    public static void terminateAOImmediately(Proxy proxy) throws Throwable {
        proxy.reify(MethodCall.getMethodCall(terminateAOImmediatelyMethod, paramTypes,
                (Map<TypeVariable, Class<?>>) null));
    }

    /**
     * This method is reified by terminateAO(Proxy proxy).
     * The _terminateAO request is then intercepted by BodyImpl.serve() which calls AbstractBody.terminate().
     * @param proxy
     */
    public void _terminateAO(Proxy proxy) {
    }

    /**
     * This method is reified by terminateAOImmediately(Proxy proxy).
     * The _terminateAOImmediately request is turn into an immediate service,
     * then intercepted by BodyImpl.serve() which calls AbstractBody.terminate().
     * @param proxy
     */
    public void _terminateAOImmediately(Proxy proxy) {
    }
}
