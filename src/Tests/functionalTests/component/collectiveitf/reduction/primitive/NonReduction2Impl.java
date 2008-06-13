package functionalTests.component.collectiveitf.reduction.primitive;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;

import functionalTests.component.collectiveitf.multicast.Identifiable;


public class NonReduction2Impl implements NonReduction2, Identifiable {
    private int id;

    public IntWrapper doIt() {
        System.out.println(" Server received call on doIt");
        return new IntWrapper(id);
    }

    public IntWrapper doItInt(IntWrapper val) {
        System.out.println(" Server received " + val.intValue());
        return new IntWrapper(id + val.intValue());
    }

    public void voidDoIt() {
        System.out.println(" Server received call on voidDoIt");
    }

    @Override
    public String getID() {
        return String.valueOf(id);
    }

    @Override
    public void setID(String id) {
        this.id = new Integer(id).intValue();
    }
}
