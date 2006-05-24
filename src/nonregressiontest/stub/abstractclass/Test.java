/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package nonregressiontest.stub.abstractclass;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;

import testsuite.test.FunctionalTest;



public class Test extends FunctionalTest {
    String stubClassName;
    byte[] data;

    public Test() {
        super("Stub generation on abstract classes",
            "Stub generation on abstract classes");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
    	
    	Factory f = (Factory) ProActive.newActive(Factory.class.getName(), new Object[]{});
		ProActive.register(f, UrlBuilder.buildUrlFromProperties("localhost", "myFactory"));
		
	     Factory factory = (Factory) ProActive.lookupActive(Factory.class.getName(), UrlBuilder.buildUrlFromProperties("localhost", "myFactory"));
	     AbstractClass abstractClass = factory.getWidget(NodeFactory.getDefaultNode());
	     abstractClass.foo();
	     abstractClass.bar();
	     abstractClass.gee();
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
    }

    public boolean postConditions() throws Exception {
    	// we just check that methods can be called in action()
        return true;
    }
    
    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
            if (test.postConditions()) {
                System.out.println("TEST SUCCEEDED");
            } else {
                System.out.println("TEST FAILED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
