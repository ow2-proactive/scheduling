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
package org.objectweb.proactive.core.runtime.jini;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


/**
 *   An adapter for a JiniRuntime to be able to receive remote calls. This helps isolate JINI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to another remote objects library.
 *          @see <a href="http://www.javaworld.com/javaworld/jw-11-2000/jw-1110-smartproxy.html">smartProxy Pattern.</a>
 */
public class JiniRuntimeAdapter implements ProActiveRuntime,
    java.io.Serializable {
    protected JiniRuntime jiniRuntime;
    protected VMInformation vmInformation;
    protected String proActiveRuntimeURL;

    //
    // -- Constructors -----------------------------------------------
    //
    protected JiniRuntimeAdapter() throws ProActiveException {
        try {
            this.jiniRuntime = createJiniRuntime();
            this.vmInformation = jiniRuntime.getVMInformation();
            this.proActiveRuntimeURL = jiniRuntime.getURL();
        } catch (java.rmi.RemoteException e) {
            throw new ProActiveException("Cannot create the jiniRuntime or get the VMInformation from the JiniRuntime",
                e);
        }
    }

    public JiniRuntimeAdapter(JiniRuntime r) throws ProActiveException {
        this.jiniRuntime = r;
        try {
            this.vmInformation = jiniRuntime.getVMInformation();
            this.proActiveRuntimeURL = jiniRuntime.getURL();
        } catch (java.rmi.RemoteException e) {
            throw new ProActiveException("Cannot get the NodeInformation of the node",
                e);
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public boolean equals(Object o) {
        if (!(o instanceof JiniRuntimeAdapter)) {
            return false;
        }
        JiniRuntimeAdapter runtime = (JiniRuntimeAdapter) o;
        return jiniRuntime.equals(runtime.jiniRuntime);
    }

    public int hashCode() {
        return jiniRuntime.hashCode();
    }

    //
    // -- Implements ProActiveRuntime -----------------------------------------------
    //
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, PolicyServer ps, String vnname,
        String jobId) throws NodeException {
        try {
            return jiniRuntime.createLocalNode(nodeName,
                replacePreviousBinding, ps, vnname, jobId);
        } catch (java.rmi.RemoteException e) {
            throw new NodeException(e);
        }
    }

    public void killAllNodes() throws ProActiveException {
        try {
            jiniRuntime.killAllNodes();
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    public void killNode(String nodeName) throws ProActiveException {
        try {
            jiniRuntime.killNode(nodeName);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    //	public void createLocalVM(JVMProcess jvmProcess)
    //		throws IOException,ProActiveException
    //	{
    //		try{
    //		jiniRuntime.createLocalVM(jvmProcess);
    //		}catch(java.rmi.RemoteException re){
    //			throw new ProActiveException(re);
    //		}
    //	}
    public void createVM(UniversalProcess remoteProcess)
        throws IOException, ProActiveException {
        try {
            jiniRuntime.createVM(remoteProcess);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    //	public Node[] getLocalNodes() throws ProActiveException
    //	{
    //		try{
    //		return remoteProActiveRuntime.getLocalNodes();
    //		}catch(java.rmi.RemoteException re){
    //			throw new ProActiveException(re);
    //			// behavior to be defined
    //		}
    //	}
    public String[] getLocalNodeNames() throws ProActiveException {
        try {
            return jiniRuntime.getLocalNodeNames();
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
            // behavior to be defined
        }
    }

    //	public String getLocalNode(String nodeName) throws ProActiveException
    //	{
    //		try{
    //		return jiniRuntime.getLocalNode(nodeName);
    //		}catch(java.rmi.RemoteException re){
    //			throw new ProActiveException(re);
    //			// behavior to be defined
    //		}
    //	}
    //
    //	
    //	public String getNode(String nodeName) throws ProActiveException
    //	{
    //		try{
    //		return jiniRuntime.getNode(nodeName);
    //		}catch(java.rmi.RemoteException re){
    //			throw new ProActiveException(re);
    //			// behavior to be defined
    //		}
    //	}
    //	public String getDefaultNodeName() throws ProActiveException{
    //		try{
    //		return remoteProActiveRuntime.getDefaultNodeName();
    //		}catch(java.rmi.RemoteException re){
    //			throw new ProActiveException(re);
    //			// behavior to be defined
    //		}
    //	}
    public VMInformation getVMInformation() {
        return vmInformation;
    }

    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) {
        try {
            //System.out.println("register in adapter"+remoteProActiveRuntime.getURL());
            jiniRuntime.register(proActiveRuntimeDist, proActiveRuntimeName,
                creatorID, creationProtocol, vmName);
        } catch (java.rmi.RemoteException re) {
            re.printStackTrace();
            // behavior to be defined
        }
    }

    public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException {
        try {
            return jiniRuntime.getProActiveRuntimes();
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
            // behavior to be defined
        }
    }

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName)
        throws ProActiveException {
        try {
            return jiniRuntime.getProActiveRuntime(proActiveRuntimeName);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
            // behavior to be defined
        }
    }
    
    public void addAcquaintance(String proActiveRuntimeName) {
    	try {
    		jiniRuntime.addAcquaintance(proActiveRuntimeName);
        } catch (RemoteException re) {
        	// hum ...
        	re.printStackTrace();
        }
    }

    public String[] getAcquaintances() {
    	try {
    		return jiniRuntime.getAcquaintances();
    	} catch (RemoteException re) {
        	// hum ...
        	re.printStackTrace();
        	return new String[0];
        }	
    }
    
    public void killRT(boolean softly) throws Exception {
        try {
            jiniRuntime.killRT(softly);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
            //			re.printStackTrace();
            //			// behavior to be defined
        }
    }

    public String getURL() throws ProActiveException {
        return proActiveRuntimeURL;
        //		try{
        //		return remoteProActiveRuntime.getURL();
        //		}catch(java.rmi.RemoteException re){
        //			throw new ProActiveException(re);
        //			// behavior to be defined
        //		}
    }

    public ArrayList getActiveObjects(String nodeName)
        throws ProActiveException {
        try {
            return jiniRuntime.getActiveObjects(nodeName);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    public ArrayList getActiveObjects(String nodeName, String objectName)
        throws ProActiveException {
        try {
            return jiniRuntime.getActiveObjects(nodeName, objectName);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    public VirtualNode getVirtualNode(String virtualNodeName)
        throws ProActiveException {
        try {
            return jiniRuntime.getVirtualNode(virtualNodeName);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws ProActiveException {
        try {
            jiniRuntime.registerVirtualNode(virtualNodeName,
                replacePreviousBinding);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    public void unregisterVirtualNode(String virtualNodeName)
        throws ProActiveException {
        try {
            jiniRuntime.unregisterVirtualNode(virtualNodeName);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    public void unregisterAllVirtualNodes() throws ProActiveException {
        try {
            jiniRuntime.unregisterAllVirtualNodes();
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws ProActiveException, ConstructorCallExecutionFailedException, 
            InvocationTargetException {
        try {
            return jiniRuntime.createBody(nodeName, bodyConstructorCall,
                isNodeLocal);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    public UniversalBody receiveBody(String nodeName, Body body)
        throws ProActiveException {
        try {
            return jiniRuntime.receiveBody(nodeName, body);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    public UniversalBody receiveCheckpoint(String nodeName, Checkpoint ckpt, int inc) 
    	throws ProActiveException {
        try {
            return jiniRuntime.receiveCheckpoint(nodeName,ckpt,inc);
    	} catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }
        
    
    // SECURITY

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getCreatorCertificate()
     */
    public X509Certificate getCreatorCertificate() throws ProActiveException {
        try {
            return jiniRuntime.getCreatorCertificate();
        } catch (RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getPolicyServer()
     */
    public PolicyServer getPolicyServer() throws ProActiveException {
        try {
            return jiniRuntime.getPolicyServer();
        } catch (RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    public String getVNName(String Nodename) throws ProActiveException {
        try {
            return jiniRuntime.getVNName(Nodename);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    public void setProActiveSecurityManager(ProActiveSecurityManager ps)
        throws ProActiveException {
        try {
            jiniRuntime.setProActiveSecurityManager(ps);
        } catch (RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#setDefaultNodeVirtualNodeName(java.lang.String)
     */
    public void setDefaultNodeVirtualNodeName(String s)
        throws ProActiveException {
        try {
            jiniRuntime.setDefaultNodeVirtualNodeName(s);
        } catch (RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#updateLocalNodeVirtualName()
     */
    public void listVirtualNodes() throws ProActiveException {
        try {
            jiniRuntime.updateLocalNodeVirtualName();
        } catch (RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getNodePolicyServer(java.lang.String)
     */
    public PolicyServer getNodePolicyServer(String nodeName)
        throws ProActiveException {
        try {
            return jiniRuntime.getNodePolicyServer(nodeName);
        } catch (RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#enableSecurityIfNeeded()
     */
    public void enableSecurityIfNeeded() throws ProActiveException {
        try {
            jiniRuntime.enableSecurityIfNeeded();
        } catch (RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getNodeCertificate(java.lang.String)
     */
    public X509Certificate getNodeCertificate(String nodeName)
        throws ProActiveException {
        try {
            return jiniRuntime.getNodeCertificate(nodeName);
        } catch (RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @param nodeName
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(String nodeName) throws ProActiveException {
        try {
            return jiniRuntime.getEntities(nodeName);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    /**
     * @param uBody
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(UniversalBody uBody) throws ProActiveException {
        try {
            return jiniRuntime.getEntities(uBody);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    /**
     * @return returns all entities associated to this runtime
     */
    public ArrayList getEntities() throws ProActiveException {
        try {
            return jiniRuntime.getEntities();
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getPolicy(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext getPolicy(SecurityContext sc)
        throws ProActiveException, SecurityNotAvailableException {
        try {
            return jiniRuntime.getPolicy(sc);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return vmInformation.getJobID();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getJobID(java.lang.String)
     */
    public String getJobID(String nodeUrl) throws ProActiveException {
        try {
            return jiniRuntime.getJobID(nodeUrl);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }
    
    public byte[] getClassDataFromParentRuntime(String className)
            throws ProActiveException {
        try {
            return jiniRuntime.getClassDataFromParentRuntime(className);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }    }
    
    
    public byte[] getClassDataFromThisRuntime(String className) throws ProActiveException {
        try {
            return jiniRuntime.getClassDataFromThisRuntime(className);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    public void setParent(String fatherRuntimeName)  throws ProActiveException {
        try {
             jiniRuntime.setParent(fatherRuntimeName);
        } catch (java.rmi.RemoteException re) {
            throw new ProActiveException(re);
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected JiniRuntime createJiniRuntime() throws java.rmi.RemoteException {
        return new JiniRuntimeImpl();
    }
}
