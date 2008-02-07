package unitTests.gcmdeployment.virtualnode;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.TechnicalServicesProperties;


public class NodeMockup implements Node {
    NodeInformationMockUp nodeInformation;

    public NodeMockup(int i) {
        nodeInformation = new NodeInformationMockUp(new Integer(i).toString());
    }

    public NodeMockup(String name) {
        nodeInformation = new NodeInformationMockUp(name);
    }

    public Object[] getActiveObjects() throws NodeException, ActiveObjectCreationException {
        return null;
    }

    public Object[] getActiveObjects(String className) throws NodeException, ActiveObjectCreationException {
        return null;
    }

    public NodeInformation getNodeInformation() {
        return nodeInformation;
    }

    public int getNumberOfActiveObjects() throws NodeException {
        return 0;
    }

    public ProActiveRuntime getProActiveRuntime() {
        return null;
    }

    public String getProperty(String key) throws ProActiveException {
        return null;
    }

    public VMInformation getVMInformation() {
        return null;
    }

    public void killAllActiveObjects() throws NodeException, IOException {
    }

    public Object setProperty(String key, String value) throws ProActiveException {
        return null;
    }
}
