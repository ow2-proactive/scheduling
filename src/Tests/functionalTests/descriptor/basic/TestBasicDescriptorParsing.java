package functionalTests.descriptor.basic;

import org.junit.Test;
import org.objectweb.proactive.api.ProDeployment;


public class TestBasicDescriptorParsing {
    @Test
    public void action() throws Exception {
        String descriptorLocation = getClass()
                                        .getResource("javaproperty_ERROR.xml")
                                        .getPath();

        Object proActiveDescriptor = ProDeployment.getProactiveDescriptor(
                "file:" + descriptorLocation);
    }
}
