package functionalTests;

import java.io.File;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * If a VariableContract is needed, before() and after() can be overridden.
 * 
 * @author The ProActive Team
 * 
 */
public class GCMFunctionalTestDefaultNodes extends GCMFunctionalTest {
    public enum DeploymentType {
        _1x1("1x1.xml", 1), _1x2("1x2.xml", 2), _2x1("2x1.xml", 2), _4x1("4x1.xml", 4), _2x2("2x2.xml", 4);

        public String filename;
        public int size;

        private DeploymentType(String filename, int size) {
            this.filename = filename;
            this.size = size;
        }

    }

    static final private File defaultApplicationDescriptor = new File(FunctionalTest.class.getResource(
            "/functionalTests/_CONFIG/JunitApp.xml").getFile());

    static public final String VN_NAME = "nodes";
    static public final String VAR_DEPDESCRIPTOR = "deploymentDescriptor";
    static public final String VAR_JVMARG = "jvmargDefinedByTest";

    DeploymentType deploymentType;

    public GCMFunctionalTestDefaultNodes(DeploymentType type) {
        super(defaultApplicationDescriptor);
        this.deploymentType = type;

        super.vContract.setVariableFromProgram(VAR_DEPDESCRIPTOR,
                "localhost/" + this.deploymentType.filename, VariableContractType.DescriptorDefaultVariable);
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
