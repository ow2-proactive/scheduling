/*
 * Created on Sep 10, 2003
 */
package nonregressiontest.group.asynchronouscall;

import nonregressiontest.group.A;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.ProActiveFunctionalTest;

/**
 * @author Laurent Baduel
 *
 */
public class Test extends ProActiveFunctionalTest {

	private A typedGroup = null;
	private A resultTypedGroup = null;
	/**
	 * 
	 */
	public Test() {
		super("asynchronous (and synchronous) call on group", "do an (a)synchronous call on a previously created group");
	}

	public void action() throws Exception {
		this.resultTypedGroup = this.typedGroup.asynchronousCall();
	}

	public void endTest() throws Exception {
		// nothing to do
	}

	public void initTest() throws Exception {
		Object[][] params = {{"Agent0"}, {"Agent1"}, {"Agent2"}};
		Node[] nodes = {this.getSameVMNode(), this.getLocalVMNode(), this.getRemoteVMNode()};
		this.typedGroup = (A) ProActiveGroup.newGroup(A.class.getName(), params, nodes);
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
		for (int i = 0 ; i < group.size() ; i++) {
			rightRankingOfResults &= ((A) groupOfResult.get(i)).getName().equals((((A) group.get(i)).asynchronousCall()).getName());
		}
		// is the result of the n-th group member at the n-th position in the result group ?
		return rightRankingOfResults;
	}

}
