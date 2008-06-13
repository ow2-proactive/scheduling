package functionalTests.component.collectiveitf.unicast;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.group.DispatchBehavior;
import org.objectweb.proactive.core.mop.MethodCall;


public class CustomUnicastDispatch implements DispatchBehavior {

    int index = 1;

    public List<Integer> getTaskIndexes(MethodCall originalMethodCall, List<MethodCall> generatedMethodCall,
            int nbWorkers) {
        List<Integer> l = new ArrayList<Integer>(1);

        if (!(generatedMethodCall.size() == 1)) {
            throw new RuntimeException("invalid number");
        }
        if (RunnerImpl.PARAMETER_1.equals(generatedMethodCall.iterator().next().getEffectiveArguments()[0])) {
            l.add(0);
        } else if (RunnerImpl.PARAMETER_2.equals(generatedMethodCall.iterator().next()
                .getEffectiveArguments()[0])) {
            l.add(1);
        }

        return l;
    }

}
