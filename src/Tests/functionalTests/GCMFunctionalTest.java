package functionalTests;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;


public class GCMFunctionalTest extends FunctionalTest {

    static public final String VAR_OS = "os";

    public File applicationDescriptor;
    public VariableContractImpl vContract;
    public GCMApplication gcmad;

    public GCMFunctionalTest() {
        vContract = new VariableContractImpl();
        vContract.setVariableFromProgram(VAR_OS, OperatingSystem.getOperatingSystem().name(),
                VariableContractType.DescriptorDefaultVariable);
    }

    public GCMFunctionalTest(File applicationDescriptor) {
        this();
        this.applicationDescriptor = applicationDescriptor;
    }

    @Before
    public void startDeployment() throws ProActiveException {
        if (gcmad != null) {
            throw new IllegalStateException("deployment already started");
        }

        gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor, vContract);
        gcmad.startDeployment();
    }

    @After
    public void killDeployment() {
        if (gcmad != null) {
            gcmad.kill();
        }
    }
}
