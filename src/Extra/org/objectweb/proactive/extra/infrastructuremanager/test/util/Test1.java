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
package org.objectweb.proactive.extra.infrastructuremanager.test.util;

import java.io.File;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUser;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.NodeSet;
import org.objectweb.proactive.extra.scheduler.common.scripting.VerifyingScript;

import junit.framework.TestCase;


public class Test1 extends TestCase {
    IMUser user;
    IMAdmin admin;
    IMMonitoring monitor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.err.println("Creating resource manager...");
        IMFactory.startLocal();
        user = IMFactory.getUser();
        admin = IMFactory.getAdmin();
        monitor = IMFactory.getMonitoring();
        System.err.println("Deploying nodes...");
        admin.deployAllVirtualNodes(new File(
                "descriptors/scheduler/deployment/Descriptor_SSH_List.xml"),
            null);
        Thread.sleep(10000);
        System.err.println("Starting tests");
    }

    public void testPrincipal() {
        try {
            // Verifying script
            VerifyingScript verif = new VerifyingScript(new File(
                        "/user/jmartin/home/scripts/test.js"), null);

            // Dispay total nodes
            int total = monitor.getNumberOfAllResources().intValue();
            System.err.println("total nodes = " + total);
            assertTrue("At Least one node", total > 0);

            // Get Exactly 2 nodes not on fiacre
            NodeSet nodes = user.getExactlyNodes(new IntWrapper(2), verif);

            //Vector<Node> nodes = (Vector<Node>) ProActive.getFutureValue(rm.getExactlyNodes(2, verif, null));
            if (!nodes.isEmpty()) {
                System.err.println("nodes obtained = " + nodes.size());
                for (Node n : nodes)
                    System.err.println(n.getNodeInformation().getURL());
                assertEquals("getExactlyNodes(2,null,null)", 2, nodes.size());
                assertEquals("freeNodes",
                    monitor.getNumberOfAllResources().intValue() - 2,
                    monitor.getNumberOfFreeResource().intValue());

                // Release thoose nodes
                user.freeNodes(nodes);
                Thread.sleep(5000);
                assertEquals("freeNodes After",
                    monitor.getNumberOfAllResources().intValue(),
                    monitor.getNumberOfFreeResource().intValue());
            }

            //			URL url = new URL("http://localhost:10080/test.js");
            //			verif = new VerifyingScript(url);

            //			Get At Most 3 nodes not on fiacre (but there is only 2 nodes corresponding"
            nodes = user.getAtMostNodes(new IntWrapper(3), verif);
            ProFuture.waitFor(nodes);
            if (!nodes.isEmpty()) {
                System.err.println("nodes obtained = " + nodes.size());
                for (Node n : nodes)
                    System.err.println(n.getNodeInformation().getURL());
                ProFuture.waitFor(nodes);
                assertEquals("getAtMostNodes(3, verif)", nodes.size(),
                    monitor.getNumberOfAllResources().intValue() -
                    monitor.getNumberOfFreeResource().intValue());
                System.err.println(monitor.getNumberOfFreeResource());
                // Release thoose nodes
                user.freeNodes(nodes);
                Thread.sleep(5000);
                assertEquals("freeNodes After",
                    monitor.getNumberOfAllResources().intValue(),
                    monitor.getNumberOfFreeResource().intValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.err.println("Stopping test");
        admin.shutdown();
        ProActive.exitSuccess();
    }

    public static void main(String[] args) throws Exception {
        Test1 test = new Test1();
        test.setUp();
        test.testPrincipal();
        test.tearDown();
    }
}
