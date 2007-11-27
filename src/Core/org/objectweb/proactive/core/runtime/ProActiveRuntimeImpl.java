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

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.security.AccessControlException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.ActiveBody;
import org.objectweb.proactive.core.body.Context;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.descriptor.util.RefactorPAD;
import org.objectweb.proactive.core.event.RuntimeRegistrationEvent;
import org.objectweb.proactive.core.event.RuntimeRegistrationEventProducerImpl;
import org.objectweb.proactive.core.gc.GarbageCollector;
import org.objectweb.proactive.core.jmx.mbean.JMXClassLoader;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapper;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.notification.RuntimeNotificationData;
import org.objectweb.proactive.core.jmx.server.ServerConnector;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.mop.JavassistByteCodeStubBuilder;
import org.objectweb.proactive.core.mop.MOPClassLoader;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.rmi.FileProcess;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurity;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.SecurityEntity;
import org.objectweb.proactive.core.security.TypedCertificate;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.crypto.SessionException;
import org.objectweb.proactive.core.security.domain.SecurityDomain;
import org.objectweb.proactive.core.security.exceptions.InvalidPolicyFile;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entities;
import org.objectweb.proactive.core.security.securityentity.Entity;
import org.objectweb.proactive.core.util.ClassDataCache;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>
 * Implementation of ProActiveRuntime
 * </p>
 *
 * @author ProActive Team
 * @version 1.0, 2001/10/23
 * @since ProActive 0.91
 *
 */
