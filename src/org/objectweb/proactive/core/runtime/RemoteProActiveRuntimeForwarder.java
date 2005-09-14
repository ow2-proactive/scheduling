package org.objectweb.proactive.core.runtime;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueRuntimeID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


public interface RemoteProActiveRuntimeForwarder extends RemoteProActiveRuntime {
    static Logger runtimeLogger = ProActiveLogger.getLogger(Loggers.RUNTIME);

    public String createLocalNode(UniqueRuntimeID ruid, String nodeName,
        boolean replacePreviousBinding, ProActiveSecurityManager psm,
        String VNname, String jobId) throws IOException, NodeException;

    public void killAllNodes(UniqueRuntimeID ruid)
        throws IOException, ProActiveException;

    public void killNode(UniqueRuntimeID ruid, String nodeName)
        throws IOException, ProActiveException;

    public void createVM(UniqueRuntimeID ruid, UniversalProcess remoteProcess)
        throws java.io.IOException, ProActiveException;

    public String[] getLocalNodeNames(UniqueRuntimeID ruid)
        throws IOException, ProActiveException;

    public VMInformation getVMInformation(UniqueRuntimeID ruid)
        throws IOException;

    public void register(UniqueRuntimeID ruid,
        ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeName,
        String creatorID, String creationProtocol, String vmName)
        throws IOException, ProActiveException;

    public void unregister(UniqueRuntimeID ruid,
        ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeName,
        String creatorID, String creationProtocol, String vmName)
        throws IOException, ProActiveException;

    public ProActiveRuntime[] getProActiveRuntimes(UniqueRuntimeID ruid)
        throws IOException, ProActiveException;

    public ProActiveRuntime getProActiveRuntime(UniqueRuntimeID ruid,
        String proActiveRuntimeName) throws IOException, ProActiveException;

    public void addAcquaintance(UniqueRuntimeID ruid,
        String proActiveRuntimeName) throws IOException, ProActiveException;

    public String[] getAcquaintances(UniqueRuntimeID ruid)
        throws IOException, ProActiveException;

    public void rmAcquaintance(UniqueRuntimeID ruid, String proActiveRuntimeName)
        throws IOException, ProActiveException;

    public void killRT(UniqueRuntimeID ruid, boolean softly)
        throws Exception;

    public String getURL(UniqueRuntimeID ruid)
        throws IOException, ProActiveException;

    public ArrayList getActiveObjects(UniqueRuntimeID ruid, String nodeName)
        throws IOException, ProActiveException;

    public ArrayList getActiveObjects(UniqueRuntimeID ruid, String nodeName,
        String objectName) throws IOException, ProActiveException;

    public VirtualNode getVirtualNode(UniqueRuntimeID ruid,
        String virtualNodeName) throws IOException, ProActiveException;

    public void registerVirtualNode(UniqueRuntimeID ruid,
        String virtualNodeName, boolean replacePreviousBinding)
        throws IOException, ProActiveException;

    public void unregisterVirtualNode(UniqueRuntimeID ruid,
        String virtualNodeName) throws IOException, ProActiveException;

    public void unregisterAllVirtualNodes(UniqueRuntimeID ruid)
        throws IOException, ProActiveException;

    public String getJobID(UniqueRuntimeID ruid, String nodeUrl)
        throws IOException, ProActiveException;

    public UniversalBody createBody(UniqueRuntimeID ruid, String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws IOException, ConstructorCallExecutionFailedException, 
            java.lang.reflect.InvocationTargetException, ProActiveException;

    public UniversalBody receiveBody(UniqueRuntimeID ruid, String nodeName,
        Body body) throws IOException, ProActiveException;

    public UniversalBody receiveCheckpoint(UniqueRuntimeID ruid,
        String nodeName, Checkpoint ckpt, int inc)
        throws IOException, ProActiveException;

    public ExternalProcess getProcessToDeploy(UniqueRuntimeID ruid, String vn,
        String vmname) throws ProActiveException, IOException;

    /**
     * @see ProActiveRuntime#getClassDataFromParentRuntime(String)
     */
    public byte[] getClassDataFromParentRuntime(UniqueRuntimeID ruid,
        String className) throws IOException, ProActiveException;

    /**
     * @see ProActiveRuntime#getClassDataFromThisRuntime(String)
     */
    public byte[] getClassDataFromThisRuntime(UniqueRuntimeID ruid,
        String className) throws IOException, ProActiveException;

    public ProActiveDescriptor getDescriptor(UniqueRuntimeID ruid, String url,
        boolean isHierarchicalSearch) throws IOException, ProActiveException;

    // 
    // -- SECURITY
    //
    public X509Certificate getCertificate(UniqueRuntimeID ruid)
        throws SecurityNotAvailableException, java.io.IOException;

    public long startNewSession(UniqueRuntimeID ruid, Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            java.io.IOException;

    public PublicKey getPublicKey(UniqueRuntimeID ruid)
        throws SecurityNotAvailableException, java.io.IOException;

    public byte[] randomValue(UniqueRuntimeID ruid, long sessionID,
        byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            java.io.IOException;

    public byte[][] publicKeyExchange(UniqueRuntimeID ruid, long sessionID,
        byte[] myPublicKey, byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            KeyExchangeException, java.io.IOException;

    byte[][] secretKeyExchange(UniqueRuntimeID ruid, long sessionID,
        byte[] encodedAESKey, byte[] encodedIVParameters,
        byte[] encodedClientMacKey, byte[] encodedLockData,
        byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            java.io.IOException;

    public SecurityContext getPolicy(UniqueRuntimeID ruid,
        SecurityContext securityContext)
        throws SecurityNotAvailableException, java.io.IOException;

    public byte[] getCertificateEncoded(UniqueRuntimeID ruid)
        throws SecurityNotAvailableException, java.io.IOException;

    public ArrayList getEntities(UniqueRuntimeID ruid)
        throws SecurityNotAvailableException, java.io.IOException;

    public void terminateSession(UniqueRuntimeID ruid, long sessionID)
        throws java.io.IOException, SecurityNotAvailableException;

    // End Security
    public String getVNName(UniqueRuntimeID ruid, String Nodename)
        throws IOException, ProActiveException;

    public void launchMain(UniqueRuntimeID ruid, String className,
        String[] parameters)
        throws IOException, ClassNotFoundException, NoSuchMethodException, 
            ProActiveException;

    public void newRemote(UniqueRuntimeID ruid, String className)
        throws IOException, ClassNotFoundException, ProActiveException;
}
