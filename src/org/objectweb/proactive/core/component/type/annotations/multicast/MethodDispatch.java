package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.util.List;

import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;


public interface MethodDispatch {
    
	/**
	 * Dispatches input parameters as passed to a multicast interfaces, into a list of invocation parameters for connected server interfaces
	 * @param inputParameters parameters passed to the multicast interface 
	 * @param nbOutputReceivers number of connected server interfaces
	 * @return input parameters to be passed to connected server interfaces  
	 * @throws ParameterDispatchException if dispatching failed
	 */
    public List<Object>[] dispatch(Object[] inputParameters, int nbOutputReceivers) throws ParameterDispatchException;
    
    public int expectedDispatchSize (Object[] inputParameters, int nbOutputReceivers) throws ParameterDispatchException;
    

}
