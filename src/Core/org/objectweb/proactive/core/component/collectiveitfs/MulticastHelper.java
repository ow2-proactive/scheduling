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

import java.util.Map;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.component.group.ProxyForComponentInterfaceGroup;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * Simple helper class
 *
 * @author The ProActive Team
 *
 */
public class MulticastHelper {

    /**
     * Transforms an invocation on a multicast interface into a list of
     * invocations which will be transferred to client interfaces. These
     * invocations are inferred from the annotations of the multicast interface
     * and the number of connected server interfaces.
     *
     * @param mc
     *            method call on the multicast interface
     * @param delegatee
     *            the group delegatee which is connected to interfaces server of
     *            this multicast interface
     * @return the reified invocations to be transferred to connected server
     *         interfaces
     * @throws ParameterDispatchException
     *             if there is an error in the dispatch of the parameters
     */
    public static Map<MethodCall, Integer> generateMethodCallsForMulticastDelegatee(ProActiveComponent owner,
            MethodCall mc, ProxyForComponentInterfaceGroup delegatee) throws ParameterDispatchException {
        try {
            return Fractive.getMulticastController(owner).generateMethodCallsForMulticastDelegatee(mc,
                    delegatee);
        } catch (NoSuchInterfaceException e) {
            throw new ParameterDispatchException("no multicast controller ", e);
        }
    }
}
