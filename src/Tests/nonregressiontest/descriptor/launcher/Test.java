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
package nonregressiontest.descriptor.launcher;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.Launcher;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;

import testsuite.test.FunctionalTest;


/**
 * @author ProActiveTeam
 * @version 1.0 26 aout 2005
 * @since ProActive 2.0.1
 */
public class Test extends FunctionalTest {
    /**
	 * 
	 */
	private static final long serialVersionUID = -2153432590156382257L;

	private static String XML_LOCATION;

    static {
    	  if ("ibis".equals(ProActiveConfiguration.getInstance().getProperty(Constants.PROPERTY_PA_COMMUNICATION_PROTOCOL))) {
    		  XML_LOCATION = Test.class.getResource(
              "/nonregressiontest/descriptor/launcher/TestLauncherIbis.xml").getPath();
          } else {
        	  XML_LOCATION = Test.class.getResource(
          "/nonregressiontest/descriptor/launcher/TestLauncher.xml").getPath();
          }
    	
    }
    
    /** node array for VN1 */
    Node[] nodeTab;

    /** node array for VN2 */
    Node[] nodeTab2;
    VirtualNode vnMain;
    ProActiveDescriptor pad;
    Launcher launcher;
    ProActiveRuntime part;
    Node mainNode;

    public Test() {
        super("Application launcher ",
            "Test launching an application via the launcher and deploys it.");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
	public void action() throws Exception {
        launcher = new Launcher(XML_LOCATION);
        launcher.activate();
  //      Thread.sleep(5000);
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
	public void initTest() throws Exception {
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#endTest()
     */
    @Override
	public void endTest() throws Exception {
        // kill the runtimes where the nodes are deployed.
        part.getVirtualNode("lVN1").killAll(true);
        part.getVirtualNode("lVN2").killAll(true);
        vnMain.killAll(true);
    }

    @Override
	public boolean postConditions() throws Exception {
        pad = launcher.getProActiveDescriptor();
        vnMain = pad.getVirtualNode("lVNmain");
        mainNode = vnMain.getNode();
        part = mainNode.getProActiveRuntime();
        Thread.sleep(5000);
        nodeTab = part.getVirtualNode("lVN1").getNodes();
        nodeTab2 = part.getVirtualNode("lVN2").getNodes();

        // 1) there must be exactly 2 nodes
        if ((nodeTab.length != 1) || (nodeTab2.length != 1)) {
            return false;
        }

        // 2) test equality between job ids
        if (!vnMain.getJobID().equals(nodeTab[0].getNodeInformation().getJobID()) ||
                !vnMain.getJobID().equals(nodeTab2[0].getNodeInformation()
                                                         .getJobID())) {
            return false;
        }

        // 3) all nodes must be in differents VM, and mainNode in current VM
        if (nodeTab[0].getNodeInformation().getVMID().equals(nodeTab2[0].getNodeInformation()
                                                                            .getVMID()) ||
                nodeTab[0].getNodeInformation().getVMID().equals(mainNode.getNodeInformation()
                                                                             .getVMID()) ||
                nodeTab2[0].getNodeInformation().getVMID().equals(mainNode.getNodeInformation()
                                                                              .getVMID()) ||
                !part.getVMInformation().getVMID().equals(mainNode.getNodeInformation()
                                                                      .getVMID())) {
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        Test test = new Test();

        try {
            test.action();
            System.out.println(test.postConditions());
            test.endTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
