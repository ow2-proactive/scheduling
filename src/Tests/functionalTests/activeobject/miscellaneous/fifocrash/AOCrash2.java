package functionalTests.activeobject.miscellaneous.fifocrash;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class AOCrash2 implements Serializable, EndActive {

    private boolean aliveCalled = false;

    public AOCrash2() {

    }

    public StringWrapper foo2() {
        StringBuilder sb = new StringBuilder();
        for (long i = 0L; i < 30000000L; i++) {
            sb.append("X");
        }
        return new StringWrapper(sb.toString());
    }

    public BooleanWrapper alive() {
        aliveCalled = true;
        return new BooleanWrapper(true);
    }

    public void endActivity(Body body) {
        if (!aliveCalled) {
            throw new RuntimeException("Unexpected End of Activity");
        } else {
            System.out.println("Expected end of activity for " + this.getClass().getName());
        }
    }

}
