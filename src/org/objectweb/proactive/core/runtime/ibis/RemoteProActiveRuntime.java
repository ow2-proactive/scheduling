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
package org.objectweb.proactive.core.runtime.ibis;

import ibis.rmi.Remote;
import ibis.rmi.RemoteException;

import java.security.cert.X509Certificate;
import java.util.ArrayList;

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


/**
 *   An adapter for a ProActiveRuntime to be able to receive remote calls. This helps isolate Ibis-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 *          @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */
public interface RemoteProActiveRuntime extends Remote {
    static Logger logger = Logger.getLogger(RemoteProActiveRuntime.class.getName());

    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding,PolicyServer ps, String vnname) throws RemoteException, NodeException;

    public void killAllNodes() throws RemoteException;

    public void killNode(String nodeName) throws RemoteException;

    //public void createLocalVM(JVMProcess jvmProcess) throws RemoteException,java.io.IOException;
    public void createVM(UniversalProcess remoteProcess)
        throws RemoteException, java.io.IOException;

    //public Node[] getLocalNodes() throws RemoteException;
    public String[] getLocalNodeNames() throws RemoteException;

    //public String getLocalNode(String nodeName) throws RemoteException;
    //public String getNode(String nodeName) throws RemoteException;
    public VMInformation getVMInformation() throws RemoteException;

    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,String vmName)
        throws RemoteException;

    public ProActiveRuntime[] getProActiveRuntimes() throws RemoteException;

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName)
        throws RemoteException;

    public void killRT(boolean softly) throws RemoteException;

    public String getURL() throws RemoteException;

    public ArrayList getActiveObjects(String nodeName)
        throws RemoteException;

    public ArrayList getActiveObjects(String nodeName, String objectName)
        throws RemoteException;

    public VirtualNode getVirtualNode(String virtualNodeName)
        throws RemoteException;

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws RemoteException;

    public void unregisterVirtualNode(String virtualNodeName)
        throws RemoteException;
        
	public void unregisterAllVirtualNodes() throws RemoteException;

    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws RemoteException, ConstructorCallExecutionFailedException, 
            java.lang.reflect.InvocationTargetException;

    public UniversalBody receiveBody(String nodeName, Body body)
        throws RemoteException;
	/**
		 * @return
		 */
		public X509Certificate getCreatorCertificate()
			throws RemoteException;

		public PolicyServer getPolicyServer() throws RemoteException;

		public void setProActiveSecurityManager(ProActiveSecurityManager ps)
			throws RemoteException;

		public String getVNName(String Nodename) throws RemoteException;

		/**
		 * @param s
		 */
		public void setDefaultNodeVirtualNodeName(String s) throws RemoteException;

		public void updateLocalNodeVirtualName() throws RemoteException;
	   public void listVirtualNodes() throws RemoteException ;
	   public PolicyServer getNodePolicyServer(String nodeName)throws RemoteException;


		 /**
		  *  sets all needed modifications to enable security components
		  * MUST be called when the descriptor is ready 
		  */
		 public void enableSecurityIfNeeded()throws RemoteException;



	   /**
		* @param nodeName
		* @return
		*/
	   public X509Certificate getNodeCertificate(String nodeName)throws RemoteException;
	
	
	   /**
		* @param nodeName
		* @return returns all entities associated to the node
		*/
	   public ArrayList getEntities(String nodeName)throws RemoteException;

	   /**
		* @param nodeName
		* @return returns all entities associated to the node
		*/
	   public ArrayList getEntities(UniversalBody uBody)throws RemoteException;



	   /**
		* @return returns all entities associated to this runtime
		*/
	   public ArrayList getEntities() throws RemoteException;

	
}