public class ProActiveRuntimeImpl extends RuntimeRegistrationEventProducerImpl
    implements ProActiveRuntime, LocalProActiveRuntime,
        ProActiveRuntimeImplMBean {

    /**
         *
         */
    private static final long serialVersionUID = 1L;

    //
    // -- STATIC MEMBERS
    // -----------------------------------------------------------
    //
    // the Unique instance of ProActiveRuntime
    private static ProActiveRuntimeImpl proActiveRuntime;

    // JMX
    private static Logger jmxLogger = ProActiveLogger.getLogger(Loggers.JMX);

    static {
        try {
            proActiveRuntime = new ProActiveRuntimeImpl();
            proActiveRuntime.createMBean();
        } catch (UnknownProtocolException e) {
            e.printStackTrace();
        }
    }

    // runtime security manager
    private static ProActiveSecurityManager runtimeSecurityManager;

    // map of local nodes, key is node name
    private java.util.Hashtable<String, LocalNode> nodeMap;

    //
    // -- PRIVATE MEMBERS
    // -----------------------------------------------------------
    //
    private VMInformationImpl vmInformation;

    // map VirtualNodes and their names
    private java.util.Hashtable<String, VirtualNodeInternal> virtualNodesMap;

    // map descriptor and their url
    private java.util.Hashtable<String, ProActiveDescriptorInternal> descriptorMap;

    // map proActiveRuntime registered on this VM and their names
    private java.util.Hashtable<String, ProActiveRuntime> proActiveRuntimeMap;

    // synchronized set of URL to runtimes in which we are registered
    private java.util.Set<String> runtimeAcquaintancesURL;
    private ProActiveRuntime parentRuntime;
    protected RemoteObjectExposer roe;

    // JMX
    /** The Server Connector to connect remotly to the JMX server */
    private ServerConnector serverConnector;
    private Object mutex = new Object();

    /** The MBean representing this ProActive Runtime */
    private ProActiveRuntimeWrapperMBean mbean;

    //
    // -- CONSTRUCTORS
    // -----------------------------------------------------------
    //
    // singleton
    protected ProActiveRuntimeImpl() throws UnknownProtocolException {
        try {
            this.vmInformation = new VMInformationImpl();
            this.proActiveRuntimeMap = new java.util.Hashtable<String, ProActiveRuntime>();
            this.runtimeAcquaintancesURL = java.util.Collections.synchronizedSortedSet(new java.util.TreeSet<String>());
            this.virtualNodesMap = new java.util.Hashtable<String, VirtualNodeInternal>();
            this.descriptorMap = new java.util.Hashtable<String, ProActiveDescriptorInternal>();
            this.nodeMap = new java.util.Hashtable<String, LocalNode>();

            try {
                String file = PAProperties.PA_RUNTIME_SECURITY.getValue();
                ProActiveSecurity.loadProvider();

                if ((file != null) && new File(file).exists()) {
                    // loading security from a file
                    ProActiveRuntimeImpl.runtimeSecurityManager = new ProActiveSecurityManager(EntityType.RUNTIME,
                            file);
                    ProActiveLogger.getLogger(Loggers.SECURITY_RUNTIME)
                                   .info("ProActive Security Policy (proactive.runtime.security) using " +
                        file);

                    runtimeSecurityManager = runtimeSecurityManager.generateSiblingCertificate(EntityType.RUNTIME,
                            this.getVMInformation().getName());

                    // Is the runtime included within a Domain ?
                    String domainURL = PAProperties.PA_RUNTIME_DOMAIN_URL.getValue();

                    if (domainURL != null) {
                        SecurityEntity domain = (SecurityDomain) ProActiveObject.lookupActive("org.objectweb.proactive.ext.security.domain.SecurityDomain",
                                domainURL);
                        ProActiveRuntimeImpl.runtimeSecurityManager.setParent(domain);
                    }
                } else {
                    ProActiveLogger.getLogger(Loggers.SECURITY_RUNTIME)
                                   .debug("ProActive Security Policy (proactive.runtime.security) not set. Runtime Security disabled ");
                }
            } catch (InvalidPolicyFile e) {
                e.printStackTrace();
            } catch (ActiveObjectCreationException e) {
                e.printStackTrace();
            }

            //System.out.println(vmInformation.getVMID().toString());
        } catch (UnknownHostException e) {
            //System.out.println();
            logger.fatal(" !!! Cannot do a reverse lookup on that host");

            // System.out.println();
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remote Object exporter
        this.roe = new RemoteObjectExposer("org.objectweb.proactive.core.runtime.ProActiveRuntime",
                this);

        String url = URIBuilder.buildURIFromProperties(URIBuilder.getHostNameFromUrl(
                    getInternalURL()),
                URIBuilder.getNameFromURI(getInternalURL())).toString();

        this.roe.activateProtocol(URI.create(url));

        // logging info
        MDC.remove("runtime");
        MDC.put("runtime", getURL());
    }

    //
    // -- PUBLIC METHODS
    // -----------------------------------------------------------
    //
    public static ProActiveRuntimeImpl getProActiveRuntime() {
        return proActiveRuntime;
    }

    /**
     * If no ServerConnector has been created, a new one is created and started.
     * Any ProActive JMX Connector Client can connect to it remotely and manage
     * the MBeans.
     *
     * @return the ServerConnector associated to this ProActiveRuntime
     */
    public void startJMXServerConnector() {
        synchronized (mutex) {
            if (serverConnector == null) {
                createServerConnector();
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getMBean()
     */
    public ProActiveRuntimeWrapperMBean getMBean() {
        return mbean;
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getMBeanServerName()
     */
    public String getMBeanServerName() {
        return URIBuilder.getNameFromURI(proActiveRuntime.getURL());
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getJMXServerConnector()
     */
    public ServerConnector getJMXServerConnector() {
        return serverConnector;
    }

    //
    // -- Implements LocalProActiveRuntime
    // -----------------------------------------------
    //

    /**
     * @see org.objectweb.proactive.core.runtime.LocalProActiveRuntime#registerLocalVirtualNode(VirtualNodeInternal
     *      vn, String vnName)
     */
    public void registerLocalVirtualNode(VirtualNodeInternal vn, String vnName) {
        // System.out.println("vn "+vnName+" registered");
        this.virtualNodesMap.put(vnName, vn);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.LocalProActiveRuntime#setParent(ProActiveRuntime
     *      parentPARuntime)
     */
    public void setParent(ProActiveRuntime parentPARuntime) {
        if (this.parentRuntime == null) {
            this.parentRuntime = parentPARuntime;
            this.runtimeAcquaintancesURL.add(parentPARuntime.getURL());
        } else {
            runtimeLogger.error("Parent runtime already set!");
        }
    }

    public void registerDescriptor(String url, ProActiveDescriptorInternal pad) {
        this.descriptorMap.put(url, pad);
    }

    public ProActiveDescriptorInternal getDescriptor(String url,
        boolean isHierarchicalSearch) throws IOException, ProActiveException {
        ProActiveDescriptorInternal pad = this.descriptorMap.get(url);

        // hierarchical search or not, look if we know the pad
        if (pad != null) {
            // if pad found and hierarchy search return pad with no main
            if (isHierarchicalSearch) {
                return RefactorPAD.buildNoMainPAD(pad);
            } else {
                // if not hierarchy search, return the normal pad
                return pad;
            }
        } else if (!isHierarchicalSearch) {
            return null; // pad == null
        } else { // else search pad in parent runtime
            if (this.parentRuntime == null) {
                throw new IOException(
                    "Descriptor cannot be found hierarchically since this runtime has no parent");
            }

            return this.parentRuntime.getDescriptor(url, true);
        }
    }

    public void removeDescriptor(String url) {
        this.descriptorMap.remove(url);
    }

    /**
     * Creates a Server Connector
     */
    private void createServerConnector() {
        // One the Serverconnector is launched any ProActive JMX Connector
        // client can connect to it remotely and manage the MBeans.
        serverConnector = new ServerConnector(URIBuilder.getNameFromURI(
                    proActiveRuntime.getURL()));
        try {
            serverConnector.start();
        } catch (IOException e) {
            jmxLogger.error("Can't start the JMX Connector in the ProActive Runtime",
                e);
        }
    }

    /**
     * Creates the MBean associated to the ProActiveRuntime
     */
    protected void createMBean() {
        // JMX registration
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        JMXClassLoader jmxClassLoader = new JMXClassLoader(classLoader);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = null;
        try {
            objectName = new ObjectName(
                    "org.objectweb.proactive:type=JMXClassLoader");
        } catch (MalformedObjectNameException e) {
            jmxLogger.error("Can't create the objectName of the JMX ClassLoader MBean",
                e);
        } catch (NullPointerException e) {
            jmxLogger.error("Can't create the objectName of the JMX ClassLoader MBean",
                e);
        }
        try {
            mbs.registerMBean(jmxClassLoader, objectName);
        } catch (InstanceAlreadyExistsException e) {
            jmxLogger.error("A MBean with the object name " + objectName +
                " already exists", e);
        } catch (MBeanRegistrationException e) {
            jmxLogger.error("Can't register the MBean of the JMX ClassLoader", e);
        } catch (NotCompliantMBeanException e) {
            jmxLogger.error("The MBean of the JMX ClassLoader is not JMX compliant",
                e);
        }

        String runtimeUrl = proActiveRuntime.getURL();
        objectName = FactoryName.createRuntimeObjectName(runtimeUrl);
        if (!mbs.isRegistered(objectName)) {
            mbean = new ProActiveRuntimeWrapper(proActiveRuntime);
            try {
                mbs.registerMBean(mbean, objectName);
            } catch (InstanceAlreadyExistsException e) {
                jmxLogger.error("A MBean with the object name " + objectName +
                    " already exists", e);
            } catch (MBeanRegistrationException e) {
                jmxLogger.error("Can't register the MBean of the ProActive Runtime",
                    e);
            } catch (NotCompliantMBeanException e) {
                jmxLogger.error("The MBean of the ProActive Runtime is not JMX compliant",
                    e);
            }
        }
    }

    //
    // -- Implements ProActiveRuntime
    // -----------------------------------------------
    //

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createLocalNode(String,
     *      boolean, ProActiveSecurityManager, String, String)
     */
    public String createLocalNode(String nodeURL,
        boolean replacePreviousBinding,
        ProActiveSecurityManager nodeSecurityManager, String vnName,
        String jobId) throws NodeException, AlreadyBoundException {
        // check if nodeName is an URI or not
        String nodeName = URIBuilder.getNameFromURI(nodeURL);

        if (!replacePreviousBinding && (this.nodeMap.get(nodeName) != null)) {
            throw new AlreadyBoundException("Node " + nodeName +
                " already created on this ProActiveRuntime. To overwrite this node, use true for replacePreviousBinding");
        }

        if (nodeSecurityManager != null) {
            // setting the current runtime as parent entity of the node
            nodeSecurityManager.setParent(this);
        }

        LocalNode newNode = new LocalNode(nodeName, jobId, nodeSecurityManager,
                vnName);

        URI realnodeURL = URI.create(nodeURL);
        if (!realnodeURL.isAbsolute()) {
            try {
                realnodeURL = RemoteObjectHelper.generateUrl(PAProperties.PA_COMMUNICATION_PROTOCOL.getValue(),
                        nodeName);
            } catch (UnknownProtocolException e) {
                throw new NodeException(e);
            }
        }
        try {
            newNode.activateProtocol(realnodeURL);
        } catch (UnknownProtocolException e) {
            throw new NodeException(e);
        }

        if (replacePreviousBinding && (this.nodeMap.get(nodeName) != null)) {
            newNode.setActiveObjects(this.nodeMap.get(nodeName)
                                                 .getActiveObjectsId());
            this.nodeMap.remove(nodeName);
        }

        this.nodeMap.put(nodeName, newNode);

        return realnodeURL.toString();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killAllNodes()
     */
    public void killAllNodes() {
        for (Enumeration<String> e = this.nodeMap.keys(); e.hasMoreElements();) {
            String nodeName = e.nextElement();
            killNode(nodeName);
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killNode(String)
     */
    public void killNode(String nodeName) {
        LocalNode localNode = this.nodeMap.get(nodeName);
        if (localNode != null) {
            localNode.terminate();
        }
        this.nodeMap.remove(nodeName);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createVM(UniversalProcess)
     */
    public void createVM(UniversalProcess remoteProcess)
        throws java.io.IOException {
        remoteProcess.startProcess();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getLocalNodeNames()
     */
    public String[] getLocalNodeNames() {
        int i = 0;
        String[] nodeNames;

        synchronized (this.nodeMap) {
            nodeNames = new String[this.nodeMap.size()];

            for (java.util.Enumeration<String> e = this.nodeMap.keys();
                    e.hasMoreElements();) {
                nodeNames[i] = e.nextElement();
                i++;
            }
        }

        return nodeNames;
    }

    public Collection<LocalNode> getLocalNodes() {
        return this.nodeMap.values();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getVMInformation()
     */
    public VMInformation getVMInformation() {
        return this.vmInformation;
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#register(ProActiveRuntime,
     *      String, String, String, String)
     */
    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) {
        // System.out.println("register in Impl");
        // System.out.println("thread"+Thread.currentThread().getName());
        // System.out.println(vmInformation.getVMID().toString());
        this.proActiveRuntimeMap.put(proActiveRuntimeName, proActiveRuntimeDist);

        // ProActiveEvent
        notifyListeners(this, RuntimeRegistrationEvent.RUNTIME_REGISTERED,
            proActiveRuntimeDist, creatorID, creationProtocol, vmName);
        // END ProActiveEvent

        // JMX Notification
        if (getMBean() != null) {
            RuntimeNotificationData notificationData = new RuntimeNotificationData(creatorID,
                    proActiveRuntimeDist.getURL(), creationProtocol, vmName);
            getMBean()
                .sendNotification(NotificationType.runtimeRegistered,
                notificationData);
        }

        // END JMX Notification
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#unregister(org.objectweb.proactive.core.runtime.ProActiveRuntime,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public void unregister(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeUrl, String creatorID, String creationProtocol,
        String vmName) {
        this.proActiveRuntimeMap.remove(proActiveRuntimeUrl);

        // ProActiveEvent
        notifyListeners(this, RuntimeRegistrationEvent.RUNTIME_UNREGISTERED,
            proActiveRuntimeDist, creatorID, creationProtocol, vmName);
        // END ProActiveEvent

        // JMX Notification
        if (getMBean() != null) {
            RuntimeNotificationData notificationData = new RuntimeNotificationData(creatorID,
                    proActiveRuntimeDist.getURL(), creationProtocol, vmName);
            getMBean()
                .sendNotification(NotificationType.runtimeUnregistered,
                notificationData);
        }

        // END JMX Notification
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getProActiveRuntimes()
     */
    public ProActiveRuntime[] getProActiveRuntimes() {
        int i = 0;
        ProActiveRuntime[] runtimeArray;

        synchronized (this.proActiveRuntimeMap) {
            runtimeArray = new ProActiveRuntime[this.proActiveRuntimeMap.size()];

            for (java.util.Enumeration<ProActiveRuntime> e = this.proActiveRuntimeMap.elements();
                    e.hasMoreElements();) {
                runtimeArray[i] = e.nextElement();
                i++;
            }
        }

        return runtimeArray;
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getProActiveRuntime(String)
     */
    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName) {
        return this.proActiveRuntimeMap.get(proActiveRuntimeName);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#addAcquaintance(String)
     */
    public void addAcquaintance(String proActiveRuntimeName) {
        this.runtimeAcquaintancesURL.add(proActiveRuntimeName);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getAcquaintances()
     */
    public String[] getAcquaintances() {
        String[] urls;

        synchronized (this.runtimeAcquaintancesURL) {
            urls = new String[this.runtimeAcquaintancesURL.size()];

            java.util.Iterator<String> iter = this.runtimeAcquaintancesURL.iterator();

            for (int i = 0; i < urls.length; i++)
                urls[i] = iter.next();
        }

        return urls;
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#rmAcquaintance(java.lang.String)
     */
    public void rmAcquaintance(String proActiveRuntimeName) {
        this.runtimeAcquaintancesURL.remove(proActiveRuntimeName);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killRT(boolean)
     */
    public void killRT(boolean softly) {
        // JMX Notification
        if (getMBean() != null) {
            getMBean().sendNotification(NotificationType.runtimeDestroyed);
        }

        // END JMX Notification
        killAllNodes();

        logger.info("terminating Runtime " + getInternalURL());

        // JMX unregistration
        if (getMBean() != null) {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = getMBean().getObjectName();
            if (mbs.isRegistered(objectName)) {
                try {
                    mbs.unregisterMBean(objectName);
                } catch (InstanceNotFoundException e) {
                    jmxLogger.error("The MBean with the objectName " +
                        objectName + " was not found", e);
                } catch (MBeanRegistrationException e) {
                    jmxLogger.error("The MBean with the objectName " +
                        objectName +
                        " can't be unregistered from the MBean server", e);
                }
            }
            mbean = null;
        }
        // END JMX unregistration
        System.exit(0);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getURL()
     */
    protected String getInternalURL() {
        return URIBuilder.buildURI(URIBuilder.getHostNameorIP(
                this.vmInformation.getInetAddress()),
            this.vmInformation.getName()).toString();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getURL()
     */
    public String getURL() {
        return this.roe.getURL();
    }

    public List<UniversalBody> getActiveObjects(String nodeName) {
        // the array to return
        List<UniversalBody> localBodies = new ArrayList<UniversalBody>();
        LocalBodyStore localBodystore = LocalBodyStore.getInstance();
        List<UniqueID> bodyList = this.nodeMap.get(nodeName).getActiveObjectsId();

        if (bodyList == null) {
            // Probably the node is killed
            return localBodies;
        }

        synchronized (bodyList) {
            for (int i = 0; i < bodyList.size(); i++) {
                UniqueID bodyID = bodyList.get(i);

                // check if the body is still on this vm
                Body body = localBodystore.getLocalBody(bodyID);

                if (body == null) {
                    // runtimeLogger.warn("body null");
                    // the body with the given ID is not any more on this
                    // ProActiveRuntime
                    // unregister it from this ProActiveRuntime
                    unregisterBody(nodeName, bodyID);
                } else {
                    // the body is on this runtime then return adapter and class
                    // name of the reified
                    // object to enable the construction of stub-proxy couple.
                    localBodies.add(0, body.getRemoteAdapter());
                }
            }

            return localBodies;
        }
    }

    public VirtualNodeInternal getVirtualNode(String virtualNodeName) {
        // System.out.println("i am in get vn ");
        return this.virtualNodesMap.get(virtualNodeName);
    }

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws UnknownProtocolException {
        String url;
        url = URIBuilder.buildURIFromProperties(URIBuilder.getHostNameFromUrl(
                    getInternalURL()), virtualNodeName).toString();
        this.roe.activateProtocol(URI.create(url));
    }

    public void unregisterVirtualNode(String virtualNodeName) {
        VirtualNodeInternal vn = virtualNodesMap.get(virtualNodeName);
        if (vn != null) {
            JMXNotificationManager.getInstance()
                                  .unsubscribe(getMBean().getObjectName(), vn);
        }
        virtualNodesMap.remove(virtualNodeName);
    }

    public void unregisterAllVirtualNodes() {
        this.virtualNodesMap.clear();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getJobID(java.lang.String)
     */
    public String getJobID(String nodeUrl) {
        String name = URIBuilder.getNameFromURI(nodeUrl);
        LocalNode localNode = this.nodeMap.get(name);
        return localNode.getJobId();
    }

    public List<UniversalBody> getActiveObjects(String nodeName,
        String className) {
        // the array to return
        ArrayList<UniversalBody> localBodies = new ArrayList<UniversalBody>();
        LocalBodyStore localBodystore = LocalBodyStore.getInstance();
        List<UniqueID> bodyList = this.nodeMap.get(nodeName).getActiveObjectsId();

        if (bodyList == null) {
            // Probably the node is killed
            return localBodies;
        }

        synchronized (bodyList) {
            for (int i = 0; i < bodyList.size(); i++) {
                UniqueID bodyID = bodyList.get(i);

                // check if the body is still on this vm
                Body body = localBodystore.getLocalBody(bodyID);

                if (body == null) {
                    // runtimeLogger.warn("body null");
                    // the body with the given ID is not any more on this
                    // ProActiveRuntime
                    // unregister it from this ProActiveRuntime
                    unregisterBody(nodeName, bodyID);
                } else {
                    String objectClass = body.getReifiedObject().getClass()
                                             .getName();

                    // if the reified object is of the specified type
                    // return the body adapter
                    if (objectClass.equals(className)) {
                        localBodies.add(body.getRemoteAdapter());
                    }
                }
            }

            return localBodies;
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createBody(String,
     *      ConstructorCall, boolean)
     */
    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isLocal)
        throws ConstructorCallExecutionFailedException,
            java.lang.reflect.InvocationTargetException {
        Body localBody = (Body) bodyConstructorCall.execute();

        // SECURITY
        ProActiveSecurityManager objectSecurityManager = ((AbstractBody) localBody).getProActiveSecurityManager();

        if (objectSecurityManager != null) {
            objectSecurityManager.setParent(this.nodeMap.get(nodeName));
        }

        ProActiveLogger.getLogger(Loggers.RUNTIME).debug("nodeName " +
            nodeName);
        registerBody(nodeName, localBody);

        if (GarbageCollector.dgcIsEnabled()) {
            ((AbstractBody) localBody).updateReferences(UniversalBodyProxy.getIncomingReferences());
        }

        if (isLocal) {
            // if the body and proxy are on the same vm, returns the local view
            // System.out.println("body and proxy on the same vm");
            // System.out.println(localBody.getReifiedObject().getClass().getName());
            // register the body in the nodemap
            return localBody;
        } else {
            // otherwise return the adapter
            // System.out.println ("RemoteProActiveImpl.createBody
            // "+vmInformation.getInetAddress().getHostName() +" -> new
            // "+bodyConstructorCall.getTargetClassName()+" on node "+nodeName);
            // System.out.println ("RemoteProActiveRuntimeImpl.localBody created
            // localBody="+localBody+" on node "+nodeName);
            return localBody.getRemoteAdapter();
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#receiveBody(String,
     *      Body)
     */
    public UniversalBody receiveBody(String nodeName, Body body) {
        ProActiveSecurityManager psm = ((AbstractBody) body).getProActiveSecurityManager();
        if (psm != null) {
            psm.setParent(this.nodeMap.get(nodeName));
        }

        registerBody(nodeName, body);

        // register futures that have been deserialized in the body
        ((AbstractBody) body).registerIncomingFutures();

        return body.getRemoteAdapter();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#receiveCheckpoint(String,
     *      Checkpoint, int)
     */
    public UniversalBody receiveCheckpoint(String nodeURL, Checkpoint ckpt,
        int inc) throws ProActiveException {
        runtimeLogger.debug("Receive a checkpoint for recovery");

        // the recovered body
        Body ret = ckpt.recover();

        // update node url
        ret.updateNodeURL(nodeURL);

        String nodeName = URIBuilder.getNameFromURI(nodeURL);

        // push the initial context for the current thread.
        // need to register as thread of the corresponding active object: this
        // thread
        // may send looged requests or logged replies
        LocalBodyStore.getInstance().pushContext(new Context(ret, null));
        try {
            ((AbstractBody) ret).getFTManager()
             .beforeRestartAfterRecovery(ckpt.getCheckpointInfo(), inc);
        } finally {
            // remove context for the current thread
            LocalBodyStore.getInstance().popContext();
        }

        // register the body
        this.registerBody(nodeName, ret);

        // register futures that have been deserialized in the body
        ((AbstractBody) ret).registerIncomingFutures();

        // restart actvity
        if (runtimeLogger.isDebugEnabled()) {
            runtimeLogger.debug(ret.getID() + " is restarting activity...");
        }

        ((ActiveBody) ret).startBody();

        // no more need to return the recovered body
        return null;
    }

    /**
     * Registers the specified body in the node with the nodeName key. In fact
     * it is the <code>UniqueID</code> of the body that is attached to the
     * node.
     *
     * @param nodeName
     *            The name where to attached the body in the
     *            <code>hostsMap</code>
     * @param body
     *            The body to register
     */
    private void registerBody(String nodeName, Body body) {
        UniqueID bodyID = body.getID();
        List<UniqueID> bodyList = this.nodeMap.get(nodeName).getActiveObjectsId();

        synchronized (bodyList) {
            if (!bodyList.contains(bodyID)) {
                // System.out.println("in registerbody id = "+
                // bodyID.toString());
                bodyList.add(bodyID);
            }
        }
    }

    /**
     * Unregisters the specified <code>UniqueID</code> from the node
     * corresponding to the nodeName key
     *
     * @param nodeName
     *            The key where to remove the <code>UniqueID</code>
     * @param bodyID
     *            The <code>UniqueID</code> to remove
     */
    private void unregisterBody(String nodeName, UniqueID bodyID) {
        // System.out.println("in remove id= "+ bodyID.toString());
        // System.out.println("array size
        // "+((ArrayList)hostsMap.get(nodeName)).size());
        List<UniqueID> bodyList = this.nodeMap.get(nodeName).getActiveObjectsId();

        synchronized (bodyList) {
            bodyList.remove(bodyID);

            // System.out.println("array size
            // "+((ArrayList)hostsMap.get(nodeName)).size());
        }
    }

    // SECURITY

    /**
     * set the runtime security manager
     */
    public static void setProActiveSecurityManager(
        ProActiveSecurityManager server) {
        if (runtimeSecurityManager != null) {
            return;
        }

        runtimeSecurityManager = server;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getEntities(java.lang.String)
     */
    public Entities getEntities(String nodeName) {
        ProActiveSecurityManager nodeSecurityManager = this.nodeMap.get(nodeName)
                                                                   .getSecurityManager();

        Entities entities = this.getEntities();

        if (nodeSecurityManager != null) {
            entities.add(new Entity(nodeSecurityManager.getMyCertificateChain()));
        }

        return entities;
    }

    /**
     * the runtime looks for a matching security entity whithin its nodes and
     * active objects
     *
     * @param securityEntity
     *            the security entity looked for.
     * @return matching entities
     */
    public Entities getEntities(SecurityEntity securityEntity) {
        if (true) {
            throw new RuntimeException();
        }

        return null;

        // hostsMap.
        // try {
        // System.out.println(" testing for securityentityID " +
        // securityEntity);
        // for (Enumeration e = hostsMap.keys(); e.hasMoreElements();) {
        // String node = (String) e.nextElement();
        //
        // System.out.println("testing for node " + node);
        // ArrayList listAO = (ArrayList) hostsMap.get(node);
        //
        // for (int i = 0; i < listAO.size(); i++) {
        // UniqueID localBodyID = (UniqueID) listAO.get(i);
        // System.out.println(" testing against localBbodyID " +
        // localBodyID);
        //
        // if (securityEntity.getCertificate().equals(localBodyID)) {
        // ArrayList a = new ArrayList();
        //
        // ProActiveSecurityManager nodeSecurityManager =
        // (ProActiveSecurityManager) nodeSecurityManagerMap.get(node);
        // PolicyServer nodePolicyServer =
        // nodeSecurityManager.getPolicyServer();
        //
        // if (nodePolicyServer != null) {
        // EntityVirtualNode entityVirtualNode = new
        // EntityVirtualNode(nodeSecurityManager.getVNName(),
        // nodePolicyServer.getApplicationCertificate(),
        // nodeSecurityManager.getCertificate());
        // a.add(entityVirtualNode);
        // return a;
        // }
        // }
        //
        // }
        // }
        // } catch (SecurityNotAvailableException e1) {
        // e1.printStackTrace();
        // } catch (IOException e1) {
        // e1.printStackTrace();
        // }
        // return new ArrayList();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getEntities()
     */
    public Entities getEntities() {
        Entities entities = new Entities();

        if (runtimeSecurityManager != null) {
            entities.add(new Entity(
                    runtimeSecurityManager.getMyCertificateChain()));
            return entities;
        }

        return null;
    }

    /**
     * @param sc
     */
    public SecurityContext getPolicy(Entities local, Entities distant)
        throws SecurityNotAvailableException {
        if (runtimeSecurityManager == null) {
            throw new SecurityNotAvailableException();
        }

        //        PolicyServer policyServer = runtimeSecurityManager.getPolicyServer();
        return runtimeSecurityManager.getPolicy(local, distant);
    }

    public byte[] getClassDataFromParentRuntime(String className)
        throws ProActiveException {
        byte[] classData = null;

        if (this.parentRuntime != null) {
            classData = this.parentRuntime.getClassDataFromThisRuntime(className);

            if (classData == null) {
                // continue searching
                classData = this.parentRuntime.getClassDataFromParentRuntime(className);
            }

            if (classData != null) {
                // caching class
                ClassDataCache.instance().addClassData(className, classData);

                if (runtimeLogger.isDebugEnabled()) {
                    runtimeLogger.debug(getURL() + " -- > Returning class " +
                        className + " found in " + this.parentRuntime.getURL());
                }

                return classData;
            }
        }

        return null;
    }

    public synchronized byte[] getClassDataFromThisRuntime(String className)
        throws ProActiveException {
        byte[] classData = null;

        // 1. look in class cache
        // this can be redundant if not looking in a parent
        classData = ClassDataCache.instance().getClassData(className);

        // found something in classloader or cache...
        if (classData != null) {
            return classData;
        }

        try {
            // 2. look in classpath
            classData = FileProcess.getBytesFromResource(className);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        if (classData != null) {
            ClassDataCache.instance().addClassData(className, classData);

            return classData;
        }

        if (this.parentRuntime == null) {
            // top of hierarchy of runtimes
            classData = generateStub(className);

            if (classData != null) {
                return classData;
            }
        }

        return null;
    }

    public void launchMain(String className, String[] parameters)
        throws ClassNotFoundException, NoSuchMethodException, ProActiveException {
        System.out.println("ProActiveRuntimeImpl.launchMain() -" + className +
            "-");

        Class<?> mainClass = Class.forName(className);
        Method mainMethod = mainClass.getMethod("main",
                new Class[] { String[].class });
        new LauncherThread(mainMethod, parameters).start();
    }

    public void newRemote(String className)
        throws ClassNotFoundException, ProActiveException {
        Class<?> remoteClass = Class.forName(className);
        new LauncherThread(remoteClass).start();
    }

    // tries to generate a stub without using MOP methods
    public byte[] generateStub(String className) {
        byte[] classData = null;

        if (Utils.isStubClassName(className)) {
            // try {
            // do not use directly MOP methods (avoid classloader cycles)
            // /Logger.getLogger(Loggers.CLASSLOADING).debug("Generating class :
            // " + className);
            // e.printStackTrace();
            String classname = Utils.convertStubClassNameToClassName(className);

            // ASM is now the default bytecode manipulator
            // if (MOPClassLoader.BYTE_CODE_MANIPULATOR.equals("ASM")) {
            // ASMBytecodeStubBuilder bsb = new
            // ASMBytecodeStubBuilder(classname);
            // classData = bsb.create();
            // } else
            if (MOPClassLoader.BYTE_CODE_MANIPULATOR.equals("javassist")) {
                classData = JavassistByteCodeStubBuilder.create(classname, null);
            } else {
                // that shouldn't happen, unless someone manually sets the
                // BYTE_CODE_MANIPULATOR static variable
                System.err.println(
                    "byteCodeManipulator argument is optionnal. If specified, it can only be set to javassist (ASM is no longer supported).");
                System.err.println(
                    "Any other setting will result in the use of javassist, the default bytecode manipulator framework");
            }

            // } catch (ClassNotFoundException ignored) {
            // }
        }

        if (classData != null) {
            ClassDataCache.instance().addClassData(className, classData);

            return classData;
        }

        // try to get the class as a generated component interface reference
        classData = org.objectweb.proactive.core.component.gen.Utils.getClassData(className);

        if (classData != null) {
            ClassDataCache.instance().addClassData(className, classData);

            return classData;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#terminateSession(long)
     */
    public void terminateSession(long sessionID)
        throws SecurityNotAvailableException {
        if (runtimeSecurityManager == null) {
            throw new SecurityNotAvailableException();
        }
        runtimeSecurityManager.terminateSession(sessionID);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getCertificate()
     */
    public TypedCertificate getCertificate()
        throws SecurityNotAvailableException {
        if (runtimeSecurityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return runtimeSecurityManager.getCertificate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getProActiveSecurityManager()
     */
    public ProActiveSecurityManager getProActiveSecurityManager() {
        return runtimeSecurityManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#startNewSession(org.objectweb.proactive.ext.security.Communication)
     */
    public long startNewSession(long distantSessionID, SecurityContext policy,
        TypedCertificate distantCertificate)
        throws SecurityNotAvailableException, SessionException {
        if (runtimeSecurityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return runtimeSecurityManager.startNewSession(distantSessionID, policy,
            distantCertificate);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getPublicKey()
     */
    public PublicKey getPublicKey() throws SecurityNotAvailableException {
        if (runtimeSecurityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return runtimeSecurityManager.getPublicKey();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#randomValue(long,
     *      byte[])
     */
    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException {
        if (runtimeSecurityManager == null) {
            throw new SecurityNotAvailableException();
        }
        try {
            return runtimeSecurityManager.randomValue(sessionID,
                clientRandomValue);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#publicKeyExchange(long,
     *      org.objectweb.proactive.core.body.UniversalBody, byte[], byte[],
     *      byte[])
     */
    public byte[] publicKeyExchange(long sessionID, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException {
        if (runtimeSecurityManager != null) {
            try {
                return runtimeSecurityManager.publicKeyExchange(sessionID,
                    signature);
            } catch (KeyExchangeException e) {
                e.printStackTrace();
            }
        } else {
            throw new SecurityNotAvailableException();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#secretKeyExchange(long,
     *      byte[], byte[], byte[], byte[], byte[])
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException {
        if (runtimeSecurityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return runtimeSecurityManager.secretKeyExchange(sessionID,
            encodedAESKey, encodedIVParameters, encodedClientMacKey,
            encodedLockData, parametersSignature);
    }

    //    /*
    //     * (non-Javadoc)
    //     *
    //     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getCertificateEncoded()
    //     */
    //    public byte[] getCertificateEncoded() throws SecurityNotAvailableException {
    //    	if (runtimeSecurityManager == null) {
    //    		throw new SecurityNotAvailableException();
    //    	}
    //        return runtimeSecurityManager.getCertificateEncoded();
    //    }
    public String getVNName(String nodename) throws ProActiveException {
        return this.nodeMap.get(nodename).getVirtualNodeName();
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    protected static class VMInformationImpl implements VMInformation,
        java.io.Serializable {
        private final java.net.InetAddress hostInetAddress;

        // the Unique ID of the JVM
        private final java.rmi.dgc.VMID uniqueVMID;
        private String name;
        private long capacity;
        private final String hostName;
        private long deploymentId;

        public VMInformationImpl() throws java.net.UnknownHostException {
            this.uniqueVMID = UniqueID.getCurrentVMID();
            this.hostInetAddress = URIBuilder.getLocalAddress();
            this.hostName = URIBuilder.getHostNameorIP(this.hostInetAddress);
            String random = Integer.toString(ProActiveRandom.nextPosInt());

            if (PAProperties.PA_RUNTIME_NAME.isSet()) {
                this.name = PAProperties.PA_RUNTIME_NAME.getValue();

                if (this.name.indexOf("PA_JVM") < 0) {
                    runtimeLogger.warn(
                        "WARNING !!! The name of a ProActiveRuntime MUST contain PA_JVM string \n" +
                        "WARNING !!! Property proactive.runtime.name does not contain PA_JVM. This name is not adapted to IC2D tool");
                }
            } else {
                this.name = "PA_JVM" + random; //+ "_" + this.hostName;
            }

            this.capacity = -1;
            this.deploymentId = -1;
        }

        //
        // -- PUBLIC METHODS -----------------------------------------------
        //
        //
        // -- implements VMInformation
        // -----------------------------------------------
        //
        public java.rmi.dgc.VMID getVMID() {
            return this.uniqueVMID;
        }

        public String getName() {
            return this.name;
        }

        public java.net.InetAddress getInetAddress() {
            return this.hostInetAddress;
        }

        /**
         * @see org.objectweb.proactive.core.runtime.VMInformation#getHostName()
         */
        public String getHostName() {
            return this.hostName;
        }

        /**
         * @see org.objectweb.proactive.core.runtime.VMInformation#getDescriptorVMName()
         */
        public String getDescriptorVMName() {
            return this.name;
        }

        public long getCapacity() {
            return capacity;
        }

        private void setCapacity(long capacity) {
            this.capacity = capacity;
        }

        public long getDeploymentId() {
            return deploymentId;
        }

        private void setDeploymentId(long deploymentId) {
            this.deploymentId = deploymentId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) +
                ((uniqueVMID == null) ? 0 : uniqueVMID.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final VMInformationImpl other = (VMInformationImpl) obj;
            if (uniqueVMID == null) {
                if (other.uniqueVMID != null) {
                    return false;
                }
            } else if (!uniqueVMID.equals(other.uniqueVMID)) {
                return false;
            }
            return true;
        }
    }

    //
    // ----------------- INNER CLASSES --------------------------------
    //

    /**
     * inner class for method invocation
     */
    private class LauncherThread extends Thread {
        private final boolean launchMain;
        private Method mainMethod;
        private Class<?> remoteClass;
        private String[] parameters;

        public LauncherThread(Class<?> remoteClass) {
            this.remoteClass = remoteClass;
            this.launchMain = false;
        }

        public LauncherThread(Method mainMethod, String[] parameters) {
            this.mainMethod = mainMethod;
            this.parameters = parameters;
            this.launchMain = true;
        }

        @Override
        public void run() {
            if (this.launchMain) {
                try {
                    this.mainMethod.invoke(null,
                        new Object[] { this.parameters });
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    this.remoteClass.newInstance();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#setLocalNodeProperty(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public Object setLocalNodeProperty(String nodeName, String key, String value) {
        return this.nodeMap.get(nodeName).setProperty(key, value);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getLocalNodeProperty(java.lang.String,
     *      java.lang.String)
     */
    public String getLocalNodeProperty(String nodeName, String key) {
        return this.nodeMap.get(nodeName).getProperty(key);
    }

    public RemoteObjectExposer getRemoteObjectExposer() {
        return this.roe;
    }

    public String[] getURLs() {
        return this.roe.getURLs();
    }

    public ProActiveSecurityManager getProActiveSecurityManager(Entity user)
        throws SecurityNotAvailableException, AccessControlException {
        if (runtimeSecurityManager == null) {
            throw new SecurityNotAvailableException();
        }
        return runtimeSecurityManager.getProActiveSecurityManager(user);
    }

    public void setProActiveSecurityManager(Entity user,
        PolicyServer policyServer)
        throws SecurityNotAvailableException, AccessControlException {
        if (runtimeSecurityManager == null) {
            throw new SecurityNotAvailableException();
        }
        runtimeSecurityManager.setProActiveSecurityManager(user, policyServer);
    }

    public Set<String> setCapacity(long capacity) {
        if (vmInformation.getCapacity() > 0) {
            throw new IllegalStateException("setCapacity already set to " +
                vmInformation.getCapacity());
        }

        if (capacity < 1) {
            throw new IllegalArgumentException(capacity +
                " is not a valid parameter for setCapicity. Must be a strictly positive long");
        }
        logger.debug("Capacity set to " + capacity + ". Creating the nodes...");
        vmInformation.setCapacity(capacity);

        Set<String> nodeUrls = new HashSet<String>();

        String protocol = PAProperties.PA_COMMUNICATION_PROTOCOL.getValue();
        String hostname = vmInformation.getHostName();
        for (long i = 0; i < capacity; i++) {
            String nodeName = this.vmInformation.getName() + "_" +
                Constants.GCM_NODE_NAME + i;
            String url = URIBuilder.buildURI(hostname, nodeName, protocol)
                                   .toString();

            try {
                // FIXME acontes PSM ?
                nodeUrls.add(createLocalNode(url, false, null,
                        VirtualNode.DEFAULT_VN, "Undefined"));
            } catch (NodeException e) {
                // Cannot do something here. This node will node be created
                logger.warn("Failed to create a capacity node", e);
            } catch (AlreadyBoundException e) {
                // CapacityNode- is a reserved name space.
                // Should not happen, log it and delete the old node
                logger.warn(url + "is already registered... replacing it !");
                try {
                    nodeUrls.add(createLocalNode(url, true, null,
                            VirtualNode.DEFAULT_VN, null));
                } catch (NodeException e1) {
                    logger.warn("Failed to create a capacity node", e1);
                } catch (AlreadyBoundException e1) {
                    // Cannot be thrown since replacePreviousBinding = true
                    logger.warn("Impossible exception ! Check Me !", e1);
                }
            }
        }
        return nodeUrls;
    }

    public void register(GCMRuntimeRegistrationNotificationData notification) {
        getMBean()
            .sendNotification(NotificationType.GCMRuntimeRegistered,
            notification);
    }

    public void setDeploymentId(long deploymentId) {
        vmInformation.setDeploymentId(deploymentId);
    }
}
