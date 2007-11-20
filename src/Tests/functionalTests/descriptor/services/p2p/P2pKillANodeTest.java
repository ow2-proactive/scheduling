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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.StartP2PService;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;
public class P2pKillANodeTest extends FunctionalTest {
    private static final long serialVersionUID = -3787507831019771599L;
    JVMProcessImpl process1;
    JVMProcessImpl process;

    @BeforeClass
    public static void globalInit() {
        /**
             * StartP2PService is started without the FunctionalTest.JVM_PARAMETERS
             * parameters. This shutdownhook is added to kill every StartP2PService.
             */
        Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    HashMap<String, String> pids = new HashMap<String, String>();

                    try {
                        // Run JPS to list all JVMs on this machine
                        Process p = Runtime.getRuntime()
                                           .exec(getJPSCommand() + " -ml");
                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                    p.getInputStream()));

                        for (String line = br.readLine(); line != null;
                                line = br.readLine()) {
                            if (line.contains("StartP2PService")) {
                                String[] fields = line.split(" ", 2);

                                switch (OperatingSystem.getOperatingSystem()) {
                                case unix:
                                    p = Runtime.getRuntime()
                                               .exec(new String[] {
                                                "kill", fields[0]
                                            }, null, null);
                                    try {
                                        p.waitFor();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                default:
                                    System.err.println(
                                        "TODO: Kill P2PService on Windows also !");
                                    break;
                                }

                                pids.put(fields[0], fields[1]);
                            }
                        }
                    } catch (IOException e) {
                        // Should not happen
                        e.printStackTrace();
                    }
                }
            });
    }

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
