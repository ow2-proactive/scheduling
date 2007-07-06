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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.axis.NoEndPointException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.filter.DefaultFilter;
import org.objectweb.proactive.core.filter.Filter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * For internal use only.
 * This class is a runtime representation of a node
 * and should not be used outside a runtime
 */
public class LocalNode {
    private String name;
    private ArrayList<UniqueID> activeObjectsId;
    private String jobId;
    private ProActiveSecurityManager securityManager;
    private String virtualNodeName;
    private Properties localProperties;
    private RemoteObjectExposer roe;

    public LocalNode(String nodeName, String jobId,
        ProActiveSecurityManager securityManager, String virtualNodeName) {
        this.name = nodeName;
        this.jobId = jobId;
        this.securityManager = securityManager;
        this.virtualNodeName = virtualNodeName;
        this.activeObjectsId = new ArrayList<UniqueID>();
        this.localProperties = new Properties();

        if (this.securityManager != null) {
            ProActiveLogger.getLogger(Loggers.SECURITY_RUNTIME)
                           .debug("Local Node : " + this.name + " VN name : " +
                this.virtualNodeName + " policyserver for app :" +
                this.securityManager.getPolicyServer().getApplicationName());

            // setting virtual node name
            this.securityManager.setVNName(this.virtualNodeName);

            ProActiveLogger.getLogger(Loggers.SECURITY_RUNTIME)
                           .debug("registering node certificate for VN " +
                this.virtualNodeName);
        }

        roe = new RemoteObjectExposer("org.objectweb.proactive.core.runtime.ProActiveRuntime",
                ProActiveRuntimeImpl.getProActiveRuntime());
    }

    public void activateProtocol(URI nodeURL) {
        roe.activateProtocol(nodeURL);
    }

    /**
     * @return Returns the active objects located inside the node.
     */
    public ArrayList<UniqueID> getActiveObjectsId() {
        return activeObjectsId;
    }

    /**
     * set the list of active objects contained by the node
     * @param activeObjects active objects to set.
     */
    public void setActiveObjects(ArrayList<UniqueID> activeObjects) {
        this.activeObjectsId = activeObjects;
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

    public void terminateActiveObjects() {
    }

    /**
     * Returns all active objects.
     * Returns All active objects.
     */
    public List<List<Object>> getActiveObjects() {
        return this.getActiveObjects(new DefaultFilter());
    }

    /**
     * Retuns all active objects filtered.
     * @param filter The filter
     * @return all active objects filtered.
     */
    public List<List<Object>> getActiveObjects(Filter filter) {
        List<List<Object>> localBodies = new ArrayList<List<Object>>();
        LocalBodyStore localBodystore = LocalBodyStore.getInstance();

        if (activeObjectsId == null) {
            // Probably the node is killed
            return localBodies;
        }

        synchronized (activeObjectsId) {
            for (int i = 0; i < activeObjectsId.size(); i++) {
                UniqueID bodyID = (UniqueID) activeObjectsId.get(i);

                //check if the body is still on this vm
                Body body = localBodystore.getLocalBody(bodyID);

                if (body == null) {
                    //runtimeLogger.warn("body null");
                    // the body with the given ID is not any more on this ProActiveRuntime
                    // unregister it from this ProActiveRuntime
                    activeObjectsId.remove(bodyID);
                } else {
                    if (filter.filter(body)) {
                        //the body is on this runtime then return adapter and class name of the reified
                        //object to enable the construction of stub-proxy couple.
                        ArrayList bodyAndObjectClass = new ArrayList(2);

                        //adapter
                        bodyAndObjectClass.add(0, body.getRemoteAdapter());

                        //className
                        bodyAndObjectClass.add(1,
                            body.getReifiedObject().getClass().getName());
                        localBodies.add(bodyAndObjectClass);
                    }
                }
            }
        }
        return localBodies;
    }

    /**
     * Unregisters the specified <code>UniqueID</code> from the node
     * @param bodyID The <code>UniqueID</code> to remove
     */
    public void unregisterBody(UniqueID bodyID) {
        activeObjectsId.remove(bodyID);
    }

    /**
     * Registers the specified body in the node. In fact it is the <code>UniqueID</code>
     * of the body that is attached to the node.
     * @param bodyID The body to register
     */
    public void registerBody(UniqueID bodyID) {
        activeObjectsId.add(bodyID);
    }

    public void terminate() {
        ArrayList activeObjects = this.getActiveObjectsId();

        for (int i = 0; i < activeObjects.size(); i++) {
            UniqueID bodyID = (UniqueID) activeObjects.get(i);

            //check if the body is still on this vm
            Body body = LocalBodyStore.getInstance().getLocalBody(bodyID);

            if (body != null) {
                ProActiveLogger.getLogger(Loggers.NODE)
                               .info("node " + this.name +
                    " is being killed, terminating body " + bodyID);
                body.terminate();
            }
        }

        roe.unregisterAll();
    }

    /**
     * Put the specified key value in this property list.
     * @param key the key to be placed into this property list.
     * @param value the value corresponding to key.
     * @return the previous value of the specified key in this property list,
     * or <code>null</code> if it did not have one.
     */
    public Object setProperty(String key, String value) {
        return this.localProperties.setProperty(key, value);
    }

    /**
     * Searches for the property with the specified key in this property list.
     * The method returns <code>null</code> if the property is not found.
     * @param key the hashtable key.
     * @return the value in this property list with the specified key value.
     */
    public String getProperty(String key) {
        return this.localProperties.getProperty(key);
    }
}
