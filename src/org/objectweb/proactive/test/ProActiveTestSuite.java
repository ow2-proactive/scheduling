package org.objectweb.proactive.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ProActiveTestSuite extends junit.framework.TestSuite {
	
	public ProActiveTestSuite(String s) {
		super(s);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(org.objectweb.proactive.core.util.test.CircularArrayListTest.class);
		suite.addTestSuite(org.objectweb.proactive.core.node.test.NodeFactoryTest.class);
		return suite;	
	}
}
