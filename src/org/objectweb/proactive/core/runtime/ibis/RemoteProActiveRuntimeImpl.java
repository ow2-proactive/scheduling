package org.objectweb.proactive.core.runtime.ibis;

import ibis.rmi.AlreadyBoundException;
import ibis.rmi.Naming;
import ibis.rmi.NotBoundException;
import ibis.rmi.RemoteException;

import ibis.rmi.server.UnicastRemoteObject;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.runtime.rmi.RemoteRuntimeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.net.UnknownHostException;

import java.util.ArrayList;


/**
 *   An adapter for a ProActiveRuntime to be able to receive remote calls. This helps isolate Ibis-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 *          @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */
public class RemoteProActiveRuntimeImpl extends UnicastRemoteObject
    implements RemoteProActiveRuntime {
    protected transient ProActiveRuntimeImpl proActiveRuntime;
    protected String proActiveRuntimeURL;

    //	stores nodes urls to be able to unregister nodes
    protected ArrayList nodesArray;

    //store vn urls to be able to unregister vns
    protected ArrayList vnNodesArray;

    //	
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public RemoteProActiveRuntimeImpl()
        throws RemoteException, AlreadyBoundException {
        //System.out.println("toto");
        this.proActiveRuntime = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();
		this.nodesArray = new java.util.ArrayList();
		this.vnNodesArray = new java.util.ArrayList();
        this.proActiveRuntimeURL = buildRuntimeURL();
        register(proActiveRuntimeURL, false);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding) throws RemoteException, NodeException {
        String nodeURL = null;

        //Node node;
        try {
            //first we build a well-formed url
            nodeURL = buildNodeURL(nodeName);
            //then take the name of the node
            String name = UrlBuilder.getNameFromUrl(nodeURL);

            //register the url in rmi registry
            register(nodeURL, replacePreviousBinding);
            proActiveRuntime.createLocalNode(name, replacePreviousBinding);
        } catch (java.net.UnknownHostException e) {
            throw new RemoteException("Host unknown in " + nodeURL, e);
        }
        nodesArray.add(nodeURL);
        return nodeURL;
    }

    public void killAllNodes() throws RemoteException {
        for (int i = 0; i < nodesArray.size(); i++) {
            String url = (String) nodesArray.get(i);
            killNode(url);
        }
    }

    public void killNode(String nodeName) throws RemoteException {
        String nodeUrl = null;
        String name = null;
        try {
            nodeUrl = buildNodeURL(nodeName);
            name = UrlBuilder.getNameFromUrl(nodeUrl);
            unregister(nodeUrl);
        } catch (UnknownHostException e) {
            throw new RemoteException("Host unknown in " + nodeUrl, e);
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
        String proActiveRuntimeName, String creatorID, String creationProtocol, String vmName) {
        proActiveRuntime.register(proActiveRuntimeDist, proActiveRuntimeName,
            creatorID, creationProtocol,vmName);
    }

    public ProActiveRuntime[] getProActiveRuntimes() {
        return proActiveRuntime.getProActiveRuntimes();
    }

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName) {
        return proActiveRuntime.getProActiveRuntime(proActiveRuntimeName);
    }

    public void killRT() throws RemoteException {
        killAllNodes();
        unregisterAllVirtualNodes();
        unregister(proActiveRuntimeURL);
        proActiveRuntime.killRT();
    }

    public String getURL() {
        return proActiveRuntimeURL;
    }

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
        boolean replacePreviousBinding) throws RemoteException {
        String virtualNodeURL = null;

        try {
            //first we build a well-formed url
            virtualNodeURL = buildNodeURL(virtualNodeName);
            //register it with the url
            register(virtualNodeURL, replacePreviousBinding);
        } catch (java.net.UnknownHostException e) {
            throw new RemoteException("Host unknown in " + virtualNodeURL, e);
        }
        vnNodesArray.add(virtualNodeURL);
    }

    public void unregisterVirtualNode(String virtualnodeName)
        throws RemoteException {
        String virtualNodeURL = null;
        proActiveRuntime.unregisterVirtualNode(UrlBuilder.removeVnSuffix(
                virtualnodeName));
        try {
            //first we build a well-formed url
            virtualNodeURL = buildNodeURL(virtualnodeName);
            unregister(virtualNodeURL);
        } catch (java.net.UnknownHostException e) {
            throw new RemoteException("Host unknown in " + virtualNodeURL, e);
        }
        vnNodesArray.remove(virtualNodeURL);
    }

    public void unregisterAllVirtualNodes() throws RemoteException {
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

    //
    // ---PRIVATE METHODS--------------------------------------
    //
    private void register(String url, boolean replacePreviousBinding)
        throws RemoteException {
        try {
            if (replacePreviousBinding) {
                Naming.rebind(UrlBuilder.removeProtocol(url, "ibis:"), this);
            } else {
                Naming.bind(UrlBuilder.removeProtocol(url, "ibis:"), this);
            }
			if(url.indexOf("PA_RT")<0){
            	logger.info(url + " successfully bound in registry at " + url);
			}
        } catch (AlreadyBoundException e) {
            throw new RemoteException(url + " already bound in registry", e);
        } catch (java.net.MalformedURLException e) {
            throw new RemoteException("cannot bind in registry at " + url, e);
        }
    }

    private void unregister(String url) throws RemoteException {
        try {
            Naming.unbind(UrlBuilder.removeProtocol(url, "ibis:"));
			if(url.indexOf("PA_RT")<0){
				logger.info(url + " unbound in registry");
			} 
        } catch (java.net.MalformedURLException e) {
            throw new RemoteException("cannot unbind in registry at " + url, e);
        } catch (NotBoundException e) {
            throw new RemoteException(url + "is not bound in the registry", e);
        }
    }

    private String buildRuntimeURL() {
        int port = RemoteRuntimeFactory.getRegistryHelper()
                                       .getRegistryPortNumber();
        String host = getVMInformation().getInetAddress().getHostName();
        String name = getVMInformation().getName();

        return UrlBuilder.buildUrl(host, name, "ibis:", port);
    }

    private String buildNodeURL(String url)
        throws java.net.UnknownHostException {
        int i = url.indexOf('/');

        if (i == -1) {
            //it is an url given by a descriptor
            String host = getVMInformation().getInetAddress().getHostName();
            int port = RemoteRuntimeFactory.getRegistryHelper()
                                           .getRegistryPortNumber();
            return UrlBuilder.buildUrl(host, url, "ibis:", port);
        } else {
            return UrlBuilder.checkUrl(url);
        }
    }
}
