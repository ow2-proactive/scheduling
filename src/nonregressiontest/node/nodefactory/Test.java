package nonregressiontest.node.nodefactory;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

import testsuite.test.FunctionalTest;

/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Test extends FunctionalTest
{
	Node rmiNode;
	Node jiniNode;
	//Node ibisNode;
	/**
	 * Constructor for Test.
	 */
	public Test()
	{
		super("NodeFactory","Test the creation of rmi, jini, ibis node whith the factory");
	}


	/**
	 * @see testsuite.test.FunctionalTest#action()
	 */
	public void action() throws Exception
	{
		NodeFactory.createNode("//localhost/RMINode");
		NodeFactory.createNode("jini://localhost/JININode");
		//NodeFactory.createNode("ibis://localhost/IBISNode");
	}

	/**
	 * @see testsuite.test.AbstractTest#initTest()
	 */
	public void initTest() throws Exception
	{
	}

	/**
	 * @see testsuite.test.AbstractTest#endTest()
	 */
	public void endTest() throws Exception
	{
		
	}
	
	public boolean postConditions() throws Exception{
		rmiNode = NodeFactory.getNode("//localhost/RMINode");
		jiniNode = NodeFactory.getNode("jini://localhost/JININode");
		//ibisNode = NodeFactory.getNode("ibis://localhost/IBISNode");
		return ((rmiNode != null) && (jiniNode != null));
	}

}
