/*
 * Created on Sep 12, 2003
 */
package nonregressiontest.group.onewaycall;

import nonregressiontest.descriptor.defaultnodes.TestNodes;

import nonregressiontest.group.A;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.FunctionalTest;

import java.util.Iterator;


/**
 * @author Laurent Baduel
 */
public class Test extends FunctionalTest {
    private A typedGroup = null;

    public Test() {
        super("oneway call on group",
            "do a oneway call on a previously created group");
    }

    public void action() throws Exception {
        this.typedGroup.onewayCall();
    }

    public void endTest() throws Exception {
        // nothing to do
    }

    public void initTest() throws Exception {
        // nothing to do : ProActive methods can not be used here
    }

    public boolean postConditions() throws Exception {
        boolean allOnewayCallDone = true;
        Group group = ProActiveGroup.getGroup(this.typedGroup);
        Iterator it = group.iterator();
        while (it.hasNext()) {
            allOnewayCallDone &= ((A) it.next()).isOnewayCallReceived();
        }
        return allOnewayCallDone;
    }

    public boolean preConditions() throws Exception {
        Object[][] params = {
            { "Agent0" },
            { "Agent1" },
            { "Agent2" }
        };
        Node[] nodes = {
            TestNodes.getSameVMNode(), TestNodes.getLocalVMNode(),
            TestNodes.getRemoteVMNode()
        };
        this.typedGroup = (A) ProActiveGroup.newGroup(A.class.getName(),
                params, nodes);
        ProActiveGroup.getGroup(this.typedGroup).setRatioNemberToThread(1);

        boolean NoOnewayCallDone = true;
        Group group = ProActiveGroup.getGroup(this.typedGroup);
        Iterator it = group.iterator();
        while (it.hasNext()) {
            NoOnewayCallDone &= !((A) it.next()).isOnewayCallReceived();
        }
        return NoOnewayCallDone;
    }
}
