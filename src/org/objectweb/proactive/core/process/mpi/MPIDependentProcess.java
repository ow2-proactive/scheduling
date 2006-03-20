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
package org.objectweb.proactive.core.process.mpi;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.DependentProcess;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.JVMNodeProcess;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * MPI Dependent Process implementation.
 * This implementation works only for ProActive deployment, and not to submit single commands
 * @author  ProActive Team
 * @version 1.0,  2005/11/10
 * @since   ProActive 3.0
 */
public class MPIDependentProcess extends MPIProcess implements DependentProcess {

    /**
     * Create a new MPIProcess
     * Used with XML Descriptors
     */
    public MPIDependentProcess() {
        super();
        this.setTargetProcess(new JVMNodeProcess());
    }

    public MPIDependentProcess(ExternalProcess targetProcess) {
        super(targetProcess);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "mpidp";
    }

    //
    //--------------------------Implements DependentProcess---------------------
    public void setDependencyParameters(Object[] dependencyParameters) {
        buildHostsFile((Node[]) dependencyParameters);
    }

    protected void buildHostsFile(Node[] nodes) {
        logger.info("Generating machinefile...");
        try {
            PrintWriter mf_writer;
            mf_writer = new PrintWriter(new BufferedWriter(
                        new FileWriter(hostsFileName)));
            for (int i = 0; i < nodes.length; i++) {
                mf_writer.println(nodes[i].getNodeInformation().getHostName());
            }
            mf_writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("machinefile -> ok");
    }
}
