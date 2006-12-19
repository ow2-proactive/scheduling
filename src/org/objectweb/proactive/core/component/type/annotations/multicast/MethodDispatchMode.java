package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;


public enum MethodDispatchMode implements MethodDispatch, Serializable {BROADCAST, ONE_TO_ONE, 
    ROUND_ROBIN, CUSTOM;

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.collective.MethodDispatch#dispatch(java.lang.Object[], int)
     */
    public List<Object>[] dispatch(Object[] inputParameters, int nbOutputReceivers) throws ParameterDispatchException {

        List<Object>[] result = new List[inputParameters.length];
        for (int i=0; i<inputParameters.length; i++) {
            switch (this) {
            case BROADCAST:
                result[i]=ParamDispatchMode.BROADCAST.dispatch(inputParameters[i], nbOutputReceivers);
                break;
            case ONE_TO_ONE:
                result[i] = ParamDispatchMode.ONE_TO_ONE.dispatch(inputParameters[i], nbOutputReceivers);
                break;
            case ROUND_ROBIN:
                result[i]=ParamDispatchMode.ROUND_ROBIN.dispatch(inputParameters[i], nbOutputReceivers);
                break;
            default:
                result[i]=ParamDispatchMode.BROADCAST.dispatch(inputParameters[i], nbOutputReceivers);
                break;
            } 
        }
        return result;
    }

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.collective.MethodDispatch#expectedDispatchSize(java.lang.Object[], int)
     */
    public int expectedDispatchSize(Object[] inputParameters, int nbOutputReceivers) throws ParameterDispatchException {
        int result = 0;
        if (inputParameters.length>=1) {
            // return the first result, as all results are equal
            switch(this) {
            case BROADCAST:
                result= ParamDispatchMode.BROADCAST.expectedDispatchSize(inputParameters[0], nbOutputReceivers);
                break;
            case ONE_TO_ONE:
                result= ParamDispatchMode.ONE_TO_ONE.expectedDispatchSize(inputParameters[0], nbOutputReceivers);
                break;
            case ROUND_ROBIN:
                result= ParamDispatchMode.ROUND_ROBIN.expectedDispatchSize(inputParameters[0], nbOutputReceivers);
                break;
            default:
                result = ParamDispatchMode.BROADCAST.expectedDispatchSize(inputParameters[0], nbOutputReceivers);
                break;
            }
        } 
        return result;
    }


}
