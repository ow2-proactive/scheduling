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
     * @throws ParameterDispatchException
     */
    public List<Object> dispatch(Object inputParameter, int nbOutputReceivers) throws ParameterDispatchException;
    
    public int expectedDispatchSize (Object inputParameter, int nbOutputReceivers) throws ParameterDispatchException;
    
    public boolean match(Type clientSideInputParameter, Type serverSideInputParameter) throws ParameterDispatchException;

}
