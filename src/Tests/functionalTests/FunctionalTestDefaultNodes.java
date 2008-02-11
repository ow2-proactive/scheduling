package functionalTests;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extra.gcmdeployment.API;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;


/**
 * If a VariableContract is needed, before() and after() can be overridden.
 * 
 * @author cmathieu
 *
 */
public class FunctionalTestDefaultNodes extends FunctionalTest {
    public enum DeploymentType {
        _0x0("0x0.xml"), _1x1("1x1.xml"), _1x2("1x2.xml"), _2x1("2x1.xml"), _4x1("4x1.xml"), _2x2("2x2.xml");

        public String filename;

        private DeploymentType(String filename) {
            this.filename = filename;
        }
    }

    static final File applicationDescriptor = new File(FunctionalTest.class.getResource(
            "/functionalTests/_CONFIG/JunitApp.xml").getFile());

    static public final String VN_NAME = "nodes";
    static public final String VAR_DEPDESCRIPTOR = "deploymentDescriptor";

    GCMApplicationDescriptor gcmad;
    DeploymentType deploymentType;
    VariableContract vContract;

    public FunctionalTestDefaultNodes(DeploymentType type) {
        this.deploymentType = type;

        vContract = new VariableContract();
        vContract.setVariableFromProgram(VAR_DEPDESCRIPTOR, "localhost/" + type.filename,
                VariableContractType.DescriptorDefaultVariable);
    }

    @Before
    public void before() throws Exception {
        startDeployment();
    }

    @After
    public void after() throws Exception {
        gcmad.kill();
    }

    public void startDeployment() throws ProActiveException {
        if (gcmad != null) {
            throw new IllegalStateException("deployment already started");
        }

        gcmad = API.getGCMApplicationDescriptor(applicationDescriptor, vContract);
        gcmad.startDeployment();
    }

    public Node getANode() {
        return getANodeFrom(VN_NAME);
    }

    private Node getANodeFrom(String vnName) {
        if (gcmad == null || !gcmad.isStarted()) {
            throw new IllegalStateException("deployment is not started");
        }

        GCMVirtualNode vn = gcmad.getVirtualNode(vnName);
        return vn.getANode();
    }
}
