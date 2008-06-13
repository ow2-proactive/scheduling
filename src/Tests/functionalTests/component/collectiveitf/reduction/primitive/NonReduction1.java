package functionalTests.component.collectiveitf.reduction.primitive;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public interface NonReduction1 {
    public IntWrapper doIt();

    public IntWrapper doItInt(IntWrapper val);

    public void voidDoIt();
}
