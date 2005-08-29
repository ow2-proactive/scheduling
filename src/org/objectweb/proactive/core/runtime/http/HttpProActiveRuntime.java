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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.http.util.exceptions.HTTPRemoteException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.rmi.ClassServer;
import org.objectweb.proactive.core.rmi.ClassServerServlet;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RemoteProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.runtime.http.messages.RuntimeRequest;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.osgi.OsgiParameters;


/**
 * An HTTP adapter for a ProActiveRuntime to be able to receive remote calls with HTTP. This helps isolate
 * HTTP specific code into a small set of specific classes.
 * @author ProActiveTeam
 * @version 1.0, 9 ao?t 2005
 * @since ProActive 2.2
 * @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */
public class HttpProActiveRuntime implements RemoteProActiveRuntime {
    private transient ProActiveRuntime localruntime;
    private boolean isLocal;
    private String url;
    protected VMInformation vmInformation;

    //    private int port;
    //this boolean is used when killing the runtime. Indeed in case of co-allocation, we avoid a second call to the runtime
    // which is already dead
    protected boolean alreadykilled = false;

    public HttpProActiveRuntime() {
        isLocal = true;
        this.localruntime = ProActiveRuntimeImpl.getProActiveRuntime();
        if (OsgiParameters.servletEnabled()) {
            this.url = ClassServerServlet.getUrl();
        } else {
            this.url = ClassServer.getUrl();
        }

        //we also cache the information here to avoid crossing the network when formatting
        //urls
        this.vmInformation = localruntime.getVMInformation();
    }

