package functionalTests.component.collectiveitf.dynamicdispatch;

import java.util.Iterator;
import java.util.List;

import org.objectweb.proactive.core.component.exceptions.ReductionException;
import org.objectweb.proactive.core.component.type.annotations.multicast.ReduceBehavior;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public class SumReduction implements ReduceBehavior {

    public Object reduce(List<?> values) throws ReductionException {
        int sum = 0;
        if (values.isEmpty()) {
            throw new ReductionException("no values to perform reduction on");
        }
        for (Iterator iterator = values.iterator(); iterator.hasNext();) {
            Object value = iterator.next();
            if (!(value instanceof IntWrapper)) {
                throw new ReductionException("wrong type: expected " + IntWrapper.class.getName() +
                    " but received " + value.getClass().getName());
            }
            IntWrapper intWrapperValue = (IntWrapper) value;
            sum += intWrapperValue.intValue();
        }
        return new IntWrapper(sum);
    }
}
