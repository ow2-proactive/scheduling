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
package testsuite.manager;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.xml.sax.SAXException;

import testsuite.exception.BadTypeException;
import testsuite.group.Group;
import testsuite.result.AbstractResult;
import testsuite.result.ResultsCollections;
import testsuite.test.FunctionalTest;


/**
 * @author Alexandre di Costanzo
 *
 */
public class ProActiveFuncTestManager extends FunctionalTestManager
    implements InterfaceProActiveManager {
    private ProActiveDescriptor pad = null;
    private VirtualNode[] virtualNodes = null;
    private String xmlFileLocation = ProActiveFuncTestManager.class.getResource(
            "/" + ProActiveFuncTestManager.class.getName().replace('.', '/') +
            ".xml").getPath();
    private Node sameVMNode = null;
    private Node localVMNode = null;
    private Node remoteVMNode = null;
    private String remoteHostname = "localhost";

    // private RSHJVMProcess rshJVM = null;

    /**
     *
     */
    public ProActiveFuncTestManager() {
        super("Remote Functional Test Manager",
            "Help you to manage your remote Functional Tests.");
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
    public ProActiveFuncTestManager(String name, String description) {
        super(name, description);
        try {
            loadAttributes();
        } catch (IOException e1) {
            logger.warn("Can't load attributes", e1);
        }
        initNodes();
    }

    public ProActiveFuncTestManager(File xmlDescriptor)
        throws IOException, SAXException, ClassNotFoundException, 
            InstantiationException, IllegalAccessException {
        super(xmlDescriptor);
        this.loadAttributes(getProperties());
        initNodes();
    }

    private void initNodes() {
        if (logger.isDebugEnabled()) {
            logger.debug("Begin nodes initialization ...");
        }

        //        rshJVM = new RSHJVMProcess(new StandardOutputMessageLogger(),
        //                new StandardOutputMessageLogger());
        //        rshJVM.setHostname(remoteHostname);
        //        String remoteNodeName = "node" + System.currentTimeMillis();
        //        rshJVM.setParameters("///" + remoteNodeName);
        //        try {
        //            rshJVM.startProcess();
        //        } catch (IOException e3) {
        //            logger.fatal("Can't start a remote JVM with RSH on " +
        //                remoteHostname, e3);
        //            new RuntimeException(e3);
        //        }
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
                    remoteVMNode = virtualNode.getNode();
                }
            }

            //            remoteVMNode = NodeFactory.getNode("//" + remoteHostname + "/" +
            //                    remoteNodeName);
            setRemoteHostname(remoteVMNode.getNodeInformation().getInetAddress()
                                          .getCanonicalHostName());
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
                "Local hostname : " +
                InetAddress.getLocalHost().getCanonicalHostName());
            if (logger.isInfoEnabled()) {
                logger.info("Local hostname : " +
                    InetAddress.getLocalHost().getCanonicalHostName());
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
            logger.warn("Unknown hostname, Functional Test will be probably fail",
                e);
            results.add(AbstractResult.ERROR,
                "Unknown hostname, Functional Test will be probably fail", e);
        }

        super.execute(useAttributesFile);
    }

    /**
     * @see testsuite.manager.FunctionalTestManager#execute(testsuite.group.Group, testsuite.test.FunctionalTest)
     */
    public void execute(Group group, FunctionalTest lastestTest,
        boolean useAttributesFile) {
        ResultsCollections results = getResults();
        try {
            results.add(AbstractResult.IMP_MSG,
                "Local hostname : " +
                InetAddress.getLocalHost().getCanonicalHostName());
            if (logger.isInfoEnabled()) {
                logger.info("Local hostname : " +
                    InetAddress.getLocalHost().getCanonicalHostName());
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
            logger.warn("Unknown hostname, Functional Test will be probably fail",
                e);
            results.add(AbstractResult.ERROR,
                "Unknown hostname, Functional Test will be probably fail", e);
        }
        super.execute(group, lastestTest, true);
    }

    /**
     * @see testsuite.manager.AbstractManager#endManager()
     */
    public void endManager() throws Exception {
        if (pad != null) {
            pad.killall(false);
        }

        //rshJVM.stopProcess();
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
