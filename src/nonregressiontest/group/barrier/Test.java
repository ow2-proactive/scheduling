/*
 * Created on Apr 5, 2004
 */
package nonregressiontest.group.barrier;

import java.util.Iterator;

import nonregressiontest.descriptor.defaultnodes.TestNodes;

import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.FunctionalTest;

/**
 * @author Laurent Baduel
 */
public class Test  extends FunctionalTest {

	private A spmdgroup = null;


	public Test() {
		super("barrier",
			"perform a barrier call on an SPMD group");
	}


	public boolean preConditions() throws Exception {
		 Object[][] params = { { "Agent0" }, { "Agent1" }, { "Agent2" } };
		 Node[] nodes = {
			 TestNodes.getSameVMNode(),
			 TestNodes.getLocalVMNode(),
			 TestNodes.getRemoteVMNode()
		 };
		 this.spmdgroup = (A) ProSPMD.newSPMDGroup(A.class.getName(),
				 params, nodes);

		 return ((this.spmdgroup != null) && (ProActiveGroup.size(this.spmdgroup) == 3));
	 }


	public void action() throws Exception {
		this.spmdgroup.start();
	}

	
	public boolean postConditions() throws Exception {
		String errors ="";
		Iterator it = ProActiveGroup.getGroup(this.spmdgroup).iterator();
		while (it.hasNext()) {
			errors += ((A) it.next()).getErrors();
		}
		System.err.print(errors);
		return "".equals(errors);
	 }

	
	public void endTest() throws Exception {
		// nothing to do
	}


	public void initTest() throws Exception {
		// nothing to do : ProActive methods can not be used here
	}

}
