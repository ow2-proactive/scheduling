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
package org.objectweb.proactive.extra.scheduler.examples;

import java.io.File;
import java.net.URI;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.NodeSet;
import org.objectweb.proactive.extra.scheduler.common.scripting.VerifyingScript;
import org.objectweb.proactive.extra.scheduler.resourcemanager.InfrastructureManagerProxy;


public class TestIMProxy {
    private IMAdmin admin = null;
    private InfrastructureManagerProxy proxy = null;

    protected void setUp() throws Exception {
        // Creating Infrastructure Manager
        //		System.err.println("Creating resource manager...");
        //		IMFactory.startLocal();
        //		admin  = IMFactory.getAdmin();
        //		
        //		// Deploying nodes
        //		System.err.println("Deploying nodes...");
        //		admin.deployAllVirtualNodes(new File("descriptors/scheduler/deployment/Descriptor_SSH_List.xml"), null);
        //		Thread.sleep(10000);

        // Launching IMProxy
        System.err.println("Launching Infrastructure Manager Proxy");

        proxy = InfrastructureManagerProxy.getProxy(new URI(
                    "rmi://localhost:1099/"));

        System.err.println("Starting tests");
    }

    public void testPrincipal() {
        try {
            // Verifying script
            VerifyingScript verif = new VerifyingScript(new File(
                        "/user/jmartin/home/scripts/test.js"), null);
            VerifyingScript post1 = new VerifyingScript(new File(
                        "/user/jmartin/home/scripts/clean2.js"), null);
            VerifyingScript post2 = new VerifyingScript(new File(
                        "/user/jmartin/home/scripts/clean5.js"), null);

            // Dispay total nodes
            int total = proxy.getNumberOfAllResources().intValue();
            System.err.println("total nodes = " + total);

            // Get Exactly 2 nodes not on fiacre
            NodeSet nodes = proxy.getExactlyNodes(2, verif);

            //Vector<Node> nodes = (Vector<Node>) ProActive.getFutureValue(rm.getExactlyNodes(2, verif, null));
            if (!nodes.isEmpty()) {
                System.err.println("nodes obtained = " + nodes.size());
                for (Node n : nodes)
                    System.err.println(n.getNodeInformation().getURL());

                // Release thoose nodes
                proxy.freeNodes(nodes);
                Thread.sleep(5000);
            }

            //			URL url = new URL("http://localhost:10080/test.js");
            //			verif = new VerifyingScript(url);
            //			Get At Most 3 nodes not on fiacre (but there is only 2 nodes corresponding"
            nodes = proxy.getAtMostNodes(2, verif);
            ProFuture.waitFor(nodes);
            if (!nodes.isEmpty()) {
                System.err.println("nodes obtained = " + nodes.size());
                for (Node n : nodes)
                    System.err.println(n.getNodeInformation().getURL());
                ProFuture.waitFor(nodes);
                System.err.println("Free nodes = " +
                    proxy.getNumberOfFreeResource());
                // Release thoose nodes
                if (nodes.size() > 1) {
                    proxy.freeNode(nodes.remove(0), post1);
                    proxy.freeNodes(nodes, post2);
                } else {
                    proxy.freeNodes(nodes, post2);
                }
                int tot;
                int free;
                int max = 5;
                while ((max-- > 0) &&
                        ((tot = proxy.getNumberOfAllResources().intValue()) != (free = proxy.getNumberOfFreeResource()
                                                                                                .intValue()))) {
                    System.out.println("Waiting for freeing resources (" +
                        free + "/" + tot + "available)");
                    Thread.sleep(2000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void tearDown() throws Exception {
        System.err.println("Stopping test :");
        System.err.println("-> Stopping Proxy:");
        proxy.shutdownProxy();
        //		System.err.println("-> Stopping IM :");
        //		admin.shutdown();
        ProActive.exitSuccess();
    }

    public static void main(String[] args) throws Exception {
        TestIMProxy test = new TestIMProxy();
        test.setUp();
        test.testPrincipal();
        test.tearDown();
    }
}
