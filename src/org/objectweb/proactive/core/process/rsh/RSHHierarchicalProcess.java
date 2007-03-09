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
package org.objectweb.proactive.core.process.rsh;

import java.io.IOException;

import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.HierarchicalProcess;
import org.objectweb.proactive.core.process.JVMProcess;


/**
 * This process starts a forwarder defined by the target process using RSH.
 * The hierarchicalProcess will then be deployed for the forwarder.
 *
 * The hierarchical process must be set before starting the process.
 *
 * @author ProActive Team
 *
 */
public class RSHHierarchicalProcess extends RSHProcess
    implements HierarchicalProcess {

    /** Embded process to be deployed from the forwarder */
    private ExternalProcess hierarchicalProcess;

    /** @see org.objectweb.proactive.core.process.HierarchicalProcess#setHierarchicalProcess(ExternalProcess) */
    public void setHierarchicalProcess(ExternalProcess process) {
        hierarchicalProcess = process;
    }

    /** @see org.objectweb.proactive.core.process.HierarchicalProcess#getHierarchicalProcess() */
    public ExternalProcess getHierarchicalProcess() {
        return hierarchicalProcess;
    }

    /** @see org.objectweb.proactive.core.process.UniversalProcess#isHierarchical() */
    @Override
    public boolean isHierarchical() {
        return true;
    }

    /** @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber() */
    @Override
    public int getNodeNumber() {
        return hierarchicalProcess.getNodeNumber();
    }

    /** @see org.objectweb.proactive.core.process.UniversalProcess#startProcess() */
    @Override
    public void startProcess() throws IOException {
        // Be sure that a forwarder runtime will be launched on the forwarder host
        JVMProcess finalProcess = (JVMProcess) getFinalProcess();
        String bClass = finalProcess.getClassname();
        finalProcess.setClassname(
            "org.objectweb.proactive.core.runtime.StartHierarchical");
        super.startProcess();
        finalProcess.setClassname(bClass);
    }
}
