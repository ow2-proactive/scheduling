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
package functionalTests.descriptor.services.p2p;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger;
import org.objectweb.proactive.core.process.JVMProcessImpl;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;


/**
 * Test service: P2P JVM acquisition in deployment descriptor
 *
 * @author The ProActive Team
 * @version 1.0 6 ao?t 2004
 * @since ProActive 2.0.1
 */
public class P2pXmlTest extends FunctionalTest {
    private static String P2P_XML_LOCATION_UNIX = P2pXmlTest.class.getResource(
            "/functionalTests/descriptor/services/p2p/TestP2P.xml").getPath();

    static {
        if ("ibis".equals(PAProperties.PA_COMMUNICATION_PROTOCOL.getValue())) {
            P2P_XML_LOCATION_UNIX = P2pXmlTest.class.getResource(
                    "/functionalTests/descriptor/services/p2p/TestP2PIbis.xml").getPath();
        } else {
            P2P_XML_LOCATION_UNIX = P2pXmlTest.class.getResource(
                    "/functionalTests/descriptor/services/p2p/TestP2P.xml").getPath();
        }
    }

    JVMProcessImpl process1;
    JVMProcessImpl process;
    Node[] nodeTab;
    ProActiveDescriptor pad;

    @Before
    public void initTest() throws Exception {
        this.process1 = new JVMProcessImpl(new StandardOutputMessageLogger());
        this.process1.setJvmOptions(FunctionalTest.JVM_PARAMETERS);
        this.process1.setClassname("org.objectweb.proactive.p2p.service.StartP2PService");
        this.process1.setParameters("-port 2900");

        this.process = new JVMProcessImpl(new StandardOutputMessageLogger());
        this.process.setJvmOptions(FunctionalTest.JVM_PARAMETERS);
        this.process.setClassname("org.objectweb.proactive.p2p.service.StartP2PService");
        this.process.setParameters("-port 3000 -s //localhost:2900");

        this.process1.startProcess();
        Thread.sleep(7000);
        this.process.startProcess();
        Thread.sleep(7000);

        assertTrue(this.process.isStarted());
        assertTrue(this.process1.isStarted());
    }

    @After
    public void cleanTest() throws Exception {
        this.process.stopProcess();
        this.process1.stopProcess();
    }

    @org.junit.Test
    public void actionAcquisition() throws Exception {
        this.pad = PADeployment.getProactiveDescriptor(P2P_XML_LOCATION_UNIX);
        this.pad.activateMappings();
        VirtualNode vn = this.pad.getVirtualNode("p2pvn");
        this.nodeTab = vn.getNodes();

        boolean resultTest = (this.nodeTab.length == 2);

        assertTrue(resultTest);
    }

    public static void main(String[] args) {
        P2pXmlTest test = new P2pXmlTest();

        try {
            test.actionAcquisition();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
