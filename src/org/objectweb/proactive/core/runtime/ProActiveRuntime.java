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

import org.apache.log4j.Logger;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.Job;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;

import java.security.cert.X509Certificate;

import java.util.ArrayList;


/**
 * <p>
 * A <code>ProActiveRuntime</code> offers a set of services needed by ProActive to work with
 * remote JVM. Each JVM that is aimed to hold active objects must contain
 * one and only one instance of the <code>ProActiveRuntime</code> class. That instance, when
 * created, will register itself to some registry where it is possible to perform a lookup
 * (such as the RMI registry).
 * </p><p>
 * When ProActive needs to interact with a remote JVM, it will lookup for the <code>ProActiveRuntime</code> associated
 * with that JVM (using typically the RMI Registry) and use the remote interface of the <code>ProActiveRuntime</code>
 * to perform the interaction.
 * </p><p>
 * Aside the getter giving information about the VM, the 3 services offered are :
 * <ul>
 * <li>the creation of local node through the method <code>createLocalNode</code></li>
 * <li>the creation of another VM(local or remote) through the method <code>createVM</code></li>
 * </ul>
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/08/29
 * @since   ProActive 0.91
 *
 */
public interface ProActiveRuntime extends Job {
    static Logger runtimeLogger = Logger.getLogger(ProActiveRuntime.class.getName());

