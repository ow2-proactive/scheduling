package nodestate;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.common.FileToBytesConverter;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.RMUser;

import functionalTests.FunctionalTest;


public class FunctionalTDefaultRM extends FunctionalTest {

    protected RMUser user;
    protected RMAdmin admin;
    protected RMMonitoring monitor;

    private static String functionalTestRMProperties = 
    	FunctionalTDefaultRM.class.getResource("/nodestate/functionalTRMProperties.ini").getPath();
    	
    protected static String defaultDescriptor =
     	FunctionalTDefaultRM.class.getResource("/nodestate/GCMNodeSourceDeployment.xml").getPath();

    protected int defaultDescriptorNodesNb = 5;

    @Before
    public void before() throws Exception {

        PAResourceManagerProperties.updateProperties(functionalTestRMProperties);
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
