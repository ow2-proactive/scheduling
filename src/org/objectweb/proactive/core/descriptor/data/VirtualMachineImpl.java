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
package org.objectweb.proactive.core.descriptor.data;

import org.objectweb.proactive.core.process.ExternalProcess;


/**
 * A <code>VirtualMachine</code> is a conceptual entity that represents
 * a JVM running a ProActiveRuntime
 *
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 *
 */
public class VirtualMachineImpl implements VirtualMachine, java.io.Serializable {
    //
    //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
    //

    /** the name of this VirtualMachine */
    private String name;

    /** number of nodes that will be deployed on this VM. One node is the default */
    private String nodeNumber = "1";

    /** the acquisition method to use to find the VirtualMachine once created */
    private String acquisitionMethod;

//    /** the port number used during the acquisition */
//    private String portNumber;
    
    /** the process to start in order to create the JVM */
    private transient ExternalProcess process;

    /** The name of the VirtualNode that created this VirtualMachine */
    private String creatorId = null;

    //
    //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
    //

    /**
     * Contructs a new intance of VirtualNode
     */
    VirtualMachineImpl() {
    }

    //
    //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
    //
    public void setHostsNumber(String nodeNumber) throws java.io.IOException {
        if (new Integer(nodeNumber).intValue() < 1) {
            throw new java.io.IOException(
                "Cannot define nodeNumber with a value < 1");
        }
        this.nodeNumber = nodeNumber;
    }
    

    public String getNodeNumber() {
        return this.nodeNumber;
    }

    public void setName(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }

    public void setAcquisitionMethod(String s) {
        acquisitionMethod = s;
    }

    public String getAcquisitionMethod() {
        return acquisitionMethod;
    }

    
//    public void setPortNumber(String s) {
//    	portNumber = s;
//    }
//    
//    public String getPortNumber() {
//    	return portNumber;
//    }
    
    public void setProcess(ExternalProcess p) {
        process = p;
    }

    public ExternalProcess getProcess() {
        return process;
    }

    /**
     * Returns the name of the machine where the process mapped to this virtual machine
     * was launched.
     * @return String
     */
    public String getHostName() {
        String hostName = process.getHostname();
        if (hostName == null) {
            hostName = "localhost";
        }
        return hostName;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorId() {
        return this.creatorId;
    }
}
