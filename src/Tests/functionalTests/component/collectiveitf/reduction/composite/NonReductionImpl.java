package functionalTests.component.collectiveitf.reduction.composite;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public class NonReductionImpl implements NonReduction {
    public IntWrapper doIt() {
        System.out.println(" Server received call on doIt");
        return new IntWrapper(123);
    }

    public IntWrapper doItInt(IntWrapper val) {
        System.out.println(" Server received " + val.intValue());
        return new IntWrapper(123);
    }

    public void voidDoIt() {
        System.out.println(" Server received call on voidDoIt");
    }
}
