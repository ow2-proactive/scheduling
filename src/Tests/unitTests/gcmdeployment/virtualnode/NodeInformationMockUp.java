package unitTests.gcmdeployment.virtualnode;

import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.VMInformation;


public class NodeInformationMockUp implements NodeInformation {
    String name;

    public NodeInformationMockUp(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getProtocol() {
        return null;
    }

    public String getURL() {
        return null;
    }

    public VMInformation getVMInformation() {
        return null;
    }

    public void setJobID(String jobId) {

    }

    public String getJobID() {
        return null;
    }

}
