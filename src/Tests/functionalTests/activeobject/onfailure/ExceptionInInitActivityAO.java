package functionalTests.activeobject.onfailure;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;


public class ExceptionInInitActivityAO implements Serializable, InitActive {
    public ExceptionInInitActivityAO() {

    }

    public void initActivity(Body body) {
        throw new RuntimeException("newActive should throw an ActiveObjectCreationException");
    }

    public boolean getTrue() {
        return true;
    }

}
