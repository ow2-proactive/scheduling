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
package org.objectweb.proactive.core.node;

import java.net.URISyntaxException;
import java.rmi.AlreadyBoundException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.config.ProProperties;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>
 * The <code>NodeFactory</code> provides a generic way to create and lookup <code>Node</code>
 * without protocol specific code (such as RMI or HTTP).
 * </p><p>
 * <code>NodeFactory</code> provides a set of static methods to create and lookup <code>Node</code>.
 * To create a node it is only necessary to associate the protocol in the node url.
 * For instance :
 * </p>
 * <pre>
 *    rmi://localhost/node1
 *    http://localhost/node2
 * </pre>
 * <p>
 * As long as a protocol specific factory has been registered for the
 * given protocol, the creation of the node will be delegated to the right factory.
 * </p><p>
 * This class also provide the concept of default node and default protocol. When the protocol is not
 * specified in the node URL, the default protocol is used. When an active object is created in the local
 * JVM but without being attached to any node, a default node is created to hold that active object.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.1,  2002/08/28
 * @since   ProActive 0.9
 *
 */
@PublicAPI
public class NodeFactory {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT);
    private static final String DEFAULT_NODE_NAME;
    private static Node defaultNode = null;

    static {
        ProActiveConfiguration.load();
        DEFAULT_NODE_NAME = URIBuilder.buildURI("localhost", "Node").toString();
    }

    // test with class loader
    //private static final ClassLoader myClassLoader = new NodeClassLoader();
    //
    // -- PUBLIC METHODS - STATIC -----------------------------------------------
    //
    public static synchronized Node getDefaultNode() throws NodeException {
        String nodeURL = null;

        ProActiveRuntime defaultRuntime = null;
        String jobID = ProActiveObject.getJobId();
        ProActiveSecurityManager securityManager = null;

        if (defaultNode == null) {
            try {
                defaultRuntime = RuntimeFactory.getDefaultRuntime();
                nodeURL = defaultRuntime.createLocalNode(DEFAULT_NODE_NAME +
                        Integer.toString(ProActiveRandom.nextPosInt()), false,
                        securityManager, "DefaultVN", jobID);
            } catch (ProActiveException e) {
                throw new NodeException("Cannot create the default Node", e);
            } catch (AlreadyBoundException e) { //if this exception is risen, we generate an othe random name for the node
                getDefaultNode();
            }

            defaultNode = new NodeImpl(defaultRuntime, nodeURL,
                    ProProperties.PA_COMMUNICATION_PROTOCOL.getValue(), jobID);
        }

        return defaultNode;
    }

    /**
     * Returns true if the given node belongs to this JVM false else.
     * @return true if the given node belongs to this JVM false else
     */
    public static boolean isNodeLocal(Node node) {
        return node.getVMInformation().getVMID()
                   .equals(UniqueID.getCurrentVMID());
    }

    /**
     * Creates a new node on the local machine. This call can only be used
     * to create a node on the local JVM on the local machine.
     * The node URL can be in the form
     * <ul>
     * <li>///nodeName</li>
     * <li>//localhost/nodeName</li>
     * <li>//<i>&lt;hostname></i>/nodeName</li>
     * </ul>
     * where <i>&lt;hostname></i> is the name of the localhost.
     * @param nodeURL the URL of the node to create
     * @return the newly created node on the local JVM
     * @exception NodeException if the node cannot be created
     */
    public static Node createNode(String nodeURL)
        throws NodeException, AlreadyBoundException {
        return createNode(nodeURL, false, null, null, null);
    }

    /**
     * Creates a new node on the local machine. This call can only be used
     * to create a node on the local JVM on the local machine.
     * The node URL can be in the form
     * <ul>
     * <li>///nodeName</li>
     * <li>//localhost/nodeName</li>
     * <li>//<i>&lt;hostname></i>/nodeName</li>
     * </ul>
     * where <i>&lt;hostname></i> is the name of the localhost.
     * @param url the URL of the node to create
     * @param replacePreviousBinding
     * @return the newly created node on the local JVM
     * @exception NodeException if the node cannot be created
     */
    public static Node createNode(String url, boolean replacePreviousBinding,
        ProActiveSecurityManager psm, String vnname, String jobId)
        throws NodeException, AlreadyBoundException {
        ProActiveRuntime proActiveRuntime;
        String nodeURL;

        if (logger.isDebugEnabled()) {
            logger.debug("NodeFactory: createNode(" + url + ")");
        }

        //first look for the prototcol
        String protocol = URIBuilder.getProtocol(url);

        //NodeFactory factory = getFactory(protocol);
        //then create a node
        try {
            proActiveRuntime = RuntimeFactory.getProtocolSpecificRuntime(protocol);
            nodeURL = proActiveRuntime.createLocalNode(url,
                    replacePreviousBinding, psm, vnname, jobId);
        } catch (ProActiveException e) {
            throw new NodeException("Cannot create a Node based on " + url, e);
        }

        Node node = new NodeImpl(proActiveRuntime, nodeURL, protocol, jobId);

        return node;
    }

    /**
     * Returns the reference to the node located at the given url.
     * This url can be either local or remote.
     * @param nodeURL The url of the node
     * @return Node. The reference of the node
     * @throws NodeException if the node cannot be found
     */
    public static Node getNode(String nodeURL) throws NodeException {
        ProActiveRuntime proActiveRuntime;
        String url;
        String jobID;

        if (logger.isDebugEnabled()) {
            logger.debug("NodeFactory: getNode() for " + nodeURL);
        }

        //do we have any association for this node?
        String protocol = URIBuilder.getProtocol(nodeURL);
        if (protocol == null) {
            protocol = ProProperties.PA_COMMUNICATION_PROTOCOL.getValue();
        }

        //String noProtocolUrl = UrlBuilder.removeProtocol(nodeURL, protocol);
        try {
            url = URIBuilder.checkURI(nodeURL).toString();
            proActiveRuntime = RuntimeFactory.getRuntime(url);
            jobID = proActiveRuntime.getJobID(url);
        } catch (ProActiveException e) {
            throw new NodeException("Cannot get the node based on " + nodeURL, e);
        } catch (URISyntaxException e) {
            throw new NodeException("Cannot get the node based on " + nodeURL, e);
        }

        Node node = new NodeImpl(proActiveRuntime, url, protocol, jobID);

        return node;
    }

    /**
     * Kills the node of the given url
     * @param nodeURL
     * @throws NodeException if a problem occurs when killing the node
     */
    public static void killNode(String nodeURL) throws NodeException {
        ProActiveRuntime proActiveRuntime;
        String url;

        try {
            url = URIBuilder.checkURI(nodeURL).toString();
            proActiveRuntime = RuntimeFactory.getRuntime(url);
            proActiveRuntime.killNode(url);
        } catch (ProActiveException e) {
            throw new NodeException("Cannot get the node based on " + nodeURL, e);
        } catch (URISyntaxException e) {
            throw new NodeException("Cannot get the node based on " + nodeURL, e);
        }
    }
}
