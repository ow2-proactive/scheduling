package functionalTests.jmx.mbean;

import java.io.Serializable;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;


public class A implements Serializable {
    public A() {
        // Empty Constructor
    }

    public boolean existBodyWrapperMBean() {
        return (ProActive.getBodyOnThis().getMBean() != null);
    }

    public boolean existProActiveRuntimeWrapperMBean() {
        return (ProActiveRuntimeImpl.getProActiveRuntime().getMBean() != null);
    }
}
