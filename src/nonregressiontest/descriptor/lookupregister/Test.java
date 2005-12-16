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
package nonregressiontest.descriptor.lookupregister;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.UrlBuilder;

import testsuite.test.FunctionalTest;

public class Test extends FunctionalTest {
  
   // private static String FS = System.getProperty("file.separator");
    private static String AGENT_XML_LOCATION_UNIX ;
    
  static {
	  if ("ibis".equals(System.getProperty("proactive.communication.protocol"))) {
		  AGENT_XML_LOCATION_UNIX = Test.class.getResource(
	      "/nonregressiontest/descriptor/lookupregister/AgentIbis.xml").getPath();
	  } else {
	  AGENT_XML_LOCATION_UNIX = Test.class.getResource(
      "/nonregressiontest/descriptor/lookupregister/Agent.xml").getPath();
	  }
  }
    
    ProActiveDescriptor proActiveDescriptorAgent;
    A a;

    public Test() {
        super("lookup register in deployment descriptor",
            "Test lookup and register in deployment descriptors");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        proActiveDescriptorAgent = ProActive.getProactiveDescriptor("file:" +
                AGENT_XML_LOCATION_UNIX);
        proActiveDescriptorAgent.activateMappings();
        VirtualNode vnAgent = proActiveDescriptorAgent.getVirtualNode("Agent");
       ProActive.newActive(A.class.getName(),
                new Object[] { "local" }, vnAgent.getNode());
        VirtualNode vnLookup = ProActive.lookupVirtualNode(UrlBuilder.buildUrlFromProperties(
                    "localhost", "Agent"));
        a = (A) vnLookup.getUniqueAO();
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
        proActiveDescriptorAgent.killall(false);
    }

    public boolean postConditions() throws Exception {
        return (a.getName().equals("local"));
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
            test.postConditions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
