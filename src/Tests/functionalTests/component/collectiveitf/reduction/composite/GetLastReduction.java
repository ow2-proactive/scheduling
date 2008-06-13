package functionalTests.component.collectiveitf.reduction.composite;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.core.component.exceptions.ReductionException;
import org.objectweb.proactive.core.component.type.annotations.multicast.ReduceBehavior;


public class GetLastReduction implements ReduceBehavior, Serializable {
    public Object reduce(List<?> values) throws ReductionException {
        System.out.println("--------------");
        System.out.println("Getting last out of " + values.size() + " elements");
        System.out.println("--------------");
        return values.get(values.size() - 1);
    }
}
