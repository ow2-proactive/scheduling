package functionalTests.component.collectiveitf.dynamicdispatch;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public class ServerImpl implements OfferedService,
        functionalTests.component.collectiveitf.multicast.Identifiable {

    String id;

    public IntWrapper method1(Integer parameter) {
        return new IntWrapper(parameter);
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;

    }

    public Result execute(Task t) {
        return t.execute(Integer.valueOf(id));
    }

}
