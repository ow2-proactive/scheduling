package org.objectweb.proactive.core.component.controller.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.controller.MulticastBindingChecker;
import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.component.group.ProxyForComponentInterfaceGroup;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatch;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.util.SerializableMethod;


/**
 * Simple helper class 
 * 
 * @author Matthieu Morel
 *
 */
public class MulticastHelper {

	// matching methods for multicast interfaces are inferred at binding time and referenced by (bodyID, multicastItfName)  
	public static Map<UniqueID, Map<String, Map<SerializableMethod, SerializableMethod>>> matchingMethods = new HashMap<UniqueID, Map<String, Map<SerializableMethod,SerializableMethod>>>();

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
	        // read from annotations
	        Object[] clientSideEffectiveArguments = mc.getEffectiveArguments();
	
	        ProActiveInterfaceType itfType;
			try {
				itfType = (ProActiveInterfaceType) ((ProActiveInterface)owner.getFcInterface(mc.getComponentMetadata()
				        .getComponentInterfaceName()))
				 .getFcItfType();
			} catch (NoSuchInterfaceException e1) {
				throw new ParameterDispatchException("cannot get multicast interface while generating list of invocations", e1);
			}
	
	        Method matchingMethodInClientInterface; // client itf as well as parent interfaces
	
	        try {
	            matchingMethodInClientInterface = Class.forName(itfType.getFcItfSignature())
	                                                   .getMethod(mc.getReifiedMethod()
	                                                                .getName(),
	                    mc.getReifiedMethod().getParameterTypes());
	        } catch (Exception e) {
	            e.printStackTrace();
	            throw new ParameterDispatchException(e.fillInStackTrace());
	        }
	
	        Class[] clientSideParamTypes = matchingMethodInClientInterface.getParameterTypes();
	        ParamDispatch[] clientSideParamDispatchModes = MulticastBindingChecker.getDispatchModes(matchingMethodInClientInterface);
	
	        List<List<Object>> dispatchedParameters = new ArrayList<List<Object>>();
	
	        int expectedMethodCallsNb = 0;
	
	        // compute dispatch sizes for annotated parameters
	        Vector<Integer> dispatchSizes = new Vector<Integer>();
	
	        for (int i = 0; i < clientSideParamTypes.length; i++) {
	            dispatchSizes.addElement(clientSideParamDispatchModes[i].expectedDispatchSize(
	                    clientSideEffectiveArguments[i], delegatee.size()));
	        }
	
	        if (dispatchSizes.size() > 0) {
	            // ok, found some annotated elements
	            expectedMethodCallsNb = dispatchSizes.get(0);
	
	            for (int i = 1; i < dispatchSizes.size(); i++) {
	                if (dispatchSizes.get(i).intValue() != expectedMethodCallsNb) {
	                    throw new ParameterDispatchException(
	                        "cannot generate invocation for multicast interface " +
	                        itfType.getFcItfName() +
	                        "because the specified distribution of parameters is incorrect in method " +
	                        matchingMethodInClientInterface.getName());
	                }
	            }
	        } else {
	            // broadcast to every member of the group 
	            expectedMethodCallsNb = delegatee.size();
	        }
	
	        // get distributed parameters
	        for (int i = 0; i < clientSideParamTypes.length; i++) {
	            List<Object> dispatchedParameter = clientSideParamDispatchModes[i].dispatch(clientSideEffectiveArguments[i],
	                    delegatee.size());
	            dispatchedParameters.add(dispatchedParameter);
	        }
	
	        Map<MethodCall, Integer> result = new HashMap<MethodCall, Integer>(expectedMethodCallsNb);
	
	        // need to find matching method in server interface
	        try {
	            Method matchingMethodInServerInterface = MulticastHelper.matchingMethods.get(ProActive.getBodyOnThis().getID()).get(mc.getComponentMetadata()
	                                                                           .getComponentInterfaceName())
	                                                                    .get(new SerializableMethod(mc.getReifiedMethod())).getMethod();
	
	            // now we have all dispatched parameters
	            // proceed to generation of method calls
	            for (int generatedMethodCallIndex = 0;
	                    generatedMethodCallIndex < expectedMethodCallsNb;
	                    generatedMethodCallIndex++) {
	                Object[] individualEffectiveArguments = new Object[matchingMethodInServerInterface.getParameterTypes().length];
	
	                for (int parameterIndex = 0;
	                        parameterIndex < individualEffectiveArguments.length;
	                        parameterIndex++) {
	                    individualEffectiveArguments[parameterIndex] = dispatchedParameters.get(parameterIndex)
	                                                                                       .get(generatedMethodCallIndex); // initialize 
	                }
	
	                result.put(new MethodCall(matchingMethodInServerInterface,
	                        individualEffectiveArguments, mc.getMetadata()),
	                    generatedMethodCallIndex % delegatee.size());
	                // default is to do some round robin when nbGeneratedMethodCalls > nbReceivers
	            }
	        } catch (SecurityException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	
	        return result;
	    }


}
