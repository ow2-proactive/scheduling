package nonregressiontest.component.collectiveitf.multicast;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatch;


public class CustomParametersDispatch implements ParamDispatch {

    /*
       * @see org.objectweb.proactive.core.component.type.annotations.collective.ParamDispatch#dispatch(java.lang.Object, int)
       */
    public List<Object> dispatch(Object inputParameter, int nbOutputReceivers)
        throws ParameterDispatchException {

        if (!(inputParameter instanceof List)
                || !(((List) inputParameter).size() >= 1)
                || !(((List) inputParameter).get(0) instanceof WrappedInteger)) {
            throw new ParameterDispatchException(
                    "needs a List of (at least 1) WrappedInteger elements");
        }

        List<Object> result = new ArrayList<Object>();
        result.add((WrappedInteger)((List) inputParameter).get(0));
        return result;
    }

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.collective.ParamDispatch#expectedDispatchSize(java.lang.Object, int)
     */
    public int expectedDispatchSize(Object inputParameter, int nbOutputReceivers)
        throws ParameterDispatchException {
        return 1;
    }

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.collective.ParamDispatch#match(java.lang.reflect.Type, java.lang.reflect.Type)
     */
    public boolean match(Type clientSideInputParameterType, Type serverSideInputParameterType) throws ParameterDispatchException {

        try {
            boolean one = ((Class)((ParameterizedType)clientSideInputParameterType).getRawType()).equals(List.class);
            boolean two = ((Class)((ParameterizedType)clientSideInputParameterType).getActualTypeArguments()[0]).equals(WrappedInteger.class);
            boolean three = ((Class)serverSideInputParameterType).equals(WrappedInteger.class);
            return one && two && three;
        } catch (ClassCastException e) {
            return false;
        }
    }    
    
}
