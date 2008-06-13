package functionalTests.component.collectiveitf.reduction.composite;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public interface NonReduction {
    public IntWrapper doIt();

    public IntWrapper doItInt(IntWrapper val);

    public void voidDoIt();
}
