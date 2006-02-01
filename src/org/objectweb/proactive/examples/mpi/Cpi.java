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
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.AbstractExternalProcess;
import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.mpi.MPIProcess;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.mpi.*;
import org.objectweb.proactive.tools.FileTransfer;

import java.io.File;
import java.io.IOException;


/**
 *  This example uses a simple mpi program (cpi) which calculates
 *  an approximation of PI number on localhost.
 *  One purpose is the possibility to launch several times consecutively
 *  this program just by calling the startMPI() method on the virtualnode
 *  which with the MPI program is associated.
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
        int exitValue = 0;

        try {
            pad = ProActive.getProactiveDescriptor("file:" + args[0]);

            // gets virtual node 
            vnCpi = pad.getVirtualNode("CPI");
            vnCpi.activate();

            /*
            
                           String filenameSrcA = "/home/smariani/test.dat";
                           //String filenamePushed = "/home/smariani/test.dat";
                           String filenamePushed = "/user/smariani/home/test.dat";
                           File fileSrcA = new File(filenameSrcA);
                           File filePushed = new File(filenamePushed);
                           Node[] testnode = vnCpi.getNodes();
                           System.out.println(testnode[0].getNodeInformation().getURL());
                           try {
                                               FileTransfer.pushFile(testnode[0], filePushed ,fileSrcA);
                                       } catch (IOException e) {
                                               // TODO Auto-generated catch block
                                               e.printStackTrace();
                                       }
             */
            MPISpmd mpiSpmd = MPI.createMPISPMDObject(vnCpi);
            System.out.println(mpiSpmd);

            mpiSpmd.startMPI();

            mpiSpmd.waitFor();

            System.out.println(" Return Value : " + mpiSpmd.getReturnValue());

            System.out.println(mpiSpmd);

            vnCpi.killAll(false);
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("Pb when reading descriptor");
        }
    }
}
