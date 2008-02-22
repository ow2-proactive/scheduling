package functionalTests.activeobject.miscellaneous.deadlocks.usecase1;

import java.io.Serializable;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;


public class AODeadlock1 implements Serializable, InitActive, RunActive {

    private AODeadlock1 stub;
    private AODeadlock2 ao2;

    public AODeadlock1() {

    }

    public AODeadlock1(AODeadlock2 ao2) {
        this.ao2 = ao2;
    }

    public void callback() {
        System.out.println("Callback");
    }

    public int foo() {
        return ao2.answer().intValue();
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        while (true) {
            service.waitForRequest();
            service.serveAll("foo");
            service.serveAll("callback");
        }

    }

    public void initActivity(Body body) {
        stub = (AODeadlock1) PAActiveObject.getStubOnThis();

    }

    public static void main(String[] args) throws ActiveObjectCreationException, NodeException {
        AODeadlock2 ao2 = (AODeadlock2) PAActiveObject.newActive(AODeadlock2.class.getName(), new Object[0]);
        AODeadlock1 ao1 = (AODeadlock1) PAActiveObject.newActive(AODeadlock1.class.getName(),
                new Object[] { ao2 });
        ao2.setAODeadlock1(ao1);

        int iw = ao1.foo();
        System.out.println(iw);

    }
}
