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
package org.objectweb.proactive.core.component.controller;

import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.component.group.ProxyForComponentInterfaceGroup;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * A controller for managing multicast interfaces, notably bindings and invocations on multicast interfaces
 *
 *
 * @author Matthieu Morel
 *
 */
@PublicAPI
public interface MulticastController extends CollectiveInterfaceController {

    /**
     * Transforms an invocation on a multicast interface into a list of invocations which will be
     * transferred to client interfaces. These invocations are inferred from the annotations of the
     * multicast interface and the number of connected server interfaces.
     *
     * @param mc method call on the multicast interface
     *
     * @param delegatee the group delegatee which is connected to interfaces server of this multicast interface
     *
     * @return the reified invocations to be transferred to connected server interfaces
     *
     * @throws ParameterDispatchException if there is an error in the dispatch of the parameters
     */
    public Map<MethodCall, Integer> generateMethodCallsForMulticastDelegatee(
        MethodCall mc, ProxyForComponentInterfaceGroup delegatee)
        throws ParameterDispatchException;

    /**
         * Performs a binding between a multicast client interface and a server
         * interface
         *
         * @param clientItfName
         *            name of a multicast client interface
         *
         * @param serverItf
         *            reference on a server interface
         */
    public void bindFcMulticast(String clientItfName,
        ProActiveInterface serverItf);

    /**
     * Removes a binding between a multicast client interface and a server interface
     * @param itfName namd of a multicast client interface
     * @param itfRef reference on a server interface
     */
    public void unbindFcMulticast(String itfName, ProActiveInterface itfRef);

    /**
     * Returns a reference on a multicast interface
     * @param multicastItfName name of a multicast interface
     * @return a reference on this multicast interface
     */
    public ProxyForComponentInterfaceGroup lookupFcMulticast(
        String multicastItfName);
}
