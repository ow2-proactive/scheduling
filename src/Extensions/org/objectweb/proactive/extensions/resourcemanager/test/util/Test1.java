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
package org.objectweb.proactive.extensions.resourcemanager.test.util;

import java.io.File;
import java.util.ArrayList;

import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extensions.resourcemanager.RMFactory;
import org.objectweb.proactive.extensions.resourcemanager.frontend.NodeSet;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMAdmin;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMMonitoring;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMUser;
import org.objectweb.proactive.extensions.scheduler.common.scripting.SelectionScript;

import junit.framework.TestCase;
import junit.framework.TestCase;


public class Test1 extends TestCase {
    RMUser user;
    RMAdmin admin;
    RMMonitoring monitor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.err.println("Creating resource manager...");
        RMFactory.startLocal();
        user = RMFactory.getUser();
        admin = RMFactory.getAdmin();
        monitor = RMFactory.getMonitoring();
        System.err.println("Deploying nodes...");

        ProActiveDescriptor pad = PADeployment.getProactiveDescriptor(
                "descriptors/scheduler/deployment/Descriptor_SSH_List.xml");
        ArrayList<ProActiveDescriptor> padList = new ArrayList<ProActiveDescriptor>();
        padList.add(pad);
        admin.createStaticNodesource("static source", padList);
        Thread.sleep(10000);
        System.err.println("Starting tests");
    }

    public void testPrincipal() {
        try {
            // selection script
            SelectionScript verif = new SelectionScript(new File(
                        "/user/jmartin/home/scripts/test.js"), null);

            // Displays total nodes
            int total = monitor.getNumberOfAllResources().intValue();
            System.err.println("total nodes = " + total);
            assertTrue("At Least one node", total > 0);

            // Get Exactly 2 nodes not on fiacre for example
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
            //			verif = new SelectionScript(url);

            //			Get At Most 3 nodes not on fiacre (but there is only 2 nodes corresponding"
            nodes = user.getAtMostNodes(new IntWrapper(3), verif);
            PAFuture.waitFor(nodes);
            if (!nodes.isEmpty()) {
                System.err.println("nodes obtained = " + nodes.size());
                for (Node n : nodes)
                    System.err.println(n.getNodeInformation().getURL());
                PAFuture.waitFor(nodes);
                assertEquals("getAtMostNodes(3, verif)", nodes.size(),
                    monitor.getNumberOfAllResources().intValue() -
                    monitor.getNumberOfFreeResource().intValue());
                System.err.println(monitor.getNumberOfFreeResource());
                // Release those nodes
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
        admin.shutdown(false);
        PALifeCycle.exitSuccess();
    }

    public static void main(String[] args) throws Exception {
        Test1 test = new Test1();
        test.setUp();
        test.testPrincipal();
        test.tearDown();
    }
}
