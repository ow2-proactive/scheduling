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

    static final private File defaultApplicationDescriptor = new File(FunctionalTest.class.getResource(
            "/functionalTests/_CONFIG/JunitApp.xml").getFile());

    static public final String VN_NAME = "nodes";
    static public final String VAR_DEPDESCRIPTOR = "deploymentDescriptor";
    static public final String VAR_JVMARG = "jvmargDefinedByTest";

    static public final String VAR_HOSTCAPACITY = "hostCapacity";
    int hostCapacity;

    static public final String VAR_VMCAPACITY = "vmCapacity";
    int vmCapacity;

    public GCMFunctionalTestDefaultNodes(int hostCapacity, int vmCapacity) {
        super(defaultApplicationDescriptor);

        this.hostCapacity = hostCapacity;
        this.vmCapacity = vmCapacity;

        super.vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_HOSTCAPACITY, new Integer(
            hostCapacity).toString(), VariableContractType.DescriptorDefaultVariable);
        super.vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_VMCAPACITY, new Integer(
            vmCapacity).toString(), VariableContractType.DescriptorDefaultVariable);

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
