package selectionscript;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import nodestate.FunctionalTDefaultRM;
import nodestate.RMEventReceiver;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.scripting.ScriptResult;
import org.ow2.proactive.resourcemanager.common.scripting.SelectionScript;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.NodeSet;
import org.ow2.proactive.resourcemanager.nodesource.gcm.GCMNodeSource;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNodeImpl;


/**
 * Test timeout in SelectionScriptExecution
 * launch a selection script that always says 'selected',
 * but before performs a Thread.sleep() two time longer 
 * than defined selection script max waiting time for selection
 * script execution. So no nodes a got for this selection script.  
 * 
 * @author ProActice team
 *
 */
public class SelectionScriptTimeOutTest extends FunctionalTDefaultRM {

    private String selectionScriptWithtimeOutPath = this.getClass().getResource(
            "selectionScriptWithtimeOut.js").getPath();

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework. 
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        log("Deployment");

        System.out.println(monitor.echo());
        System.out.println(admin.echo());

        RMEventType[] eventsList = { RMEventType.NODE_ADDED, RMEventType.NODE_REMOVED,
                RMEventType.NODESOURCE_CREATED, RMEventType.NODE_BUSY, RMEventType.NODE_FREE, };

        RMEventReceiver receiver = (RMEventReceiver) PAActiveObject.newActive(
                RMEventReceiver.class.getName(), new Object[] { monitor, eventsList });

        receiver.cleanEventLists();
        super.deployDefault();

        //wait for creation of GCM Node Source event, and deployment of its nodes
        receiver.waitForNEvent(defaultDescriptorNodesNb + 1);
        receiver.cleanEventLists();

        int scriptSleepingTime = PAResourceManagerProperties.RM_SELECT_SCRIPT_TIMEOUT.getValueAsInt() * 2;

        log("Test 1");

        //create the static selection script object
        SelectionScript sScript = new SelectionScript(new File(selectionScriptWithtimeOutPath),
            new String[] { Integer.toString(scriptSleepingTime) }, false);

        NodeSet nodes = admin.getAtMostNodes(new IntWrapper(2), sScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 0);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);
    }
}
