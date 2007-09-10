package functionalTests.component.conform.components;

import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public interface Slave {
    void compute(String arg, String other);

    StringWrapper computeAsync(String arg, String other);

    String computeSync(String arg, String other);
}
