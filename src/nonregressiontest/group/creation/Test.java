/*
 * Created on Sep 10, 2003
 */
package nonregressiontest.group.creation;

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
		super("group creation", "create a group with 3 active objects");
	}

	private A createGroup () throws Exception {
		Object[][] params = {{"Agent0"}, {"Agent1"}, {"Agent2"}};
		Node[] nodes = {this.getSameVMNode(), this.getLocalVMNode(), this.getRemoteVMNode()};

		this.typedGroup = (A) ProActiveGroup.newGroup(A.class.getName(), params, hosts);
		return this.typedGroup;
	}

	public void action() throws Exception {
		this.createGroup();
	}

	public A action(Object o) throws Exception {
		return this.createGroup();
	}

	public void initTest() throws Exception {
		// nothing to do
	}

	public void endTest() throws Exception {
		// nothing to do
	}

	public boolean postConditions() throws Exception {
		// was the group created ?
		if (this.typedGroup == null) {
			return false;
		}
		Group agentGroup = ProActiveGroup.getGroup(this.typedGroup);
		// has the group the right size ?
		if (agentGroup.size() != 3) {
			return false;
		}
		
		A agent0 = (A) agentGroup.get(0);
		A agent1 = (A) agentGroup.get(1);
		A agent2 = (A) agentGroup.get(2);

		boolean rightLocations = 
			(agent0.getNodeName().compareTo(this.getSameVMNode().getNodeInformation().getURL().toUpperCase()) == 0)
			&& (agent1.getNodeName().compareTo(this.getLocalVMNode().getNodeInformation().getURL().toUpperCase()) == 0)
			&& (agent2.getNodeName().compareTo(this.getRemoteVMNode().getNodeInformation().getURL().toUpperCase()) == 0);
			
		boolean rightNames =
			(agent0.getName().equals("Agent0"))
			&& (agent1.getName().equals("Agent1"))
			&& (agent2.getName().equals("Agent2"));

		// are the agents at the correct location with the correct names ?
		return (rightLocations && rightNames);
	}

}
