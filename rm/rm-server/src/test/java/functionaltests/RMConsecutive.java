package functionaltests;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.tests.Consecutive;
import org.junit.After;
import org.junit.Before;


@Consecutive
public class RMConsecutive extends RMFunctionalTest {
    protected final String RM_USER_TEST = "admin";
    protected final String RM_PASS_TEST = "admin";

    @Before
    public void prepareForTest() throws Exception {
        super.prepareForTest();

        if (shouldBeExecutedInConsecutiveMode(this.getClass())) {
            RMTHelper.log("Cleaning the RM before the test execution");
            // clean the state of the RM
            cleanState();
        }

    }

    private void cleanState() throws Exception {
        RMTHelper helper = rmHelper;
        helper.flushEvents();
        ResourceManager rm = helper.getResourceManager(null, RM_USER_TEST, RM_PASS_TEST);
        int nodeNumber = rm.getState().getTotalNodesNumber();

        List<String> sources = new ArrayList<>();
        RMInitialState state = rm.getMonitoring().getState();
        for (RMNodeSourceEvent sourceEvent : state.getNodeSource()) {
            sources.add(sourceEvent.getSourceName());
        }

        for (String source : sources) {
            rm.removeNodeSource(source, true);
            helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, source);
        }

        for (int i = 0; i < nodeNumber; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
        }

        helper.flushEvents();
    }

    @After
    public void killAllProcessesIfNeeded() throws Exception {

        if (shouldBeExecutedInConsecutiveMode(this.getClass())) {
            // show RM state after the test execution (for debugging purposes)

            RMTHelper.log("Events that were not expected by the test");
            rmHelper.getMonitorsHandler().dumpEvents();

            RMInitialState state = rmHelper.getResourceManager().getMonitoring()
                    .getState();
            RMTHelper.log("RMState after the test execution");
            for (RMNodeEvent nodeEvent : state.getNodesEvents()) {
                RMTHelper.log(nodeEvent.toString());
            }

            cleanState();

            rmHelper.getResourceManager().getMonitoring().removeRMEventListener();

        }
        super.killAllProcessesIfNeeded();
    }

}
