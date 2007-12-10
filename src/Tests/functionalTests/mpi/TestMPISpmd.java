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
package functionalTests.mpi;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.mpi.MPI;
import org.objectweb.proactive.mpi.MPIConstants;
import org.objectweb.proactive.mpi.MPISpmd;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Tests if dependency is well ensured between processes. That is MPISpmd object is created from a Virtual Node.
 */
public class TestMPISpmd extends FunctionalTest {
    static final long serialVersionUID = 1;
    private static Logger logger = ProActiveLogger.getLogger("functionalTests");
    private static String XML_FILE = TestMPISpmd.class.getResource(
            "/functionalTests/mpi/MPILocal-descriptor.xml").getPath();
    ProActiveDescriptor pad;
    MPISpmd mpi_spmd;

    @After
    public void endTest() throws Exception {
        if (pad != null) {
            pad.killall(false);
        }
    }

    @Test
    public void action() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading descriptor from: " + XML_FILE);
        }

        pad = PADeployment.getProactiveDescriptor(XML_FILE);

        VirtualNode testNode = pad.getVirtualNode("CPI");
        this.mpi_spmd = MPI.newMPISpmd(testNode);

        String status = mpi_spmd.getStatus();
        assertTrue(status.equals(MPIConstants.MPI_DEFAULT_STATUS));
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
            System.out.println("endTest");
            test.endTest();
            System.out.println("The end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
