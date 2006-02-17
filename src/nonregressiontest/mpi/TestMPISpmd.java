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
package nonregressiontest.mpi;

import org.apache.log4j.Logger;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.mpi.*;

import testsuite.test.FunctionalTest;


public class TestMPISpmd extends FunctionalTest {
    static final long serialVersionUID = 1;
    private static Logger logger = ProActiveLogger.getLogger(
            "nonregressiontest");
    private static String XML_FILE = TestMPISpmd.class.getResource(
            "/nonregressiontest/mpi/MPILocal-descriptor.xml").getPath();
    ProActiveDescriptor pad;
    MPISpmd mpi_spmd;

    public TestMPISpmd() {
        super("MPI: Resources booking - MPISpmd object creation",
            "Tests if dependency is well ensured between processes. That is MPISpmd object is created from a Virtual Node.");
    }

    public boolean postConditions() throws Exception {
        String status = mpi_spmd.getStatus();
        return (status.equals(MPIConstants.MPI_DEFAULT_STATUS));
    }

    public void initTest() throws Exception {
    }

    public void endTest() throws Exception {
        if (pad != null) {
            pad.killall(false);
        }
    }

    public void action() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading descriptor from: " + XML_FILE);
        }

        pad = ProActive.getProactiveDescriptor(XML_FILE);

        VirtualNode testNode = pad.getVirtualNode("CPI");
        this.mpi_spmd = MPI.newMPISpmd(testNode);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        TestMPISpmd test = new TestMPISpmd();
        try {
            System.out.println("Action");
            test.action();
            System.out.println("postConditions");
            System.out.println("Result=" + test.postConditions());
            System.out.println("endTest");
            test.endTest();
            System.out.println("The end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
