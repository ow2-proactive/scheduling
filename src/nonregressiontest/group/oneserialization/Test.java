/*
 * Created on Sep 22, 2003
 */
package nonregressiontest.group.oneserialization;

import java.util.Iterator;

import nonregressiontest.group.A;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.ProActiveFunctionalTest;

/**
 * @author Laurent Baduel
 */
public class Test extends ProActiveFunctionalTest {

	private A typedGroup = null;


	public Test() {
		super("one serialization of the methodcall object in a group communication", "do only serialization of the MethodCall object (in broadcast call only)");
	}

	public void action() throws Exception {
		ProActiveGroup.setUniqueSerialization(this.typedGroup);
		this.typedGroup.onewayCall();
		ProActiveGroup.unsetUniqueSerialization(this.typedGroup);
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
