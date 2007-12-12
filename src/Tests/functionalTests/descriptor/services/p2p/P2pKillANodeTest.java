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

import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.StartP2PService;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;
public class P2pKillANodeTest extends FunctionalTest {
    private static final long serialVersionUID = -3787507831019771599L;
    JVMProcessImpl process1;
    JVMProcessImpl process;

    @Before
    public void initTest() throws Exception {
        this.process1 = new JVMProcessImpl(new StandardOutputMessageLogger());
        this.process1.setJvmOptions(FunctionalTest.JVM_PARAMETERS);
        this.process1.setClassname(
            "org.objectweb.proactive.p2p.service.StartP2PService");
        this.process1.setParameters("-port 2900");

        this.process = new JVMProcessImpl(new StandardOutputMessageLogger());
        this.process.setJvmOptions(FunctionalTest.JVM_PARAMETERS);
        this.process.setClassname(
            "org.objectweb.proactive.p2p.service.StartP2PService");
        this.process.setParameters("-port 3000 -s //localhost:2900/");

        this.process1.startProcess();
        Thread.sleep(7000);
        this.process.startProcess();
        Thread.sleep(7000);

        assertTrue(this.process.isStarted());
        assertTrue(this.process1.isStarted());
    }

    @After
    public void cleanTest() throws Exception {
    }

    @org.junit.Test
    public void actionKillANode() throws Exception {
        Vector<String> peers = new Vector<String>();
        peers.add("//localhost:2900/");
        StartP2PService starter = new StartP2PService(peers);
        starter.start();
        P2PService p2pService = starter.getP2PService();
        Thread.sleep(7000);

        Node node = p2pService.getANode("Alex", "007");

        assertTrue(node != null);

        p2pService.killNode(node.getNodeInformation().getURL());

        Thread.sleep(10000);
        this.process.stopProcess();
        this.process1.stopProcess();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        P2pKillANodeTest test = new P2pKillANodeTest();

        try {
            test.actionKillANode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
