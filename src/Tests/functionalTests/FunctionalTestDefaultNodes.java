package functionalTests;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.VariableContract;
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
    static final File applicationDescriptor = new File(FunctionalTest.class.getResource(
            "FunctionalTestApplication.xml").getFile());

    static public final String VAR_LOCAL_DEPDESCRIPTOR = "localDeploymentDescriptor";
    static public final String VAR_REMOTE_DEPDESCRIPTOR = "remoteDeploymentDescriptor";

    public GCMApplicationDescriptor gcmad;

    @Before
    public void before() throws Exception {
        startDeployment();
    }

    @After
    public void after() throws Exception {
        gcmad.kill();
    }

    public void startDeployment() throws ProActiveException {
        startDeployment(null);
    }

    public void startDeployment(VariableContract contract) throws ProActiveException {
        if (gcmad != null) {
            throw new IllegalStateException("deployment already started");
        }

        gcmad = API.getGCMApplicationDescriptor(applicationDescriptor, contract);
        gcmad.startDeployment();
    }

    public Node getALocalNode() {
        return getANodeFrom(VN_LOCAL);
    }

    public Node getARemoteNode() {
        return getANodeFrom(VN_REMOTE);
    }

    private Node getANodeFrom(String vnName) {
        if (gcmad == null || !gcmad.isStarted()) {
            throw new IllegalStateException("deployment is not started");
        }

        GCMVirtualNode vn = gcmad.getVirtualNode(vnName);
        return vn.getANode();
    }
}
