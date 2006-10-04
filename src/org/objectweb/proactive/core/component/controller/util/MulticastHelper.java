package org.objectweb.proactive.core.component.controller.util;

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
 * @author Matthieu Morel
 *
 */
public class MulticastHelper {


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
	    public static Map<MethodCall, Integer> generateMethodCallsForMulticastDelegatee(ProActiveComponent owner, 
	        MethodCall mc, ProxyForComponentInterfaceGroup delegatee)
	        throws ParameterDispatchException {
	    	
	    	// will need to find a way to avoid direct access to multicast controller through Fractive methods here
	    	// (in the case of a componentized membrane)
	    	try {
				return Fractive.getMulticastController(owner).generateMethodCallsForMulticastDelegatee(mc, delegatee);
			} catch (NoSuchInterfaceException e) {
				throw new ParameterDispatchException("no multicast controller ", e);
			}
	    }

}
