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

import org.apache.log4j.Logger;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
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

import java.rmi.RemoteException;

import java.security.cert.X509Certificate;

import java.util.ArrayList;


/**
 *   An adapter for a ProActiveRuntime to be able to receive remote calls. This helps isolate JINI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 *          @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */
public interface JiniRuntime extends java.rmi.Remote {
    static Logger logger = Logger.getLogger(JiniRuntime.class.getName());

    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, PolicyServer ps, String vnname,
        String jobId) throws java.rmi.RemoteException, NodeException;

    public void killAllNodes() throws java.rmi.RemoteException;

    public void killNode(String nodeName) throws java.rmi.RemoteException;

    //public void createLocalVM(JVMProcess jvmProcess) throws java.rmi.RemoteException,java.io.IOException;
    public void createVM(UniversalProcess remoteProcess)
        throws java.rmi.RemoteException, java.io.IOException;

    //public Node[] getLocalNodes() throws java.rmi.RemoteException;
    public String[] getLocalNodeNames() throws java.rmi.RemoteException;

    //public String getLocalNode(String nodeName) throws java.rmi.RemoteException;
    //public String getNode(String nodeName) throws java.rmi.RemoteException;
    public VMInformation getVMInformation() throws java.rmi.RemoteException;

    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) throws java.rmi.RemoteException;

    public ProActiveRuntime[] getProActiveRuntimes()
        throws java.rmi.RemoteException;

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName)
        throws java.rmi.RemoteException;

    public void addAcquaintance(String proActiveRuntimeName) throws java.rmi.RemoteException;

    public String[] getAcquaintances() throws java.rmi.RemoteException;
    
    public void killRT(boolean softly) throws java.rmi.RemoteException;

    public String getURL() throws java.rmi.RemoteException;

    public ArrayList getActiveObjects(String nodeName)
        throws java.rmi.RemoteException;

    public ArrayList getActiveObjects(String nodeName, String objectName)
        throws java.rmi.RemoteException;

    public VirtualNode getVirtualNode(String virtualNodeName)
        throws java.rmi.RemoteException;

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws java.rmi.RemoteException;

    public void unregisterVirtualNode(String virtualNodeName)
        throws java.rmi.RemoteException;

    public void unregisterAllVirtualNodes() throws java.rmi.RemoteException;

    public String getJobID(String nodeUrl) throws java.rmi.RemoteException;

    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws java.rmi.RemoteException, 
            ConstructorCallExecutionFailedException, 
            java.lang.reflect.InvocationTargetException;

    public UniversalBody receiveBody(String nodeName, Body body)
        throws java.rmi.RemoteException;

    /**
     * @return creator certificate
     */
    public X509Certificate getCreatorCertificate()
        throws java.rmi.RemoteException;

    public PolicyServer getPolicyServer() throws RemoteException;

    public void setProActiveSecurityManager(ProActiveSecurityManager ps)
        throws java.rmi.RemoteException;

    public String getVNName(String Nodename) throws RemoteException;

    /**
     * @param s
     */
    public void setDefaultNodeVirtualNodeName(String s)
        throws java.rmi.RemoteException;

    public void updateLocalNodeVirtualName() throws RemoteException;

    public PolicyServer getNodePolicyServer(String nodeName)
        throws RemoteException;

    /**
     *  sets all needed modifications to enable security components
     * MUST be called when the descriptor is ready
     */
    public void enableSecurityIfNeeded() throws RemoteException;

    /**
     * @param nodeName
     * @return node certificate
     */
    public X509Certificate getNodeCertificate(String nodeName)
        throws RemoteException;

    /**
     * @param nodeName
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(String nodeName) throws RemoteException;

    /**
     * @param uBody
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(UniversalBody uBody) throws RemoteException;

    /**
     * @param sc
     */
    public SecurityContext getPolicy(SecurityContext sc)
        throws RemoteException, SecurityNotAvailableException;

    /**
     * @return returns all entities associated to this runtime
     */
    public ArrayList getEntities() throws RemoteException;
    
    /**
     * @see ProActiveRuntime#getClassDataFromParentRuntime(String)
     */
    public byte[] getClassDataFromParentRuntime(String className) throws RemoteException;    

    /**
     * @see ProActiveRuntime#getClassDataFromThisRuntime(String)
     */
    public byte[] getClassDataFromThisRuntime(String className) throws RemoteException;
    
    /**
     * @see ProActiveRuntime#setParent(String)
     */
    public void setParent(String parentRuntimeName) throws RemoteException;
    
}
