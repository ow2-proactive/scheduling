/*
 * Created on Feb 24, 2004
 */
package nonregressiontest.group.dynamicthreadpool;

import nonregressiontest.descriptor.defaultnodes.TestNodes;

import nonregressiontest.group.A;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.FunctionalTest;


/**
 * @author Laurent Baduel
 */
public class Test extends FunctionalTest {
    private A typedGroup = null;

    /**
     *
     */
    public Test() {
        super("dynamic threadpool",
            "add and remove member in a group to see the threadpool vary");
    }

    public void action() throws Exception {
        Group g = ProActiveGroup.getGroup(this.typedGroup);

        this.typedGroup.onewayCall();

        for (int i = 0; i < 100; i++) {
            g.add(g.get(i % 3));
        }

        this.typedGroup.onewayCall();

        int i = 3;
        while (i < g.size()) {
            g.remove(g.size() - 1);
        }
        this.typedGroup.onewayCall();
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

        return (this.typedGroup != null);
    }

    public void endTest() throws Exception {
        // nothing to do
    }

    public void initTest() throws Exception {
        // nothing to do : ProActive methods can not be used here
    }
}
