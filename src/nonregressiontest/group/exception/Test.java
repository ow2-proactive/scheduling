/*
 * Created on Sep 19, 2003
 */
package nonregressiontest.group.exception;

import nonregressiontest.group.A;

import org.objectweb.proactive.core.group.ExceptionList;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.ProActiveFunctionalTest;

/**
 * @author Laurent Baduel
 */
public class Test extends ProActiveFunctionalTest {

	private A typedGroup = null;
	private A resultTypedGroup = null;


	public Test() {
		super("Exception returned in a method call on group", "do an (a)synchronous call that rise exception");
	}

	public void action() throws Exception {
		this.resultTypedGroup = this.typedGroup.asynchronousCallException();
				
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
			System.err.println("the result group containing exception is not build");
			return false;
		}

		Group group = ProActiveGroup.getGroup(this.typedGroup);
		Group groupOfResult = ProActiveGroup.getGroup(this.resultTypedGroup);
		// has the result group the same size as the caller group ?
		if (groupOfResult.size() != group.size()) {
			System.err.println("the result group containing exception has the correct size");
			return false;
		}
		
		boolean exceptionInResultGroup = true;
		for (int i = 0 ; i < groupOfResult.size() ; i++) {
			exceptionInResultGroup &= (groupOfResult.get(i) instanceof Throwable);
		}
		// is the result group containing exceptions ?
		if (!exceptionInResultGroup) {
			System.err.println("the result group doesn't contain (exclusively) exception");
			return false;
		}
		
		// has the ExceptionList the correct size ?
		ExceptionList el = groupOfResult.getExceptionList();
		if (el.size() != groupOfResult.size()) {
			System.err.println("the ExceptionList hasn't the right size");
			return false;
		}
		
		A resultOfResultGroup = (A) this.resultTypedGroup.asynchronousCall();
		Group groupOfResultResult = ProActiveGroup.getGroup(resultOfResultGroup);
		// has the result-result group the correct size ?
		if (groupOfResultResult.size() != groupOfResult.size()) {
			System.err.println("the result of a call on a group containing exception hasn't the correct size");
			return false;
		}

		boolean nullInResultResultGroup = true;
		for (int i = 0 ; i < groupOfResultResult.size() ; i++) {
			nullInResultResultGroup &= (groupOfResultResult.get(i) == null);
		}
		// is the result group containing null ?
		if (!nullInResultResultGroup) {
			System.err.println("the result group of a group containing exception doesn't contain null");
			return false;
		}
		
		// are the exceptions deleted ?
		groupOfResult.purgeExceptionAndNull();
		if (groupOfResult.size() != 0) {
			System.err.println("the exceptions in a group are not correctly (totaly) purged");
			return false;
		}

		// are the null deleted ?
		groupOfResultResult.purgeExceptionAndNull();
		if (groupOfResultResult.size() != 0) {
			System.err.println("the null in a group are not correctly (totaly) purged");
			return false;
		}
		
		return true;
	}

}
