package functionaltests.utils;

import functionaltests.monitor.RMMonitorEventReceiver;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.NotConnectedException;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.tests.ProActiveTest;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class RMFunctionalTest extends ProActiveTest {

    static {
        configureLogging();
        ProActiveConfiguration.load();
    }

    protected static final Logger logger = Logger.getLogger("RMTests");

    @Rule
    public Timeout testTimeout = new Timeout(CentralPAPropertyRepository.PA_TEST_TIMEOUT.getValue(),
        TimeUnit.MILLISECONDS);

    protected RMTHelper rmHelper;

    // For tests which use a single test node (separate JVM process)
    protected TestNode testNode;

    // For tests which use multiple test nodes (separate JVM processes)
    protected List<TestNode> testNodes = new ArrayList<>();

    @Before
    public void prepareForTest() throws Exception {
        CentralPAPropertyRepository.PA_TEST.setValue(true);
        CentralPAPropertyRepository.PA_RUNTIME_PING.setValue(false);

        rmHelper = new RMTHelper();
        try {
            cleanState();
        } catch (IllegalArgumentException | NotConnectedException ignored) {
            // ns extra not found
        }
    }

    /**
     * Kill all standalone nodes created by the test
     */
    protected void killTestNodes() {
        try {
            if (testNode != null) {
                testNode.kill();
            }
        } catch (Exception e) {
        }
        for (TestNode tn : testNodes) {
            try {
                tn.kill();
            } catch (Exception e) {
            }
        }
    }

    @After
    public void cleanForNextTest() throws Exception {
        killTestNodes();

        try {
            cleanState();
        } catch (IllegalArgumentException | NotConnectedException ignored) {
            // ns extra not found
        }
        try {
            rmHelper.disconnect();
        } catch (NotConnectedException alreadyDisconnected) {

        }
    }

    private static void configureLogging() {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            URL defaultLog4jConfig = RMFunctionalTest.class.getResource("/log4j-junit");
            System.setProperty(CentralPAPropertyRepository.LOG4J.getName(), defaultLog4jConfig.toString());
            PropertyConfigurator.configure(defaultLog4jConfig);
        }
    }

    /**
     * Remove all node sources and nodes in the RM
     *
     * @throws Exception
     */
    private void cleanState() throws Exception {
        if (rmHelper.isRMStarted()) {
            rmHelper.disconnect(); // force reconnection
            ResourceManager rm = rmHelper.getResourceManager();
            int nodeNumber = rm.getState().getTotalNodesNumber();

            RMInitialState state = ((RMMonitorEventReceiver) rmHelper.getResourceManager()).getInitialState();
            for (RMNodeSourceEvent sourceEvent : state.getNodeSource()) {
                String nodeSource = sourceEvent.getSourceName();
                rm.removeNodeSource(nodeSource, true);
                rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nodeSource);
            }

            for (int i = 0; i < nodeNumber; i++) {
                rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            }
        }
    }

}
