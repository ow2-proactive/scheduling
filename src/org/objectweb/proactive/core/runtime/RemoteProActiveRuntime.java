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
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.SecurityEntity;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


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
public interface RemoteProActiveRuntime extends Serializable, SecurityEntity {
    static Logger runtimeLogger = ProActiveLogger.getLogger(Loggers.RUNTIME);

    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding,
        ProActiveSecurityManager securityManager, String VNname, String jobId)
        throws IOException, NodeException;

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

    public ProActiveDescriptor getDescriptor(String url,
        boolean isHierarchicalSearch) throws IOException, ProActiveException;

    //Security

    /**
     * entity certificate
     * @return returns entity certificate
     * @throws SecurityNotAvailableException if security is not available
     * @throws java.io.IOException if communication fails
     */
    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, java.io.IOException;

    /**
     * start an unvalidated empty session
     * @param policy policy associated to the session
     * @return session ID
     * @throws SecurityNotAvailableException if security is not available
     * @throws RenegotiateSessionException if the session immediatly expires
     * @throws java.io.IOException if communication fails
     */
    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            java.io.IOException;

    /**
     * entity public key
     * @return returns entity public key
     * @throws SecurityNotAvailableException
     * @throws java.io.IOException
     */
    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, java.io.IOException;

    /**
     * Exchange random value between client and server entity
     * @param sessionID the session ID
     * @param clientRandomValue client random value
     * @return server random value
     * @throws SecurityNotAvailableException if the security is not available
     * @throws RenegotiateSessionException if the session has expired
     * @throws java.io.IOException if communication fails
     */
    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            java.io.IOException;

    /**
     * exchange entity certificate and/or public key if certificate are not available
     * @param sessionID the session ID
     * @param myPublicKey encoded public key
     * @param myCertificate encoded certificate
     * @param signature encoded signature of previous paramaters
     * @return an array containing :
     *           - server certificate and/or server public key
     *           - encoded signature of these parameters
     * @throws SecurityNotAvailableException if the security is not available
     * @throws RenegotiateSessionException if the session has expired
     * @throws KeyExchangeException if a key data/length/algorithm is not supported
     * @throws java.io.IOException if communication fails
     */
    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            KeyExchangeException, java.io.IOException;

    /**
     * this method sends encoded secret parameters to the target entity
     * @param sessionID session ID
     * @param encodedAESKey the AES key use to exchange secret message
     * @param encodedIVParameters Initilization parameters for the AES key
     * @param encodedClientMacKey MAC key for checking signature of future messages
     * @param encodedLockData random value to prevent message replays by an external attacker
     * @param parametersSignature encoded signature of the previous parameters
     * @return an array containing  :
     *             - encoded server AES key
     *             - encoded IV parameters
     *             - encoded server MAC key
     *             - encoded lock data to prevent message replays
     *             - encoded signature of previous parameters
     * @throws SecurityNotAvailableException if this entity does not support security
     * @throws RenegotiateSessionException if the session has expired or has been cancelled during this exchange
     * @throws java.io.IOException if communication fails
     */
    byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            java.io.IOException;

    /**
     * Ask the entity to fill the securityContext parameters with its own policy
     * according to the communication details contained in the given securityContext
     * @param securityContext communication details allowing the entity to
     * look for a matching policy
     * @return securityContext filled with this entity's policy
     * @throws SecurityNotAvailableException thrown the entity doest not support the security
     * @throws java.io.IOException throw when a communication with entity's parent fails.
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, java.io.IOException;

    /**
     * Entity's X509Certificate as byte array
     * @return entity's X509Certificate as byte array
     */
    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, java.io.IOException;

    /**
     * Retrieves all the entity's ID which contain this entity plus this entity ID.
     * @return returns all the entity's ID which contain this entity plus this entity ID.
     * @throws SecurityNotAvailableException if the target entity does not support security
     * @throws java.io.IOException if the communication with one of the entity's parent entities fails
     */
    public ArrayList getEntities()
        throws SecurityNotAvailableException, java.io.IOException;

    /**
     * terminate a given session
     * @param sessionID
     * @throws java.io.IOException if communication fails
     * @throws SecurityNotAvailableException id security is not available
     */
    public void terminateSession(long sessionID)
        throws java.io.IOException, SecurityNotAvailableException;
}
