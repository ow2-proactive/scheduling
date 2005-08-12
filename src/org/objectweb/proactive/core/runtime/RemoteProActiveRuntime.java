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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


/**
 * @author ProActiveTeam
 * @version 1.0, 9 août 2005
 * @since ProActive 2.2
 *
 */
public interface RemoteProActiveRuntime extends Serializable {
    static Logger runtimeLogger = ProActiveLogger.getLogger(Loggers.RUNTIME);

    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, PolicyServer ps, String VNname,
        String jobId) throws IOException, NodeException;

    public void killAllNodes() throws IOException, ProActiveException;

    public void killNode(String nodeName)
        throws IOException, ProActiveException;

    public void createVM(UniversalProcess remoteProcess)
        throws java.io.IOException, ProActiveException;

    public String[] getLocalNodeNames() throws IOException, ProActiveException;

    public VMInformation getVMInformation() throws IOException;

    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) throws IOException, ProActiveException;

    public void unregister(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) throws IOException, ProActiveException;

    public ProActiveRuntime[] getProActiveRuntimes()
        throws IOException, ProActiveException;

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName)
        throws IOException, ProActiveException;

    public void addAcquaintance(String proActiveRuntimeName)
        throws IOException, ProActiveException;

    public String[] getAcquaintances() throws IOException, ProActiveException;

    public void rmAcquaintance(String proActiveRuntimeName)
        throws IOException, ProActiveException;

    public void killRT(boolean softly) throws Exception;

    public String getURL() throws IOException, ProActiveException;

    public ArrayList getActiveObjects(String nodeName)
        throws IOException, ProActiveException;

    public ArrayList getActiveObjects(String nodeName, String objectName)
        throws IOException, ProActiveException;

    public VirtualNode getVirtualNode(String virtualNodeName)
        throws IOException, ProActiveException;

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws IOException, ProActiveException;

    public void unregisterVirtualNode(String virtualNodeName)
        throws IOException, ProActiveException;

    public void unregisterAllVirtualNodes()
        throws IOException, ProActiveException;

    public String getJobID(String nodeUrl)
        throws IOException, ProActiveException;

    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws IOException, ConstructorCallExecutionFailedException, 
            java.lang.reflect.InvocationTargetException, ProActiveException;

    public UniversalBody receiveBody(String nodeName, Body body)
        throws IOException, ProActiveException;

    public UniversalBody receiveCheckpoint(String nodeName, Checkpoint ckpt,
        int inc) throws IOException, ProActiveException;

    //Security
    public PolicyServer getPolicyServer()
        throws IOException, ProActiveException;

    /**
     * @param sc
     */
    public SecurityContext getPolicy(SecurityContext sc)
        throws IOException, SecurityNotAvailableException, ProActiveException;

    /**
     * @param nodeName
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(String nodeName)
        throws IOException, ProActiveException;

    public String getVNName(String Nodename)
        throws IOException, ProActiveException;

    //  -----------------------------------------
    //	Security: methods not used 
    //-----------------------------------------
    //    public void setProActiveSecurityManager(ProActiveSecurityManager ps)
    //    throws IOException, ProActiveException;
    //        /**
    //         * @return creator certificate
    //         */
    //        public X509Certificate getCreatorCertificate()
    //            throws IOException;
    //        /**
    //         * @param s
    //         */
    //        public void setDefaultNodeVirtualNodeNAme(String s)
    //            throws IOException;
    //
    //        public PolicyServer getNodePolicyServer(String nodeName)
    //            throws IOException;
    //
    //        /**
    //         *  sets all needed modifications to enable security components
    //         * MUST be called when the descriptor is ready
    //         */
    //        public void enableSecurityIfNeeded() throws IOException;
    //
    //        public X509Certificate getNodeCertificate(String nodeName)
    //            throws IOException;
    //
    //       
    //
    //        /**
    //         * @param uBody
    //         * @return returns all entities associated to the node
    //         */
    //        public ArrayList getEntities(UniversalBody uBody) throws IOException;
    //
    //        /**
    //         * @return returns all entities associated to this runtime
    //         */
    //        public ArrayList getEntities() throws IOException;

    /**
     * @see ProActiveRuntime#getClassDataFromParentRuntime(String)
     */
    public byte[] getClassDataFromParentRuntime(String className)
        throws IOException, ProActiveException;

    /**
     * @see ProActiveRuntime#getClassDataFromThisRuntime(String)
     */
    public byte[] getClassDataFromThisRuntime(String className)
        throws IOException, ProActiveException;
}
