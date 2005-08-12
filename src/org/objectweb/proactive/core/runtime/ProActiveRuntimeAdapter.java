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
package org.objectweb.proactive.core.runtime;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;

import java.io.IOException;
import java.io.Serializable;

import java.lang.reflect.InvocationTargetException;

import java.rmi.UnmarshalException;

import java.util.ArrayList;


/**
 * @author ProActiveTeam
 * @version 1.0, 5 août 2005
 * @since ProActive 2.2
 *
 */
public class ProActiveRuntimeAdapter implements ProActiveRuntime, Serializable {
    protected RemoteProActiveRuntime proActiveRuntime;
    protected VMInformation vmInformation;
    protected String proActiveRuntimeURL;

    //this boolean is used when killing the runtime. Indeed in case of co-allocation, we avoid a second call to the runtime
    // which is already dead
    protected boolean alreadykilled = false;

    //
    // -- Constructors -----------------------------------------------
    //
    public ProActiveRuntimeAdapter() {
    }

    public ProActiveRuntimeAdapter(RemoteProActiveRuntime proActiveRuntime)
        throws ProActiveException {
        this.proActiveRuntime = proActiveRuntime;
        try {
            this.vmInformation = proActiveRuntime.getVMInformation();
            this.proActiveRuntimeURL = proActiveRuntime.getURL();
        } catch (IOException e) {
           throw new ProActiveException(e);
        }
        
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public boolean equals(Object o) {
        if (!(o instanceof ProActiveRuntimeAdapter)) {
            return false;
        }
        ProActiveRuntimeAdapter runtime = (ProActiveRuntimeAdapter) o;
        return proActiveRuntime.equals(runtime.proActiveRuntime);
    }

    public int hashCode() {
        return proActiveRuntime.hashCode();
    }

    //
    // -- Implements ProActiveRuntime -----------------------------------------------
    //

    /**
     * @throws NodeException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createLocalNode(java.lang.String, boolean, org.objectweb.proactive.ext.security.PolicyServer, java.lang.String, java.lang.String)
     */
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, PolicyServer policyServer,
        String vnName, String jobId) throws NodeException {
        try {
            return proActiveRuntime.createLocalNode(nodeName,
                replacePreviousBinding, policyServer, vnName, jobId);
        } catch (IOException e) {
            throw new NodeException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killAllNodes()
     */
    public void killAllNodes() throws ProActiveException {
        try {
            proActiveRuntime.killAllNodes();
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killNode(java.lang.String)
     */
    public void killNode(String nodeName) throws ProActiveException {
        try {
            proActiveRuntime.killNode(nodeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws IOException
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createVM(org.objectweb.proactive.core.process.UniversalProcess)
     */
    public void createVM(UniversalProcess remoteProcess)
        throws IOException, ProActiveException {
        proActiveRuntime.createVM(remoteProcess);
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getLocalNodeNames()
     */
    public String[] getLocalNodeNames() throws ProActiveException {
        try {
            return proActiveRuntime.getLocalNodeNames();
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getVMInformation()
     */
    public VMInformation getVMInformation() {
        return vmInformation;
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#register(org.objectweb.proactive.core.runtime.ProActiveRuntime, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeUrl, String creatorID, String creationProtocol,
        String vmName) throws ProActiveException {
        try {
            proActiveRuntime.register(proActiveRuntimeDist, proActiveRuntimeUrl,
                creatorID, creationProtocol, vmName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#unregister(org.objectweb.proactive.core.runtime.ProActiveRuntime, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void unregister(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeUrl, String creatorID, String creationProtocol,
        String vmName) throws ProActiveException {
        try {
            this.proActiveRuntime.unregister(proActiveRuntimeDist,
                proActiveRuntimeUrl, creatorID, creationProtocol, vmName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getProActiveRuntimes()
     */
    public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException {
        try {
            return proActiveRuntime.getProActiveRuntimes();
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getProActiveRuntime(java.lang.String)
     */
    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName) throws ProActiveException {
        try {
            return proActiveRuntime.getProActiveRuntime(proActiveRuntimeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#addAcquaintance(java.lang.String)
     */
    public void addAcquaintance(String proActiveRuntimeName) throws ProActiveException {
        try {
            proActiveRuntime.addAcquaintance(proActiveRuntimeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getAcquaintances()
     */
    public String[] getAcquaintances() throws ProActiveException {
        try {
            return proActiveRuntime.getAcquaintances();
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#rmAcquaintance(java.lang.String)
     */
    public void rmAcquaintance(String proActiveRuntimeName) throws ProActiveException {
        try {
            proActiveRuntime.rmAcquaintance(proActiveRuntimeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killRT(boolean)
     */
    public void killRT(boolean softly) throws Exception {
        try {
            if (!alreadykilled) {
                proActiveRuntime.killRT(softly);
            }
            alreadykilled = true;
        } catch (UnmarshalException e) {
            //here should be caught the exception from System.exit
            alreadykilled = true;
            throw e;
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getURL()
     */
    public String getURL() {
        return proActiveRuntimeURL;
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getActiveObjects(java.lang.String)
     */
    public ArrayList getActiveObjects(String nodeName) throws ProActiveException {
        try {
            return proActiveRuntime.getActiveObjects(nodeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getActiveObjects(java.lang.String, java.lang.String)
     */
    public ArrayList getActiveObjects(String nodeName, String className) throws ProActiveException {
        try {
            return proActiveRuntime.getActiveObjects(nodeName, className);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getVirtualNode(java.lang.String)
     */
    public VirtualNode getVirtualNode(String virtualNodeName) throws ProActiveException {
        try {
            return proActiveRuntime.getVirtualNode(virtualNodeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#registerVirtualNode(java.lang.String, boolean)
     */
    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws ProActiveException {
        try {
            proActiveRuntime.registerVirtualNode(virtualNodeName,
                replacePreviousBinding);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#unregisterVirtualNode(java.lang.String)
     */
    public void unregisterVirtualNode(String virtualNodeName) throws ProActiveException {
        try {
            proActiveRuntime.unregisterVirtualNode(virtualNodeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#unregisterAllVirtualNodes()
     */
    public void unregisterAllVirtualNodes() throws ProActiveException {
        try {
            proActiveRuntime.unregisterAllVirtualNodes();
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getJobID(java.lang.String)
     */
    public String getJobID(String nodeUrl) throws ProActiveException {
        try {
            return proActiveRuntime.getJobID(nodeUrl);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createBody(java.lang.String, org.objectweb.proactive.core.mop.ConstructorCall, boolean)
     */
    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws ConstructorCallExecutionFailedException, 
            InvocationTargetException, ProActiveException {
        try {
            return proActiveRuntime.createBody(nodeName, bodyConstructorCall,
                isNodeLocal);
        }  catch (IOException e) {
            throw new ProActiveException(e);
        } 
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#receiveBody(java.lang.String, org.objectweb.proactive.Body)
     */
    public UniversalBody receiveBody(String nodeName, Body body) throws ProActiveException {
        try {
            return proActiveRuntime.receiveBody(nodeName, body);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#receiveCheckpoint(java.lang.String, org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint, int)
     */
    public UniversalBody receiveCheckpoint(String nodeName, Checkpoint ckpt,
        int inc) throws ProActiveException {
        try {
            return proActiveRuntime.receiveCheckpoint(nodeName, ckpt, inc);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    //-------------
    // Security
    //-------------

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getPolicyServer()
     */
    public PolicyServer getPolicyServer() throws ProActiveException {
        try {
            return proActiveRuntime.getPolicyServer();
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getEntities(java.lang.String)
     */
    public ArrayList getEntities(String nodeName) throws ProActiveException {
        try {
            return proActiveRuntime.getEntities(nodeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getPolicy(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext getPolicy(SecurityContext sc)
        throws SecurityNotAvailableException, ProActiveException {
        try {
            return proActiveRuntime.getPolicy(sc);
        } catch (IOException e) {
            throw new ProActiveException(e);
        } 
    }
    
    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getVNName(java.lang.String)
     */
    public String getVNName(String Nodename) throws ProActiveException {
        try {
            return proActiveRuntime.getVNName(Nodename);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getClassDataFromThisRuntime(java.lang.String)
     */
    public byte[] getClassDataFromThisRuntime(String className) throws ProActiveException {
        try {
            return proActiveRuntime.getClassDataFromThisRuntime(className);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getClassDataFromParentRuntime(java.lang.String)
     */
    public byte[] getClassDataFromParentRuntime(String className)
        throws ProActiveException {
        try {
            return proActiveRuntime.getClassDataFromParentRuntime(className);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    //
    // Implements Job Interface
    //

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return vmInformation.getJobID();
    }

    
}
