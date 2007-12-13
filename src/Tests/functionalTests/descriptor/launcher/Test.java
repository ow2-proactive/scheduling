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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.descriptor.launcher;

import org.junit.After;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.Launcher;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


/**
 * Test launching an application via the launcher and deploys it.
 *
 * @author ProActiveTeam
 * @version 1.0 26 aout 2005
 * @since ProActive 2.0.1
 */
public class Test extends FunctionalTest {
    private static String XML_LOCATION;

    static {
        if ("ibis".equals(PAProperties.PA_COMMUNICATION_PROTOCOL.getValue())) {
            XML_LOCATION = Test.class
                    .getResource("/functionalTests/descriptor/launcher/TestLauncherIbis.xml").getPath();
        } else {
            XML_LOCATION = Test.class.getResource("/functionalTests/descriptor/launcher/TestLauncher.xml")
                    .getPath();
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

    @org.junit.Test
    public void action() throws Exception {
        launcher = new Launcher(XML_LOCATION);
        launcher.activate();
        //      Thread.sleep(5000);
        pad = launcher.getProActiveDescriptor();
        vnMain = pad.getVirtualNode("lVNmain");
        mainNode = vnMain.getNode();
        part = mainNode.getProActiveRuntime();
        Thread.sleep(5000);
        nodeTab = part.getVirtualNode("lVN1").getNodes();
        nodeTab2 = part.getVirtualNode("lVN2").getNodes();

        // 1) there must be exactly 2 nodes
        assertTrue(nodeTab.length == 1);
        assertTrue(nodeTab2.length == 1);

        // 2) test equality between job ids
        assertTrue(vnMain.getJobID().equals(nodeTab[0].getNodeInformation().getJobID()));
        assertTrue(vnMain.getJobID().equals(nodeTab2[0].getNodeInformation().getJobID()));

        // 3) all nodes must be in different VM, and mainNode in current VM
        assertFalse((nodeTab[0].getVMInformation().getVMID().equals(nodeTab2[0].getVMInformation().getVMID())));
        assertFalse(nodeTab[0].getVMInformation().getVMID().equals(mainNode.getVMInformation().getVMID()));
        assertFalse(nodeTab2[0].getVMInformation().getVMID().equals(mainNode.getVMInformation().getVMID()));
        assertTrue(part.getVMInformation().getVMID().equals(mainNode.getVMInformation().getVMID()));
    }

    @After
    public void endTest() throws Exception {
        // kill the runtimes where the nodes are deployed.
        part.getVirtualNode("lVN1").killAll(true);
        part.getVirtualNode("lVN2").killAll(true);
        vnMain.killAll(true);
        pad.killall(false);
    }
}
