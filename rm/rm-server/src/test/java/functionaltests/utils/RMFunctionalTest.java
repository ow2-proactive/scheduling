package functionaltests.utils;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.NotConnectedException;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.tests.ProActiveTest;
import org.ow2.tests.ProcessCleaner;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;


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

    static {
        //        try {
        //            new ProcessCleaner(".*proactive.test=true.*").killAliveProcesses();
        //            new ProcessCleaner(".*RMNodeStarter.*").killAliveProcesses();
        //            new ProcessCleaner(".*RMStarterForFunctionalTest.*").killAliveProcesses();
        //        } catch (IOException e) {
        //            throw new RuntimeException(e);
        //        }
    }

    @Before
    public void prepareForTest() throws Exception {
        CentralPAPropertyRepository.PA_TEST.setValue(true);
        CentralPAPropertyRepository.PA_RUNTIME_PING.setValue(false);

        rmHelper = new RMTHelper();
    }

    @After
    public void cleanForNextTest() throws Exception {
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

    private void cleanState() throws Exception {
        rmHelper.disconnect();
        ResourceManager rm = rmHelper.getResourceManager();
        int nodeNumber = rm.getState().getTotalNodesNumber();

        RMInitialState state = rm.getMonitoring().getState();
        for (RMNodeSourceEvent sourceEvent : state.getNodeSource()) {
            String nodeSource = sourceEvent.getSourceName();
            rm.removeNodeSource(nodeSource, true);
            rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nodeSource);
        }

        for (int i = 0; i < nodeNumber; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }
    }

    private static void killAliveProcesses() throws IOException {
        new ProcessCleaner(".*proactive.test=true.*").killAliveProcesses();
        new ProcessCleaner(".*RMNodeStarter.*").killAliveProcesses();
        new ProcessCleaner(".*SchedulerTStarter.*").killAliveProcesses();
        new ProcessCleaner(".*RMTStarter.*").killAliveProcesses();
    }

}
