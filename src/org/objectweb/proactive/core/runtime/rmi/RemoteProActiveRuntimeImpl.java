package org.objectweb.proactive.core.runtime.rmi;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.rmi.RegistryHelper;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.net.UnknownHostException;

import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.security.cert.X509Certificate;

import java.util.ArrayList;


/**
 *   An adapter for a ProActiveRuntime to be able to receive remote calls. This helps isolate RMI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 *          @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */
public class RemoteProActiveRuntimeImpl extends UnicastRemoteObject
    implements RemoteProActiveRuntime {
    protected transient ProActiveRuntimeImpl proActiveRuntime;
    protected String proActiveRuntimeURL;

    //stores nodes urls to be able to unregister nodes
    protected ArrayList nodesArray;

    //store vn urls to be able to unregister vns
    protected ArrayList vnNodesArray;
    protected boolean hasCreatedRegistry;

    //	
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public RemoteProActiveRuntimeImpl()
        throws java.rmi.RemoteException, java.rmi.AlreadyBoundException {
        //System.out.println("toto");
        this.hasCreatedRegistry = RegistryHelper.getRegistryCreator();
        this.proActiveRuntime = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();
        this.nodesArray = new java.util.ArrayList();
        this.vnNodesArray = new java.util.ArrayList();
        //this.urlBuilder = new UrlBuilder();
        this.proActiveRuntimeURL = buildRuntimeURL();
        //            java.rmi.Naming.bind(proActiveRuntimeURL, this);
        register(proActiveRuntimeURL, false);
        //System.out.println ("ProActiveRuntime successfully bound in registry at "+proActiveRuntimeURL);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, PolicyServer ps, String VNname, String jobId)
        throws java.rmi.RemoteException, NodeException {
        String nodeURL = null;

        //Node node;
        try {
            //first we build a well-formed url
            nodeURL = buildNodeURL(nodeName);
            //then take the name of the node
            String name = UrlBuilder.getNameFromUrl(nodeURL);

            //register the url in rmi registry
            register(nodeURL, replacePreviousBinding);

            proActiveRuntime.createLocalNode(name, replacePreviousBinding, ps,
                VNname, jobId);
        } catch (java.net.UnknownHostException e) {
            throw new java.rmi.RemoteException("Host unknown in " + nodeURL, e);
        }
        nodesArray.add(nodeURL);
        return nodeURL;
    }

    public void killAllNodes() throws java.rmi.RemoteException {
        for (int i = 0; i < nodesArray.size(); i++) {
            String url = (String) nodesArray.get(i);
            killNode(url);
        }
    }

    public void killNode(String nodeName) throws java.rmi.RemoteException {
        String nodeUrl = null;
        String name = null;
        try {
            nodeUrl = buildNodeURL(nodeName);
            name = UrlBuilder.getNameFromUrl(nodeUrl);
            unregister(nodeUrl);
        } catch (UnknownHostException e) {
            throw new java.rmi.RemoteException("Host unknown in " + nodeUrl, e);
        }
        proActiveRuntime.killNode(nodeName);
    }

    //	public void createLocalVM(JVMProcess jvmProcess)
    //		throws IOException
    //	{
    //	proActiveRuntime.createLocalVM(jvmProcess);
    //	}
    public void createVM(UniversalProcess remoteProcess)
        throws IOException {
        proActiveRuntime.createVM(remoteProcess);
    }

    //	public Node[] getLocalNodes()
    //	{
    //		return proActiveRuntime.getLocalNodes(); 
    //	}
    public String[] getLocalNodeNames() {
        return proActiveRuntime.getLocalNodeNames();
    }

    //	public String getLocalNode(String nodeName)
    //	{
    //		return proActiveRuntime.getLocalNode(nodeName);
    //	}
    //
    //	
    //	public String getNode(String nodeName)
    //	{
    //		return proActiveRuntime.getNode(nodeName);
    //	}
    //	public String getDefaultNodeName(){
    //		return proActiveRuntime.getDefaultNodeName();
    //	}
    public VMInformation getVMInformation() {
        return proActiveRuntime.getVMInformation();
    }

    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) {
        proActiveRuntime.register(proActiveRuntimeDist, proActiveRuntimeName,
            creatorID, creationProtocol, vmName);
    }

    public ProActiveRuntime[] getProActiveRuntimes() {
        return proActiveRuntime.getProActiveRuntimes();
    }

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName) {
        return proActiveRuntime.getProActiveRuntime(proActiveRuntimeName);
    }

    public void killRT(boolean softly) throws java.rmi.RemoteException {
        killAllNodes();
        unregisterAllVirtualNodes();
        unregister(proActiveRuntimeURL);
        if (hasCreatedRegistry) {
            if (softly) {
                if (RegistryHelper.getRegistry().list().length > 0) {
                    new RMIKillerThread().start();
                    return;
                }
            }
        }
        proActiveRuntime.killRT(softly);
    }

    public String getURL() {
        return proActiveRuntimeURL;
    }

    //    public void setPortNumber(int p) {
    //    	this.portNumber = p;
    //    }
    //    
    //    public int getPortNumber() {
    //    	return this.portNumber;
    //    }
    public ArrayList getActiveObjects(String nodeName) {
        return proActiveRuntime.getActiveObjects(nodeName);
    }

    public ArrayList getActiveObjects(String nodeName, String objectName) {
        return proActiveRuntime.getActiveObjects(nodeName, objectName);
    }

    public VirtualNode getVirtualNode(String virtualNodeName) {
        return proActiveRuntime.getVirtualNode(virtualNodeName);
    }

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws java.rmi.RemoteException {
        String virtualNodeURL = null;

        try {
            //first we build a well-formed url
            virtualNodeURL = buildNodeURL(virtualNodeName);
            //register it with the url
            register(virtualNodeURL, replacePreviousBinding);
        } catch (java.net.UnknownHostException e) {
            throw new java.rmi.RemoteException("Host unknown in " +
                virtualNodeURL, e);
        }
        vnNodesArray.add(virtualNodeURL);
    }

    public void unregisterVirtualNode(String virtualnodeName)
        throws java.rmi.RemoteException {
        String virtualNodeURL = null;
        proActiveRuntime.unregisterVirtualNode(UrlBuilder.removeVnSuffix(
                virtualnodeName));
        try {
            //first we build a well-formed url
            virtualNodeURL = buildNodeURL(virtualnodeName);
            unregister(virtualNodeURL);
        } catch (java.net.UnknownHostException e) {
            throw new java.rmi.RemoteException("Host unknown in " +
                virtualNodeURL, e);
        }
        vnNodesArray.remove(virtualNodeURL);
    }

    public void unregisterAllVirtualNodes() throws java.rmi.RemoteException {
        for (int i = 0; i < vnNodesArray.size(); i++) {
            String url = (String) vnNodesArray.get(i);
            unregisterVirtualNode(url);
        }
    }

    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws ConstructorCallExecutionFailedException, 
            InvocationTargetException {
        return proActiveRuntime.createBody(nodeName, bodyConstructorCall,
            isNodeLocal);
    }

    public UniversalBody receiveBody(String nodeName, Body body) {
        return proActiveRuntime.receiveBody(nodeName, body);
    }

    // SECURITY

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime#getCreatorCertificate()
     */
    public X509Certificate getCreatorCertificate()
        throws java.rmi.RemoteException {
        return proActiveRuntime.getCreatorCertificate();
    }

    /**
	* @return policy server
	*/
    public PolicyServer getPolicyServer() throws java.rmi.RemoteException {
        return proActiveRuntime.getPolicyServer();
    }

    public String getVNName(String nodename) throws java.rmi.RemoteException {
        return proActiveRuntime.getVNName(nodename);
    }

    public void setProActiveSecurityManager(ProActiveSecurityManager ps)
        throws java.rmi.RemoteException {
        proActiveRuntime.setProActiveSecurityManager(ps);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime#setDefaultNodeVirtualNodeNAme(java.lang.String)
     */
    public void setDefaultNodeVirtualNodeNAme(String s)
        throws RemoteException {
        proActiveRuntime.setDefaultNodeVirtualNodeName(s);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime#updateLocalNodeVirtualName()
     */
    public void updateLocalNodeVirtualName() throws RemoteException {
        proActiveRuntime.listVirtualNodes();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime#getNodePolicyServer(java.lang.String)
     */
    public PolicyServer getNodePolicyServer(String nodeName)
        throws RemoteException {
        return proActiveRuntime.getNodePolicyServer(nodeName);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime#enableSecurityIfNeeded()
     */
    public void enableSecurityIfNeeded() throws RemoteException {
        proActiveRuntime.enableSecurityIfNeeded();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime#getNodeCertificate(java.lang.String)
     */
    public X509Certificate getNodeCertificate(String nodeName)
        throws RemoteException {
        return proActiveRuntime.getNodeCertificate(nodeName);
    }

    /**
     * @param nodeName
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(String nodeName) throws RemoteException {
        return proActiveRuntime.getEntities(nodeName);
    }

    /**
     * @param nodeName
     * @return returns all entities associated to the node
     */
    public ArrayList getEntities(UniversalBody uBody) throws RemoteException {
        return proActiveRuntime.getEntities(uBody);
    }

    /**
     * @return returns all entities associated to this runtime
     */
    public ArrayList getEntities() throws RemoteException {
        return proActiveRuntime.getEntities();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime#getJobID(java.lang.String)
     */
    public String getJobID(String nodeUrl) throws RemoteException {
		return proActiveRuntime.getJobID(nodeUrl);
    }

    //
    // ---PRIVATE METHODS--------------------------------------
    //
    private void register(String url, boolean replacePreviousBinding)
        throws java.rmi.RemoteException {
        try {
            if (replacePreviousBinding) {
                java.rmi.Naming.rebind(UrlBuilder.removeProtocol(url, "rmi:"),
                    this);
            } else {
                java.rmi.Naming.bind(UrlBuilder.removeProtocol(url, "rmi:"),
                    this);
            }
            if (url.indexOf("PA_RT") < 0) {
                logger.info(url + " successfully bound in registry at " + url);
            }
        } catch (java.rmi.AlreadyBoundException e) {
            throw new java.rmi.RemoteException(url +
                " already bound in registry", e);
        } catch (java.net.MalformedURLException e) {
            throw new java.rmi.RemoteException("cannot bind in registry at " +
                url, e);
        }
    }

    private void unregister(String url) throws java.rmi.RemoteException {
        try {
            java.rmi.Naming.unbind(UrlBuilder.removeProtocol(url, "rmi:"));
            if (url.indexOf("PA_RT") < 0) {
                logger.info(url + " unbound in registry");
            }
        } catch (ConnectException e) {
            //if we get a connect exception, the rmi registry is unreachable. We cannot throw
            //an exception otherwise the killRT method cannot reach the end!
            if ((e.getCause().getClass().getName().equals("java.net.ConnectException") &&
                    e.getCause().getMessage().equals("Connection refused"))) {
                if (url.indexOf("PA_RT") < 0) {
                    logger.info("RMIRegistry unreachable on host " +
                        getVMInformation().getInetAddress().getCanonicalHostName() +
                        " to unregister " + url + ". Killed anyway !!!");
                }
            }
        } catch (java.net.MalformedURLException e) {
            throw new java.rmi.RemoteException("cannot unbind in registry at " +
                url, e);
        } catch (java.rmi.NotBoundException e) {
            //No need to throw an exception if an object is already unregistered
            logger.info(url + "is not bound in the registry", e);
        }
    }

    private String buildRuntimeURL() {
        int port = RemoteRuntimeFactory.getRegistryHelper()
                                       .getRegistryPortNumber();
        String host = getVMInformation().getInetAddress().getCanonicalHostName();
        String name = getVMInformation().getName();
        return UrlBuilder.buildUrl(host, name, "rmi:", port);
    }

    private String buildNodeURL(String url)
        throws java.net.UnknownHostException {
        int i = url.indexOf('/');
        if (i == -1) {
            //it is an url given by a descriptor
            String host = getVMInformation().getInetAddress().getCanonicalHostName();

            int port = RemoteRuntimeFactory.getRegistryHelper()
                                           .getRegistryPortNumber();
            return UrlBuilder.buildUrl(host, url, "rmi:", port);
        } else {
            return UrlBuilder.checkUrl(url);
        }
    }

    //
    // ----------------- INNER CLASSES --------------------------------
    //
    private class RMIKillerThread extends Thread {
        public RMIKillerThread() {
        }

        public void run() {
            try {
                while (RegistryHelper.getRegistry().list().length > 0) {
                    // the thread sleeps for 10 minutes
                    Thread.sleep(600000);
                }

                proActiveRuntime.killRT(false);
            } catch (InterruptedException e) {
                proActiveRuntime.killRT(false);
                e.printStackTrace();
            } catch (AccessException e) {
                logger.error(e.getMessage());
                proActiveRuntime.killRT(false);
            } catch (RemoteException e) {
                logger.error(e.getMessage());
                proActiveRuntime.killRT(false);
            }
        }
    }
}
