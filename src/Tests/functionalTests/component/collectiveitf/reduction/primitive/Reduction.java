package functionalTests.component.collectiveitf.reduction.primitive;

import org.objectweb.proactive.core.component.type.annotations.multicast.Reduce;
import org.objectweb.proactive.core.component.type.annotations.multicast.ReduceMode;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public interface Reduction {
    @Reduce(reductionMode = ReduceMode.CUSTOM, customReductionMode = SumReduction.class)
    public IntWrapper doIt();

    @Reduce(reductionMode = ReduceMode.CUSTOM, customReductionMode = SumReduction.class)
    public IntWrapper doItInt(IntWrapper val);

    public void voidDoIt();
}
