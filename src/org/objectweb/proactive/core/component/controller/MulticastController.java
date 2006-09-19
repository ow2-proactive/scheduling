package org.objectweb.proactive.core.component.controller;

import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.group.ProxyForComponentInterfaceGroup;


/**
 * A controller for managing multicast interfaces, notably bindings and invocations on multicast interfaces
 * 
 * 
 * @author Matthieu Morel
 *
 */
public interface MulticastController extends CollectiveInterfaceController {
    
//	
//    public Map<MethodCall, Integer> generateMethodCallsForMulticastDelegatee(
//        MethodCall mc, ProxyForComponentInterfaceGroup delegatee)
//        throws ParameterDispatchException;

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
