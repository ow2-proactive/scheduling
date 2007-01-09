package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.lang.reflect.Type;
import java.util.List;

import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;

/**
 * This interface declares a set of methods which define the distribution strategies for parameters in multicast interfaces. 
 * 
 * @author Matthieu Morel
 *
 */
public interface ParamDispatch {
    
    /**
     * Transforms an input parameter passed to a multicast interface into input parameters for connected server interfaces 
     * @param inputParameter input parameter as given to a multicast interface
     * @param nbOutputReceivers number of server interfaces connected to the multicast interface
     * @return a map of parameters to be distributed 
     * @throws ParameterDispatchException if parameter dispatch fails
     */
    public List<Object> dispatch(Object inputParameter, int nbOutputReceivers) throws ParameterDispatchException;
    
    /**
     * Computes the number of method invocations that will be generated with the selection distribution algorithm.
     * @param inputParameter input parameter as given to a multicast interface
     * @param nbOutputReceivers number of server interfaces connected to the multicast interface
     * @return the number of method invocations to expect from the specified distribution
     * @throws ParameterDispatchException if parameter dispatch computation fails
     */
    public int expectedDispatchSize (Object inputParameter, int nbOutputReceivers) throws ParameterDispatchException;
    
    /**
     * Verifies that, for the specified distribution mode, the types of parameters are compatible between client side and server side.
     * @param clientSideInputParameter type of parameter from the client side
     * @param serverSideInputParameter type of parameter from the server side
     * @return true if the types are compatible, false otherwise
     * @throws ParameterDispatchException if verification fails
     */
    public boolean match(Type clientSideInputParameter, Type serverSideInputParameter) throws ParameterDispatchException;

}
