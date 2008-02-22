package functionalTests.activeobject.miscellaneous.deadlocks.usecase1;

import java.io.Serializable;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public class AODeadlock2 implements Serializable {

    private AODeadlock1 ao1;

    public AODeadlock2() {

    }

    public void setAODeadlock1(AODeadlock1 ao1) {
        this.ao1 = ao1;
    }

    public IntWrapper answer() {
        ao1.callback();
        return new IntWrapper(1);
    }

}
