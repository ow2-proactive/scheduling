package org.objectweb.proactive.examples.components.userguide.multicast;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


//@ClassDispatchMetadata(mode = @ParamDispatchMetadata(mode = ParamDispatchMode.ROUND_ROBIN)
//)
public class SlaveImpl implements Slave {
    public SlaveImpl() {
    }

    public void compute(String arg, String other) {
        System.err.println(PAActiveObject.getBodyOnThis().getNodeURL() + "Slave: " + this + " arg: " + arg +
            " other: " + other);
    }

    public StringWrapper computeAsync(String arg, String other) {
        return new StringWrapper(computeSync(arg, other));
    }

    public String computeSync(String arg, String other) {
        compute(arg, other);
        return "arg: '" + arg + "',other: '" + other + "'";
    }
}
