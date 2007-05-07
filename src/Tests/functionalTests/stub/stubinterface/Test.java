/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package functionalTests.stub.stubinterface;

import org.objectweb.proactive.core.mop.MOP;

import testsuite.test.FunctionalTest;
import static junit.framework.Assert.assertTrue;
/**
 * Test stub generation for interface
 */
public class Test  {
 	private static final long serialVersionUID = 7137686002811784615L;
	String result1;
    String result2;

 
    
    @org.junit.Test
	public void action() throws Exception {
        StringInterface i1 = (StringInterface) MOP.newInstance("nonregressiontest.stub.stubinterface.StringInterface",
                "nonregressiontest.stub.stubinterface.StringInterfaceImpl",
                null,
                new Object[] { "toto" }, "nonregressiontest.stub.stubinterface.ProxyOne", new Object[0]);
        result1 = i1.getMyString();

        StringInterfaceImpl i2 = (StringInterfaceImpl) MOP.newInstance("nonregressiontest.stub.stubinterface.StringInterfaceImpl",
                null, new Object[] { "titi" }, "nonregressiontest.stub.stubinterface.ProxyOne", new Object[0]);
        result2 = i2.getMyString();
     
        assertTrue(result1.equals("toto"));
        assertTrue(result2.equals("titi"));
    }
    
    
public static void main(String[] args) {
        
        Test test = new Test();
        try {
            test.action();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
