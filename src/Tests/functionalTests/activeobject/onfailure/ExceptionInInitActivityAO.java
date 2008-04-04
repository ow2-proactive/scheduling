package functionalTests.activeobject.onfailure;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;


public class ExceptionInInitActivityAO implements Serializable, InitActive {
    public ExceptionInInitActivityAO() {

    }

    public void initActivity(Body body) {
        throw new RuntimeException("An occurs during initActivity...");
    }

    public boolean getTrue() {
        return true;
    }

}
