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
package org.objectweb.proactive.core.runtime.http;

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
import org.objectweb.proactive.core.rmi.ClassServer;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;

import java.io.IOException;
import java.io.Serializable;

import java.lang.reflect.InvocationTargetException;

import java.net.UnknownHostException;

import java.security.cert.X509Certificate;

import java.util.ArrayList;


public class HttpRuntimeAdapter implements ProActiveRuntime, Serializable {
    private static transient Logger logger = Logger.getLogger("XML_HTTP");
    protected int port = ClassServer.getServerSocketPort();
    protected String url = ClassServer.getUrl();
    private transient ProActiveRuntime runtimestrategyadapter;
    protected VMInformation vmInformation;

    //protected VMInformation vmInformation;
    public HttpRuntimeAdapter() {
        runtimestrategyadapter = ProActiveRuntimeImpl.getProActiveRuntime();
        this.vmInformation = runtimestrategyadapter.getVMInformation();
    }

    /**
     * @param newurl
     */
    public HttpRuntimeAdapter(String newurl) {
        try {
            this.url = UrlBuilder.checkUrl(newurl);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        runtimestrategyadapter = new HttpRemoteRuntimeAdapterImpl(this, newurl);
        this.vmInformation = runtimestrategyadapter.getVMInformation();
    }

    //    public void createURL() {
    //
    //        /* !!! */
    //        if (!url.startsWith("http:")) {
    //            url = "http:" + url;
    //        }
    //
    //        if (port == 0) {
    //            port = UrlBuilder.getPortFromUrl(url);
    //        }
    //
    //        try {
    //            url = "http://" + UrlBuilder.getHostNameAndPortFromUrl(url);
    //        } catch (UnknownHostException e) {
    //            e.printStackTrace();
    //        }
    //    }
    //
    // -- Implements ProActiveRuntime -----------------------------------------------
    //
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, PolicyServer ps, String vname,
        String jobId) throws NodeException {
        try {
            String nodeURL = null;

            try {
                nodeURL = buildNodeURL(nodeName);
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            }

            //      then take the name of the node
            String name = UrlBuilder.getNameFromUrl(nodeURL);
            runtimestrategyadapter.createLocalNode(name,
                replacePreviousBinding, ps, vname, jobId);

            return nodeURL;
        } catch (NodeException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void killAllNodes() throws ProActiveException {
        runtimestrategyadapter.killAllNodes();
    }

    public void killNode(String nodeName) throws ProActiveException {
        runtimestrategyadapter.killNode(nodeName);
    }

    public void createVM(UniversalProcess remoteProcess)
        throws IOException, ProActiveException {
        runtimestrategyadapter.createVM(remoteProcess);
    }

    public String[] getLocalNodeNames() throws ProActiveException {
        return runtimestrategyadapter.getLocalNodeNames();
    }

    public VMInformation getVMInformation() {
        return this.vmInformation;
    }

    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) {
        runtimestrategyadapter.register(proActiveRuntimeDist,
            proActiveRuntimeName, creatorID, creationProtocol, vmName);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#unregister(org.objectweb.proactive.core.runtime.ProActiveRuntime, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void unregister(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeUrl, String creatorID, String creationProtocol,
        String vmName) {
        this.runtimestrategyadapter.unregister(proActiveRuntimeDist,
            proActiveRuntimeUrl, creatorID, creationProtocol, vmName);
    }

    public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException {
        return runtimestrategyadapter.getProActiveRuntimes();
    }

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName)
        throws ProActiveException {
        return runtimestrategyadapter.getProActiveRuntime(proActiveRuntimeName);
    }

    public void killRT(boolean softly) throws ProActiveException, Exception {
        runtimestrategyadapter.killRT(softly);
    }

    public String getURL() throws ProActiveException {
        return this.url;
    }

    public ArrayList getActiveObjects(String nodeName)
        throws ProActiveException {
        return runtimestrategyadapter.getActiveObjects(nodeName);
    }

    public ArrayList getActiveObjects(String nodeName, String objectName)
        throws ProActiveException {
        return runtimestrategyadapter.getActiveObjects(nodeName, objectName);
    }

    public VirtualNode getVirtualNode(String virtualNodeName)
        throws ProActiveException {
        return runtimestrategyadapter.getVirtualNode(virtualNodeName);
    }

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws ProActiveException {
        runtimestrategyadapter.registerVirtualNode(virtualNodeName,
            replacePreviousBinding);

        String url;
        try {
            url = buildNodeURL(virtualNodeName);
        } catch (UnknownHostException e) {
            throw new ProActiveException(e);
        }
        logger.info(virtualNodeName + " successfully registered at " + url);
    }

    public void unregisterVirtualNode(String virtualNodeName)
        throws ProActiveException {
        runtimestrategyadapter.unregisterVirtualNode(virtualNodeName);
    }

    public void unregisterAllVirtualNodes() throws ProActiveException {
        runtimestrategyadapter.unregisterAllVirtualNodes();
    }

    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws ProActiveException, ConstructorCallExecutionFailedException, 
            InvocationTargetException {
        return runtimestrategyadapter.createBody(nodeName, bodyConstructorCall,
            isNodeLocal);
    }

    public UniversalBody receiveBody(String nodeName, Body body)
        throws ProActiveException {
        return runtimestrategyadapter.receiveBody(nodeName, body);
    }

