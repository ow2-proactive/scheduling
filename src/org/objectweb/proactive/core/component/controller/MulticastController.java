package org.objectweb.proactive.core.component.controller;

import java.util.Map;

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
public interface MulticastController extends CollectiveInterfaceController {
    //	
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
