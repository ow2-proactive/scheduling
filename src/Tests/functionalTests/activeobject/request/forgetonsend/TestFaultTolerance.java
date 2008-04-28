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
package functionalTests.activeobject.request.forgetonsend;

import static junit.framework.Assert.assertTrue;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.JVMProcessImpl;

import functionalTests.FunctionalTest;
import functionalTests.ft.cic.Test;


public class TestFaultTolerance extends FunctionalTest {

    private JVMProcessImpl server;
    private static String FT_XML_LOCATION_UNIX = Test.class.getResource("/functionalTests/ft/testFT_CIC.xml")
            .getPath();

    /**
     * We will try to perform a failure during a sending, and then verify that the sending restart
     * from the new location
     * 
     * @throws Exception
     */
    @org.junit.Test
    public void action() throws Exception {
        // deployer le FTServer !
        this.server = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());

        //        this.server.setJvmOptions(FunctionalTest.JVM_PARAMETERS +
        //  " -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8005 ");

        this.server.setClassname("org.objectweb.proactive.core.body.ft.servers.StartFTServer");
        this.server.startProcess();
        Thread.sleep(3000);

        // ProActive descriptor
        ProActiveDescriptor pad;
        VirtualNode vnode;

        // create nodes
        pad = PADeployment.getProactiveDescriptor(TestFaultTolerance.FT_XML_LOCATION_UNIX);
        pad.activateMappings();
        vnode = pad.getVirtualNode("Workers");
        Node[] nodes = vnode.getNodes();

        FTObject a = (FTObject) PAActiveObject.newActive(FTObject.class.getName(), new Object[] { "a" },
                nodes[0]);
        FTObject b = (FTObject) PAActiveObject.newActive(FTObject.class.getName(), new Object[] { "b" },
                nodes[1]);

        a.init(b); // Will produce b.a(), b.b() and b.c()

        // failure in 11 sec...
        Thread.sleep(7000);
        try {
            nodes[0].getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            // e.printStackTrace();
        }

        Thread.sleep(20000);

        boolean result = b.getServices().equals("abc");

        // cleaning
        this.server.stopProcess();
        pad.killall(false);

        assertTrue(result);
    }
}
