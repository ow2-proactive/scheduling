package functionalTests.activeobject.getstubonthis;

import java.io.File;
import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTestDefaultNodes;
import functionalTests.GCMFunctionalTestDefaultNodes.DeploymentType;


public class DeployerAO implements Serializable, InitActive {
    GCMApplication gcma;
    boolean notified = false;

    public DeployerAO() {

    }

    public DeployerAO(GCMApplication gcma) {
        this.gcma = gcma;
    }

    public void initActivity(Body body) {
        try {
            File appDesc = new File(this.getClass().getResource("/functionalTests/_CONFIG/JunitApp.xml")
                    .getFile());

            VariableContractImpl vContract = new VariableContractImpl();
            vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_DEPDESCRIPTOR, "localhost/" +
                DeploymentType._1x1.filename, VariableContractType.DescriptorDefaultVariable);
            vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_OS, OperatingSystem
                    .getOperatingSystem().name(), VariableContractType.DescriptorDefaultVariable);

            GCMApplication gcma = PAGCMDeployment.loadApplicationDescriptor(appDesc, vContract);

            GCMVirtualNode vn = gcma.getVirtualNode("nodes");
            vn.subscribeNodeAttachment(PAActiveObject.getStubOnThis(), "callback", false);

            gcma.startDeployment();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callback(Node node, GCMVirtualNode vn) {
        System.out.println("Callback called");
    }

    public boolean waitUntilCallbackOccur() throws InterruptedException {
        while (!notified) {
            Thread.sleep(250);
        }

        return true;
    }

}
