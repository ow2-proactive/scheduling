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
package org.objectweb.proactive.core.runtime;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * An adapter for a ProActiveRuntime to be able to receive remote calls. This helps isolate
 * protocol-specific code into a small set of specific classes, thus enabling reuse when
 * using another remote objects library.
 * Implemented protocols are RMI, RMISSH, IBIS, JINI, HTTP
 * @author ProActiveTeam
 * @version 1.0
 * @since ProActive 2.2
 * @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */
public interface RemoteProActiveRuntime extends Serializable {
    static Logger runtimeLogger = ProActiveLogger.getLogger(Loggers.RUNTIME);

    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding,
        ProActiveSecurityManager securityManager, String VNname, String jobId)
        throws IOException, NodeException, AlreadyBoundException;

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

    public VirtualNodeInternal getVirtualNode(String virtualNodeName)
        throws IOException, ProActiveException;

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding)
        throws IOException, AlreadyBoundException, ProActiveException;

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

    public ExternalProcess getProcessToDeploy(
        ProActiveRuntime proActiveRuntimeDist, String creatorID, String vmName,
        String padURL) throws ProActiveException, IOException;

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

    public String getVNName(String Nodename)
        throws IOException, ProActiveException;

    public void launchMain(String className, String[] parameters)
        throws IOException, ClassNotFoundException, NoSuchMethodException,
            ProActiveException;

    public void newRemote(String className)
        throws IOException, ClassNotFoundException, ProActiveException;

    public ProActiveDescriptorInternal getDescriptor(String url,
        boolean isHierarchicalSearch) throws IOException, ProActiveException;

    //
    // -- Security
    //
    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, java.io.IOException;

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            java.io.IOException;

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, java.io.IOException;

    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            java.io.IOException;

    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            KeyExchangeException, java.io.IOException;

    byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            java.io.IOException;

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, java.io.IOException;

    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, java.io.IOException;

    public ArrayList<Entity> getEntities()
        throws SecurityNotAvailableException, java.io.IOException;

    public void terminateSession(long sessionID)
        throws java.io.IOException, SecurityNotAvailableException;

    public Object setLocalNodeProperty(String nodeName, String key, String value)
        throws IOException, ProActiveException;

    public String getLocalNodeProperty(String nodeName, String key)
        throws IOException, ProActiveException;
}
