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
package org.objectweb.proactive.examples.mpi;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.mpi.MPI;
import org.objectweb.proactive.mpi.MPIResult;
import org.objectweb.proactive.mpi.MPISpmd;


/**
 *  This example uses a simple mpi program which implements a simple Jacobi iteration for approximating
 *  the solution to a linear system of equations.
 */
public class Jacobi {
    static public void main(String[] args) {
        Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

        if (args.length != 1) {
            logger.error("Usage: java " + Jacobi.class.getName() +
                " <deployment file>");
            System.exit(0);
        }

        ProActiveConfiguration.load();

        VirtualNode vnJacobi;
        ProActiveDescriptor pad = null;

        try {
            pad = PADeployment.getProactiveDescriptor("file:" + args[0]);

            // gets virtual node 
            vnJacobi = pad.getVirtualNode("JacobiMPI");
            vnJacobi.activate();

            MPISpmd mpiSpmd_01 = MPI.newMPISpmd(vnJacobi);
            System.out.println(mpiSpmd_01);
            MPIResult res = mpiSpmd_01.startMPI();
            logger.info("[JACOBI] Result value: " + res.getReturnValue());

            vnJacobi.killAll(false);
            System.exit(0);
        } catch (ProActiveException e) {
            e.printStackTrace();
            logger.error("!!! Error when reading descriptor");
        }
    }
}