    // SECURITY 
    public PolicyServer getPolicyServer() throws ProActiveException {
        return runtimestrategyadapter.getPolicyServer();
    }

    public void setProActiveSecurityManager(ProActiveSecurityManager ps)
        throws ProActiveException {
        runtimestrategyadapter.setProActiveSecurityManager(ps);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getCreatorCertificate()
     */
    public X509Certificate getCreatorCertificate() throws ProActiveException {
        return runtimestrategyadapter.getCreatorCertificate();
    }

    public String getVNName(String nodename) throws ProActiveException {
        return runtimestrategyadapter.getVNName(nodename);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#setDefaultNodeVirtualNodeName(java.lang.String)
     */
    public void setDefaultNodeVirtualNodeName(String s)
        throws ProActiveException {
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getNodePolicyServer(java.lang.String)
     */
    public PolicyServer getNodePolicyServer(String nodeName)
        throws ProActiveException {
        return runtimestrategyadapter.getNodePolicyServer(nodeName);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#enableSecurityIfNeeded()
     */
    public void enableSecurityIfNeeded() throws ProActiveException {
        runtimestrategyadapter.enableSecurityIfNeeded();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getNodeCertificate(java.lang.String)
     */
    public X509Certificate getNodeCertificate(String nodeName)
        throws ProActiveException {
        return runtimestrategyadapter.getNodeCertificate(nodeName);
    }

    /**
     * @param nodeName
     * @return all entities associated to the node
     */
    public ArrayList getEntities(String nodeName) throws ProActiveException {
        return runtimestrategyadapter.getEntities(nodeName);
    }

    /**
     * @param uBody
     * @return all entities associated to the node
     */
    public ArrayList getEntities(UniversalBody uBody) throws ProActiveException {
        return runtimestrategyadapter.getEntities(uBody);
    }

    /**
     * @return returns all entities associated to this runtime
     */
    public ArrayList getEntities() throws ProActiveException {
        return runtimestrategyadapter.getEntities();
    }

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return runtimestrategyadapter.getJobID();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getJobID(java.lang.String)
     */
    public String getJobID(String nodeUrl) throws ProActiveException {
        return runtimestrategyadapter.getJobID(nodeUrl);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#addAcquaintance(java.lang.String)
     */
    public void addAcquaintance(String proActiveRuntimeName) {
        this.runtimestrategyadapter.addAcquaintance(proActiveRuntimeName);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getAcquaintances()
     */
    public String[] getAcquaintances() {
        return this.runtimestrategyadapter.getAcquaintances();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#rmAcquaintance(java.lang.String)
     */
    public void rmAcquaintance(String proActiveRuntimeName) {
        this.runtimestrategyadapter.rmAcquaintance(proActiveRuntimeName);
    }

    public SecurityContext getPolicy(SecurityContext sc)
        throws ProActiveException, SecurityNotAvailableException {
        return this.runtimestrategyadapter.getPolicy(sc);
    }

    /**
     * @see ProActiveRuntime#getClassDataFromParentRuntime(String)
     */
    public byte[] getClassDataFromParentRuntime(String className)
        throws ProActiveException {
        return this.runtimestrategyadapter.getClassDataFromParentRuntime(className);
    }

    /**
     * @see ProActiveRuntime#getClassDataFromThisRuntime(String)
     */
    public byte[] getClassDataFromThisRuntime(String className)
        throws ProActiveException {
        return this.runtimestrategyadapter.getClassDataFromThisRuntime(className);
    }

    /**
     * @see ProActiveRuntime#setParent(String)
     */
    public void setParent(String parentRuntimeName) throws ProActiveException {
        this.runtimestrategyadapter.setParent(parentRuntimeName);
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.runtimestrategyadapter = new HttpRemoteRuntimeAdapterImpl(this,
                this.url);
    }

    protected String buildNodeURL(String url)
        throws java.net.UnknownHostException {
        int i = url.indexOf('/');

        if (i == -1) {
            //it is an url given by a descriptor
            String host = UrlBuilder.getHostNameorIP(getVMInformation()
                                                         .getInetAddress());

            return UrlBuilder.buildUrl(host, url, "http:", port);
        } else {
            i = url.indexOf('/', 7);

            String computerName = url.substring(7, i);

            if (computerName.indexOf(':') == -1) {
                //no port
                computerName = computerName + ":" + port;
                url = "http://" + computerName + url.substring(i);
            }

            return UrlBuilder.checkUrl(url);
        }
    }

    public String[] getNodesNames() throws ProActiveException {
        if (runtimestrategyadapter instanceof ProActiveRuntime) {
            return runtimestrategyadapter.getLocalNodeNames();
        } else {
            return ((HttpRemoteRuntimeAdapterImpl) runtimestrategyadapter).getNodesNames();
        }
    }

    public String getStrategyURL() {
        return url;
    }

    public boolean equals(Object o) {
        if (!(o instanceof HttpRuntimeAdapter)) {
            return false;
        }

        HttpRuntimeAdapter runtimeadapter = (HttpRuntimeAdapter) o;

        return runtimestrategyadapter.equals(runtimeadapter.runtimestrategyadapter);
    }

    public int hashCode() {
        return runtimestrategyadapter.hashCode();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#receiveCheckpoint(java.lang.String, org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint, int)
     */
    public UniversalBody receiveCheckpoint(String nodeName, Checkpoint ckpt,
        int inc) throws ProActiveException {
        return null;
    }
}
