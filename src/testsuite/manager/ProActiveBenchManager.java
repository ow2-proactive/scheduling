/*
 * Created on Jul 30, 2003
 *
 */
package testsuite.manager;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger;
import org.objectweb.proactive.core.process.rsh.RSHJVMProcess;

import testsuite.exception.BadTypeException;

import testsuite.result.AbstractResult;
import testsuite.result.ResultsCollections;

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * <p><b>Warning : </b>the prop file is obligatory to specify the remote host name.</p>
 *
 * @author Alexandre di Costanzo
 *
 */
public class ProActiveBenchManager extends BenchmarkManager
    implements InterfaceProActiveManager {
    private ProActiveDescriptor pad = null;
    private VirtualNode[] virtualNodes = null;
    private String xmlFileLocation = ProActiveBenchManager.class.getResource(
	        "/" + ProActiveBenchManager.class.getName().replace('.', '/') +
	        ".xml").getPath();
    private Node sameVMNode = null;
    private Node localVMNode = null;
    private Node remoteVMNode = null;
    private String remoteHostname = "localhost";
    private RSHJVMProcess rshJVM = null;

    /**
     *
     */
    public ProActiveBenchManager() {
        super("Remote Benchmark Manager",
            "Help you to manage your remote benchmarks.");
        try {
            loadAttributes();
        } catch (IOException e1) {
            logger.warn("Can't load attributes", e1);
        }
        initNodes();
    }

    /**
     * @param name
     * @param description
     */
    public ProActiveBenchManager(String name, String description) {
        super(name, description);
        try {
            loadAttributes();
        } catch (IOException e1) {
            logger.warn("Can't load attributes", e1);
        }
        initNodes();
    }

    private void initNodes() {
        if (logger.isDebugEnabled()) {
            logger.debug("Begin nodes initialization ...");
        }
        rshJVM = new RSHJVMProcess(new StandardOutputMessageLogger(),
                new StandardOutputMessageLogger());
        rshJVM.setHostname(remoteHostname);
        String remoteNodeName = "node" + System.currentTimeMillis();
        rshJVM.setParameters("///" + remoteNodeName);
        try {
            rshJVM.startProcess();
        } catch (IOException e3) {
            logger.fatal("Can't start a remote JVM with RSH on " +
                remoteHostname, e3);
            new RuntimeException(e3);
        }

        try {
            pad = ProActive.getProactiveDescriptor("file:" + xmlFileLocation);
        } catch (ProActiveException e) {
            logger.fatal("Problem with the ProActive descriptor", e);
            throw new RuntimeException(e);
        }
        pad.activateMappings();
        virtualNodes = pad.getVirtualNodes();

        try {
            for (int i = 0; i < virtualNodes.length; i++) {
                VirtualNode virtualNode = virtualNodes[i];
                if (virtualNode.getName().compareTo("Dispatcher") == 0) {
                    sameVMNode = virtualNode.getNode();
                } else if (virtualNode.getName().compareTo("Dispatcher1") == 0) {
                    localVMNode = virtualNode.getNode();
                } else {
                    continue;
                }
            }
            remoteVMNode = NodeFactory.getNode("//" + remoteHostname + "/" +
                    remoteNodeName);
        } catch (NodeException e1) {
            logger.fatal("Problem with a node", e1);
            throw new RuntimeException(e1);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("... end of nodes initialization with success");
        }
    }

    /**
     * @see testsuite.manager.AbstractManager#execute()
     */
    public void execute(boolean useAttributesFile) {
        ResultsCollections results = getResults();

        try {
            results.add(AbstractResult.IMP_MSG,
                "Local hostname : " + InetAddress.getLocalHost().getHostName());
            if (logger.isInfoEnabled()) {
                logger.info("Local hostname : " +
                    InetAddress.getLocalHost().getHostName());
            }
            results.add(AbstractResult.IMP_MSG,
                "Remote hostname : " + getRemoteHostname());
            if (logger.isInfoEnabled()) {
                logger.info("Remote hostname : " + getRemoteHostname());
            }
        } catch (BadTypeException e) {
            logger.warn("Bad Type to resolve hostname", e);
            results.add(AbstractResult.ERROR, "Bad Type to resolve hostname", e);
        } catch (UnknownHostException e) {
            logger.warn("Unknown hostname, benchmark will be probably fail", e);
            results.add(AbstractResult.ERROR,
                "Unknown hostname, benchmark will be probably fail", e);
        }

        super.execute(true);
    }

    /**
     * @see testsuite.manager.AbstractManager#endManager()
     */
    public void endManager() throws Exception {
        if (pad != null) {
            pad.killall();
        }
        rshJVM.stopProcess();
    }

    /**
     * @return
     */
    public Node getLocalVMNode() {
        return localVMNode;
    }

    /**
     * @return
     */
    public Node getRemoteVMNode() {
        return remoteVMNode;
    }

    /**
     * @return
     */
    public Node getSameVMNode() {
        return sameVMNode;
    }

    /**
     * @return
     */
    public VirtualNode[] getVirtualNodes() {
        return virtualNodes;
    }

    public void initManager() throws Exception {
        // do nothing
    }

    /**
     * @return
     */
    public String getRemoteHostname() {
        return remoteHostname;
    }

    public void setRemoteHostname(String remoteHostName) {
        this.remoteHostname = remoteHostName;
    }

    /**
             * @return
             */
    public String getXmlFileLocation() {
        return xmlFileLocation;
    }

    /**
     * @param xmlFileLocation
     */
    public void setXmlFileLocation(String xmlFileLocation) {
        this.xmlFileLocation = xmlFileLocation;
    }
}
