/*
 * Created on 26
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.runtime.http;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.ext.webservices.utils.HTTPRemoteException;
import org.objectweb.proactive.ext.webservices.utils.ProActiveXMLUtils;


public class HttpRemoteRuntimeAdapterImpl implements ProActiveRuntime {
    private static transient Logger logger = Logger.getLogger("XML_HTTP");
    private String url;
    private int port;
    
    //this boolean is used when killing the runtime. Indeed in case of co-allocation, we avoid a second call to the runtime
    // which is already dead
    protected boolean alreadykilled = false;

    /**
     *
     * @param url
     */
    public HttpRemoteRuntimeAdapterImpl(HttpRuntimeAdapter newruntimeadapter,
        String newurl) {
        logger.debug("Adapter URL = " + newurl);
        this.url = newurl;
        this.port = UrlBuilder.getPortFromUrl(newurl);
        logger.debug("New Remote XML Adapter : " + url +
            " port = " + port);
    }


    //
    // -- Implements ProActiveRuntime -----------------------------------------------
    //
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, PolicyServer ps, String vname,
        String jobId) throws NodeException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(nodeName);
            paramsList.add(new Boolean(replacePreviousBinding));
            paramsList.add(ps);
            paramsList.add(vname);
            paramsList.add(jobId);

            RuntimeRequest req = new RuntimeRequest("createLocalNode",
                    paramsList);

            Object result = sendRequest(req);

            return nodeName;
        } catch (NodeException e) {
            throw e;
        } catch (Exception e) {
            throw new NodeException(e);
        }
    }

    /**
     *
     * @param req
     * @return
     * @throws ProActiveException
     */
    private Object sendRequest(RuntimeRequest req) throws Exception {
        logger.debug("Send request to : " + url + ":" +
            port);

        if (req.getMethodName() == null) {
            throw new ProActiveException("Null request");
        }

        RuntimeReply reply = (RuntimeReply) ProActiveXMLUtils.sendMessage(url,
                port, req, ProActiveXMLUtils.RUNTIME_REQUEST);

        if (reply != null) {
            return reply.getReturnedObject();
        }

        return null;
    }

    public void killAllNodes() throws ProActiveException {
        try {
            Object o = sendRequest(new RuntimeRequest("killAllNodes"));
        } catch (Exception re) {
            throw new ProActiveException(re);
        }
    }

    public void killNode(String nodeName) throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeName);

        try {
            Object o = sendRequest(new RuntimeRequest("killNode", params));
        } catch (Exception re) {
            throw new ProActiveException(re);
        }
    }

    public void createVM(UniversalProcess remoteProcess)
        throws IOException, ProActiveException {
        ArrayList params = new ArrayList();
        params.add(remoteProcess);

        try {
            Object o = sendRequest(new RuntimeRequest("createVM", params));
        } catch (Exception re) {
            throw new ProActiveException(re);
        }
    }

    public String[] getLocalNodeNames() throws ProActiveException {
        try {
            return (String[]) sendRequest(new RuntimeRequest(
                    "getLocalNodeNames"));
        } catch (Exception re) {
            throw new ProActiveException(re);
        }
    }

    public VMInformation getVMInformation() {
        try {
            return (VMInformation) sendRequest(new RuntimeRequest(
                    "getVMInformation"));
        } catch (Exception re) {
            //throw new ProActiveException(re);
            re.printStackTrace();
        }

        return null;
    }

    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) {
        try {
            ArrayList params = new ArrayList();
            ArrayList paramsTypes = new ArrayList();

            params.add(proActiveRuntimeDist);
            paramsTypes.add(ProActiveRuntime.class);
            params.add(proActiveRuntimeName);
            paramsTypes.add(String.class);
            params.add(creatorID);
            paramsTypes.add(String.class);
            params.add(creationProtocol);
            paramsTypes.add(String.class);
            params.add(vmName);
            paramsTypes.add(String.class);

            RuntimeRequest req = new RuntimeRequest("register", params,
                    paramsTypes);

            Object o = sendRequest(req);

            //runtimeadapter.register(proActiveRuntimeDist, proActiveRuntimeName, creatorID, creationProtocol, vmName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException {
        try {
            return (ProActiveRuntime[]) sendRequest(new RuntimeRequest(
                    "getProActiveRuntime"));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(proActiveRuntimeName);

        try {
            return (ProActiveRuntime) sendRequest(new RuntimeRequest(
                    "getProActiveRuntime", params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public void killRT(boolean softly) throws Exception {
        if (!alreadykilled) {
            ArrayList params = new ArrayList();
            params.add(new Boolean(softly));

            try {
                sendRequest(new RuntimeRequest("killRT", params));
            } catch (HTTPRemoteException e) {
                // do nothing (results from distant System.exit(0))
            } catch (Exception e) {
                throw new ProActiveException(e);
            }
        }

        alreadykilled = true;
    }

    public String getURL() throws ProActiveException {
        //return runtimeadapter.getStrategyURL();
        throw new ProActiveException("This method should never be called," +
            " since the Adapter already does the job.");
    }

    public ArrayList getActiveObjects(String nodeName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeName);

        try {
            return (ArrayList) sendRequest(new RuntimeRequest(
                    "getActiveObjects", params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public ArrayList getActiveObjects(String nodeName, String objectName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeName);
        params.add(objectName);

        try {
            return (ArrayList) sendRequest(new RuntimeRequest(
                    "getActiveObjects", params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public VirtualNode getVirtualNode(String virtualNodeName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(virtualNodeName);

        try {
            return (VirtualNode) sendRequest(new RuntimeRequest(
                    "getVirtualNode", params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(virtualNodeName);
        params.add(new Boolean(replacePreviousBinding));

        try {
            sendRequest(new RuntimeRequest("registerVirtualNode", params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public void unregisterVirtualNode(String virtualNodeName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(virtualNodeName);

        try {
            sendRequest(new RuntimeRequest("unregisterVirtualNode", params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public void unregisterAllVirtualNodes() throws ProActiveException {
        try {
            sendRequest(new RuntimeRequest("unregisterAllVirtualNodes"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws ProActiveException, ConstructorCallExecutionFailedException, 
            InvocationTargetException {
        ArrayList params = new ArrayList();
        params.add(nodeName);
        params.add(bodyConstructorCall);
        params.add(new Boolean(isNodeLocal));

        try {
            return (UniversalBody) sendRequest(new RuntimeRequest(
                    "createBody", params));
        } catch (ConstructorCallExecutionFailedException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public UniversalBody receiveBody(String nodeName, Body body)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeName);
        params.add(body);

        try {
            return (UniversalBody) sendRequest(new RuntimeRequest(
                    "receiveBody", params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    // SECURITY 
    public PolicyServer getPolicyServer() throws ProActiveException {
        try {
            return (PolicyServer) sendRequest(new RuntimeRequest(
                    "getPolicyServer"));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public void setProActiveSecurityManager(ProActiveSecurityManager ps)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(ps);

        try {
            sendRequest(new RuntimeRequest("setProActiveSecurityManager", params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getCreatorCertificate()
     */
    public X509Certificate getCreatorCertificate() throws ProActiveException {
        try {
            return (X509Certificate) sendRequest(new RuntimeRequest(
                    "getCreatorCertificate"));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public String getVNName(String nodename) throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodename);

        try {
            return (String) sendRequest(new RuntimeRequest("getVNName", params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#setDefaultNodeVirtualNodeName(java.lang.String)
     */
    public void setDefaultNodeVirtualNodeName(String s)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(s);

        try {
            sendRequest(new RuntimeRequest("setDefaultNodeVirtualNodeName",
                    params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getNodePolicyServer(java.lang.String)
     */
    public PolicyServer getNodePolicyServer(String nodeName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeName);

        try {
            return (PolicyServer) sendRequest(new RuntimeRequest(
                    "getNodePolicyServer", params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#enableSecurityIfNeeded()
     */
    public void enableSecurityIfNeeded() throws ProActiveException {
        try {
            sendRequest(new RuntimeRequest("enableSecurityIfNeeded"));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getNodeCertificate(java.lang.String)
     */
    public X509Certificate getNodeCertificate(String nodeName)
        throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeName);

        try {
            return (X509Certificate) sendRequest(new RuntimeRequest(
                    "getNodeCertificate", params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @param nodeName
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(String nodeName) throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeName);

        try {
            return (ArrayList) sendRequest(new RuntimeRequest("getEntities",
                    params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @param nodeName
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(UniversalBody uBody) throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(uBody);

        try {
            return (ArrayList) sendRequest(new RuntimeRequest("getEntities",
                    params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @return returns all entities associated to this runtime
     */
    public ArrayList getEntities() throws ProActiveException {
        try {
            return (ArrayList) sendRequest(new RuntimeRequest("getEntities"));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getJobID(java.lang.String)
     */
    public String getJobID(String nodeUrl) throws ProActiveException {
        ArrayList params = new ArrayList();
        params.add(nodeUrl);

        try {
            return (String) sendRequest(new RuntimeRequest("getJobID", params));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public String[] getNodesNames() throws ProActiveException {
        try {
            return (String[]) sendRequest(new RuntimeRequest("getNodesNames"));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        String methodName = "getJobID";
        RuntimeRequest req = new RuntimeRequest(methodName);
        Object o = null;

        try {
            o = sendRequest(req);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (String) o;
    }

    ///////////////
    public void addParent(String proActiveRuntimeName) {
        ArrayList params = new ArrayList();
        params.add(proActiveRuntimeName);

        try {
            sendRequest(new RuntimeRequest("addParent", params));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] getParents() {
        try {
            return (String[]) sendRequest(new RuntimeRequest("getParents"));
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public SecurityContext getPolicy(SecurityContext sc)
        throws ProActiveException, SecurityNotAvailableException {
        ArrayList params = new ArrayList();
        params.add(sc);

        try {
            return (SecurityContext) sendRequest(new RuntimeRequest(
                    "getPolicy", params));
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public void listVirtualNodes() throws ProActiveException {
        try {
            //  remoteProActiveRuntime.updateLocalNodeVirtualName();
            sendRequest(new RuntimeRequest("listVirtualNodes"));
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }
}
