/*
 * Created on
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.runtime.http;

import org.apache.log4j.Logger;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
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

    //protected VMInformation vmInformation;
    public HttpRuntimeAdapter() {
        runtimestrategyadapter = ProActiveRuntimeImpl.getProActiveRuntime();

        String host = getVMInformation().getInetAddress().getCanonicalHostName();

        //runtimeadapter.url = "http://"+host+":"+runtimeadapter.port;
        url = UrlBuilder.buildUrl(host, "", "http:", port);
        logger.debug("url de l adapter runtime = " + url);
    }

    /**
     *
     * @param url
     */
    public HttpRuntimeAdapter(String newurl) {
        runtimestrategyadapter = new HttpRemoteRuntimeAdapterImpl(this, newurl);
    }

    //
    // -- Implements ProActiveRuntime -----------------------------------------------
    //
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, PolicyServer ps, String vname,
        String jobId) throws NodeException {
        if (runtimestrategyadapter instanceof ProActiveRuntime) {
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
        } else {
            return runtimestrategyadapter.createLocalNode(nodeName,
                replacePreviousBinding, ps, vname, jobId);
        }
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
        return runtimestrategyadapter.getVMInformation();
    }

    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) {
        runtimestrategyadapter.register(proActiveRuntimeDist,
            proActiveRuntimeName, creatorID, creationProtocol, vmName);
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
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#updateLocalNodeVirtualName()
     */
    public void listVirtualNodes() throws ProActiveException {
        //  remoteProActiveRuntime.updateLocalNodeVirtualName();
        this.runtimestrategyadapter.listVirtualNodes();
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
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(String nodeName) throws ProActiveException {
        return runtimestrategyadapter.getEntities(nodeName);
    }

    /**
     * @param nodeName
     * @return returns all entities associated to the node
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
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#addParent(java.lang.String)
     */
    public void addParent(String proActiveRuntimeName) {
        this.runtimestrategyadapter.addParent(proActiveRuntimeName);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getParents()
     */
    public String[] getParents() {
        return this.runtimestrategyadapter.getParents();
    }

    public SecurityContext getPolicy(SecurityContext sc)
        throws ProActiveException, SecurityNotAvailableException {
        return this.runtimestrategyadapter.getPolicy(sc);
    }

    /*
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        out.defaultWriteObject();
    }*/
    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.runtimestrategyadapter = new HttpRemoteRuntimeAdapterImpl(this,this.url);
    }

    protected String buildNodeURL(String url)
        throws java.net.UnknownHostException {
        int i = url.indexOf('/');

        if (i == -1) {
            //it is an url given by a descriptor
            String host = getVMInformation().getInetAddress()
                              .getCanonicalHostName();

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
    
    
    
    
    
    
    
    
}
