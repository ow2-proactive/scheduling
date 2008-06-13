package functionalTests.component.collectiveitf.unicast;

import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class ServerImpl implements OfferedService,
        functionalTests.component.collectiveitf.multicast.Identifiable {

    String id;

    public StringWrapper method1(String parameter) {
        // TODO Auto-generated method stub
        return new StringWrapper("server " + id + " received " + parameter);
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;

    }

}