    /**
     * Creates a new Node in the same VM as this ProActiveRuntime
     * @param nodeName the name of the node to create localy
     * @param replacePreviousBinding
     * @return the url of the newly created node in the target VM
     * @exception NodeException if the new node cannot be created
     */
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, PolicyServer policyServer,
        String vnName, String jobId) throws NodeException;

    /**
     * Kills all Nodes in this ProActiveRuntime
     */
    public void killAllNodes() throws ProActiveException;

    /**
     * Kills the Node of the given name and all Active Objects deployed on it.
     * @param nodeName the name of the node to kill
     */
    public void killNode(String nodeName) throws ProActiveException;

    /**
     * Creates a new ProActiveRuntime associated with a new VM on the host defined in the given process.
     * @param remoteProcess the process that will originate the creation of the runtime
     * @exception java.io.IOException if the new VM cannot be created
     * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
     */
    public void createVM(UniversalProcess remoteProcess)
        throws java.io.IOException, ProActiveException;

    /**
     * Returns the name of all nodes known by this ProActiveRuntime on this VM
     * @return the name of all nodes known by this ProActiveRuntime on this VM
     * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
     */
    public String[] getLocalNodeNames() throws ProActiveException;

    /**
     * Returns the JVM information as one object. This method allows to
     * retrieve all JVM information in one call to optimize performance.
     * @return the JVM information as one object
     */
    public VMInformation getVMInformation();

    /**
     * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
     * Allows this ProactiveRuntime on this VM to register another ProActiveRuntime
     * @param proActiveRuntimeDist the remote ProactiveRuntime to register
     * @param proActiveRuntimeUrl the url of the remote ProActiveRuntime
     * @param creatorID the name of the creator of the remote ProActiveRuntime
     * @param creationProtocol the protocol used to register the remote ProActiveRuntime when created
     */
    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeUrl, String creatorID, String creationProtocol,
        String vmName);

    /**
     * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
     * Allows this ProactiveRuntime on this VM to <b>unregister</b> an already resigesterd
     * ProActiveRuntime.
     * @param proActiveRuntimeDist the remote ProactiveRuntime to <b>unregister</b>.
     * @param proActiveRuntimeUrl the url of the remote ProActiveRuntime
     * @param creatorID the name of the creator of the remote ProActiveRuntime
     * @param creationProtocol the protocol used to register the remote ProActiveRuntime when created
     */
    public void unregister(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeUrl, String creatorID, String creationProtocol,
        String vmName);

    /**
     * Returns all the ProActiveRuntime registered on this ProActiveRuntime on this VM
     * @return all the ProActiveRuntime registered on this ProActiveRuntime on this VM
     * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
     */
    public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException;

    /**
     * Returns the ProActiveRuntime of specified name
     * @param proActiveRuntimeName the name of the ProActiveruntime to return
     * @return the ProActiveRuntime of specified name
     * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
     */
    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName)
        throws ProActiveException;

    /**
     * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
     * Tells this runtime that it's registered in another one
     * @param proActiveRuntimeName the name of the remote ProActiveRuntime in which this runtime is registered
     */
    void addAcquaintance(String proActiveRuntimeName);

    /**
     * Returns all the ProActiveRuntime URL in which this runtime is registered
     * @return all the ProActiveRuntime URL in which this runtime is registered
     */
    public String[] getAcquaintances();
    
	/**
	 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>.
	 * Tell to this runtime that is no more registered in <code>proActiveRuntimeName</code>.
	 * @param proActiveRuntimeName the name of the remote ProActiveRuntime.
	 */
	public void rmAcquaintance(String proActiveRuntimeName);

    /**
     * Kills this ProActiveRuntime and this VM
     * @param softly if false, this Runtime is killed abruptely
     * if true, if that runtime originates the creation of  a rmi registry, it waits until the registry is empty before
     * dying. To be more precise a thread is created to ask periodically the registry if objects are still
     * registered.
     */
    public void killRT(boolean softly) throws Exception;

    /**
     * Returns the url of this ProActiveRuntime on the local or remote VM
     */
    public String getURL() throws ProActiveException;

    /**
     * Returns all Active Objects deployed on the node with the given name on
     * this ProActiveRuntime
     * @param nodeName the name of the node
     * @return ArrayList of ArrayList. The latter contains [body, classname].
     */
    public ArrayList getActiveObjects(String nodeName)
        throws ProActiveException;

    /**
     * Returns all Active Objects with the specified class name, deployed on the node with the given name on
     * this ProActiveRuntime
     * @param nodeName the name of the node
     * @param className class of the Active Objects to look for
     * @return Active Objects of the specified class name deployed on this ProactiveRuntime
     */
    public ArrayList getActiveObjects(String nodeName, String className)
        throws ProActiveException;

    /**
     * Returns the VirtualNode with the given name
     * @param virtualNodeName the name of the VirtualNode to be acquired
     * @return VirtualNode the virtualnode of the given name or null if such virtualNode
     * does not exist.
     */
    public VirtualNode getVirtualNode(String virtualNodeName)
        throws ProActiveException;

    /**
     * Registers the virtualNode of the given name in a registry such RMIRegistry or Jini Service Lookup
     * @param virtualNodeName
     */
    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws ProActiveException;

    /**
     * Unregisters the VirtualNode of the given name from the local runtime.
     * @param virtualNodeName the virtualNode to unregister.
     * @throws ProActiveException if a problem occurs when trying to unregister the virtualNode
     */
    public void unregisterVirtualNode(String virtualNodeName)
        throws ProActiveException;

    /**
     * Unregisters all VirtualNodes from the local runtime
     * @throws ProActiveException
     */
    public void unregisterAllVirtualNodes() throws ProActiveException;

    /**
     * @param nodeUrl
     * @return the jobId of the node with the given name
     * @throws ProActiveException
     */
    public String getJobID(String nodeUrl) throws ProActiveException;

    /**
     * <p>
     * This method is the basis for creating remote active objects.
     * It receives a <code>ConstructorCall</code> that is the constructor call of the body
     * of the active object to create. Inside the parameters of this constructor call is
     * the constructor call of the reified object. Upon execution of the constructor call of the
     * body, the body holding a reference on the reified object will get created and returned.
     * </p><p>
     * The method returns a reference on the RMI stub of the newly created body.
     * </p>
     * @param nodeName the name of the node the newly created active object will be associated to
     * @param bodyConstructorCall the Constructor call allowing to create the body
     * @param isNodeLocal boolean. True if proxy and body are on the same vm, false otherwise
     * @return a stub on the newly created body.
     * @exception ProActiveException if a problem occurs due to the remote nature of this ProactiveRuntime
     * @exception ConstructorCallExecutionFailedException if the constructor call cannot be executed
     * @exception java.lang.reflect.InvocationTargetException if the java constructor execution failed
     */
    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws ProActiveException, ConstructorCallExecutionFailedException, 
            java.lang.reflect.InvocationTargetException;

    /**
     * <p>
     * This method is the basis for migrating active objects.
     * It receives a <code>Body</code> that embbeds the reified object and its graph of
     * passive objects. Once transfered remotely using serialization, the body should restart
     * itself and perform all updates needed to be functionning.
     * </p><p>
     * The method returns a reference on the RMI stub of the migrated body.
     * </p>
     * @param nodeName the name of the node the newly created active object will be associated to
     * @param body the body of the active object migrating to this node.
     * @return a RMI stub on the migrated body.
     * @exception ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
     */
    public UniversalBody receiveBody(String nodeName, Body body)
        throws ProActiveException;

    /**
     * The runtime recovers the body contained in the checkpoint ckpt.
     * @param nodeName node on which the body is restarted
     * @param ckpt checkpoint to use for recovery
     * @param inc incarnation number of this recovery
     * @return *not used*
     * @throws ProActiveException if a problem occurs due to the remote nature of this ProActiveRuntime
     */
    public UniversalBody receiveCheckpoint(String nodeName, Checkpoint ckpt,
        int inc) throws ProActiveException;

    // SECURITY

    /**
     * @return creator's certificate if exists
     */
    public X509Certificate getCreatorCertificate() throws ProActiveException;

    /**
     * @return Policy server
     */
    public PolicyServer getPolicyServer() throws ProActiveException;

    public void setProActiveSecurityManager(ProActiveSecurityManager ps)
        throws ProActiveException;

    public String getVNName(String Nodename) throws ProActiveException;

    public void setDefaultNodeVirtualNodeName(String s)
        throws ProActiveException;

    public PolicyServer getNodePolicyServer(String nodeName)
        throws ProActiveException;

    /**
     *  sets all needed modifications to enable security components
     * MUST be called when the descriptor is ready
     */
    public void enableSecurityIfNeeded() throws ProActiveException;

    /**
     * @param nodeName
     * @return return certificate associated to the node designed by nodeName
     */
    public X509Certificate getNodeCertificate(String nodeName)
        throws ProActiveException;

    /**
     * @param nodeName
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(String nodeName) throws ProActiveException;

    /**
     * @param uBody
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(UniversalBody uBody) throws ProActiveException;

    /**
     * @return returns all entities associated to this runtime
     */
    public ArrayList getEntities() throws ProActiveException;

    /**
     * @param sc
     */
    public SecurityContext getPolicy(SecurityContext sc)
        throws ProActiveException, SecurityNotAvailableException;

    /**
     * Looks for class bytecode in the current runtime.
     * If current runtime has no father, stub generation can be intented to
     * get the class bytecode
     * @param className name of the class
     * @return the bytecode corresponding to the given class, or null if not found
     */
    public byte[] getClassDataFromThisRuntime(String className)
        throws ProActiveException;

    /**
     * Looks for class bytecode in the ancestors of the current runtime : first it tries in the father runtime,
     * then in the grand-father etc...
     * @param className name of the class
     * @return the bytecode corresponding to the given class, or null if not found
     */
    public byte[] getClassDataFromParentRuntime(String className)
        throws ProActiveException;

    /**
     * This method adds a reference to the runtime that created this runtime.
     * It is called when a new runtime is created from another one.
     * @param parentRuntimeName the name of the creator of this runtime
     */
    public void setParent(String parentRuntimeName) throws ProActiveException;

}
