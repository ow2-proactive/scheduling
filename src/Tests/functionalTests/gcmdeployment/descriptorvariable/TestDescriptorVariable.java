package functionalTests.gcmdeployment.descriptorvariable;

import java.io.File;

import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;


public class TestDescriptorVariable {

    @Test
    public void simpleTest() throws ProActiveException {
        File desc = new File(this.getClass().getResource("simple.xml").getPath());
        PAGCMDeployment.loadApplicationDescriptor(desc);
    }

    @Test
    public void recursiveTest1() throws ProActiveException {
        File desc = new File(this.getClass().getResource("recursiveDescriptorVar.xml").getPath());
        PAGCMDeployment.loadApplicationDescriptor(desc);
    }

    @Test
    public void recursiveTest2() throws ProActiveException {
        File desc = new File(this.getClass().getResource("recursiveJavaProp.xml").getPath());
        PAGCMDeployment.loadApplicationDescriptor(desc);
    }
}
