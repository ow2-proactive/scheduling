package functionalTests.activeobject.miscellaneous.fifocrash;

import java.io.Serializable;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class AOCrash1 implements Serializable, EndActive {

    private AOCrash2 ao2;

    private StringWrapper future;

    public AOCrash1() {

    }

    public AOCrash1(AOCrash2 ao2) {
        this.ao2 = ao2;
    }

    public void foo() {
        // the foo method will trigger a receiveReply from object ao2
        future = ao2.foo2();
    }

    public boolean terminate() {
        PAActiveObject.terminateActiveObject(true);
        return true;
    }

    public void endActivity(Body body) {
        System.out.println("Expected end of activity for " + this.getClass().getName());
    }

}
