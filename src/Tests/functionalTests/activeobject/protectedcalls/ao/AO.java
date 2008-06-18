package functionalTests.activeobject.protectedcalls.ao;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;


public class AO implements RunActive {
    public AO() {
        // Empty
    }

    protected IntWrapper foo() {
        return new IntWrapper(-10);
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        service.waitForRequest();
        service.serveAll("foo");
    }
}