    /**
     *
     */
    public HttpProActiveRuntime(String newurl) {
        runtimeLogger.debug("Adapter URL = " + newurl);
        try {
            this.url = UrlBuilder.checkUrl(newurl);
            isLocal = false;
            runtimeLogger.debug("New Remote XML Adapter : " + url);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //
    // -- Implements ProActiveRuntime -----------------------------------------------
    //
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding,
        ProActiveSecurityManager securityManager, String vname, String jobId)
        throws NodeException, HTTPRemoteException {
        String nodeURL = null;
        try {
            nodeURL = buildNodeURL(nodeName);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        //      then take the name of the node
        String name = UrlBuilder.getNameFromUrl(nodeURL);

        if (isLocal) {
            localruntime.createLocalNode(name, replacePreviousBinding,
                securityManager, vname, jobId);
        } else {
            ArrayList paramsList = new ArrayList();
            paramsList.add(name);
            paramsList.add(new Boolean(replacePreviousBinding));
            paramsList.add(securityManager);
            paramsList.add(vname);
            paramsList.add(jobId);
            RuntimeRequest req = new RuntimeRequest("createLocalNode",
                    paramsList, this.url);
            req.send();
        }
        return nodeURL;
    }

    public void killAllNodes() throws HTTPRemoteException, ProActiveException {
        if (isLocal) {
            localruntime.killAllNodes();
            return;
        }
        new RuntimeRequest("killAllNodes", this.url).send();
    }

    public void killNode(String nodeName)
        throws ProActiveException, HTTPRemoteException {
        String name = UrlBuilder.getNameFromUrl(nodeName);
        if (isLocal) {
            localruntime.killNode(name);
            return;
        }
        ArrayList params = new ArrayList();
        params.add(name);
        new RuntimeRequest("killNode", params, this.url).send();
    }

    public void createVM(UniversalProcess remoteProcess)
        throws IOException, ProActiveException {
        if (isLocal) {
            localruntime.createVM(remoteProcess);
            return;
        }
        ArrayList params = new ArrayList();
        params.add(remoteProcess);
        new RuntimeRequest("createVM", params, this.url).send();
    }

    public String[] getLocalNodeNames()
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            return localruntime.getLocalNodeNames();
        }
        RuntimeRequest req = new RuntimeRequest("getLocalNodeNames", this.url);
        req.send();
        try {
            return (String[]) req.getReturnedObject();
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public VMInformation getVMInformation() throws HTTPRemoteException {
        if (this.vmInformation == null) {
            if (isLocal) {
                this.vmInformation = localruntime.getVMInformation();
            }
            RuntimeRequest req = new RuntimeRequest("getVMInformation", this.url);
            req.send();
            try {
                this.vmInformation = (VMInformation) req.getReturnedObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.vmInformation;
    }

    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            localruntime.register(proActiveRuntimeDist, proActiveRuntimeName,
                creatorID, creationProtocol, vmName);
            return;
        }
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

        new RuntimeRequest("register", params, paramsTypes, this.url).send();
    }

    /**
     * @throws ProActiveException
     * @throws HTTPRemoteException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#unregister(org.objectweb.proactive.core.runtime.ProActiveRuntime, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void unregister(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeUrl, String creatorID, String creationProtocol,
        String vmName) throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            localruntime.unregister(proActiveRuntimeDist, proActiveRuntimeUrl,
                creatorID, creationProtocol, vmName);
            return;
        }
        ArrayList params = new ArrayList();
        ArrayList paramsTypes = new ArrayList();

        params.add(proActiveRuntimeDist);
        paramsTypes.add(ProActiveRuntime.class);
        params.add(proActiveRuntimeUrl);
        paramsTypes.add(String.class);
        params.add(creatorID);
        paramsTypes.add(String.class);
        params.add(creationProtocol);
        paramsTypes.add(String.class);
        params.add(vmName);
        paramsTypes.add(String.class);

        new RuntimeRequest("unregister", params, paramsTypes, this.url).send();
    }

    public ProActiveRuntime[] getProActiveRuntimes()
        throws ProActiveException, HTTPRemoteException {
        RuntimeRequest req = new RuntimeRequest("getProActiveRuntimes", this.url);
        if (isLocal) {
            return localruntime.getProActiveRuntimes();
        }
        req.send();
        try {
            return (ProActiveRuntime[]) req.getReturnedObject();
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName)
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            return localruntime.getProActiveRuntime(proActiveRuntimeName);
        }
        ArrayList params = new ArrayList();
        params.add(proActiveRuntimeName);

        RuntimeRequest req = new RuntimeRequest("getProActiveRuntime", params,
                this.url);
        req.send();
        try {
            return (ProActiveRuntime) req.getReturnedObject();
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public void killRT(boolean softly) throws Exception {
        if (!alreadykilled) {
            if (isLocal) {
                localruntime.killRT(softly);
            } else {
                ArrayList params = new ArrayList();
                params.add(new Boolean(softly));

                new RuntimeRequest("killRT", params, this.url).send();
            }
        }

        alreadykilled = true;
    }

    public String getURL() throws ProActiveException {
        return this.url;
    }

    public ArrayList getActiveObjects(String nodeName)
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            return localruntime.getActiveObjects(nodeName);
        }
        ArrayList params = new ArrayList();
        params.add(nodeName);

        RuntimeRequest req = new RuntimeRequest("getActiveObjects", params,
                this.url);
        req.send();
        try {
            return (ArrayList) req.getReturnedObject();
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public ArrayList getActiveObjects(String nodeName, String objectName)
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            return localruntime.getActiveObjects(nodeName, objectName);
        }
        ArrayList params = new ArrayList();
        params.add(nodeName);
        params.add(objectName);

        RuntimeRequest req = new RuntimeRequest("getActiveObjects", params,
                this.url);
        req.send();
        try {
            return (ArrayList) req.getReturnedObject();
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public VirtualNode getVirtualNode(String virtualNodeName)
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            return localruntime.getVirtualNode(virtualNodeName);
        }
        ArrayList params = new ArrayList();
        params.add(virtualNodeName);
        RuntimeRequest req = new RuntimeRequest("getVirtualNode", params,
                this.url);
        req.send();
        try {
            return (VirtualNode) req.getReturnedObject();
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding)
        throws ProActiveException, HTTPRemoteException {
        //here we don't send the request to the remote runtime,
        //indeed the local registration occured when the vn was created.
        // It is the same behavior(for this method) in all remote parts
        String vn_url;
        try {
            vn_url = buildNodeURL(virtualNodeName);
        } catch (UnknownHostException e) {
            throw new ProActiveException(e);
        }

        runtimeLogger.info(virtualNodeName + " successfully registered at " +
            vn_url);
    }

    public void unregisterVirtualNode(String virtualNodeName)
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            localruntime.unregisterVirtualNode(virtualNodeName);
            return;
        }
        ArrayList params = new ArrayList();
        params.add(virtualNodeName);

        new RuntimeRequest("unregisterVirtualNode", params, this.url).send();
    }

    public void unregisterAllVirtualNodes() throws ProActiveException {
        if (isLocal) {
            localruntime.unregisterAllVirtualNodes();
            return;
        }
        try {
            new RuntimeRequest("unregisterAllVirtualNodes", this.url).send();
        } catch (HTTPRemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws ProActiveException, ConstructorCallExecutionFailedException, 
            InvocationTargetException, HTTPRemoteException {
        if (isLocal) {
            return localruntime.createBody(nodeName, bodyConstructorCall,
                isNodeLocal);
        }
        ArrayList params = new ArrayList();
        params.add(nodeName);
        params.add(bodyConstructorCall);
        params.add(new Boolean(isNodeLocal));

        RuntimeRequest req = new RuntimeRequest("createBody", params, this.url);
        req.send();
        try {
            return (UniversalBody) req.getReturnedObject();
        } catch (ConstructorCallExecutionFailedException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public UniversalBody receiveBody(String nodeName, Body body)
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            return localruntime.receiveBody(nodeName, body);
        }
        ArrayList params = new ArrayList();
        params.add(nodeName);
        params.add(body);

        RuntimeRequest req = new RuntimeRequest("receiveBody", params, this.url);
        req.send();
        try {
            return (UniversalBody) req.getReturnedObject();
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws HTTPRemoteException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getJobID(java.lang.String)
     */
    public String getJobID(String nodeUrl)
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            return localruntime.getJobID(nodeUrl);
        }
        ArrayList params = new ArrayList();
        params.add(nodeUrl);

        RuntimeRequest req = new RuntimeRequest("getJobID", params, this.url);
        req.send();
        try {
            return (String) req.getReturnedObject();
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public void addAcquaintance(String proActiveRuntimeName)
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            localruntime.addAcquaintance(proActiveRuntimeName);
            return;
        }
        ArrayList params = new ArrayList();
        params.add(proActiveRuntimeName);

        new RuntimeRequest("addAcquaintance", params, this.url).send();
    }

