package functionalTests.resourcemanager;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.process.JVMNodeProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.extensions.resourcemanager.RMFactory;
import org.objectweb.proactive.extensions.resourcemanager.common.FileToBytesConverter;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMAdmin;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMMonitoring;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMUser;

import functionalTests.FunctionalTest;
import functionalTests.descriptor.variablecontract.javapropertiesDescriptor.Test;


public class FunctionalTDefaultRM extends FunctionalTest {

    protected RMUser user;
    protected RMAdmin admin;
    protected RMMonitoring monitor;

    protected static String defaultDescriptor = Test.class.getResource(
            "/functionalTests/resourcemanager/GCMNodeSourceDeployment.xml").getPath();
    protected int defaultDescriptorNodesNb = 5;

    @Before
    public void before() throws Exception {
        RMFactory.startLocal();
        user = RMFactory.getUser();
        admin = RMFactory.getAdmin();
        monitor = RMFactory.getMonitoring();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deployDefault() throws Exception {
        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray((new File(defaultDescriptor)));
        admin.createGCMNodesource(GCMDeploymentData, "GCM_Node_Source");
    }

    public void createNode(String nodeName) throws IOException {

        JVMProcessImpl nodeProcess = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        nodeProcess.setClassname("org.objectweb.proactive.core.node.StartNode");
        nodeProcess.setJvmOptions(FunctionalTest.JVM_PARAMETERS);
        nodeProcess.setParameters(nodeName);
        nodeProcess.startProcess();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
