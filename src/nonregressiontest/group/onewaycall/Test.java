/*
 * Created on Sep 12, 2003
 */
package nonregressiontest.group.onewaycall;

import java.util.Iterator;

import nonregressiontest.group.A;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.ProActiveFunctionalTest;

/**
 * @author Laurent Baduel
 */
public class Test  extends ProActiveFunctionalTest {

	private A typedGroup = null;

	public Test() {
		super("oneway call on group", "do a oneway call on a previously created group");
	}

	public void action() throws Exception {
		this.typedGroup.onewayCall();
	}

	public void endTest() throws Exception {
		// nothing to do
	}

	public void initTest() throws Exception {
		Object[][] params = {{"Agent0"}, {"Agent1"}, {"Agent2"}};
		Node[] nodes = {this.getSameVMNode(), this.getLocalVMNode(), this.getRemoteVMNode()};
		this.typedGroup = (A) ProActiveGroup.newGroup(A.class.getName(), params, hosts);
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
		boolean NoOnewayCallDone = true;
		Group group = ProActiveGroup.getGroup(this.typedGroup);
		Iterator it = group.iterator();
		while (it.hasNext()) {
			NoOnewayCallDone &= !((A) it.next()).isOnewayCallReceived();
		}
		return NoOnewayCallDone;
	}

}
