/*
 * Created on Fep 25, 2004
 */
package nonregressiontest.group.javaobject;

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
    private A resultTypedGroup = null;

    /**
     *
     */
    public Test() {
        super("standard Java object in a typed group",
            "create a group with active nd non-ctive object then launch method calls");
    }

    public void action() throws Exception {
        this.resultTypedGroup = this.typedGroup.asynchronousCall();
        this.resultTypedGroup.asynchronousCall();
    }

    public void endTest() throws Exception {
        // nothing to do
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

        Group g = ProActiveGroup.getGroup(this.typedGroup);

        g.add(new A("Agent3"));
        g.add(new A("Agent4"));
        g.add(new A("Agent5"));

        g.setRatioNemberToThread(1);

        return (this.typedGroup != null);
    }

    public boolean postConditions() throws Exception {
        // was the result group created ?
        if (this.resultTypedGroup == null) {
            return false;
        }

        Group group = ProActiveGroup.getGroup(this.typedGroup);
        Group groupOfResult = ProActiveGroup.getGroup(this.resultTypedGroup);

        // has the result group the same size as the caller group ?
        if (groupOfResult.size() != group.size()) {
            return false;
        }

        boolean rightRankingOfResults = true;
        for (int i = 0; i < group.size(); i++) {
            rightRankingOfResults &= ((A) groupOfResult.get(i)).getName()
                                      .equals((((A) group.get(i)).asynchronousCall()).getName());
        }

        // is the result of the n-th group member at the n-th position in the result group ?
        return rightRankingOfResults;
    }

    public void initTest() throws Exception {
        // nothing to do : ProActive methods can not be used here
    }
}
