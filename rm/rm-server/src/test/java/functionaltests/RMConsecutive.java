package functionaltests;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.tests.Consecutive;
import org.ow2.tests.FunctionalTest;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;


@Consecutive
public class RMConsecutive extends FunctionalTest {
    protected final String RM_USER_TEST = "admin";
    protected final String RM_PASS_TEST = "admin";

    @Before
    public void prepareForTest() throws Exception {

        if (!shouldBeExecuted()) {
            Assume.assumeTrue(false);
        }

        if (shouldBeExecutedInConsecutiveMode(this.getClass())) {
            RMTHelper.log("Cleaning the RM before the test execution");
            // clean the state of the RM
            RMTHelper helper = RMTHelper.getDefaultInstance();
            ResourceManager rm = helper.getResourceManager(null, RM_USER_TEST, RM_PASS_TEST);
            int nodeNumber = rm.getState().getTotalNodesNumber();

            List<String> sources = new ArrayList<String>();
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
        }

        super.prepareForTest();
    }

    @After
    public void afterClass() throws Exception {

        if (!shouldBeExecuted()) {
            return;
        }

        if (shouldBeExecutedInConsecutiveMode(this.getClass())) {
            // show RM state after the test execution (for debugging purposes)

            RMTHelper.log("Events that were not expected by the test");
            RMTHelper.getDefaultInstance().getMonitorsHandler().dumpEvents();

            RMInitialState state = RMTHelper.getDefaultInstance().getResourceManager().getMonitoring()
                    .getState();
            RMTHelper.log("RMState after the test execution");
            for (RMNodeEvent nodeEvent : state.getNodesEvents()) {
                RMTHelper.log(nodeEvent.toString());
            }

        }
        super.afterClass();
    }
}
