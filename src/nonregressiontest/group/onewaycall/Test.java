/*
 * Created on Sep 10, 2003
 */
package nonregressiontest.group.onewaycall;

import nonregressiontest.group.A;

import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.ProActiveFunctionalTest;

/**
 * @author Laurent Baduel
 *
 */
public class Test  extends ProActiveFunctionalTest {

	/**
	 * 
	 */
	public Test() {
		super("oneway call on group", "do a oneway call on a previously created group");
	}

	public void action() throws Exception {
		Object[][] params = {{"Agent0"}, {"Agent1"}, {"Agent2"}};
		Node[] nodes = {this.getSameVMNode(), this.getLocalVMNode(), this.getRemoteVMNode()};

		A group = (A) ProActiveGroup.newGroup(A.class.getName(), params, nodes);
		group.onewayCall();
	}

	public void endTest() throws Exception {
		// nothing to do
	}

	public void initTest() throws Exception {
		// nothing to do
	}

}
