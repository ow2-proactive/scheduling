package functionalTests.component.conform.components;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class SlaveImpl implements Slave {
    public SlaveImpl() {
    }

    public void computeOneWay(String arg, String other) {
        System.err.println(ProActiveObject.getBodyOnThis().getNodeURL() + "Slave: " +
            this + " arg: " + arg + " other: " + other);
    }

    public StringWrapper computeAsync(String arg, String other) {
        return new StringWrapper(computeSync(arg, other));
    }

    public GenericTypeWrapper<String> computeAsyncGenerics(String arg,
        String other) {
        return new GenericTypeWrapper<String>(computeSync(arg, other));
    }

    public String computeSync(String arg, String other) {
        computeOneWay(arg, other);
        return "arg: '" + arg + "',other: '" + other + "'";
    }
}
