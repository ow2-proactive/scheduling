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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.runtime;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.security.AccessControlException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.Job;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.filter.DefaultFilter;
import org.objectweb.proactive.core.filter.Filter;
import org.objectweb.proactive.core.jmx.mbean.NodeWrapper;
import org.objectweb.proactive.core.jmx.mbean.NodeWrapperMBean;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.SecurityEntity;
import org.objectweb.proactive.core.security.TypedCertificate;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.crypto.SessionException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entities;
import org.objectweb.proactive.core.security.securityentity.Entity;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * For internal use only.
 * This class is a runtime representation of a node
 * and should not be used outside a runtime
 */
public class LocalNode implements SecurityEntity {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.JMX_MBEAN);
    private String name;
    private List<UniqueID> activeObjectsId;
    private String jobId;
    private ProActiveSecurityManager securityManager;
    private String virtualNodeName;
    private Properties localProperties;
    private RemoteObjectExposer<ProActiveRuntime> runtimeRoe;

    // JMX MBean
    private NodeWrapperMBean mbean;

    public LocalNode(String nodeName, String jobId, ProActiveSecurityManager securityManager,
            String virtualNodeName) {
        this.name = nodeName;
        this.jobId = ((jobId != null) ? jobId : Job.DEFAULT_JOBID);
        this.securityManager = securityManager;
        this.virtualNodeName = virtualNodeName;
        this.activeObjectsId = new ArrayList<UniqueID>();
        this.localProperties = new Properties();

        if (this.securityManager != null) {
            ProActiveLogger.getLogger(Loggers.SECURITY_RUNTIME).debug(
                    "Local Node : " + this.name + " VN name : " + this.virtualNodeName +
                        " policyserver for app :" + this.securityManager.getApplicationName());

            // setting virtual node name
            //            this.securityManager.setVNName(this.virtualNodeName);
            ProActiveLogger.getLogger(Loggers.SECURITY_RUNTIME).debug(
                    "registering node certificate for VN " + this.virtualNodeName);
        }

        this.runtimeRoe = new RemoteObjectExposer<ProActiveRuntime>(
            "org.objectweb.proactive.core.runtime.ProActiveRuntime", ProActiveRuntimeImpl
                    .getProActiveRuntime(), ProActiveRuntimeRemoteObjectAdapter.class);

        // JMX registration
        //        if (PAProperties.PA_JMX_MBEAN.isTrue()) {
        String runtimeUrl = ProActiveRuntimeImpl.getProActiveRuntime().getURL();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName oname = FactoryName.createNodeObjectName(runtimeUrl, nodeName);
        if (!mbs.isRegistered(oname)) {
            mbean = new NodeWrapper(oname, this, runtimeUrl);
            try {
                mbs.registerMBean(mbean, oname);
            } catch (InstanceAlreadyExistsException e) {
                logger.error("A MBean with the object name " + oname + " already exists", e);
            } catch (MBeanRegistrationException e) {
                logger.error("Can't register the MBean of the LocalNode", e);
            } catch (NotCompliantMBeanException e) {
                logger.error("The MBean of the LocalNode is not JMX compliant", e);
            }
        }

        //        }

        // END JMX registration
    }

    public void activateProtocol(URI nodeURL) throws UnknownProtocolException {
        this.runtimeRoe.activateProtocol(nodeURL);
    }

    /**
     * @return Returns the active objects located inside the node.
     */
    public List<UniqueID> getActiveObjectsId() {
        return this.activeObjectsId;
    }

    /**
     * set the list of active objects contained by the node
     * @param activeObjects active objects to set.
     */
    public void setActiveObjects(List<UniqueID> activeObjects) {
        this.activeObjectsId = activeObjects;
    }

    /**
     * @return Returns the jobId.
     */
    public String getJobId() {
        return this.jobId;
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
        return this.name;
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
        return this.securityManager;
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
        return this.virtualNodeName;
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
    public List<UniversalBody> getActiveObjects() {
        return this.getActiveObjects(new DefaultFilter());
    }

    /**
     * Retuns all active objects filtered.
     * @param filter The filter
     * @return all active objects filtered.
     */
    public List<UniversalBody> getActiveObjects(Filter filter) {
        List<UniversalBody> localBodies = new ArrayList<UniversalBody>();
        LocalBodyStore localBodystore = LocalBodyStore.getInstance();

        if (this.activeObjectsId == null) {
            // Probably the node is killed
            return localBodies;
        }

        synchronized (this.activeObjectsId) {
            for (int i = 0; i < this.activeObjectsId.size(); i++) {
                UniqueID bodyID = this.activeObjectsId.get(i);

                //check if the body is still on this vm
                Body body = localBodystore.getLocalBody(bodyID);

                if (body == null) {
                    //runtimeLogger.warn("body null");
                    // the body with the given ID is not any more on this ProActiveRuntime
                    // unregister it from this ProActiveRuntime
                    this.activeObjectsId.remove(bodyID);
                } else {
                    if (filter.filter(body)) {
                        //the body is on this runtime then return the remote reference of the active object
                        localBodies.add(body.getRemoteAdapter());
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
        this.activeObjectsId.remove(bodyID);
    }

    /**
     * Registers the specified body in the node. In fact it is the <code>UniqueID</code>
     * of the body that is attached to the node.
     * @param bodyID The body to register
     */
    public void registerBody(UniqueID bodyID) {
        this.activeObjectsId.add(bodyID);
    }

    public void terminate() {
        List<UniqueID> activeObjects = this.getActiveObjectsId();

        for (int i = 0; i < activeObjects.size(); i++) {
            UniqueID bodyID = activeObjects.get(i);

            //check if the body is still on this vm
            Body body = LocalBodyStore.getInstance().getLocalBody(bodyID);

            if (body != null) {
                ProActiveLogger.getLogger(Loggers.NODE).info(
                        "node " + this.name + " is being killed, terminating body " + bodyID);
                body.terminate();
            }
        }

        this.runtimeRoe.unregisterAll();

        // JMX Notification
        ProActiveRuntimeWrapperMBean runtimeMBean = ProActiveRuntimeImpl.getProActiveRuntime().getMBean();
        if ((runtimeMBean != null) && (this.mbean != null)) {
            runtimeMBean.sendNotification(NotificationType.nodeDestroyed, this.mbean.getURL());
        }

        // END JMX Notification

        // JMX unregistration
        if (mbean != null) {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = this.mbean.getObjectName();
            if (mbs.isRegistered(objectName)) {
                try {
                    mbs.unregisterMBean(objectName);
                } catch (InstanceNotFoundException e) {
                    logger.error("The MBean with the objectName " + objectName + " was not found", e);
                } catch (MBeanRegistrationException e) {
                    logger.error("The MBean with the objectName " + objectName +
                        " can't be unregistered from the MBean server", e);
                }
            }
            this.mbean = null;
        }

        // END JMX unregistration
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

    // Implements Security Entity
    public TypedCertificate getCertificate() throws SecurityNotAvailableException {
        if (this.securityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return this.securityManager.getCertificate();
    }

    //	public byte[] getCertificateEncoded() throws SecurityNotAvailableException {
    //		if (this.securityManager == null) {
    //			throw new SecurityNotAvailableException();
    //		}
    //		return this.securityManager.getCertificateEncoded();
    //	}
    public Entities getEntities() throws SecurityNotAvailableException {
        if (this.securityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return this.securityManager.getEntities();
    }

    public SecurityContext getPolicy(Entities local, Entities distant) throws SecurityNotAvailableException {
        if (this.securityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return this.securityManager.getPolicy(local, distant);
    }

    public ProActiveSecurityManager getProActiveSecurityManager(Entity user)
            throws SecurityNotAvailableException, AccessControlException {
        if (this.securityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return this.securityManager.getProActiveSecurityManager(user);
    }

    public PublicKey getPublicKey() throws SecurityNotAvailableException {
        if (this.securityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return this.securityManager.getPublicKey();
    }

    public byte[] publicKeyExchange(long sessionID, byte[] signature) throws SecurityNotAvailableException,
            RenegotiateSessionException, KeyExchangeException {
        if (this.securityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return this.securityManager.publicKeyExchange(sessionID, signature);
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue) throws SecurityNotAvailableException,
            RenegotiateSessionException {
        if (this.securityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return this.securityManager.randomValue(sessionID, clientRandomValue);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey, byte[] encodedIVParameters,
            byte[] encodedClientMacKey, byte[] encodedLockData, byte[] parametersSignature)
            throws SecurityNotAvailableException {
        if (this.securityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return this.securityManager.secretKeyExchange(sessionID, encodedAESKey, encodedIVParameters,
                encodedClientMacKey, encodedLockData, parametersSignature);
    }

    public void setProActiveSecurityManager(Entity user, PolicyServer policyServer)
            throws SecurityNotAvailableException, AccessControlException {
        if (this.securityManager == null) {
            throw new SecurityNotAvailableException();
        }
        this.securityManager.setProActiveSecurityManager(user, policyServer);
    }

    public long startNewSession(long distantSessionID, SecurityContext policy,
            TypedCertificate distantCertificate) throws SecurityNotAvailableException, SessionException {
        if (this.securityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return this.securityManager.startNewSession(distantSessionID, policy, distantCertificate);
    }

    public void terminateSession(long sessionID) throws SecurityNotAvailableException {
        if (this.securityManager == null) {
            throw new SecurityNotAvailableException();
        }
        this.securityManager.terminateSession(sessionID);
    }
}
