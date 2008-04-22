package functionalTests.resourcemanager;

import org.junit.Before;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.extensions.resourcemanager.RMFactory;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMAdmin;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMMonitoring;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMUser;

import functionalTests.FunctionalTest;
import functionalTests.descriptor.variablecontract.javapropertiesDescriptor.Test;


public class FunctionalTestDefaultRM extends FunctionalTest {

    protected RMUser user;
    protected RMAdmin admin;
    protected RMMonitoring monitor;

    private static String defaultDescriptor = Test.class.getResource(
            "/functionalTests/resourcemanager/localRMNodes.xml").getPath();

    public int defaultDescriptorNodesNb = 5;

    @Before
    public void before() throws Exception {

        RMFactory.startLocal();
        user = RMFactory.getUser();
        admin = RMFactory.getAdmin();
        monitor = RMFactory.getMonitoring();
    }

    public void deployDefault() throws Exception {
        admin.addNodes(PADeployment.getProactiveDescriptor(defaultDescriptor));
    }
}