    public String[] getAcquaintances()
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            return localruntime.getAcquaintances();
        }
        RuntimeRequest req = new RuntimeRequest("getAcquaintances", this.url);
        req.send();
        try {
            return (String[]) req.getReturnedObject();
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @throws HTTPRemoteException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#rmAcquaintance(java.lang.String)
     */
    public void rmAcquaintance(String proActiveRuntimeName)
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            localruntime.rmAcquaintance(proActiveRuntimeName);
            return;
        }
        ArrayList params = new ArrayList();
        params.add(proActiveRuntimeName);

        new RuntimeRequest("rmAcquaintance", params, this.url).send();
    }

    public byte[] getClassDataFromParentRuntime(String className)
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            return localruntime.getClassDataFromParentRuntime(className);
        }
        ArrayList params = new ArrayList();
        params.add(className);

        RuntimeRequest req = new RuntimeRequest("getClassDataFromParentRuntime",
                params, this.url);
        req.send();
        try {
            return (byte[]) req.getReturnedObject();
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public byte[] getClassDataFromThisRuntime(String className)
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            return localruntime.getClassDataFromThisRuntime(className);
        }
        ArrayList params = new ArrayList();
        params.add(className);

        RuntimeRequest req = new RuntimeRequest("getClassDataFromThisRuntime",
                params, this.url);
        req.send();
        try {
            return (byte[]) req.getReturnedObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#receiveCheckpoint(java.lang.String, org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint, int)
     */
    public UniversalBody receiveCheckpoint(String nodeName, Checkpoint ckpt,
        int inc) throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            return localruntime.receiveCheckpoint(nodeName, ckpt, inc);
        }
        ArrayList params = new ArrayList();
        params.add(nodeName);
        params.add(ckpt);
        params.add(new Integer(inc));

        RuntimeRequest req = new RuntimeRequest("receiveCheckpoint", params,
                this.url);
        req.send();
        try {
            return (UniversalBody) req.getReturnedObject();
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public ProActiveDescriptor getDescriptor(String url,
        boolean isHierarchicalSearch) throws IOException, ProActiveException {
        if (isLocal) {
            return localruntime.getDescriptor(url, isHierarchicalSearch);
        }
        ArrayList params = new ArrayList();
        params.add(url);
        params.add(new Boolean(isHierarchicalSearch));

        RuntimeRequest req = new RuntimeRequest("getDescriptor", params,
                this.url);
        req.send();
        try {
            return (ProActiveDescriptor) req.getReturnedObject();
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public void launchMain(String className, String[] parameters)
        throws IOException, ClassNotFoundException, NoSuchMethodException, 
            ProActiveException {
        if (isLocal) {
            localruntime.launchMain(className, parameters);
            return;
        }
        ArrayList params = new ArrayList();
        params.add(className);
        params.add(parameters);

        RuntimeRequest req = new RuntimeRequest("launchMain", params, this.url);
        req.send();
    }

    public void newRemote(String className)
        throws IOException, ClassNotFoundException, ProActiveException {
        if (isLocal) {
            localruntime.newRemote(className);
            return;
        }
        ArrayList params = new ArrayList();
        params.add(className);

        RuntimeRequest req = new RuntimeRequest("newRemote", params, this.url);
        req.send();
    }

    protected String buildNodeURL(String url)
        throws java.net.UnknownHostException {
        int i = url.indexOf('/');

        //    System.out.println("BUILD NODE URL = " + url);
        if (i == -1) {
            //it is an url given by a descriptor
            String host = null;
            try {
                host = UrlBuilder.getHostNameorIP(getVMInformation()
                                                      .getInetAddress());
            } catch (HTTPRemoteException e) {
                e.printStackTrace();
            }

            //        System.out.println("host = " + host);
            int n = host.indexOf(":");
            String u = "";
            if (n == -1) {
                int port = Integer.parseInt(System.getProperty(
                            "proactive.http.port"));
                u = UrlBuilder.buildUrl(host, url, "http:", port);
                //            System.out.println("U = " + u + " -- " + port);
            } else {
                u = UrlBuilder.buildUrl(host, url, "http:");
            }

            return u;
        } else {
            i = url.indexOf('/', 7);

            String computerName = url.substring(7, i);

            if (computerName.indexOf(':') == -1) {
                //no port
                //            computerName = computerName ;
                url = "http://" + computerName + url.substring(i);
            }

            return UrlBuilder.checkUrl(url);
        }
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.isLocal = false;
    }

    public String getVNName(String nodename)
        throws ProActiveException, HTTPRemoteException {
        if (isLocal) {
            return localruntime.getVNName(nodename);
        }
        ArrayList params = new ArrayList();
        params.add(nodename);

        RuntimeRequest req = new RuntimeRequest("getVNName", params, this.url);
        req.send();
        try {
            return (String) req.getReturnedObject();
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        if (isLocal) {
            return localruntime.getCertificate();
        }

        ArrayList params = new ArrayList();

        RuntimeRequest req = new RuntimeRequest("getCertificate", params,
                this.url);
        req.send();
        try {
            return (X509Certificate) req.getReturnedObject();
        } catch (SecurityException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        if (isLocal) {
            return localruntime.startNewSession(policy);
        }

        ArrayList params = new ArrayList();
        params.add(policy);

        RuntimeRequest req = new RuntimeRequest("startNewSession", params,
                this.url);

        req.send();
        try {
            return ((Long) req.getReturnedObject()).longValue();
        } catch (SecurityException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        if (isLocal) {
            return localruntime.getPublicKey();
        }

        ArrayList params = new ArrayList();

        RuntimeRequest req = new RuntimeRequest("getPublicKey", params, this.url);

        req.send();
        try {
            return (PublicKey) req.getReturnedObject();
        } catch (SecurityException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        if (isLocal) {
            return localruntime.randomValue(sessionID, clientRandomValue);
        }

        ArrayList params = new ArrayList();
        params.add(new Long(sessionID));
        params.add(clientRandomValue);

        RuntimeRequest req = new RuntimeRequest("randomValue", params, this.url);

        req.send();
        try {
            return (byte[]) req.getReturnedObject();
        } catch (SecurityException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            KeyExchangeException, IOException {
        if (isLocal) {
            return localruntime.publicKeyExchange(sessionID, myPublicKey,
                myCertificate, signature);
        }

        ArrayList params = new ArrayList();
        params.add(new Long(sessionID));
        params.add(myPublicKey);
        params.add(myCertificate);
        params.add(signature);

        RuntimeRequest req = new RuntimeRequest("publicKeyExchange", params,
                this.url);

        req.send();
        try {
            return (byte[][]) req.getReturnedObject();
        } catch (SecurityException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        if (isLocal) {
            return localruntime.secretKeyExchange(sessionID, encodedAESKey,
                encodedIVParameters, encodedClientMacKey, encodedLockData,
                parametersSignature);
        }

        ArrayList params = new ArrayList();
        params.add(new Long(sessionID));
        params.add(encodedAESKey);
        params.add(encodedIVParameters);
        params.add(encodedClientMacKey);
        params.add(encodedLockData);
        params.add(parametersSignature);

        RuntimeRequest req = new RuntimeRequest("secretKeyExchange", params,
                this.url);

        req.send();
        try {
            return (byte[][]) req.getReturnedObject();
        } catch (SecurityException e) {
            throw e;
        } catch (RenegotiateSessionException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        if (isLocal) {
            return localruntime.getPolicy(securityContext);
        }

        ArrayList params = new ArrayList();
        params.add(securityContext);

        RuntimeRequest req = new RuntimeRequest("getPolicy", params, this.url);

        req.send();
        try {
            return (SecurityContext) req.getReturnedObject();
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        if (isLocal) {
            return localruntime.getCertificateEncoded();
        }

        ArrayList params = new ArrayList();

        RuntimeRequest req = new RuntimeRequest("getCertificateEncoded",
                params, this.url);

        req.send();
        try {
            return (byte[]) req.getReturnedObject();
        } catch (SecurityException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        if (isLocal) {
            return localruntime.getEntities();
        }

        ArrayList params = new ArrayList();

        RuntimeRequest req = new RuntimeRequest("getEntities", params, this.url);

        req.send();
        try {
            return (ArrayList) req.getReturnedObject();
        } catch (SecurityException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
        if (isLocal) {
            localruntime.terminateSession(sessionID);
        }

        ArrayList params = new ArrayList();
        params.add(new Long(sessionID));

        RuntimeRequest req = new RuntimeRequest("terminateSession", params,
                this.url);

        req.send();
        try {
            req.getReturnedObject();
        } catch (SecurityException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
