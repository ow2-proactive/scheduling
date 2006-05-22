/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.examples.mpi;

import org.apache.log4j.Logger;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.mpi.*;


/**
 *  This example uses a simple mpi program (cpi) which calculates
 *  an approximation of PI number on localhost.
 *  It permitts to manage as many MPI program as the user define some virtual nodes.
 *
 */
public class Cpi {
    static public void main(String[] args) {
        Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

        if (args.length != 1) {
            logger.error("Usage: java " + Cpi.class.getName() +
                " <deployment file>");
            System.exit(0);
        }

        ProActiveConfiguration.load();

        VirtualNode vnCpi;
        ProActiveDescriptor pad = null;

        try {
            pad = ProActive.getProactiveDescriptor("file:" + args[0]);

            // gets virtual node 
            vnCpi = pad.getVirtualNode("CPI");
            vnCpi.activate();

            MPISpmd mpiSpmd_01 = MPI.newMPISpmd(vnCpi);
            System.out.println(mpiSpmd_01);
            MPIResult res = mpiSpmd_01.startMPI();
            logger.info("Result value: " + res.getReturnValue());
            vnCpi.killAll(false);
            System.exit(0);
        } catch (ProActiveException e) {
            e.printStackTrace();
            logger.error("Error when reading descriptor");
        }
    }
}
