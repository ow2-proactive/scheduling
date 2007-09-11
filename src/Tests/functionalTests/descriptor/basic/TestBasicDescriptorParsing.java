package functionalTests.descriptor.basic;

import org.junit.Test;
import org.objectweb.proactive.ProActive;


public class TestBasicDescriptorParsing {
    @Test
    public void action() throws Exception {
        String descriptorLocation = getClass()
                                        .getResource("javaproperty_ERROR.xml")
                                        .getPath();

        Object proActiveDescriptor = ProActive.getProactiveDescriptor("file:" +
                descriptorLocation);
    }
}
