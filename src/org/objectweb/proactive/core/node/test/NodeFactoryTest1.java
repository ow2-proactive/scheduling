package org.objectweb.proactive.core.node.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

public class NodeFactoryTest1 extends TestCase {    

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new NodeFactoryTest1("testCreateNode"));
		return suite;
	}

	public NodeFactoryTest1(String str) {
		super(str);
	}

	public void testCreateNode() {
		Node node = null;
		Node node2 = null;
		try {
			node = NodeFactory.createNode("//tata/Node", true);
		} catch (NodeException e) {
		}
		try {
			node2 = NodeFactory.createNode("//localhost/Node", true);
		} catch (NodeException e) {
		}
		assertNull(" Node should not have been created", node);
		assertNotNull(" Node should have been created", node2);
	}

	public static void main(String[] args) {
		NodeFactoryTest1 test = new NodeFactoryTest1("testCreateNode");
		TestResult result = test.run();
		System.out.println("Errors " + result.errorCount());
		System.out.println("Failures " + result.failureCount());
	}
}