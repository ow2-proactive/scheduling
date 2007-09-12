package functionalTests.component.conform.components;

import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public interface Slave {
    void computeOneWay(String arg, String other);

    StringWrapper computeAsync(String arg, String other);

    GenericTypeWrapper<String> computeAsyncGenerics(String arg, String other);

    String computeSync(String arg, String other);
}
