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
package org.objectweb.proactive.core.process.mpi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.DependentProcess;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.JVMNodeProcess;


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
    @Override
    public String getProcessId() {
        return "mpidp";
    }

    //
    //--------------------------Implements DependentProcess---------------------
    public void setDependencyParameters(Object[] dependencyParameters) {
        if (this.hostsFileName.compareTo(DEFAULT_HOSTSFILENAME_PATH) == 0) {
            // change hostsfile name to avoid doubloon
            String firstHostName = ((Node[]) dependencyParameters)[0].getNodeInformation().getName();

            StringBuilder sb = new StringBuilder(this.hostsFileName);
            sb.append("_").append(firstHostName);
            this.hostsFileName = sb.toString();
        }

        buildHostsFile((Node[]) dependencyParameters);
    }

    protected void buildHostsFile(Node[] nodes) {
        logger.info("Generating machinefile...");
        try {
            PrintWriter mf_writer;
            mf_writer = new PrintWriter(new BufferedWriter(new FileWriter(hostsFileName)));
            for (int i = 0; i < nodes.length; i++) {
                mf_writer.println(nodes[i].getVMInformation().getHostName());
            }
            mf_writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Machinefile generated");
    }
}
