/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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

import java.util.ArrayList;

import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;

/**
 * For internal use only.
 * This class is a runtime representation of a node 
 * and should not be used outside a runtime 
 */

public class LocalNode {

	private String name;
	private ArrayList activeObjects;
	private String jobId;
	private ProActiveSecurityManager securityManager;
	private String virtualNodeName;
	
	public LocalNode (String nodeName, String jobId, ProActiveSecurityManager securityManager, String virtualNodeName) {
		this.name = nodeName;
		this.jobId = jobId;
		this.securityManager = securityManager;
		this.virtualNodeName = virtualNodeName;
		this.activeObjects = new ArrayList();
	
		if (this.securityManager != null) {
            ProActiveLogger.getLogger(Loggers.SECURITY_RUNTIME).debug("Local Node : " +
                this.name + " VN name : " + this.virtualNodeName + " policyserver for app :" +
                this.securityManager.getPolicyServer().getApplicationName());

            // setting virtual node name
            this.securityManager.setVNName(this.virtualNodeName);

            ProActiveLogger.getLogger(Loggers.SECURITY_RUNTIME).debug("registering node certificate for VN " +
               this.virtualNodeName);
        }
		
	}
	
	/**
	 * @return Returns the active objects located inside the node.
	 */
	public ArrayList getActiveObjects() {
		return activeObjects;
	}
	
	/**
	 * set the list of active objects contained by the node
	 * @param activeObjects active objects to set.
	 */
	public void setActiveObjects(ArrayList activeObjects) {
		this.activeObjects = activeObjects;
	}
	
	
	
	
	/**
	 * @return Returns the jobId.
	 */
	public String getJobId() {
		return jobId;
	}
	
	/**
	 * @param jobId The jobId to set.
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	
	/**
	 * @return Returns the node name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name The node name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return Returns the node' security manager.
	 */
	public ProActiveSecurityManager getSecurityManager() {
		return securityManager;
	}
	
	/**
	 * @param securityManager The securityManager to set.
	 */
	public void setSecurityManager(ProActiveSecurityManager securityManager) {
		this.securityManager = securityManager;
	}
	
	/**
	 * @return Returns the name of the virtual node by which the node 
	 * has been instancied if any.
	 */
	public String getVirtualNodeName() {
		return virtualNodeName;
	}
	
	/**
	 * @param virtualNodeName The virtualNodeName to set.
	 */
	public void setVirtualNodeName(String virtualNodeName) {
		this.virtualNodeName = virtualNodeName;
	}
	
	
}
