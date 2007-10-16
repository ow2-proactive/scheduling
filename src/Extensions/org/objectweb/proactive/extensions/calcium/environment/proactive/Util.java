/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.environment.proactive;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.extensions.calcium.environment.FileServer;


public class Util {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);

    // LocalNode node= NodeFactory.getDefaultNode();
    static public AOTaskPool createActiveTaskPool(Node frameworkNode)
        throws ActiveObjectCreationException, NodeException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Active Object TaskPool.");
        }

        AOTaskPool aom = (AOTaskPool) ProActiveObject.newActive(AOTaskPool.class.getName(),
                new Object[] {  }, frameworkNode);

        return aom;
    }

    static public AOInterpreterPool createActiveInterpreterPool(
        Node frameworkNode) throws ActiveObjectCreationException, NodeException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Active Interpreter Pool.");
        }

        AOInterpreterPool aip = (AOInterpreterPool) ProActiveObject.newActive(AOInterpreterPool.class.getName(),
                new Object[] {  }, frameworkNode);

        return aip;
    }

    public static FileServerClientImpl createFileServer(Node frameworkNode)
        throws ActiveObjectCreationException, NodeException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating File Server Proxy.");
        }

        FileServer fserver = (FileServer) ProActiveObject.newActive(FileServer.class.getName(),
                new Object[] {  }, frameworkNode);
        fserver.initFileServer();

        FileServerClientImpl fserverproxy = new FileServerClientImpl(frameworkNode,
                fserver);

        return fserverproxy;
    }

    static public AOInterpreter[] createAOinterpreter(Node[] nodes)
        throws ProActiveException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Active Object Interpreters in nodes.");
        }

        Object[][] params = new Object[nodes.length][0];

        AOInterpreter[] aip;
        try {
            aip = (AOInterpreter[]) ProActiveObject.newActiveInParallel(AOInterpreter.class.getName(),
                    params, nodes);
        } catch (ClassNotFoundException e) {
            throw new ProActiveException(e);
        }

        return aip;
    }

    public static Node getFrameWorkNode(ProActiveDescriptor pad,
        VariableContract vc) throws NodeException {
        String vnName = vc.getValue("SKELETON_FRAMEWORK_VN");

        return getNode(pad, vnName);
    }

    public static Node[] getInterpreterNodes(ProActiveDescriptor pad,
        VariableContract vc) throws NodeException {
        String vnName = vc.getValue("INTERPRETERS_VN");

        return getNodes(pad, vnName);
    }

    static public Node[] getNodes(ProActiveDescriptor pad,
        String virtualNodeName) throws NodeException {
        VirtualNode vn = pad.getVirtualNode(virtualNodeName);
        vn.activate();

        return vn.getNodes();
    }

    static public Node getNode(ProActiveDescriptor pad, String virtualNodeName)
        throws NodeException {
        VirtualNode vn = pad.getVirtualNode(virtualNodeName);
        vn.activate();

        return vn.getNode();
    }
}
